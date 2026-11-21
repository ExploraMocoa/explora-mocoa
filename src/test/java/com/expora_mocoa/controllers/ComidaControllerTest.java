package com.expora_mocoa.controllers;

import com.expora_mocoa.entities.Comida;
import com.expora_mocoa.services.ComidaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Prueba unitaria de ComidaController (capa Controller). Autora: Desiree
@ExtendWith(MockitoExtension.class)
class ComidaControllerTest {

    @Mock
    private ComidaService comidaService;

    @InjectMocks
    private ComidaController comidaController;

    // Prueba: listar devuelve las comidas que entrega el service
    @Test
    void listar_devuelveLasComidas() {
        Comida c1 = new Comida();
        c1.setNombre("Mojarra frita");

        Comida c2 = new Comida();
        c2.setNombre("Cuy asado");

        when(comidaService.findAll()).thenReturn(List.of(c1, c2));

        List<Comida> resultado = comidaController.listar();

        assertEquals(2, resultado.size());
        assertEquals("Mojarra frita", resultado.get(0).getNombre());
        assertEquals("Cuy asado", resultado.get(1).getNombre());

        verify(comidaService).findAll();
    }
}
