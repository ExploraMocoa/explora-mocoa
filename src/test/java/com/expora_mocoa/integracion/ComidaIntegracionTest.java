package com.expora_mocoa.integracion;

import com.expora_mocoa.entities.Comida;
import com.expora_mocoa.services.ComidaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

// Prueba de integración: ComidaService + Repository + base H2. Autora: Desiree
@SpringBootTest
@Transactional
class ComidaIntegracionTest {

    @Autowired
    private ComidaService comidaService;

    // Prueba: guarda una comida y después la recupera de la base de datos
    @Test
    void crearYConsultarComida_guardaYRecuperaCorrectamente() {
        Comida comida = new Comida();
        comida.setNombre("Trucha amazónica");
        comida.setDescripcion("Trucha preparada al estilo regional");
        comida.setTipo("Plato típico");
        comida.setPrecio(new BigDecimal("30000.00"));

        Comida guardada = comidaService.create(comida);

        assertNotNull(guardada.getId());

        Comida encontrada = comidaService.findById(guardada.getId());

        assertNotNull(encontrada);
        assertEquals("Trucha amazónica", encontrada.getNombre());
        assertEquals(new BigDecimal("30000.00"), encontrada.getPrecio());
    }
}
