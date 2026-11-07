package com.alberto.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger; // Importar Logger
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/piezas")
@CrossOrigin(origins = "*")
public class PiezaController {
    private static final Logger logger = LoggerFactory.getLogger(PiezaController.class);

    @Autowired
    private PiezaRepository piezaRepository;

    // Obtener todo el catálogo global para el Admin
    @GetMapping("/todas")
    public List<Pieza> obtenerTodasLasPiezas() {
        // Ahora solo devuelve las que están vivas
        return piezaRepository.findByActivoTrue();
    }

    // Endpoint destructivo de "Poder Absoluto" para eliminar cualquier pieza
    @DeleteMapping("/admin/eliminar/{id}")
    public ResponseEntity<String> eliminarPiezaPorAdmin(@PathVariable("id") UUID id) {
        Pieza pieza = piezaRepository.findById(id).orElse(null);

        if (pieza != null) {
            // En lugar de destruirla (piezaRepository.deleteById), la desactivamos
            pieza.setActivo(false);
            piezaRepository.save(pieza);
            logger.warn("ACCION_ADMIN: Pieza desactivada del catálogo. ID Pieza: {}", id);
            return ResponseEntity.ok("Pieza desactivada correctamente. Ya no será visible para los clientes.");
        }

        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/reactivar")
    public ResponseEntity<String> reactivarPieza(@PathVariable UUID id) {
        Pieza pieza = piezaRepository.findById(id).orElseThrow();
        pieza.setActivo(true);
        piezaRepository.save(pieza);
        return ResponseEntity.ok("Pieza reactivada");
    }

    @PutMapping("/{id}/suspender")
    public ResponseEntity<String> suspenderPieza(@PathVariable UUID id) {
        Pieza pieza = piezaRepository.findById(id).orElseThrow();
        pieza.setActivo(false);
        piezaRepository.save(pieza);
        return ResponseEntity.ok("Pieza suspendida");
    }
}