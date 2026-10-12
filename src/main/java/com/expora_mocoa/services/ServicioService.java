package com.expora_mocoa.services;

import com.expora_mocoa.entities.LugarTuristico;
import com.expora_mocoa.entities.Servicio;
import com.expora_mocoa.repositories.LugarTuristicoRepository;
import com.expora_mocoa.repositories.ServicioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class ServicioService {

    private final ServicioRepository repository;
    private final LugarTuristicoRepository lugarRepository;

    public ServicioService(ServicioRepository repository, LugarTuristicoRepository lugarRepository) {
        this.repository = repository;
        this.lugarRepository = lugarRepository;
    }

    @Transactional(readOnly = true)
    public List<Servicio> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Servicio findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado con id " + id));
    }

    @Transactional(readOnly = true)
    public List<Servicio> findByLugar(Long lugarId) {
        return repository.findByLugarTuristicoId(lugarId);
    }

    public Servicio create(Servicio servicio) {
        if (servicio.getNombre() == null || servicio.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del servicio es obligatorio");
        }
        if (servicio.getLugarTuristico() == null || servicio.getLugarTuristico().getId() == null) {
            throw new IllegalArgumentException("El lugar turístico es obligatorio");
        }
        if (servicio.getPrecio() != null && servicio.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        LugarTuristico lugar = lugarRepository.findById(servicio.getLugarTuristico().getId())
                .orElseThrow(() -> new EntityNotFoundException("Lugar turístico no encontrado"));
        servicio.setLugarTuristico(lugar);
        if (servicio.getPrecio() == null) {
            servicio.setPrecio(BigDecimal.ZERO);
        }
        return repository.save(servicio);
    }

    public Servicio update(Long id, Servicio datos) {
        Servicio servicio = findById(id);
        if (datos.getNombre() != null && !datos.getNombre().isBlank()) {
            servicio.setNombre(datos.getNombre().trim());
        }
        if (datos.getDescripcion() != null) {
            servicio.setDescripcion(datos.getDescripcion());
        }
        if (datos.getPrecio() != null) {
            if (datos.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("El precio no puede ser negativo");
            }
            servicio.setPrecio(datos.getPrecio());
        }
        if (datos.getLugarTuristico() != null && datos.getLugarTuristico().getId() != null) {
            LugarTuristico lugar = lugarRepository.findById(datos.getLugarTuristico().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Lugar turístico no encontrado"));
            servicio.setLugarTuristico(lugar);
        }
        return repository.save(servicio);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }
}
