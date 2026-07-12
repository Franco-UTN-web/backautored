package com.alberto.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/api/establecimientos")
@CrossOrigin(origins = "*")
public class EstablecimientoController {

    private static final Logger logger = LoggerFactory.getLogger(EstablecimientoController.class);
    @Autowired
    private EstablecimientoRepository establecimientoRepository;

    // Inyectamos el PerfilRepository para poder ascender al usuario
    @Autowired
    private PerfilRepository perfilRepository;

    @GetMapping("/todos")
    public List<Establecimiento> obtenerTodos() {
        return establecimientoRepository.findAll();
    }

    // --- NUEVAS RUTAS PARA EL PANEL ADMIN ---

    @GetMapping("/pendientes")
    public List<Establecimiento> obtenerPendientes() {
        return establecimientoRepository.findByEstado("PENDIENTE");
    }

    @PutMapping("/aprobar/{id}")
    public Establecimiento aprobarEstablecimiento(@PathVariable("id") UUID id) {
        Establecimiento tienda = establecimientoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tienda no encontrada"));

        tienda.setEstado("APROBADO");

        if (tienda.getPropietario_id() != null) {
            Perfil dueño = perfilRepository.findById(tienda.getPropietario_id()).orElse(null);
            if (dueño != null) {
                dueño.setRol("tienda");
                perfilRepository.save(dueño);
            }
        }

        return establecimientoRepository.save(tienda);
    }
    @PutMapping("/rechazar/{id}")
    public Establecimiento rechazarEstablecimiento(@PathVariable("id") UUID id) {
        Establecimiento tienda = establecimientoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tienda no encontrada"));

        tienda.setEstado("RECHAZADO");
        return establecimientoRepository.save(tienda);
    }
    // --- RUTA PARA QUE EL USUARIO MANDE SU SOLICITUD ---
    @PostMapping("/solicitar")
    public Establecimiento solicitarRegistro(@RequestBody Establecimiento nuevaTienda) {

        // ¡LA PIEZA FALTANTE! Le generamos un ID único a la nueva tienda
        nuevaTienda.setId(UUID.randomUUID());

        // Obligamos a que nazca como pendiente
        nuevaTienda.setEstado("PENDIENTE");
        logger.info("ACCION_CREACION: Nueva solicitud de registro recibida. Empresa: {}, RFC: {}", nuevaTienda.getNombre(), nuevaTienda.getRfc());

        return establecimientoRepository.save(nuevaTienda);
    }

    @PutMapping("/suspender/{id}")
    public Establecimiento suspenderEstablecimiento(@PathVariable("id") UUID id) {
        Establecimiento tienda = establecimientoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tienda no encontrada"));

        tienda.setEstado("SUSPENDIDO");

        // LÓGICA DE ROLES: Le quitamos el poder de tienda y lo regresamos a comprador
        if (tienda.getPropietario_id() != null) {
            Perfil dueño = perfilRepository.findById(tienda.getPropietario_id()).orElse(null);
            if (dueño != null) {
                dueño.setRol("comprador");
                perfilRepository.save(dueño);
            }
        }
        logger.info("ACCION_ADMIN: Tienda suspendida. ID Tienda: {}, Estado nuevo: SUSPENDIDO", id);
        return establecimientoRepository.save(tienda);
    }

    @PutMapping("/reactivar/{id}")
    public Establecimiento reactivarEstablecimiento(@PathVariable("id") UUID id) {
        Establecimiento tienda = establecimientoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tienda no encontrada"));

        tienda.setEstado("APROBADO");

        // LÓGICA DE ROLES: Le devolvemos su acceso al dashboard de tienda
        if (tienda.getPropietario_id() != null) {
            Perfil dueño = perfilRepository.findById(tienda.getPropietario_id()).orElse(null);
            if (dueño != null) {
                dueño.setRol("tienda");
                perfilRepository.save(dueño);
            }
        }

        return establecimientoRepository.save(tienda);
    }


}