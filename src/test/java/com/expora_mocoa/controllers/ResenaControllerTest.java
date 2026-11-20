package com.expora_mocoa.controllers;
 
import com.expora_mocoa.entities.Resena;
import com.expora_mocoa.services.ResenaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
 
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
 
// Prueba unitaria de ResenaController (capa Controller). Autora: Valery
@ExtendWith(MockitoExtension.class)
class ResenaControllerTest {
 
    @Mock
    private ResenaService resenaService;
 
    @InjectMocks
    private ResenaController resenaController;
 
    // Prueba 3: obtener por id devuelve la reseña que entrega el service (simulado)
    @Test
    void obtener_devuelveLaResena() {
        Resena resena = new Resena();
        resena.setId(1L);
        resena.setCalificacion(5);
        resena.setComentario("Excelente");
        when(resenaService.findById(1L)).thenReturn(resena);
 
        Resena resultado = resenaController.obtener(1L);
 
        assertEquals(5, resultado.getCalificacion());
        assertEquals("Excelente", resultado.getComentario());
        verify(resenaService).findById(1L);
    }
}
