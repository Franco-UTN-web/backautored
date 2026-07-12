package com.alberto.backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/archivos")
@CrossOrigin(origins = "*")
public class UploadController {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @PostMapping("/subir")
    public ResponseEntity<Map<String, String>> subirArchivo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "carpeta", defaultValue = "refaccionarias") String carpeta) { // <-- CAPTURA LA CARPETA

        Map<String, String> response = new HashMap<>();

        try {
            String nombreOriginal = file.getOriginalFilename();
            String extension = (nombreOriginal != null && nombreOriginal.contains("."))
                    ? nombreOriginal.substring(nombreOriginal.lastIndexOf("."))
                    : ".jpg";
            String nombreUnico = System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 5) + extension;

            // Usamos la carpeta dinámica
            String urlSubida = supabaseUrl + "/storage/v1/object/" + carpeta + "/" + nombreUnico;

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(supabaseKey);

            String tipoMime = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
            headers.setContentType(MediaType.parseMediaType(tipoMime));

            HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);
            ResponseEntity<String> respuestaSupabase = restTemplate.exchange(urlSubida, HttpMethod.POST, requestEntity, String.class);

            if (respuestaSupabase.getStatusCode().is2xxSuccessful()) {
                String urlPublica = supabaseUrl + "/storage/v1/object/public/" + carpeta + "/" + nombreUnico;
                response.put("url", urlPublica);
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Supabase rechazó el archivo.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Falla al subir: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}