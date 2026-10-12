package com.expora_mocoa.repositories;

import com.expora_mocoa.entities.ItemOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItemOrdenRepository extends JpaRepository<ItemOrden, Long> {
    List<ItemOrden> findByOrdenId(Long ordenId);
    List<ItemOrden> findByComidaId(Long comidaId);
}
