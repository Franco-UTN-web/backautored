package com.alberto.backend;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID; // <-- Asegúrate de importar UUID

@RestController
@RequestMapping("/api/perfiles")
@CrossOrigin(origins = "*")
public class PerfilController {

    @Autowired
    private PerfilRepository perfilRepository;

    @PutMapping("/actualizar/{id}")
    // Cambiamos el tipo de dato de 'String' a 'UUID'
    public ResponseEntity<Perfil> actualizarPerfil(@PathVariable UUID id, @RequestBody Perfil perfilActualizado) {
        Optional<Perfil> perfilExistente = perfilRepository.findById(id);

        if (perfilExistente.isPresent()) {
            Perfil perfil = perfilExistente.get();

            // Actualizamos solo los campos que vengan en la petición
            if (perfilActualizado.getNombre() != null) {
                perfil.setNombre(perfilActualizado.getNombre());
            }
            if (perfilActualizado.getTelefono() != null) {
                perfil.setTelefono(perfilActualizado.getTelefono());
            }
            if (perfilActualizado.getAvatar_url() != null) {
                perfil.setAvatar_url(perfilActualizado.getAvatar_url());
            }
            if (perfilActualizado.getApellido() != null) {
                perfil.setApellido(perfilActualizado.getApellido());
            }
            if (perfilActualizado.getFecha_nacimiento() != null) {
                perfil.setFecha_nacimiento(perfilActualizado.getFecha_nacimiento());
            }


            Perfil perfilGuardado = perfilRepository.save(perfil);
            return ResponseEntity.ok(perfilGuardado);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}