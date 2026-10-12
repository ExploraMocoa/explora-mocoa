package com.expora_mocoa.repositories;

import com.expora_mocoa.entities.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Long> {
    List<Resena> findByLugarTuristicoId(Long lugarTuristicoId);
    List<Resena> findByUsuarioId(Long usuarioId);
}
