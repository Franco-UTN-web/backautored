package com.alberto.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;
import java.util.List;

public interface PiezaRepository extends JpaRepository<Pieza, UUID> {
    List<Pieza> findByActivoTrue();
    @Query("SELECT p FROM Pieza p WHERE LOWER(p.nombre_comercial) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Pieza> buscarPorNombre(@Param("nombre") String nombre);;
    @Query("SELECT p FROM Pieza p WHERE LOWER(p.nombre_comercial) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.numeroParte) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Pieza> buscarPorNombreOPartNumber(@Param("keyword") String keyword);

}