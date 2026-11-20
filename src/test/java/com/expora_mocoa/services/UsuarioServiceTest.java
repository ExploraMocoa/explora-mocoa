package com.expora_mocoa.services;

import com.expora_mocoa.entities.Rol;
import com.expora_mocoa.entities.Usuario;
import com.expora_mocoa.repositories.RolRepository;
import com.expora_mocoa.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Pruebas unitarias de UsuarioService (capa Service). Autora: Ana
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario nuevoUsuario() {
        Rol rol = new Rol("TURISTA");
        rol.setId(3L);
        Usuario usuario = new Usuario();
        usuario.setNombre("Ana Prueba");
        usuario.setCorreo("ana@test.com");
        usuario.setContrasena("1234");
        usuario.setRol(rol);
        return usuario;
    }

    // Prueba 1: al crear un usuario, la contraseña se guarda encriptada con BCrypt
    @Test
    void crear_encriptaLaContrasena() {
        Usuario usuario = nuevoUsuario();
        when(usuarioRepository.findByCorreo("ana@test.com")).thenReturn(Optional.empty());
        when(rolRepository.findById(3L)).thenReturn(Optional.of(usuario.getRol()));
        when(passwordEncoder.encode("1234")).thenReturn("$2a$10$hashFalso");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario guardado = usuarioService.create(usuario);

        assertEquals("$2a$10$hashFalso", guardado.getContrasena());
        verify(passwordEncoder).encode("1234");
        verify(usuarioRepository).save(usuario);
    }

    // Prueba 2: si el correo ya existe, lanza error y NO guarda nada
    @Test
    void crear_correoRepetido_lanzaError() {
        Usuario usuario = nuevoUsuario();
        Usuario existente = new Usuario();
        existente.setId(1L);
        when(usuarioRepository.findByCorreo("ana@test.com")).thenReturn(Optional.of(existente));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.create(usuario));

        assertEquals("El correo ya está registrado", error.getMessage());
        verify(usuarioRepository, never()).save(any());
    }
}
