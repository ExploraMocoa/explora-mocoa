package com.expora_mocoa.services;
 
import com.expora_mocoa.entities.Orden;
import com.expora_mocoa.entities.Usuario;
import com.expora_mocoa.repositories.OrdenRepository;
import com.expora_mocoa.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
 
import java.math.BigDecimal;
import java.util.Optional;
 
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
 
// Prueba unitaria de OrdenService (capa Service). Autor: Julio
@ExtendWith(MockitoExtension.class)
class OrdenServiceTest {
 
    @Mock
    private OrdenRepository ordenRepository;
 
    @Mock
    private UsuarioRepository usuarioRepository;
 
    @InjectMocks
    private OrdenService ordenService;
 
    // Prueba 1: una orden nueva sin estado queda en PENDIENTE y con total 0
    @Test
    void crear_sinEstado_quedaPendienteYTotalCero() {
        Usuario turista = new Usuario();
        turista.setId(1L);
        Orden orden = new Orden();
        orden.setEstado(null);
        orden.setTotal(null);
        orden.setUsuario(turista);
 
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(turista));
        when(ordenRepository.save(any(Orden.class))).thenAnswer(inv -> inv.getArgument(0));
 
        Orden guardada = ordenService.create(orden);
 
        assertEquals("PENDIENTE", guardada.getEstado());
        assertEquals(BigDecimal.ZERO, guardada.getTotal());
        assertNotNull(guardada.getFecha());
    }
}

 