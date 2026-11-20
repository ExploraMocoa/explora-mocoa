package com.expora_mocoa.integracion;

import com.expora_mocoa.entities.Usuario;
import com.expora_mocoa.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Prueba de integración: AuthController + Spring Security + Repository + base H2. Autora: Ana
@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Prueba 5: el registro real devuelve 201 con token y guarda al usuario encriptado
    @Test
    void registro_devuelve201_yGuardaUsuarioEncriptado() throws Exception {
        String json = """
                {"nombre": "Turista Integracion", "correo": "turista.int@test.com", "contrasena": "1234"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.rol").value("TURISTA"));

        Usuario guardado = usuarioRepository.findByCorreo("turista.int@test.com").orElseThrow();
        assertNotEquals("1234", guardado.getContrasena());
        assertTrue(guardado.getContrasena().startsWith("$2a$"));
    }
}
