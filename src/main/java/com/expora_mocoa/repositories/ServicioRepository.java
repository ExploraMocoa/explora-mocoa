package com.expora_mocoa.repositories;

import com.expora_mocoa.entities.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    List<Servicio> findByLugarTuristicoId(Long lugarTuristicoId);
}
