package com.alberto.backend;

import jakarta.persistence.*;
import java.util.UUID;
import java.sql.Date; // IMPORTANTE para la fecha de nacimiento

@Entity
@Table(name = "perfiles")
public class Perfil {

    @Id
    private UUID id;

    private String nombre;
    private String apellido;
    private String telefono;
    private Date fecha_nacimiento;
    private String avatar_url;
    private String rol;
    private boolean activo = true;

    // --- GETTERS Y SETTERS ---

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public Date getFecha_nacimiento() { return fecha_nacimiento; }
    public void setFecha_nacimiento(Date fecha_nacimiento) { this.fecha_nacimiento = fecha_nacimiento; }

    public String getAvatar_url() { return avatar_url; }
    public void setAvatar_url(String avatar_url) { this.avatar_url = avatar_url; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}