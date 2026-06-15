package com.alberto.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PiezaRepository extends JpaRepository<Pieza, UUID> {
}