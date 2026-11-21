package com.expora_mocoa.services;
 
import com.expora_mocoa.entities.Resena;
import com.expora_mocoa.repositories.LugarTuristicoRepository;
import com.expora_mocoa.repositories.ResenaRepository;
import com.expora_mocoa.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
 
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
 
// Prueba unitaria de ResenaService (capa Service). Autora: Valery
@ExtendWith(MockitoExtension.class)
class ResenaServiceTest {
 
    @Mock
    private ResenaRepository resenaRepository;
 
    @Mock
    private UsuarioRepository usuarioRepository;
 
    @Mock
    private LugarTuristicoRepository lugarRepository;
 
    @InjectMocks
    private ResenaService resenaService;
 
    // Prueba 1: una calificación de 6 estrellas no se permite (solo de 1 a 5)
    @Test
    void crear_calificacionFueraDeRango_lanzaError() {
        Resena resena = new Resena();
        resena.setCalificacion(6);
        resena.setComentario("Muy bonito");
 
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> resenaService.create(resena));
 
        assertEquals("La calificación debe estar entre 1 y 5", error.getMessage());
        verify(resenaRepository, never()).save(any());
    }
}
