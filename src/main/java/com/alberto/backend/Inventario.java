package com.alberto.backend;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "inventarios")
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID establecimiento_id;

    @ManyToOne
    @JoinColumn(name = "pieza_id")
    private Pieza pieza;

    private Integer stock;
    private BigDecimal precio_lista;
    private BigDecimal precio_oferta;

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getEstablecimiento_id() { return establecimiento_id; }
    public void setEstablecimiento_id(UUID establecimiento_id) { this.establecimiento_id = establecimiento_id; }
    public Pieza getPieza() { return pieza; }
    public void setPieza(Pieza pieza) { this.pieza = pieza; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public BigDecimal getPrecio_lista() { return precio_lista; }
    public void setPrecio_lista(BigDecimal precio_lista) { this.precio_lista = precio_lista; }
    public BigDecimal getPrecio_oferta() { return precio_oferta; }
    public void setPrecio_oferta(BigDecimal precio_oferta) { this.precio_oferta = precio_oferta; }
}