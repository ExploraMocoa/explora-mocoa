package com.expora_mocoa.controllers;

import com.expora_mocoa.entities.ItemOrden;
import com.expora_mocoa.services.ItemOrdenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/items-orden")
@CrossOrigin(origins = "*")
public class ItemOrdenController {

    private final ItemOrdenService service;

    public ItemOrdenController(ItemOrdenService service) {
        this.service = service;
    }

    @GetMapping
    public List<ItemOrden> listar(@RequestParam(required = false) Long ordenId) {
        if (ordenId != null) {
            return service.findByOrden(ordenId);
        }
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ItemOrden obtener(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<ItemOrden> crear(@Valid @RequestBody ItemOrden item) {
        return new ResponseEntity<>(service.create(item), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ItemOrden actualizar(@PathVariable Long id, @RequestBody ItemOrden item) {
        return service.update(id, item);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
