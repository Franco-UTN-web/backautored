package com.alberto.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private EstablecimientoRepository establecimientoRepository;

    // Si más adelante quieres gráficas de piezas, inyectas el PiezaRepository aquí

    @GetMapping("/metricas")
    public Map<String, Object> obtenerMetricasDashboard() {
        Map<String, Object> metricas = new HashMap<>();

        // Usamos el método que ya tienes en el repositorio para contar por estado
        int aprobadas = establecimientoRepository.findByEstado("APROBADO").size();
        int pendientes = establecimientoRepository.findByEstado("PENDIENTE").size();
        int suspendidas = establecimientoRepository.findByEstado("SUSPENDIDO").size();

        int total = aprobadas + pendientes + suspendidas;

        // Agrupamos la información
        metricas.put("tiendasAprobadas", aprobadas);
        metricas.put("tiendasPendientes", pendientes);
        metricas.put("tiendasSuspendidas", suspendidas);
        metricas.put("totalTiendas", total);

        // Aquí podríamos agregar después: metricas.put("totalPiezas", piezaRepository.count());

        return metricas;
    }
}