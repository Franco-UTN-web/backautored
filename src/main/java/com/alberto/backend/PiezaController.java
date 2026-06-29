package com.alberto.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/piezas")
@CrossOrigin(origins = "*")
public class PiezaController {

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

            return ResponseEntity.ok("Pieza desactivada correctamente. Ya no será visible para los clientes.");
        }

        return ResponseEntity.notFound().build();
    }
}