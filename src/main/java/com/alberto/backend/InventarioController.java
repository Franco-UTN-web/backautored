package com.alberto.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/inventario")
@CrossOrigin(origins = "*")
public class InventarioController {

    @Autowired
    private PiezaRepository piezaRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @PostMapping("/agregar")
    public ResponseEntity<String> agregarStock(@RequestBody Map<String, Object> payload) {
        try {
            // 1. Extraer y guardar los datos de la Pieza global
            Pieza nuevaPieza = new Pieza();
            nuevaPieza.setMarca((String) payload.get("marca"));
            nuevaPieza.setModelo((String) payload.get("modelo"));
            nuevaPieza.setNombre_comercial((String) payload.get("nombre_comercial"));
            nuevaPieza.setDescripcion((String) payload.get("descripcion"));
            nuevaPieza.setFabricante_original((String) payload.get("fabricante_original"));
            nuevaPieza.setNivel_calidad((String) payload.get("nivel_calidad"));
            nuevaPieza.setTiempo_duracion((String) payload.get("tiempo_duracion"));
            nuevaPieza.setCategoria((String) payload.get("categoria"));
            nuevaPieza.setTipo_vehiculo((String) payload.get("tipo_vehiculo"));
            nuevaPieza.setMarca_vehiculo((String) payload.get("marca_vehiculo"));
            nuevaPieza.setAnio_vehiculo((String) payload.get("anio_vehiculo"));
            nuevaPieza.setImagen_url((String) payload.get("imagen_url"));


            nuevaPieza.setNumeroParte((String) payload.get("numero_parte"));
            nuevaPieza.setCondicion((String) payload.get("condicion"));
            nuevaPieza.setGarantia((String) payload.get("garantia"));

            Pieza piezaGuardada = piezaRepository.save(nuevaPieza);

            // 2. Vincular la pieza creada con el inventario físico de la tienda
            Inventario nuevoInventario = new Inventario();
            nuevoInventario.setPieza(piezaGuardada);
            nuevoInventario.setStock(Integer.parseInt(payload.get("stock").toString()));
            nuevoInventario.setPrecio_lista(new BigDecimal(payload.get("precio_lista").toString()));
            nuevoInventario.setPrecio_oferta(new BigDecimal(payload.get("precio_oferta").toString()));

            // Usamos el UUID del establecimiento que generamos en el Paso 1
            UUID estId = UUID.fromString((String) payload.get("establecimiento_id"));
            nuevoInventario.setEstablecimiento_id(estId);

            inventarioRepository.save(nuevoInventario);

            return ResponseEntity.ok("Pieza registrada exitosamente en el inventario de la sucursal.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al procesar el registro: " + e.getMessage());
        }
    }

