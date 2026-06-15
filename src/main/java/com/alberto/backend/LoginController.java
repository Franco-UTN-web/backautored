package com.alberto.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class LoginController {

    @Autowired
    private PerfilRepository perfilRepository;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> credentials) {
        // Tomamos lo que el usuario escribió en el campo "email" de React
        String nombreBuscado = credentials.get("email");

        Map<String, String> response = new HashMap<>();

        // Buscamos directamente en Supabase
        Optional<Perfil> perfilOpt = perfilRepository.findByNombre(nombreBuscado);

        if (perfilOpt.isPresent()) {
            Perfil perfil = perfilOpt.get();
            response.put("role", perfil.getRol());
            response.put("redirectUrl", "/dashboard/" + perfil.getRol());
            return ResponseEntity.ok(response);
        }

        // Si no existe en la base de datos
        response.put("error", "Usuario no encontrado en la red AutoRed");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}