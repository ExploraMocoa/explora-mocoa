package com.expora_mocoa.services;

import com.expora_mocoa.entities.Orden;
import com.expora_mocoa.entities.Usuario;
import com.expora_mocoa.repositories.OrdenRepository;
import com.expora_mocoa.repositories.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class OrdenService {

    private static final Set<String> ESTADOS = new HashSet<>(Arrays.asList(
            "PENDIENTE", "CONFIRMADA", "PREPARACION", "ENTREGADA", "CANCELADA"));

    private final OrdenRepository repository;
    private final UsuarioRepository usuarioRepository;

    public OrdenService(OrdenRepository repository, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Orden> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Orden findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Orden no encontrada con id " + id));
    }

    @Transactional(readOnly = true)
    public List<Orden> findByUsuario(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }

    public Orden create(Orden orden) {
        if (orden.getUsuario() == null || orden.getUsuario().getId() == null) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }
        Usuario usuario = usuarioRepository.findById(orden.getUsuario().getId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        orden.setUsuario(usuario);
        if (orden.getFecha() == null) {
            orden.setFecha(LocalDateTime.now());
        }
        if (orden.getEstado() == null || orden.getEstado().isBlank()) {
            orden.setEstado("PENDIENTE");
        } else {
            validarEstado(orden.getEstado());
            orden.setEstado(orden.getEstado().trim().toUpperCase());
        }
        // El total se calcula desde los items; al crear va en 0 y se recalcula al agregar items
        if (orden.getTotal() == null) {
            orden.setTotal(BigDecimal.ZERO);
        }
        // Evita persistir items anidados por aquí; usa ItemOrdenService para agregarlos
        orden.setItems(null);
        Orden guardada = repository.save(orden);
        guardada.setItems(new java.util.ArrayList<>());
        return guardada;
    }

    public Orden cambiarEstado(Long id, String nuevoEstado) {
        Orden orden = findById(id);
        validarEstado(nuevoEstado);
        orden.setEstado(nuevoEstado.trim().toUpperCase());
        return repository.save(orden);
    }

    /** Recalcula el total sumando cantidad * precio_unitario de sus items. */
    public Orden recalcularTotal(Long ordenId) {
        Orden orden = findById(ordenId);
        BigDecimal total = orden.getItems() == null ? BigDecimal.ZERO :
                orden.getItems().stream()
                        .map(i -> i.getPrecioUnitario().multiply(BigDecimal.valueOf(i.getCantidad())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        orden.setTotal(total);
        return repository.save(orden);
    }

    public void delete(Long id) {
        Orden orden = findById(id);
        // CASCADE: se borran sus items automáticamente
        repository.delete(orden);
    }

    private void validarEstado(String estado) {
        if (estado == null || !ESTADOS.contains(estado.trim().toUpperCase())) {
            throw new IllegalArgumentException("Estado inválido. Permitidos: " + ESTADOS);
        }
    }
}
