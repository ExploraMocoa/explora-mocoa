package com.expora_mocoa.repositories;

import com.expora_mocoa.entities.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Long> {
    List<Evento> findByLugarTuristicoId(Long lugarTuristicoId);
    List<Evento> findByUsuarioId(Long usuarioId);
}
