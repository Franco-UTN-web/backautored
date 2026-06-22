package com.alberto.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface EstablecimientoRepository extends JpaRepository<Establecimiento, UUID> {
    // Con solo escribir esto, Spring Boot ya sabe que tiene que hacer
    // un "SELECT * WHERE estado = ?"
    List<Establecimiento> findByEstado(String estado);
}