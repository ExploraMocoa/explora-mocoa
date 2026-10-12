package com.expora_mocoa.controllers;

import com.expora_mocoa.entities.LugarTuristico;
import com.expora_mocoa.services.LugarTuristicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/lugares")
@CrossOrigin(origins = "*")
public class LugarTuristicoController {

    private final LugarTuristicoService service;

    public LugarTuristicoController(LugarTuristicoService service) {
        this.service = service;
    }

    @GetMapping
    public List<LugarTuristico> listar(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long usuarioId) {
        if (categoriaId != null) {
            return service.findByCategoria(categoriaId);
        }
        if (usuarioId != null) {
            return service.findByUsuario(usuarioId);
        }
        return service.findAll();
    }

    @GetMapping("/{id}")
    public LugarTuristico obtener(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<LugarTuristico> crear(@Valid @RequestBody LugarTuristico lugar) {
        return new ResponseEntity<>(service.create(lugar), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public LugarTuristico actualizar(@PathVariable Long id, @RequestBody LugarTuristico lugar) {
        return service.update(id, lugar);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
