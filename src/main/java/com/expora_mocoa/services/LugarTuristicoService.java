package com.expora_mocoa.services;

import com.expora_mocoa.entities.Categoria;
import com.expora_mocoa.entities.LugarTuristico;
import com.expora_mocoa.entities.Usuario;
import com.expora_mocoa.repositories.CategoriaRepository;
import com.expora_mocoa.repositories.LugarTuristicoRepository;
import com.expora_mocoa.repositories.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class LugarTuristicoService {

    private final LugarTuristicoRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    public LugarTuristicoService(LugarTuristicoRepository repository,
                                 CategoriaRepository categoriaRepository,
                                 UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<LugarTuristico> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public LugarTuristico findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lugar turístico no encontrado con id " + id));
    }

    @Transactional(readOnly = true)
    public List<LugarTuristico> findByCategoria(Long categoriaId) {
        return repository.findByCategoriaId(categoriaId);
    }

    @Transactional(readOnly = true)
    public List<LugarTuristico> findByUsuario(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }

    public LugarTuristico create(LugarTuristico lugar) {
        if (lugar.getNombre() == null || lugar.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del lugar es obligatorio");
        }
        if (lugar.getCategoria() == null || lugar.getCategoria().getId() == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
        if (lugar.getUsuario() == null || lugar.getUsuario().getId() == null) {
            throw new IllegalArgumentException("El usuario responsable es obligatorio");
        }
        Categoria categoria = categoriaRepository.findById(lugar.getCategoria().getId())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
        Usuario usuario = usuarioRepository.findById(lugar.getUsuario().getId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        lugar.setCategoria(categoria);
        lugar.setUsuario(usuario);
        return repository.save(lugar);
    }

    public LugarTuristico update(Long id, LugarTuristico datos) {
        LugarTuristico lugar = findById(id);
        if (datos.getNombre() != null && !datos.getNombre().isBlank()) {
            lugar.setNombre(datos.getNombre().trim());
        }
        if (datos.getDescripcion() != null) {
            lugar.setDescripcion(datos.getDescripcion());
        }
        if (datos.getUbicacion() != null) {
            lugar.setUbicacion(datos.getUbicacion());
        }
        if (datos.getImagenUrl() != null) {
            lugar.setImagenUrl(datos.getImagenUrl());
        }
        if (datos.getCategoria() != null && datos.getCategoria().getId() != null) {
            Categoria categoria = categoriaRepository.findById(datos.getCategoria().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
            lugar.setCategoria(categoria);
        }
        if (datos.getUsuario() != null && datos.getUsuario().getId() != null) {
            Usuario usuario = usuarioRepository.findById(datos.getUsuario().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
            lugar.setUsuario(usuario);
        }
        return repository.save(lugar);
    }

    public void delete(Long id) {
        LugarTuristico lugar = findById(id);
        // CASCADE: se borran servicios y reseñas. Eventos quedan con lugar NULL (SET NULL).
        repository.delete(lugar);
    }
}
