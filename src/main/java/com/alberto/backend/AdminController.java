package com.alberto.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}, maxAge = 3600)
public class AdminController {


    @Autowired
    private EstablecimientoRepository establecimientoRepository;

    // 1. Inyectamos el repositorio de Perfiles
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

    // 2. NUEVO ENDPOINT: Listar todos los usuarios compradores
    @GetMapping("/compradores")
    public List<Perfil> obtenerCompradores() {
        // Busca en la base de datos a todos los que tengan el rol exacto de "comprador"
        return perfilRepository.findByRol("comprador");
    }

    @PutMapping("/compradores/{id}/suspender")
    public ResponseEntity<?> suspenderComprador(@PathVariable("id") UUID id) {
        Optional<Perfil> perfilOpt = perfilRepository.findById(id);

        if (perfilOpt.isPresent()) {
            Perfil perfil = perfilOpt.get();
            // Invertimos el estado (si era true pasa a false, y viceversa)
            perfil.setActivo(!perfil.isActivo());
            perfilRepository.save(perfil);
            return ResponseEntity.ok(perfil);
        }

        return ResponseEntity.notFound().build();
    }
}