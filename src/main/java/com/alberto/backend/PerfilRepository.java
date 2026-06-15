package com.alberto.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PerfilRepository extends JpaRepository<Perfil, UUID> {
    // Le pedimos a Spring que busque en la columna "nombre"
    Optional<Perfil> findByNombre(String nombre);
}