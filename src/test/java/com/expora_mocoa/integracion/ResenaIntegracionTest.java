package com.expora_mocoa.integracion;
 
import com.expora_mocoa.entities.Categoria;
import com.expora_mocoa.entities.LugarTuristico;
import com.expora_mocoa.entities.Usuario;
import com.expora_mocoa.repositories.CategoriaRepository;
import com.expora_mocoa.repositories.LugarTuristicoRepository;
import com.expora_mocoa.repositories.ResenaRepository;
import com.expora_mocoa.repositories.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
 
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
 
// Prueba de integración: login + ResenaController + ResenaService + base H2. Autora: Valery
@SpringBootTest
@AutoConfigureMockMvc
class ResenaIntegracionTest {
 
    @Autowired
    private MockMvc mockMvc;
 
    @Autowired
    private UsuarioRepository usuarioRepository;
 
    @Autowired
    private CategoriaRepository categoriaRepository;
 
    @Autowired
    private LugarTuristicoRepository lugarRepository;
 
    @Autowired
    private ResenaRepository resenaRepository;
 
    // Inicia sesión con el admin que crea el DataInitializer y devuelve el token
    private String tokenAdmin() throws Exception {
        String respuesta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\": \"admin@gmail.com\", \"contrasena\": \"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.token");
    }
 
    // Prueba 5: se crea una reseña real (201), se le pone la fecha sola y queda guardada
    @Test
    void crearResena_devuelve201_yQuedaGuardada() throws Exception {
        Usuario admin = usuarioRepository.findByCorreo("admin@gmail.com").orElseThrow();
        Categoria categoria = categoriaRepository.save(new Categoria("Paisajes", "Miradores"));
        LugarTuristico lugar = new LugarTuristico();
        lugar.setNombre("Mirador de Mocoa");
        lugar.setCategoria(categoria);
        lugar.setUsuario(admin);
        lugarRepository.save(lugar);
 
        String json = "{\"calificacion\": 5, \"comentario\": \"Hermoso lugar\","
                + " \"usuario\": {\"id\": " + admin.getId() + "},"
                + " \"lugarTuristico\": {\"id\": " + lugar.getId() + "}}";
 
        mockMvc.perform(post("/api/resenas")
                        .header("Authorization", "Bearer " + tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.calificacion").value(5))
                .andExpect(jsonPath("$.fecha").exists());
 
        assertEquals(1, resenaRepository.findByLugarTuristicoId(lugar.getId()).size());
    }
}