    @GetMapping("/tienda/{tiendaId}")
    public ResponseEntity<List<Inventario>> obtenerInventarioTienda(@PathVariable("tiendaId") UUID tiendaId) {
        try {
            List<Inventario> inventario = inventarioRepository.buscarPorTienda(tiendaId);
            return ResponseEntity.ok(inventario);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<String> actualizarStock(@PathVariable("id") UUID id, @RequestBody Map<String, Object> payload) {
        try {
            // 1. Buscamos el registro en el inventario por su ID
            Optional<Inventario> inventarioOpt = inventarioRepository.findById(id);
            if (inventarioOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("No se encontró el producto en el inventario.");
            }

            Inventario inventario = inventarioOpt.get();

            // 2. Actualizamos los datos del inventario (Stock y Precios)
            inventario.setStock(Integer.parseInt(payload.get("stock").toString()));
            inventario.setPrecio_lista(new BigDecimal(payload.get("precio_lista").toString()));
            inventario.setPrecio_oferta(new BigDecimal(payload.get("precio_oferta").toString()));

            // 3. Actualizamos los datos de la Pieza asociada
            Pieza pieza = inventario.getPieza();
            pieza.setMarca((String) payload.get("marca"));
            pieza.setModelo((String) payload.get("modelo"));
            pieza.setNombre_comercial((String) payload.get("nombre_comercial"));
            pieza.setNivel_calidad((String) payload.get("nivel_calidad"));
            pieza.setCategoria((String) payload.get("categoria"));
            pieza.setTipo_vehiculo((String) payload.get("tipo_vehiculo"));
            pieza.setMarca_vehiculo((String) payload.get("marca_vehiculo"));
            pieza.setAnio_vehiculo((String) payload.get("anio_vehiculo"));

            // ¡AQUÍ TAMBIÉN! Para que al editar no se borren
            pieza.setNumeroParte((String) payload.get("numero_parte"));
            pieza.setCondicion((String) payload.get("condicion"));
            pieza.setGarantia((String) payload.get("garantia"));

            // Solo actualizamos la imagen si mandaron una nueva
            if (payload.get("imagen_url") != null && !payload.get("imagen_url").toString().isEmpty()) {
                pieza.setImagen_url((String) payload.get("imagen_url"));
            }

            // Guardamos los cambios
            piezaRepository.save(pieza);
            inventarioRepository.save(inventario);

            return ResponseEntity.ok("Producto actualizado exitosamente.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar: " + e.getMessage());
        }
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> eliminarStock(@PathVariable("id") UUID id) {
        try {
            // 1. Buscamos si existe el inventario
            Optional<Inventario> inventarioOpt = inventarioRepository.findById(id);
            if (inventarioOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("No se encontró el producto para eliminar.");
            }

            Inventario inventario = inventarioOpt.get();
            Pieza pieza = inventario.getPieza();

            // 2. Orden de eliminación crítico (por la llave foránea de la base de datos)
            // Primero borramos el registro del inventario de la tienda
            inventarioRepository.delete(inventario);

            // Después borramos la pieza general del catálogo
            piezaRepository.delete(pieza);

            return ResponseEntity.ok("Producto eliminado correctamente.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al eliminar: " + e.getMessage());
        }
    }

    @GetMapping("/todos")
    public ResponseEntity<List<Inventario>> obtenerCatalogoGlobal() {
        try {
            // 🔥 En lugar de findAll(), usamos nuestra nueva consulta estricta 🔥
            List<Inventario> catalogoActivo = inventarioRepository.findInventarioTiendasAprobadas();

            return ResponseEntity.ok(catalogoActivo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 📥 1. DESCARGAR PLANTILLA CSV
    // ==========================================
    @GetMapping("/plantilla-csv")
    public ResponseEntity<byte[]> descargarPlantilla() {
        // Estas son las columnas exactas que tu sistema necesita
        String cabeceras = "nombre_comercial,marca,modelo,categoria,numero_parte,condicion,stock,precio_lista,precio_oferta\n";
        byte[] bytes = cabeceras.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=plantilla_inventario.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    // ==========================================
    // 📤 2. SUBIR Y LEER EL CSV
    // ==========================================
    @PostMapping("/upload-csv")
    public ResponseEntity<String> subirCSV(
            @RequestParam("file") MultipartFile file,
            @RequestParam("establecimiento_id") UUID establecimientoId) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("El archivo está vacío.");
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String linea;
            boolean esPrimeraLinea = true;
            int contadorGuardados = 0;

            while ((linea = br.readLine()) != null) {
                // Saltamos la fila 1 porque son los encabezados
                if (esPrimeraLinea) {
                    esPrimeraLinea = false;
                    continue;
                }

                // Separamos los datos por coma
                String[] datos = linea.split(",");

                // Si la línea está vacía o incompleta, la saltamos
                if (datos.length < 9) continue;

                // 1. Creamos la pieza
                Pieza nuevaPieza = new Pieza();
                nuevaPieza.setNombre_comercial(datos[0].trim());
                nuevaPieza.setMarca(datos[1].trim());
                nuevaPieza.setModelo(datos[2].trim());
                nuevaPieza.setCategoria(datos[3].trim());
                nuevaPieza.setNumeroParte(datos[4].trim());
                nuevaPieza.setCondicion(datos[5].trim());
                nuevaPieza.setActivo(true);

                Pieza piezaGuardada = piezaRepository.save(nuevaPieza);

                // 2. Creamos el inventario asociado a la sucursal
                Inventario nuevoInventario = new Inventario();
                nuevoInventario.setPieza(piezaGuardada);
                nuevoInventario.setEstablecimiento_id(establecimientoId);
                nuevoInventario.setStock(Integer.parseInt(datos[6].trim()));
                nuevoInventario.setPrecio_lista(new BigDecimal(datos[7].trim()));
                nuevoInventario.setPrecio_oferta(new BigDecimal(datos[8].trim()));

                inventarioRepository.save(nuevoInventario);
                contadorGuardados++;
            }

            return ResponseEntity.ok("Éxito. Se agregaron " + contadorGuardados + " productos al inventario.");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al leer el CSV. Revisa el formato: " + e.getMessage());
        }
    }
}