package com.expora_mocoa.services;

import com.expora_mocoa.entities.Comida;
import com.expora_mocoa.entities.ItemOrden;
import com.expora_mocoa.entities.Orden;
import com.expora_mocoa.repositories.ComidaRepository;
import com.expora_mocoa.repositories.ItemOrdenRepository;
import com.expora_mocoa.repositories.OrdenRepository;
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

// Prueba unitaria de ItemOrdenService (capa Service). Autor: Julio
@ExtendWith(MockitoExtension.class)
class ItemOrdenServiceTest {

    @Mock
    private ItemOrdenRepository itemRepository;

    @Mock
    private OrdenRepository ordenRepository;

    @Mock
    private ComidaRepository comidaRepository;

    @Mock
    private OrdenService ordenService;

    @InjectMocks
    private ItemOrdenService itemOrdenService;

    // Prueba 2: si el item no trae precio, usa el precio de la comida y recalcula el total
    @Test
    void crear_sinPrecio_usaElPrecioDeLaComida() {
        Orden orden = new Orden();
        orden.setId(1L);
        Comida comida = new Comida();
        comida.setId(5L);
        comida.setPrecio(new BigDecimal("12000"));

        ItemOrden item = new ItemOrden();
        item.setCantidad(2);
        item.setOrden(orden);
        item.setComida(comida);

        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(comidaRepository.findById(5L)).thenReturn(Optional.of(comida));
        when(itemRepository.save(any(ItemOrden.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemOrden guardado = itemOrdenService.create(item);

        assertEquals(new BigDecimal("12000"), guardado.getPrecioUnitario());
        verify(ordenService).recalcularTotal(1L);
    }
}
