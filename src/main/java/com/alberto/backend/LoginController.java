package com.alberto.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Asegúrate de permitir tu puerto de Vite
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private PerfilRepository perfilRepository;

    @PostMapping("/login/sync")
    public ResponseEntity<Map<String, String>> sincronizarUsuario(@RequestBody Map<String, String> payload) {
        String idStr = payload.get("id");
        String email = payload.get("email");
        String nombre = payload.get("nombre");

        Map<String, String> response = new HashMap<>();

        try {
            UUID id = UUID.fromString(idStr);
            Optional<Perfil> perfilOpt = perfilRepository.findById(id);

            Perfil perfilFinal;

            if (perfilOpt.isPresent()) {
                // El usuario ya existe, respetamos su rol INTOCABLE de la base de datos
                perfilFinal = perfilOpt.get();
                logger.info("ACCION_LOGIN: Usuario existente sincronizado. Email: {}, Rol Real: {}", email, perfilFinal.getRol());
            } else {
                // Es la primera vez que entra (ej. Google Login nuevo), lo creamos como comprador
                perfilFinal = new Perfil();
                perfilFinal.setId(id);
                perfilFinal.setNombre(nombre != null ? nombre : email);
                perfilFinal.setRol("comprador");
                perfilRepository.save(perfilFinal);
                logger.info("ACCION_LOGIN: Nuevo usuario registrado vía Sync. Email: {}", email);
            }

            // Devolvemos la verdad absoluta al frontend
            response.put("rol", perfilFinal.getRol());
            response.put("nombre", perfilFinal.getNombre());
            // ---> NUEVA LÍNEA: Mandamos el estado de suspensión
            response.put("activo", String.valueOf(perfilFinal.isActivo()));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error al sincronizar usuario: ", e);
            response.put("error", "Error interno al sincronizar el perfil");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}