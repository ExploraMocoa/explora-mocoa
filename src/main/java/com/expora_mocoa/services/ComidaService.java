package com.expora_mocoa.services;

import com.expora_mocoa.entities.Comida;
import com.expora_mocoa.repositories.ComidaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class ComidaService {

    private final ComidaRepository repository;

    public ComidaService(ComidaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Comida> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Comida findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comida no encontrada con id " + id));
    }

    public Comida create(Comida comida) {
        if (comida.getNombre() == null || comida.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la comida es obligatorio");
        }
        if (comida.getPrecio() == null || comida.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio es obligatorio y no puede ser negativo");
        }
        return repository.save(comida);
    }

    public Comida update(Long id, Comida datos) {
        Comida comida = findById(id);
        if (datos.getNombre() != null && !datos.getNombre().isBlank()) {
            comida.setNombre(datos.getNombre().trim());
        }
        if (datos.getDescripcion() != null) {
            comida.setDescripcion(datos.getDescripcion());
        }
        if (datos.getTipo() != null) {
            comida.setTipo(datos.getTipo());
        }
        if (datos.getPrecio() != null) {
            if (datos.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("El precio no puede ser negativo");
            }
            comida.setPrecio(datos.getPrecio());
        }
        return repository.save(comida);
    }

    public void delete(Long id) {
        Comida comida = findById(id);
        // La FK es ON DELETE RESTRICT: no se puede borrar si está en items de órdenes
        if (comida.getItemsOrden() != null && !comida.getItemsOrden().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar la comida: está incluida en órdenes");
        }
        repository.delete(comida);
    }
}
