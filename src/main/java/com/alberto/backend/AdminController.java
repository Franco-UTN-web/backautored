package com.alberto.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173", maxAge = 3600)
public class AdminController {

    @Autowired
    private EstablecimientoRepository establecimientoRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @GetMapping("/metricas")
    public Map<String, Object> obtenerMetricasDashboard() {
        Map<String, Object> metricas = new HashMap<>();

        int aprobadas = establecimientoRepository.findByEstado("APROBADO").size();
        int pendientes = establecimientoRepository.findByEstado("PENDIENTE").size();
        int suspendidas = establecimientoRepository.findByEstado("SUSPENDIDO").size();

        int total = aprobadas + pendientes + suspendidas;

        metricas.put("tiendasAprobadas", aprobadas);
        metricas.put("tiendasPendientes", pendientes);
        metricas.put("tiendasSuspendidas", suspendidas);
        metricas.put("totalTiendas", total);

        return metricas;
    }

    @GetMapping("/compradores")
    public List<Perfil> obtenerCompradores() {
        return perfilRepository.findByRol("comprador");
    }

    // 🔥 EL ENDPOINT QUE EVITA EL ERROR DE REACT 🔥
    @GetMapping("/usuarios")
    public List<Perfil> obtenerTodosLosUsuarios() {
        return perfilRepository.findAll();
    }

    @PutMapping("/compradores/{id}/suspender")
    public ResponseEntity<?> suspenderComprador(@PathVariable("id") UUID id) {
        Optional<Perfil> perfilOpt = perfilRepository.findById(id);

        if (perfilOpt.isPresent()) {
            Perfil perfil = perfilOpt.get();
            perfil.setActivo(!perfil.isActivo());
            perfilRepository.save(perfil);
            return ResponseEntity.ok(perfil);
        }
        return ResponseEntity.notFound().build();
    }
}