package com.expora_mocoa.integracion;

import com.expora_mocoa.entities.Comida;
import com.expora_mocoa.entities.Orden;
import com.expora_mocoa.entities.Usuario;
import com.expora_mocoa.repositories.ComidaRepository;
import com.expora_mocoa.repositories.OrdenRepository;
import com.expora_mocoa.repositories.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Prueba de integración: login + OrdenController + ItemOrdenController + services + base H2. Autor: Julio
@SpringBootTest
@AutoConfigureMockMvc
class OrdenIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ComidaRepository comidaRepository;

    @Autowired
    private OrdenRepository ordenRepository;

    // Inicia sesión con el admin que crea el DataInitializer y devuelve el token
    private String tokenAdmin() throws Exception {
        String respuesta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\": \"admin@gmail.com\", \"contrasena\": \"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.token");
    }

    // Prueba 5: se crea una orden, se le agrega un item y el total se recalcula solo
    @Test
    void crearOrdenYAgregarItem_elTotalSeRecalcula() throws Exception {
        String token = tokenAdmin();
        Usuario admin = usuarioRepository.findByCorreo("admin@gmail.com").orElseThrow();

        Comida comida = new Comida();
        comida.setNombre("Tamal");
        comida.setPrecio(new BigDecimal("8000"));
        comidaRepository.save(comida);

        String respuestaOrden = mockMvc.perform(post("/api/ordenes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\": {\"id\": " + admin.getId() + "}}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andReturn().getResponse().getContentAsString();
        Integer ordenId = JsonPath.read(respuestaOrden, "$.id");

        mockMvc.perform(post("/api/items-orden")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cantidad\": 3, \"orden\": {\"id\": " + ordenId + "},"
                                + " \"comida\": {\"id\": " + comida.getId() + "}}"))
                .andExpect(status().isCreated());

        Orden orden = ordenRepository.findById(ordenId.longValue()).orElseThrow();
        assertEquals(0, new BigDecimal("24000").compareTo(orden.getTotal()));
    }
}
