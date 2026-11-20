package com.expora_mocoa.controllers;

import com.expora_mocoa.entities.Usuario;
import com.expora_mocoa.services.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Prueba unitaria de UsuarioController (capa Controller). Autora: Ana
@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    // Prueba 3: listar devuelve los usuarios que entrega el service (simulado)
    @Test
    void listar_devuelveLosUsuarios() {
        Usuario u1 = new Usuario();
        u1.setNombre("Ana");
        Usuario u2 = new Usuario();
        u2.setNombre("Eduin");
        when(usuarioService.findAll()).thenReturn(List.of(u1, u2));

        List<Usuario> resultado = usuarioController.listar();

        assertEquals(2, resultado.size());
        assertEquals("Ana", resultado.get(0).getNombre());
        verify(usuarioService).findAll();
    }
}
