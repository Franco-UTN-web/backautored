package com.alberto.backend;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "piezas")
public class Pieza {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String marca;
    private String modelo;
    private String nombre_comercial;
    private String descripcion;
    private boolean activo = true;
    private String fabricante_original;
    private String nivel_calidad;
    private String tiempo_duracion;
    private String categoria;
    private String tipo_vehiculo;
    private String marca_vehiculo;
    private String anio_vehiculo;
    private String imagen_url;

    @JsonProperty("numero_parte") // Esto le dice a Java: "En el JSON de React búscalo con guion bajo"
    @Column(name = "numero_parte") // Esto le dice a Java: "En Supabase guárdalo con guion bajo"
    private String numeroParte;

    private String condicion;

    private String garantia;

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getNombre_comercial() { return nombre_comercial; }

    public void setNombre_comercial(String nombre_comercial) { this.nombre_comercial = nombre_comercial; }
    public String getDescripcion() { return descripcion; }

    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public boolean isActivo() { return activo; }

    public void setActivo(boolean activo) { this.activo = activo; }
    public String getFabricante_original() { return fabricante_original; }

    public void setFabricante_original(String fabricante_original) { this.fabricante_original = fabricante_original; }
    public String getNivel_calidad() { return nivel_calidad; }

    public void setNivel_calidad(String nivel_calidad) { this.nivel_calidad = nivel_calidad; }
    public String getTiempo_duracion() { return tiempo_duracion; }
    public void setTiempo_duracion(String tiempo_duracion) { this.tiempo_duracion = tiempo_duracion; }
    public String getCategoria() { return categoria; }

    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getTipo_vehiculo() { return tipo_vehiculo; }
    public void setTipo_vehiculo(String tipo_vehiculo) { this.tipo_vehiculo = tipo_vehiculo; }

    public String getMarca_vehiculo() { return marca_vehiculo; }
    public void setMarca_vehiculo(String marca_vehiculo) { this.marca_vehiculo = marca_vehiculo; }

    public String getAnio_vehiculo() { return anio_vehiculo; }
    public void setAnio_vehiculo(String anio_vehiculo) { this.anio_vehiculo = anio_vehiculo; }

    public String getImagen_url() { return imagen_url; }
    public void setImagen_url(String imagen_url) { this.imagen_url = imagen_url; }

    public String getNumeroParte() {
        return numeroParte;
    }

    public void setNumeroParte(String numeroParte) {
        this.numeroParte = numeroParte;
    }

    public String getCondicion() {
        return condicion;
    }

    public void setCondicion(String condicion) {
        this.condicion = condicion;
    }

    public String getGarantia() {
        return garantia;
    }

    public void setGarantia(String garantia) {
        this.garantia = garantia;
    }
}