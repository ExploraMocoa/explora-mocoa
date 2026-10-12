package com.expora_mocoa.controllers;

import com.expora_mocoa.entities.Resena;
import com.expora_mocoa.services.ResenaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/resenas")
@CrossOrigin(origins = "*")
public class ResenaController {

    private final ResenaService service;

    public ResenaController(ResenaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Resena> listar(
            @RequestParam(required = false) Long lugarId,
            @RequestParam(required = false) Long usuarioId) {
        if (lugarId != null) {
            return service.findByLugar(lugarId);
        }
        if (usuarioId != null) {
            return service.findByUsuario(usuarioId);
        }
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Resena obtener(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<Resena> crear(@Valid @RequestBody Resena resena) {
        return new ResponseEntity<>(service.create(resena), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public Resena actualizar(@PathVariable Long id, @RequestBody Resena resena) {
        return service.update(id, resena);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
