package com.expora_mocoa.services;

import com.expora_mocoa.entities.Rol;
import com.expora_mocoa.entities.Usuario;
import com.expora_mocoa.repositories.RolRepository;
import com.expora_mocoa.repositories.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository repository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Usuario findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id " + id));
    }

    @Transactional(readOnly = true)
    public Usuario findByCorreo(String correo) {
        return repository.findByCorreo(correo)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con correo " + correo));
    }

    public Usuario create(Usuario usuario) {
        validar(usuario, null);
        // Resolver rol (puede venir solo con id)
        usuario.setRol(resolverRol(usuario.getRol() != null ? usuario.getRol().getId() : null));
        // Encriptar con BCrypt antes de guardar. Si ya viene encriptado (seed), no re-encriptar.
        usuario.setContrasena(encriptarSiEsNecesario(usuario.getContrasena()));
        return repository.save(usuario);
    }

    public Usuario update(Long id, Usuario datos) {
        Usuario usuario = findById(id);
        validar(datos, id);
        usuario.setNombre(datos.getNombre());
        usuario.setCorreo(datos.getCorreo());
        if (datos.getContrasena() != null && !datos.getContrasena().isBlank()) {
            usuario.setContrasena(encriptarSiEsNecesario(datos.getContrasena()));
        }
        // Permite cambiar a rol null (SET NULL) o a otro rol
        if (datos.getRol() != null) {
            usuario.setRol(resolverRol(datos.getRol().getId()));
        }
        return repository.save(usuario);
    }

    public void delete(Long id) {
        Usuario usuario = findById(id);
        // CASCADE: se borran lugares, reseñas, eventos y órdenes del usuario
        repository.delete(usuario);
    }

    private void validar(Usuario u, Long idActual) {
        if (u.getNombre() == null || u.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (u.getCorreo() == null || u.getCorreo().isBlank()) {
            throw new IllegalArgumentException("El correo es obligatorio");
        }
        if (idActual == null && (u.getContrasena() == null || u.getContrasena().isBlank())) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        repository.findByCorreo(u.getCorreo().trim()).ifPresent(existente -> {
            if (idActual == null || !existente.getId().equals(idActual)) {
                throw new IllegalArgumentException("El correo ya está registrado");
            }
        });
        u.setCorreo(u.getCorreo().trim());
        u.setNombre(u.getNombre().trim());
    }

    private Rol resolverRol(Long rolId) {
        if (rolId == null) {
            return null;
        }
        return rolRepository.findById(rolId)
                .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con id " + rolId));
    }

    
    private String encriptarSiEsNecesario(String contrasena) {
        if (contrasena.startsWith("$2a$") || contrasena.startsWith("$2b$") || contrasena.startsWith("$2y$")) {
            return contrasena;
        }
        return passwordEncoder.encode(contrasena);
    }
}
