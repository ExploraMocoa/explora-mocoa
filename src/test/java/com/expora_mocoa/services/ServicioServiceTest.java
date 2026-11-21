package com.expora_mocoa.services;

import com.expora_mocoa.entities.LugarTuristico;
import com.expora_mocoa.entities.Servicio;
import com.expora_mocoa.repositories.LugarTuristicoRepository;
import com.expora_mocoa.repositories.ServicioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Prueba unitaria de ServicioService. Autora: Desiree
@ExtendWith(MockitoExtension.class)
class ServicioServiceTest {

    @Mock
    private ServicioRepository servicioRepository;

    @Mock
    private LugarTuristicoRepository lugarTuristicoRepository;

    @InjectMocks
    private ServicioService servicioService;

    // Prueba: no permite crear un servicio con precio negativo
    @Test
    void crear_precioNegativo_lanzaError() {
        LugarTuristico lugar = new LugarTuristico();
        lugar.setId(1L);

        Servicio servicio = new Servicio();
        servicio.setNombre("Recorrido turístico");
        servicio.setLugarTuristico(lugar);
        servicio.setPrecio(new BigDecimal("-10000"));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> servicioService.create(servicio)
        );

        assertEquals(
                "El precio no puede ser negativo",
                error.getMessage()
        );

        verify(servicioRepository, never()).save(any());
        verifyNoInteractions(lugarTuristicoRepository);
    }
}
