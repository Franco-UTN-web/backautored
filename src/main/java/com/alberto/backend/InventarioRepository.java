package com.alberto.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface InventarioRepository extends JpaRepository<Inventario, UUID> {

    // Le decimos a Java que busque todos los inventarios que coincidan con el ID de la tienda
    @Query("SELECT i FROM Inventario i WHERE i.establecimiento_id = :tiendaId")
    List<Inventario> buscarPorTienda(@Param("tiendaId") UUID tiendaId);

    @Query("SELECT i FROM Inventario i, Establecimiento e WHERE i.establecimiento_id = e.id AND e.estado = 'APROBADO'")
    List<Inventario> findInventarioTiendasAprobadas();
}
