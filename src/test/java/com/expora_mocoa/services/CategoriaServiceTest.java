package com.expora_mocoa.services;
 
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.expora_mocoa.entities.Categoria;
import com.expora_mocoa.repositories.CategoriaRepository;
 
// Prueba unitaria de CategoriaService (capa Service). Autor: Eduin
@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {
 
    @Mock
    private CategoriaRepository categoriaRepository;
 
    @InjectMocks
    private CategoriaService categoriaService;
 
    // Prueba 1: no deja crear una categoría con un nombre que ya existe
    @Test
    void crear_nombreRepetido_lanzaError() {
        Categoria categoria = new Categoria("Naturaleza", "Ríos y cascadas");
        when(categoriaRepository.existsByNombre("Naturaleza")).thenReturn(true);
 
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> categoriaService.create(categoria));
 
        assertEquals("Ya existe una categoría con ese nombre", error.getMessage());
        verify(categoriaRepository, never()).save(any());
    }
}
