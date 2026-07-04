package com.alberto.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:5173", maxAge = 3600)
public class ChatController {

    @Autowired
    private PiezaRepository piezaRepository;

    // Java lee la llave secreta desde application.properties mágicamente
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @PostMapping("/mensaje")
    public ResponseEntity<Map<String, String>> procesarMensaje(@RequestBody Map<String, String> request) {
        String mensajeUsuario = request.get("mensaje");
        String rolUsuario = request.getOrDefault("rol", "comprador"); // Por defecto es comprador
        Map<String, String> respuesta = new HashMap<>();

        try {
            // 1. BUSCAR EN BD (Si es necesario)
            String textoLimpio = mensajeUsuario.toLowerCase().replaceAll("[.,¿?¡!]", "");
            String[] palabras = textoLimpio.split(" ");
            List<String> ignorar = Arrays.asList("hola", "oye", "quiero", "busco", "necesito", "puedes");
            String palabraClave = "";
            for (String p : palabras) {
                if (p.length() > 2 && !ignorar.contains(p)) {
                    palabraClave = p; break;
                }
            }

            String contextoDB = "No hay datos de piezas.";
            if (!palabraClave.isEmpty()) {
                // Asegúrate de tener este método en PiezaRepository
                List<Pieza> piezas = piezaRepository.buscarPorNombre(palabraClave);
                if (!piezas.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < Math.min(5, piezas.size()); i++) {
                        Pieza p = piezas.get(i);
                        if (p.isActivo()) {
                            sb.append("Pieza: ").append(p.getNombre_comercial())
                                    .append(" | Marca: ").append(p.getMarca())
                                    .append(" | Detalles: ").append(p.getDescripcion() != null ? p.getDescripcion() : "N/A").append("; ");
                        }
                    }
                    if (sb.length() > 0) contextoDB = sb.toString();
                }
            }

            // 2. DEFINIR LA PERSONALIDAD SEGÚN EL ROL
            String instruccionesRol = "";
            switch(rolUsuario) {
                case "admin":
                    instruccionesRol = "Eres el Asistente del Administrador de AutoRed. Tu objetivo es ayudar a moderar usuarios y analizar datos.";
                    break;
                case "tienda":
                    instruccionesRol = "Eres un Experto en Ventas. Ayuda a las refaccionarias de AutoRed a redactar mejores descripciones para sus productos.";
                    break;
                default:
                    instruccionesRol = "Eres el Copiloto Mecánico de AutoRed. Ayuda a los clientes a encontrar sus refacciones de forma amigable.";
                    break;
            }

            // 3. OBLIGAR A GEMINI A RESPONDER EN FORMATO DE "ACCIÓN"
            String promptSistema = instruccionesRol +
                    "\n\nContexto de la Base de Datos: " + contextoDB +
                    "\nPregunta del usuario: " + mensajeUsuario +
                    "\n\nREGLA ESTRICTA: NO respondas con texto normal. Tu respuesta DEBE ser ÚNICAMENTE un objeto JSON válido con esta estructura exacta (sin comillas invertidas ni Markdown):" +
                    "\n{" +
                    "\n  \"mensaje\": \"Tu respuesta amigable o de ayuda aquí\"," +
                    "\n  \"accion\": \"Escribe NAVIGATE_CATALOG (si buscan piezas), NAVIGATE_HOME (si quieren ir al inicio), o NINGUNA (si solo es plática)\"" +
                    "\n}";

            // 4. LLAMADA SEGURA A GEMINI
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey;
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            Map<String, Object> contentNode = new HashMap<>();
            Map<String, Object> partNode = new HashMap<>();
            partNode.put("text", promptSistema);
            contentNode.put("parts", Collections.singletonList(partNode));
            body.put("contents", Collections.singletonList(contentNode));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> geminiRes = restTemplate.postForEntity(url, entity, Map.class);

            // 5. EXTRAER RESPUESTA
            Map resBody = geminiRes.getBody();
            List candidates = (List) resBody.get("candidates");
            Map first = (Map) candidates.get(0);
            Map content = (Map) first.get("content");
            List parts = (List) content.get("parts");
            String textoBot = (String) ((Map) parts.get(0)).get("text");

            // Limpiamos los "```json" que Gemini a veces manda por error
            textoBot = textoBot.replaceAll("```json", "").replaceAll("```", "").trim();

            respuesta.put("rawJson", textoBot);
            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            e.printStackTrace();
            respuesta.put("rawJson", "{\"mensaje\":\"Ups, mis engranes fallaron en el servidor.\", \"accion\":\"NINGUNA\"}");
            return ResponseEntity.status(500).body(respuesta);
        }
    }
}