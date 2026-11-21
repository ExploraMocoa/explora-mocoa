package com.expora_mocoa.repositories;

import com.expora_mocoa.entities.Orden;
import com.expora_mocoa.entities.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Prueba unitaria de OrdenRepository (capa Repository, base H2 en memoria). Autor: Julio
@DataJpaTest
class OrdenRepositoryTest {

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Prueba 4: findByEstado trae solo las órdenes con ese estado
    @Test
    void findByEstado_traeSoloLasDeEseEstado() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Julio");
        usuario.setCorreo("julio@test.com");
        usuario.setContrasena("$2a$10$hashFalso");
        usuarioRepository.save(usuario);

        Orden pendiente = new Orden();
        pendiente.setUsuario(usuario);
        pendiente.setFecha(LocalDateTime.now());
        pendiente.setEstado("PENDIENTE");
        ordenRepository.save(pendiente);

        Orden entregada = new Orden();
        entregada.setUsuario(usuario);
        entregada.setFecha(LocalDateTime.now());
        entregada.setEstado("ENTREGADA");
        ordenRepository.save(entregada);

        List<Orden> resultado = ordenRepository.findByEstado("PENDIENTE");

        assertEquals(1, resultado.size());
        assertEquals("PENDIENTE", resultado.get(0).getEstado());
    }
}
