package com.ejemplo.articulos.service;

import com.ejemplo.articulos.dto.CarritoResponse;
import com.ejemplo.articulos.dto.LineaCarritoRequest;
import com.ejemplo.articulos.dto.LineaCarritoResponse;
import com.ejemplo.articulos.model.Carrito;
import com.ejemplo.articulos.model.LineaCarrito;
import com.ejemplo.articulos.model.Producto;
import com.ejemplo.articulos.model.Usuario;
import com.ejemplo.articulos.repository.CarritoRepository;
import com.ejemplo.articulos.repository.ProductoRepository;
import com.ejemplo.articulos.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class CarritoService {
    private final CarritoRepository carritos;
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;

    public CarritoService(CarritoRepository carritos, ProductoRepository productos, UsuarioRepository usuarios) {
        this.carritos = carritos;
        this.productos = productos;
        this.usuarios = usuarios;
    }

    public CarritoResponse obtener(Long usuarioId) { return response(obtenerOCrear(usuarioId)); }

    public CarritoResponse agregar(Long usuarioId, LineaCarritoRequest request) {
        Carrito carrito = obtenerOCrear(usuarioId);
        Producto producto = productos.findById(request.productoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado."));
        if (!Boolean.TRUE.equals(producto.getActivo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El producto no está disponible.");
        }
        LineaCarrito linea = carrito.getLineas().stream()
                .filter(item -> item.getProducto().getId().equals(producto.getId())).findFirst().orElse(null);
        if (linea == null) {
            linea = new LineaCarrito();
            linea.setCarrito(carrito);
            linea.setProducto(producto);
            linea.setCantidad(request.cantidad());
            carrito.getLineas().add(linea);
        } else {
            if (request.cantidad() > Integer.MAX_VALUE - linea.getCantidad()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "La cantidad solicitada es demasiado grande.");
            }
            linea.setCantidad(linea.getCantidad() + request.cantidad());
        }
        return response(carritos.save(carrito));
    }

    public CarritoResponse cambiarCantidad(Long usuarioId, Long productoId, Integer cantidad) {
        Carrito carrito = obtenerOCrear(usuarioId);
        LineaCarrito linea = buscarLinea(carrito, productoId);
        linea.setCantidad(cantidad);
        return response(carritos.save(carrito));
    }

    public CarritoResponse quitar(Long usuarioId, Long productoId) {
        Carrito carrito = obtenerOCrear(usuarioId);
        carrito.getLineas().remove(buscarLinea(carrito, productoId));
        return response(carritos.save(carrito));
    }

    public CarritoResponse vaciar(Long usuarioId) {
        Carrito carrito = obtenerOCrear(usuarioId);
        carrito.getLineas().clear();
        return response(carritos.save(carrito));
    }

    private Carrito obtenerOCrear(Long usuarioId) {
        return carritos.findByUsuarioIdWithDetails(usuarioId).orElseGet(() -> {
            Usuario usuario = usuarios.findById(usuarioId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
            Carrito nuevo = new Carrito();
            nuevo.setUsuario(usuario);
            return carritos.save(nuevo);
        });
    }

    private LineaCarrito buscarLinea(Carrito carrito, Long productoId) {
        return carrito.getLineas().stream().filter(item -> item.getProducto().getId().equals(productoId)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El producto no está en el carrito."));
    }

    private CarritoResponse response(Carrito carrito) {
        var items = carrito.getLineas().stream()
                .sorted(Comparator.comparing(linea -> linea.getProducto().getNombre(), String.CASE_INSENSITIVE_ORDER))
                .map(linea -> {
                    Producto producto = linea.getProducto();
                    BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(linea.getCantidad()));
                    return new LineaCarritoResponse(producto.getId(), producto.getNombre(), producto.getImagenUrl(),
                            producto.getPrecio(), linea.getCantidad(), subtotal, producto.getStock());
                }).toList();
        BigDecimal total = items.stream().map(LineaCarritoResponse::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CarritoResponse(carrito.getUsuario().getId(), items, total);
    }
}
