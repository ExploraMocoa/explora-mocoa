package com.expora_mocoa.controllers;

import com.expora_mocoa.entities.Orden;
import com.expora_mocoa.services.OrdenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Prueba unitaria de OrdenController (capa Controller). Autor: Julio
@ExtendWith(MockitoExtension.class)
class OrdenControllerTest {

    @Mock
    private OrdenService ordenService;

    @InjectMocks
    private OrdenController ordenController;

    // Prueba 3: cambiarEstado le pasa el estado del JSON al service y devuelve la orden
    @Test
    void cambiarEstado_devuelveLaOrdenActualizada() {
        Orden actualizada = new Orden();
        actualizada.setId(1L);
        actualizada.setEstado("CONFIRMADA");
        when(ordenService.cambiarEstado(1L, "CONFIRMADA")).thenReturn(actualizada);

        Orden resultado = ordenController.cambiarEstado(1L, Map.of("estado", "CONFIRMADA"));

        assertEquals("CONFIRMADA", resultado.getEstado());
        verify(ordenService).cambiarEstado(1L, "CONFIRMADA");
    }
}
