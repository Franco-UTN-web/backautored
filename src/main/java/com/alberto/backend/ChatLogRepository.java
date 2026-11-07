package com.alberto.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
public interface ChatLogRepository extends JpaRepository<ChatLog, UUID> {
    // Consulta para obtener los últimos 10 registros ordenados por fecha
    List<ChatLog> findTop10ByOrderByFechaDesc();

    @Query("SELECT SUM(c.tokensEstimados) FROM ChatLog c")
    Integer sumarTokensTotales();
}