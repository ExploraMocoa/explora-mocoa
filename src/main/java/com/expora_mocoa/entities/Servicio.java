package com.expora_mocoa.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Entity
@Table(name = "servicios")
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del servicio es obligatorio")
    @Size(max = 255, message = "El nombre no puede superar 255 caracteres")
    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @PositiveOrZero(message = "El precio no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El precio debe tener máximo 8 enteros y 2 decimales")
    @Column(name = "precio", precision = 10, scale = 2)
    private BigDecimal precio = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lugar_turistico_id", nullable = false)
    @JsonIgnoreProperties({"servicios", "resenas", "eventos", "hibernateLazyInitializer", "handler"})
    private LugarTuristico lugarTuristico;

    public Servicio() {
    }

    @PrePersist
    public void prePersist() {
        if (precio == null) {
            precio = BigDecimal.ZERO;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public LugarTuristico getLugarTuristico() {
        return lugarTuristico;
    }

    public void setLugarTuristico(LugarTuristico lugarTuristico) {
        this.lugarTuristico = lugarTuristico;
    }
}
