package com.ejemplo.articulos.service;

import com.ejemplo.articulos.dto.EstadoPedidoRequest;
import com.ejemplo.articulos.dto.LineaPedidoRequest;
import com.ejemplo.articulos.dto.LineaPedidoResponse;
import com.ejemplo.articulos.dto.PedidoRequest;
import com.ejemplo.articulos.dto.PedidoResponse;
import com.ejemplo.articulos.model.EstadoPedido;
import com.ejemplo.articulos.model.Carrito;
import com.ejemplo.articulos.model.LineaPedido;
import com.ejemplo.articulos.model.Pedido;
import com.ejemplo.articulos.model.Producto;
import com.ejemplo.articulos.model.Usuario;
import com.ejemplo.articulos.repository.PedidoRepository;
import com.ejemplo.articulos.repository.CarritoRepository;
import com.ejemplo.articulos.repository.ProductoRepository;
import com.ejemplo.articulos.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class PedidoService {
    private final PedidoRepository pedidos;
    private final CarritoRepository carritos;
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;

    public PedidoService(PedidoRepository pedidos, CarritoRepository carritos, ProductoRepository productos, UsuarioRepository usuarios) {
        this.pedidos = pedidos;
        this.carritos = carritos;
        this.productos = productos;
        this.usuarios = usuarios;
    }

    public PedidoResponse crear(PedidoRequest request) {
        Usuario usuario = usuarios.findById(request.usuarioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
        Map<Long, Integer> cantidades = new TreeMap<>();
        for (LineaPedidoRequest linea : request.productos()) {
            cantidades.merge(linea.productoId(), linea.cantidad(), Integer::sum);
        }

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setFechaCreacion(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setTotal(BigDecimal.ZERO);
        List<LineaPedido> lineas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entrada : cantidades.entrySet()) {
            Producto producto = productos.findById(entrada.getKey())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Un producto del pedido no existe."));
            if (!Boolean.TRUE.equals(producto.getActivo())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Un producto del pedido ya no está disponible.");
            if (producto.getStock() < entrada.getValue()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente para " + producto.getNombre() + ". Disponible: " + producto.getStock());

            LineaPedido linea = new LineaPedido();
            linea.setPedido(pedido);
            linea.setProducto(producto);
            linea.setNombreProducto(producto.getNombre());
            linea.setCantidad(entrada.getValue());
            linea.setPrecioUnitario(producto.getPrecio());
            linea.setSubtotal(producto.getPrecio().multiply(BigDecimal.valueOf(entrada.getValue())));
            total = total.add(linea.getSubtotal());
            lineas.add(linea);
        }
        pedido.setLineas(lineas);
        pedido.setTotal(total);
        return response(pedidos.save(pedido));
    }

    public PedidoResponse crearDesdeCarrito(Long usuarioId) {
        if (!usuarios.existsById(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado.");
        }
        Carrito carrito = carritos.findByUsuarioIdWithDetails(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "El carrito está vacío."));
        if (carrito.getLineas().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El carrito está vacío.");
        }
        List<LineaPedidoRequest> lineas = carrito.getLineas().stream()
                .map(linea -> new LineaPedidoRequest(linea.getProducto().getId(), linea.getCantidad()))
                .toList();
        PedidoResponse pedido = crear(new PedidoRequest(usuarioId, lineas));
        carrito.getLineas().clear();
        carritos.save(carrito);
        return pedido;
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar(Long usuarioId) {
        List<Pedido> lista = usuarioId == null ? pedidos.findAllWithDetails() : pedidos.findByUserIdWithDetails(usuarioId);
        return lista.stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtener(Long id) { return response(encontrar(id)); }

    public PedidoResponse cambiarEstado(Long id, EstadoPedidoRequest request) {
        Pedido pedido = pedidos.findByIdForUpdateWithDetails(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido " + id + " no encontrado."));
        EstadoPedido anterior = pedido.getEstado();
        EstadoPedido nuevo = request.estado();
        if (anterior == nuevo) return response(pedido);

        if (nuevo == EstadoPedido.CONFIRMADO && anterior == EstadoPedido.PENDIENTE) {
            confirmarStock(pedido);
        } else if (nuevo == EstadoPedido.CANCELADO && (anterior == EstadoPedido.PENDIENTE || anterior == EstadoPedido.CONFIRMADO)) {
            if (anterior == EstadoPedido.CONFIRMADO) reponerStock(pedido);
            pedido.setEstado(EstadoPedido.CANCELADO);
        } else if (nuevo == EstadoPedido.ENVIADO && anterior == EstadoPedido.CONFIRMADO) {
            pedido.setEstado(EstadoPedido.ENVIADO);
        } else if (nuevo == EstadoPedido.ENTREGADO && anterior == EstadoPedido.ENVIADO) {
            pedido.setEstado(EstadoPedido.ENTREGADO);
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Transición de estado no permitida: " + anterior + " → " + nuevo + ".");
        }
        return response(pedidos.save(pedido));
    }

    private void confirmarStock(Pedido pedido) {
        for (LineaPedido linea : pedido.getLineas()) {
            if (linea.getProducto() == null || productos.descontarStock(linea.getProducto().getId(), linea.getCantidad()) == 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente o producto eliminado para confirmar " + linea.getNombreProducto() + ".");
            }
        }
        pedido.setEstado(EstadoPedido.CONFIRMADO);
    }

    private void reponerStock(Pedido pedido) {
        for (LineaPedido linea : pedido.getLineas()) {
            if (linea.getProducto() == null) continue;
            productos.reponerStock(linea.getProducto().getId(), linea.getCantidad());
        }
    }

    private Pedido encontrar(Long id) {
        return pedidos.findByIdWithDetails(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido " + id + " no encontrado."));
    }

    private PedidoResponse response(Pedido pedido) {
        List<LineaPedidoResponse> lineas = pedido.getLineas().stream()
                .map(linea -> new LineaPedidoResponse(
                        linea.getProducto() == null ? null : linea.getProducto().getId(),
                        linea.getNombreProducto(), linea.getCantidad(), linea.getPrecioUnitario(), linea.getSubtotal()))
                .toList();
        return new PedidoResponse(pedido.getId(), pedido.getUsuario().getId(), pedido.getUsuario().getNombre(),
                pedido.getUsuario().getEmail(), pedido.getFechaCreacion(), pedido.getEstado(), pedido.getTotal(), lineas);
    }
}
