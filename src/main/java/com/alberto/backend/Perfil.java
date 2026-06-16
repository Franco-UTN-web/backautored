package com.alberto.backend;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "perfiles")
public class Perfil {

    @Id

    private UUID id;

    private String nombre;
    private String avatar_url;
    private String rol;

    // Tus Getters y Setters se quedan exactamente igual, no los toques
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getAvatar_url() { return avatar_url; }
    public void setAvatar_url(String avatar_url) { this.avatar_url = avatar_url; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}