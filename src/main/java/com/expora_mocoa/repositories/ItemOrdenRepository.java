package com.expora_mocoa.repositories;

import com.expora_mocoa.entities.ItemOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ItemOrdenRepository extends JpaRepository<ItemOrden, Long> {
    List<ItemOrden> findByOrdenId(Long ordenId);
    List<ItemOrden> findByComidaId(Long comidaId);
}
