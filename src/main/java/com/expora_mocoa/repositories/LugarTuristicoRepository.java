package com.expora_mocoa.repositories;

import com.expora_mocoa.entities.LugarTuristico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LugarTuristicoRepository extends JpaRepository<LugarTuristico, Long> {
    List<LugarTuristico> findByCategoriaId(Long categoriaId);
    List<LugarTuristico> findByUsuarioId(Long usuarioId);
    List<LugarTuristico> findByNombreContainingIgnoreCase(String nombre);
}
