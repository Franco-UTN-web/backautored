package com.alberto.backend;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "establecimientos")
public class Establecimiento {

    @Id
    private UUID id;

    private UUID propietario_id;
    private String nombre;

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPropietario_id() { return propietario_id; }
    public void setPropietario_id(UUID propietario_id) { this.propietario_id = propietario_id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}