package com.alberto.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface PiezaRepository extends JpaRepository<Pieza, UUID> {
    List<Pieza> findByActivoTrue();
}