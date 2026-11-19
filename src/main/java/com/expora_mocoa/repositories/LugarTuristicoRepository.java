package com.expora_mocoa.repositories;

import com.expora_mocoa.entities.LugarTuristico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LugarTuristicoRepository extends JpaRepository<LugarTuristico, Long> {
    List<LugarTuristico> findByCategoriaId(Long categoriaId);
    List<LugarTuristico> findByUsuarioId(Long usuarioId);
    List<LugarTuristico> findByNombreContainingIgnoreCase(String nombre);
}
