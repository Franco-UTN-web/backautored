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
    private String rfc;
    private String estado;
    private String telefono;

    // Documentos
    private String doc_csf_url;
    private String doc_ine_url;

    // Dirección separada
    private String calle;
    private String numero;
    private String colonia;

    @Column(name = "codigo_postal")
    private String codigoPostal;

    private String ciudad;

    @Column(name = "estado_rep")
    private String estadoRep;

    // ==========================================
    //           GETTERS Y SETTERS
    // ==========================================

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPropietario_id() { return propietario_id; }
    public void setPropietario_id(UUID propietario_id) { this.propietario_id = propietario_id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRfc() { return rfc; }
    public void setRfc(String rfc) { this.rfc = rfc; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDoc_csf_url() { return doc_csf_url; }
    public void setDoc_csf_url(String doc_csf_url) { this.doc_csf_url = doc_csf_url; }

    public String getDoc_ine_url() { return doc_ine_url; }
    public void setDoc_ine_url(String doc_ine_url) { this.doc_ine_url = doc_ine_url; }

    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getColonia() { return colonia; }
    public void setColonia(String colonia) { this.colonia = colonia; }

    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getEstadoRep() { return estadoRep; }
    public void setEstadoRep(String estadoRep) { this.estadoRep = estadoRep; }
}