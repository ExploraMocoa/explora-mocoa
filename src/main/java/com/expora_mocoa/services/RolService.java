package com.expora_mocoa.services;

import com.expora_mocoa.entities.Rol;
import com.expora_mocoa.repositories.RolRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class RolService {

    private final RolRepository repository;

    public RolService(RolRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Rol> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Rol findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con id " + id));
    }

    public Rol create(Rol rol) {
        if (rol.getNombre() == null || rol.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del rol es obligatorio");
        }
        if (repository.existsByNombre(rol.getNombre().trim().toUpperCase())) {
            throw new IllegalArgumentException("Ya existe un rol con ese nombre");
        }
        rol.setNombre(rol.getNombre().trim().toUpperCase());
        return repository.save(rol);
    }

    public Rol update(Long id, Rol datos) {
        Rol rol = findById(id);
        if (datos.getNombre() != null && !datos.getNombre().isBlank()) {
            String nuevo = datos.getNombre().trim().toUpperCase();
            if (!nuevo.equals(rol.getNombre()) && repository.existsByNombre(nuevo)) {
                throw new IllegalArgumentException("Ya existe un rol con ese nombre");
            }
            rol.setNombre(nuevo);
        }
        return repository.save(rol);
    }

    public void delete(Long id) {
        Rol rol = findById(id);
        // Los usuarios quedan con rol NULL (ON DELETE SET NULL), por eso se permite borrar.
        // Si quieres impedirlo cuando tenga usuarios, descomenta:
        // if (!rol.getUsuarios().isEmpty()) throw new IllegalStateException("No se puede eliminar: tiene usuarios asociados");
        repository.delete(rol);
    }
}
