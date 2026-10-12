package com.expora_mocoa.services;

import com.expora_mocoa.entities.Categoria;
import com.expora_mocoa.repositories.CategoriaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class CategoriaService {

    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Categoria> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Categoria findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con id " + id));
    }

    public Categoria create(Categoria categoria) {
        if (categoria.getNombre() == null || categoria.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio");
        }
        if (repository.existsByNombre(categoria.getNombre().trim())) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre");
        }
        categoria.setNombre(categoria.getNombre().trim());
        return repository.save(categoria);
    }

    public Categoria update(Long id, Categoria datos) {
        Categoria categoria = findById(id);
        if (datos.getNombre() != null && !datos.getNombre().isBlank()) {
            String nuevo = datos.getNombre().trim();
            if (!nuevo.equalsIgnoreCase(categoria.getNombre()) && repository.existsByNombre(nuevo)) {
                throw new IllegalArgumentException("Ya existe una categoría con ese nombre");
            }
            categoria.setNombre(nuevo);
        }
        if (datos.getDescripcion() != null) {
            categoria.setDescripcion(datos.getDescripcion());
        }
        return repository.save(categoria);
    }

    public void delete(Long id) {
        Categoria categoria = findById(id);
        // La FK es ON DELETE RESTRICT: no se puede borrar si tiene lugares asociados
        if (categoria.getLugaresTuristicos() != null && !categoria.getLugaresTuristicos().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar la categoría: tiene lugares turísticos asociados");
        }
        repository.delete(categoria);
    }
}
