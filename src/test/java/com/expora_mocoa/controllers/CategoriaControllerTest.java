package com.expora_mocoa.controllers;
 
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.expora_mocoa.entities.Categoria;
import com.expora_mocoa.services.CategoriaService;
 
// Prueba unitaria de CategoriaController (capa Controller). Autor: Eduin
@ExtendWith(MockitoExtension.class)
class CategoriaControllerTest {
 
    @Mock
    private CategoriaService categoriaService;
 
    @InjectMocks
    private CategoriaController categoriaController;
 
    // Prueba 3: crear responde 201 CREATED con la categoría creada
    @Test
    void crear_responde201() {
        Categoria entrada = new Categoria("Gastronomía", "Comida típica");
        Categoria creada = new Categoria("Gastronomía", "Comida típica");
        creada.setId(1L);
        when(categoriaService.create(entrada)).thenReturn(creada);
 
        ResponseEntity<Categoria> respuesta = categoriaController.crear(entrada);
 
        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertEquals(1L, respuesta.getBody().getId());
    }
}
