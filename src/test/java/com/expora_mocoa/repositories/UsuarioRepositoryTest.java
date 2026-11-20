package com.expora_mocoa.repositories;

import com.expora_mocoa.entities.Rol;
import com.expora_mocoa.entities.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

// Prueba unitaria de UsuarioRepository (capa Repository, base H2 en memoria). Autora: Ana
@DataJpaTest
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    // Prueba 4: findByCorreo encuentra al usuario guardado
    @Test
    void findByCorreo_encuentraElUsuario() {
        Rol rol = rolRepository.save(new Rol("TURISTA"));
        Usuario usuario = new Usuario();
        usuario.setNombre("Ana Repo");
        usuario.setCorreo("repo@test.com");
        usuario.setContrasena("$2a$10$hashFalso");
        usuario.setRol(rol);
        usuarioRepository.save(usuario);

        Optional<Usuario> encontrado = usuarioRepository.findByCorreo("repo@test.com");

        assertTrue(encontrado.isPresent());
        assertEquals("Ana Repo", encontrado.get().getNombre());
    }
}
