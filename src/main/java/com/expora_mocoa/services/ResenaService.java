package com.expora_mocoa.services;

import com.expora_mocoa.entities.LugarTuristico;
import com.expora_mocoa.entities.Resena;
import com.expora_mocoa.entities.Usuario;
import com.expora_mocoa.repositories.LugarTuristicoRepository;
import com.expora_mocoa.repositories.ResenaRepository;
import com.expora_mocoa.repositories.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ResenaService {

    private final ResenaRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final LugarTuristicoRepository lugarRepository;

    public ResenaService(ResenaRepository repository,
                         UsuarioRepository usuarioRepository,
                         LugarTuristicoRepository lugarRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.lugarRepository = lugarRepository;
    }

    @Transactional(readOnly = true)
    public List<Resena> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Resena findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reseña no encontrada con id " + id));
    }

    @Transactional(readOnly = true)
    public List<Resena> findByLugar(Long lugarId) {
        return repository.findByLugarTuristicoId(lugarId);
    }

    @Transactional(readOnly = true)
    public List<Resena> findByUsuario(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }

    public Resena create(Resena resena) {
        validarCalificacion(resena.getCalificacion());
        if (resena.getUsuario() == null || resena.getUsuario().getId() == null) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }
        if (resena.getLugarTuristico() == null || resena.getLugarTuristico().getId() == null) {
            throw new IllegalArgumentException("El lugar turístico es obligatorio");
        }
        Usuario usuario = usuarioRepository.findById(resena.getUsuario().getId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        LugarTuristico lugar = lugarRepository.findById(resena.getLugarTuristico().getId())
                .orElseThrow(() -> new EntityNotFoundException("Lugar turístico no encontrado"));
        resena.setUsuario(usuario);
        resena.setLugarTuristico(lugar);
        if (resena.getFecha() == null) {
            resena.setFecha(LocalDateTime.now());
        }
        return repository.save(resena);
    }

    public Resena update(Long id, Resena datos) {
        Resena resena = findById(id);
        if (datos.getCalificacion() != null) {
            validarCalificacion(datos.getCalificacion());
            resena.setCalificacion(datos.getCalificacion());
        }
        if (datos.getComentario() != null) {
            resena.setComentario(datos.getComentario());
        }
        return repository.save(resena);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private void validarCalificacion(Integer calificacion) {
        if (calificacion == null || calificacion < 1 || calificacion > 5) {
            throw new IllegalArgumentException("La calificación debe estar entre 1 y 5");
        }
    }
}
