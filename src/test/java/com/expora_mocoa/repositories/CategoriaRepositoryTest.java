package com.expora_mocoa.repositories;
 
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.expora_mocoa.entities.Categoria;
 
// Prueba unitaria de CategoriaRepository (capa Repository, base H2 en memoria). Autor: Eduin
@DataJpaTest
class CategoriaRepositoryTest {
 
    @Autowired
    private CategoriaRepository categoriaRepository;
 
    // Prueba 4: existsByNombre dice true si existe y false si no
    @Test
    void existsByNombre_funciona() {
        categoriaRepository.save(new Categoria("Aventura", "Deportes extremos"));
 
        assertTrue(categoriaRepository.existsByNombre("Aventura"));
        assertFalse(categoriaRepository.existsByNombre("No existe"));
    }
}
