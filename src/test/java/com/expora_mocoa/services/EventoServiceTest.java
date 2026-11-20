package com.expora_mocoa.services;
 
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.expora_mocoa.entities.Evento;
import com.expora_mocoa.entities.Usuario;
import com.expora_mocoa.repositories.EventoRepository;
import com.expora_mocoa.repositories.LugarTuristicoRepository;
import com.expora_mocoa.repositories.UsuarioRepository;
 
// Prueba unitaria de EventoService (capa Service). Autor: Eduin
@ExtendWith(MockitoExtension.class)
class EventoServiceTest {
 
    @Mock
    private EventoRepository eventoRepository;
 
    @Mock
    private LugarTuristicoRepository lugarRepository;
 
    @Mock
    private UsuarioRepository usuarioRepository;
 
    @InjectMocks
    private EventoService eventoService;
 
    // Prueba 2: crea el evento con su organizador y sin lugar (el lugar es opcional)
    @Test
    void crear_conUsuarioYSinLugar_guardaElEvento() {
        Usuario organizador = new Usuario();
        organizador.setId(1L);
        organizador.setNombre("Organizador");
 
        Evento evento = new Evento();
        evento.setNombre("Festival del Río");
        Usuario soloId = new Usuario();
        soloId.setId(1L);
        evento.setUsuario(soloId);
 
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(organizador));
        when(eventoRepository.save(any(Evento.class))).thenAnswer(inv -> inv.getArgument(0));
 
        Evento guardado = eventoService.create(evento);
 
        assertEquals("Organizador", guardado.getUsuario().getNombre());
        assertNull(guardado.getLugarTuristico());
        verify(eventoRepository).save(evento);
    }
}
