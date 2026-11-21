package com.expora_mocoa.integracion;
 
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.expora_mocoa.repositories.CategoriaRepository;
import com.jayway.jsonpath.JsonPath;
 
// Prueba de integración: login + CategoriaController + CategoriaService + base H2. Autor: Eduin
@SpringBootTest
@AutoConfigureMockMvc
class CategoriaIntegracionTest {
 
    @Autowired
    private MockMvc mockMvc;
 
    @Autowired
    private CategoriaRepository categoriaRepository;
 
    // Inicia sesión con el admin que crea el DataInitializer y devuelve el token
    private String tokenAdmin() throws Exception {
        String respuesta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\": \"admin@gmail.com\", \"contrasena\": \"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.token");
    }
 
    // Prueba 5: el ADMIN crea una categoría (201) y queda guardada en la base
    @Test
    void adminCreaCategoria_devuelve201_yQuedaGuardada() throws Exception {
        mockMvc.perform(post("/api/categorias")
                        .header("Authorization", "Bearer " + tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"Cultura\", \"descripcion\": \"Museos y tradiciones\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Cultura"));
 
        assertTrue(categoriaRepository.existsByNombre("Cultura"));
    }
}
