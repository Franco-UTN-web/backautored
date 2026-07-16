package com.alberto.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

// 🔥 Aquí está el import que faltaba para las fechas
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private PiezaRepository piezaRepository;

    @Autowired
    private ChatLogRepository chatLogRepository;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    // ==========================================
    // 🛡️ 1. VALIDACIÓN CONTRA GASTO DE TOKENS
    // ==========================================
    private String validarMensajeChatbot(String mensaje) {
        if (mensaje == null || mensaje.trim().isEmpty()) {
            return null;
        }
        if (mensaje.length() > 90) {
            return null;
        }
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
                return false;
            }
        }
        return true;
    }

    // ==========================================
    // 🧠 3. CONTROLADOR PRINCIPAL
    // ==========================================
    @PostMapping("/mensaje")
    public ResponseEntity<Map<String, String>> procesarMensaje(@RequestBody Map<String, String> request) {
        Map<String, String> respuesta = new HashMap<>();
        String mensajeOriginal = request.get("mensaje");
        String rolUsuario = request.getOrDefault("rol", "comprador");

        String mensajeLimpio = validarMensajeChatbot(mensajeOriginal);

        if (mensajeLimpio == null) {
            logger.warn("⚠️ [SEGURIDAD] Mensaje vacío o demasiado largo rechazado.");
            respuesta.put("rawJson", "{\"mensaje\":\"Tu mensaje es inválido o demasiado largo.\", \"accion\":\"NINGUNA\"}");
            return ResponseEntity.ok(respuesta);
        }

        if (!pasaFirewall(mensajeLimpio)) {
            logger.error("🚨 [ALERTA DE SEGURIDAD] Intento de Prompt Injection bloqueado.");
            respuesta.put("rawJson", "{\"mensaje\":\"Lo siento, por políticas de seguridad de AutoRed no puedo procesar esa solicitud. 🛡️\", \"accion\":\"NINGUNA\"}");
            return ResponseEntity.ok(respuesta);
        }

        try {
            String textoParaBuscar = mensajeLimpio.toLowerCase().replaceAll("[.,¿?¡!]", "");
            String[] palabras = textoParaBuscar.split(" ");

            List<String> ignorar = Arrays.asList(
                    "hola", "oye", "quiero", "busco", "necesito", "puedes", "estoy", "buscando",
                    "refaccion", "refacción", "numero", "número", "parte", "para", "con", "del",
                    "este", "esta", "auto", "coche", "pieza", "las", "los", "una", "uno", "que"
            );

            String palabraClave = "";

            for (String p : palabras) {
                if (p.length() > 3 && !ignorar.contains(p)) {
                    palabraClave = p;
                    if (p.matches(".*\\d.*")) {
                        break;
                    }
                }
            }

            String contextoDB = "No hay datos.";
            if (!palabraClave.isEmpty()) {
                List<Pieza> piezas = piezaRepository.buscarPorNombreOPartNumber(palabraClave);

                if (!piezas.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < Math.min(5, piezas.size()); i++) {
                        Pieza p = piezas.get(i);
                        if (p.isActivo()) {
                            sb.append("Pieza: ").append(p.getNombre_comercial())
                                    .append(" | Marca: ").append(p.getMarca_vehiculo())
                                    .append(" | No. Parte: ").append(p.getNumeroParte())
                                    .append(" | ID: ").append(p.getId()).append("; ");
                        }
                    }
                    if (sb.length() > 0) contextoDB = sb.toString();
                }
            }

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

            String promptSistema = instruccionesRol +
                    "\n\nINSTRUCCIÓN DE ESTILO: Usa formato Markdown en las respuestas. Utiliza negritas (**texto**) para resaltar los nombres de las piezas, y viñetas (-) si das varias opciones, para que tu texto sea fácil de leer." +
                    "\n\nContexto BD: " + contextoDB +
                    "\nPregunta del usuario: " + mensajeLimpio +
                    "\n\nREGLA DE FORMATO: Responde ÚNICAMENTE en JSON con esta estructura exacta:" +
                    "\n{" +
                    "\n  \"mensaje\": \"Tu respuesta redactada aquí usando el formato Markdown\"," +
                    "\n  \"accion\": \"Nombre de la accion (ej. NAVIGATE_CATALOG, OPEN_PRODUCT) o NINGUNA\"," +
                    "\n  \"producto_id\": \"El ID del producto si recomiendas uno específico, o vacío si no aplica\"" +
                    "\n}";

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

            // ==========================================
            // LÓGICA DE REGISTRO
            // ==========================================
            try {
                int tokensCalculados = (promptSistema.length() + textoBot.length()) / 4;

                ChatLog log = new ChatLog();
                log.setRolUsuario(rolUsuario);
                log.setPrompt(mensajeLimpio);
                log.setTokensEstimados(tokensCalculados);
                log.setFecha(LocalDateTime.now());

                chatLogRepository.save(log);
            } catch (Exception logEx) {
                logger.error("No se pudo guardar el log del chat: " + logEx.getMessage());
            }

            logger.info("✅ Interacción de chatbot procesada correctamente para rol: " + rolUsuario);
            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            logger.error("❌ Error de servidor en Gemini: " + e.getMessage());
            respuesta.put("rawJson", "{\"mensaje\":\"Mis servidores están en mantenimiento. Intenta en un momento. 🔧\", \"accion\":\"NINGUNA\"}");
            return ResponseEntity.status(500).body(respuesta);
        }
    }

    // ==========================================
    // 📊 4. ESTADÍSTICAS DEL PANEL
    // ==========================================
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        Map<String, Object> stats = new HashMap<>();

        try {
            long totalPeticiones = chatLogRepository.count();
            Integer totalTokens = chatLogRepository.sumarTokensTotales();
            if (totalTokens == null) totalTokens = 0;

            List<ChatLog> historialReciente = chatLogRepository.findTop10ByOrderByFechaDesc();

            stats.put("peticionesTotales", totalPeticiones);
            stats.put("tokensConsumidos", totalTokens);
            stats.put("historial", historialReciente);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error al obtener estadísticas: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}