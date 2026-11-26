package com.ejemplo.articulos.service;

import com.ejemplo.articulos.dto.UsuarioRequest;
import com.ejemplo.articulos.dto.UsuarioResponse;
import com.ejemplo.articulos.model.Usuario;
import com.ejemplo.articulos.repository.PedidoRepository;
import com.ejemplo.articulos.repository.UsuarioRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final PedidoRepository pedidos;

    public UsuarioService(UsuarioRepository usuarios, PedidoRepository pedidos) {
        this.usuarios = usuarios;
        this.pedidos = pedidos;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() { return usuarios.findAll().stream().map(this::response).toList(); }

    @Transactional(readOnly = true)
    public UsuarioResponse login(String email) {
        Usuario usuario = usuarios.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No existe un usuario con ese correo."));
        return response(usuario);
    }

    public UsuarioResponse crear(UsuarioRequest request) {
        String email = request.email().trim().toLowerCase();
        if (usuarios.findByEmailIgnoreCase(email).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo.");
        return response(usuarios.save(new Usuario(request.nombre().trim(), email)));
    }

    public UsuarioResponse actualizar(Long id, UsuarioRequest request) {
        Usuario usuario = usuarios.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
        String email = request.email().trim().toLowerCase();
        usuarios.findByEmailIgnoreCase(email).filter(otro -> !otro.getId().equals(id)).ifPresent(otro -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo.");
        });
        usuario.setNombre(request.nombre().trim());
        usuario.setEmail(email);
        return response(usuarios.save(usuario));
    }

    public void eliminar(Long id) {
        Usuario usuario = usuarios.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
        if (pedidos.existsByUsuarioId(id)) throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede eliminar un usuario con pedidos registrados.");
        usuarios.delete(usuario);
    }

    private UsuarioResponse response(Usuario usuario) { return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getEmail()); }
}
