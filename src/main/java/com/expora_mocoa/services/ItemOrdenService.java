package com.expora_mocoa.services;

import com.expora_mocoa.entities.Comida;
import com.expora_mocoa.entities.ItemOrden;
import com.expora_mocoa.entities.Orden;
import com.expora_mocoa.repositories.ComidaRepository;
import com.expora_mocoa.repositories.ItemOrdenRepository;
import com.expora_mocoa.repositories.OrdenRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class ItemOrdenService {

    private final ItemOrdenRepository repository;
    private final OrdenRepository ordenRepository;
    private final ComidaRepository comidaRepository;
    private final OrdenService ordenService;

    public ItemOrdenService(ItemOrdenRepository repository,
                            OrdenRepository ordenRepository,
                            ComidaRepository comidaRepository,
                            OrdenService ordenService) {
        this.repository = repository;
        this.ordenRepository = ordenRepository;
        this.comidaRepository = comidaRepository;
        this.ordenService = ordenService;
    }

    @Transactional(readOnly = true)
    public List<ItemOrden> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public ItemOrden findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item no encontrado con id " + id));
    }

    @Transactional(readOnly = true)
    public List<ItemOrden> findByOrden(Long ordenId) {
        return repository.findByOrdenId(ordenId);
    }

    public ItemOrden create(ItemOrden item) {
        if (item.getOrden() == null || item.getOrden().getId() == null) {
            throw new IllegalArgumentException("La orden es obligatoria");
        }
        if (item.getComida() == null || item.getComida().getId() == null) {
            throw new IllegalArgumentException("La comida es obligatoria");
        }
        Orden orden = ordenRepository.findById(item.getOrden().getId())
                .orElseThrow(() -> new EntityNotFoundException("Orden no encontrada"));
        Comida comida = comidaRepository.findById(item.getComida().getId())
                .orElseThrow(() -> new EntityNotFoundException("Comida no encontrada"));

        if (item.getCantidad() == null || item.getCantidad() < 1) {
            item.setCantidad(1);
        }
        // Si no envían precio, se usa el precio actual de la comida (foto del precio al momento de la compra)
        if (item.getPrecioUnitario() == null) {
            item.setPrecioUnitario(comida.getPrecio());
        }
        if (item.getPrecioUnitario() == null || item.getPrecioUnitario().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio unitario no puede ser negativo");
        }
        item.setOrden(orden);
        item.setComida(comida);
        ItemOrden guardado = repository.save(item);
        // Mantiene el total de la orden siempre actualizado
        ordenService.recalcularTotal(orden.getId());
        return guardado;
    }

    public ItemOrden update(Long id, ItemOrden datos) {
        ItemOrden item = findById(id);
        if (datos.getCantidad() != null) {
            if (datos.getCantidad() < 1) {
                throw new IllegalArgumentException("La cantidad mínima es 1");
            }
            item.setCantidad(datos.getCantidad());
        }
        if (datos.getPrecioUnitario() != null) {
            if (datos.getPrecioUnitario().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("El precio unitario no puede ser negativo");
            }
            item.setPrecioUnitario(datos.getPrecioUnitario());
        }
        if (datos.getComida() != null && datos.getComida().getId() != null) {
            Comida comida = comidaRepository.findById(datos.getComida().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Comida no encontrada"));
            item.setComida(comida);
        }
        ItemOrden guardado = repository.save(item);
        ordenService.recalcularTotal(item.getOrden().getId());
        return guardado;
    }

    public void delete(Long id) {
        ItemOrden item = findById(id);
        Long ordenId = item.getOrden().getId();
        repository.delete(item);
        ordenService.recalcularTotal(ordenId);
    }
}
