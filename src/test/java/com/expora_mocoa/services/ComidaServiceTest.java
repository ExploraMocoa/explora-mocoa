package com.expora_mocoa.services;

import com.expora_mocoa.entities.Comida;
import com.expora_mocoa.repositories.ComidaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Prueba unitaria de ComidaService (capa Service). Autora: Desiree
@ExtendWith(MockitoExtension.class)
class ComidaServiceTest {

    @Mock
    private ComidaRepository comidaRepository;

    @InjectMocks
    private ComidaService comidaService;

    // Prueba 1: no deja crear una comida con precio negativo
    @Test
    void crear_precioNegativo_lanzaError() {
        Comida comida = new Comida();
        comida.setNombre("Mojarra frita");
        comida.setPrecio(new BigDecimal("-5000"));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> comidaService.create(comida)
        );

        assertEquals(
                "El precio es obligatorio y no puede ser negativo",
                error.getMessage()
        );

        verify(comidaRepository, never()).save(any());
    }
}
