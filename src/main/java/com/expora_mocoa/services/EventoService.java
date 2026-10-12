package com.expora_mocoa.services;

import com.expora_mocoa.entities.Evento;
import com.expora_mocoa.entities.LugarTuristico;
import com.expora_mocoa.entities.Usuario;
import com.expora_mocoa.repositories.EventoRepository;
import com.expora_mocoa.repositories.LugarTuristicoRepository;
import com.expora_mocoa.repositories.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class EventoService {

    private final EventoRepository repository;
    private final LugarTuristicoRepository lugarRepository;
    private final UsuarioRepository usuarioRepository;

    public EventoService(EventoRepository repository,
                         LugarTuristicoRepository lugarRepository,
                         UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.lugarRepository = lugarRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Evento> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Evento findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Evento no encontrado con id " + id));
    }

    public Evento create(Evento evento) {
        if (evento.getNombre() == null || evento.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del evento es obligatorio");
        }
        if (evento.getUsuario() == null || evento.getUsuario().getId() == null) {
            throw new IllegalArgumentException("El usuario organizador es obligatorio");
        }
        Usuario usuario = usuarioRepository.findById(evento.getUsuario().getId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        evento.setUsuario(usuario);
        // El lugar es opcional (ON DELETE SET NULL)
        if (evento.getLugarTuristico() != null && evento.getLugarTuristico().getId() != null) {
            LugarTuristico lugar = lugarRepository.findById(evento.getLugarTuristico().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Lugar turístico no encontrado"));
            evento.setLugarTuristico(lugar);
        } else {
            evento.setLugarTuristico(null);
        }
        return repository.save(evento);
    }

    public Evento update(Long id, Evento datos) {
        Evento evento = findById(id);
        if (datos.getNombre() != null && !datos.getNombre().isBlank()) {
            evento.setNombre(datos.getNombre().trim());
        }
        if (datos.getDescripcion() != null) {
            evento.setDescripcion(datos.getDescripcion());
        }
        if (datos.getFecha() != null) {
            evento.setFecha(datos.getFecha());
        }
        if (datos.getCategoria() != null) {
            evento.setCategoria(datos.getCategoria());
        }
        if (datos.getLugarTuristico() != null) {
            if (datos.getLugarTuristico().getId() == null) {
                evento.setLugarTuristico(null);
            } else {
                LugarTuristico lugar = lugarRepository.findById(datos.getLugarTuristico().getId())
                        .orElseThrow(() -> new EntityNotFoundException("Lugar turístico no encontrado"));
                evento.setLugarTuristico(lugar);
            }
        }
        if (datos.getUsuario() != null && datos.getUsuario().getId() != null) {
            Usuario usuario = usuarioRepository.findById(datos.getUsuario().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
            evento.setUsuario(usuario);
        }
        return repository.save(evento);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }
}
