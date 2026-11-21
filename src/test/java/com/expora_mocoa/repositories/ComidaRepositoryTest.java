package com.expora_mocoa.repositories;

import com.expora_mocoa.entities.Comida;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Prueba de ComidaRepository con base de datos H2. Autora: Desiree
@DataJpaTest
class ComidaRepositoryTest {

    @Autowired
    private ComidaRepository comidaRepository;

    // Prueba: busca comidas por nombre sin distinguir mayúsculas
    @Test
    void findByNombreContainingIgnoreCase_encuentraLasComidas() {
        Comida comida = new Comida();
        comida.setNombre("Mojarra Frita");
        comida.setTipo("Plato típico");
        comida.setPrecio(new BigDecimal("25000.00"));

        comidaRepository.save(comida);

        List<Comida> resultado =
                comidaRepository.findByNombreContainingIgnoreCase("mojarra");

        assertEquals(1, resultado.size());
        assertEquals("Mojarra Frita", resultado.get(0).getNombre());
    }
}
