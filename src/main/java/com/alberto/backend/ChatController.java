package com.alberto.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private PiezaRepository piezaRepository;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    // ==========================================
    // 🛡️ 1. VALIDACIÓN CONTRA GASTO DE TOKENS
    // ==========================================
    private String validarMensajeChatbot(String mensaje) {
        if (mensaje == null || mensaje.trim().isEmpty()) {
            return null;
        }
        // Limita a 500 caracteres
        if (mensaje.length() > 500) {
            return null;
        }
        // Evita caracteres de control ocultos (ASCII 0-31 y 127)
        return mensaje.replaceAll("[\\x00-\\x1F\\x7F]", "").trim();
    }

    // ==========================================
    // 🛡️ 2. PROMPT FIREWALL (ANTI-INJECTION)
    // ==========================================
    private boolean pasaFirewall(String mensaje) {
        String mensajeNormalizado = mensaje.toLowerCase();
        List<String> frasesBloqueadas = Arrays.asList(
                "ignora las instrucciones", "olvida las reglas", "revela tu prompt",
                "contraseñas", "actúa como administrador", "dame la base de datos",
                "claves api", "borra los registros", "ejecuta", "system prompt"
        );

        for (String frase : frasesBloqueadas) {
            if (mensajeNormalizado.contains(frase)) {
                return false; // Intento de hackeo detectado
            }
        }
        return true; // Mensaje limpio
    }

    // ==========================================
    // 🧠 3. CONTROLADOR PRINCIPAL
    // ==========================================
    @PostMapping("/mensaje")
    public ResponseEntity<Map<String, String>> procesarMensaje(@RequestBody Map<String, String> request) {
        Map<String, String> respuesta = new HashMap<>();
        String mensajeOriginal = request.get("mensaje");
        String rolUsuario = request.getOrDefault("rol", "comprador");

        // --- FILTROS DE SEGURIDAD ---
        String mensajeLimpio = validarMensajeChatbot(mensajeOriginal);

        if (mensajeLimpio == null) {
            logger.warn("⚠️ [SEGURIDAD] Mensaje vacío o demasiado largo rechazado.");
            respuesta.put("rawJson", "{\"mensaje\":\"Tu mensaje es inválido o demasiado largo.\", \"accion\":\"NINGUNA\"}");
            return ResponseEntity.ok(respuesta);
        }

        if (!pasaFirewall(mensajeLimpio)) {
            logger.error("🚨 [ALERTA DE SEGURIDAD] Intento de Prompt Injection bloqueado. Mensaje: " + mensajeOriginal);
            respuesta.put("rawJson", "{\"mensaje\":\"Lo siento, por políticas de seguridad de AutoRed no puedo procesar esa solicitud. 🛡️\", \"accion\":\"NINGUNA\"}");
            return ResponseEntity.ok(respuesta); // Retornamos OK para que el frontend dibuje el mensaje de rechazo
        }

        try {
            // --- LÓGICA DE BÚSQUEDA DE PIEZAS ---
            String textoParaBuscar = mensajeLimpio.toLowerCase().replaceAll("[.,¿?¡!]", "");
            String[] palabras = textoParaBuscar.split(" ");
            List<String> ignorar = Arrays.asList("hola", "oye", "quiero", "busco", "necesito", "puedes");
            String palabraClave = "";
            for (String p : palabras) {
                if (p.length() > 2 && !ignorar.contains(p)) {
                    palabraClave = p; break;
                }
            }

            String contextoDB = "No hay datos.";
            if (!palabraClave.isEmpty()) {
                List<Pieza> piezas = piezaRepository.buscarPorNombre(palabraClave);
                if (!piezas.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < Math.min(5, piezas.size()); i++) {
                        Pieza p = piezas.get(i);
                        if (p.isActivo()) {
                            sb.append("Pieza: ").append(p.getNombre_comercial()).append(" | Marca: ").append(p.getMarca()).append("; ");
                        }
                    }
                    if (sb.length() > 0) contextoDB = sb.toString();
                }
            }

            // --- PERSONALIDAD POR ROL ---
            String instruccionesRol = "";
            switch(rolUsuario) {
                case "admin":
                    instruccionesRol = "Eres el Auditor IA de AutoRed. Ayudas al administrador a revisar tiendas y usuarios. Si te piden ir a usuarios, tu accion será NAVIGATE_USERS. Si piden tiendas, será NAVIGATE_STORES.";
                    break;
                case "tienda":
                    instruccionesRol = "Eres el Consultor de Ventas de AutoRed. Ayudas a las refaccionarias a redactar descripciones que vendan más. Si quieren ver sus piezas, tu accion será NAVIGATE_MY_INVENTORY.";
                    break;
                default:
                    instruccionesRol = "Eres el Mecánico Virtual de AutoRed. Ayudas a los clientes a encontrar piezas. Si quieren buscar refacciones la accion es NAVIGATE_CATALOG. Si quieren registrar un auto es NAVIGATE_GARAGE.";
                    break;
            }

            // --- PROMPT FINAL CERRADO ---
            String promptSistema = instruccionesRol +
                    "\n\nContexto BD: " + contextoDB +
                    "\nPregunta del usuario: " + mensajeLimpio +
                    "\n\nREGLA DE FORMATO: Responde ÚNICAMENTE en JSON con esta estructura exacta:" +
                    "\n{" +
                    "\n  \"mensaje\": \"Tu respuesta en texto\"," +
                    "\n  \"accion\": \"Nombre de la accion o NINGUNA\"" +
                    "\n}";

            // --- LLAMADA A GEMINI ---
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

            Map resBody = geminiRes.getBody();
            List candidates = (List) resBody.get("candidates");
            Map first = (Map) candidates.get(0);
            Map content = (Map) first.get("content");
            List parts = (List) content.get("parts");
            String textoBot = (String) ((Map) parts.get(0)).get("text");

            textoBot = textoBot.replaceAll("```json", "").replaceAll("```", "").trim();

            respuesta.put("rawJson", textoBot);
            logger.info("✅ Interacción de chatbot procesada correctamente para rol: " + rolUsuario);
            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            logger.error("❌ Error de servidor en Gemini: " + e.getMessage());
            respuesta.put("rawJson", "{\"mensaje\":\"Mis servidores están en mantenimiento. Intenta en un momento. 🔧\", \"accion\":\"NINGUNA\"}");
            return ResponseEntity.status(500).body(respuesta);
        }
    }
}