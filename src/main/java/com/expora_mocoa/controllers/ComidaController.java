package com.expora_mocoa.controllers;

import com.expora_mocoa.entities.Comida;
import com.expora_mocoa.services.ComidaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/comidas")
@CrossOrigin(origins = "*")
public class ComidaController {

    private final ComidaService service;

    public ComidaController(ComidaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Comida> listar() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Comida obtener(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<Comida> crear(@Valid @RequestBody Comida comida) {
        return new ResponseEntity<>(service.create(comida), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public Comida actualizar(@PathVariable Long id, @RequestBody Comida comida) {
        return service.update(id, comida);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
