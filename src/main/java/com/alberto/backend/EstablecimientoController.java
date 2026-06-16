package com.alberto.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/establecimientos")
@CrossOrigin(origins = "*") // Importante para que React no marque error de CORS
public class EstablecimientoController {

    @Autowired
    private EstablecimientoRepository establecimientoRepository;

    @GetMapping("/todos")
    public List<Establecimiento> obtenerTodos() {
        return establecimientoRepository.findAll();
    }
}