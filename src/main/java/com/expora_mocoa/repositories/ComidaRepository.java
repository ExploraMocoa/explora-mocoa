package com.expora_mocoa.repositories;

import com.expora_mocoa.entities.Comida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComidaRepository extends JpaRepository<Comida, Long> {
    List<Comida> findByTipo(String tipo);
    List<Comida> findByNombreContainingIgnoreCase(String nombre);
}
