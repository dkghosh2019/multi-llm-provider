package org.sweetie.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.sweetie.dto.ChatRequest;
import org.sweetie.dto.ChatResponse;
import org.sweetie.service.ChatService;

import java.util.Arrays;

/**
 * REST controller responsible for handling AI chat interactions.
 *
 * <p>
 * Provides GET and POST endpoints to interact with AI providers.
 * Supports input validation, logging, provider validation, and defaulting to a configured provider.
 * </p>
 *
 * <ul>
 *     <li>GET /api/chat?message=Hello&llm=ollama - simple query</li>
 *     <li>POST /api/chat - JSON body with "message" and optional "llm"</li>
 * </ul>
 *
 * <p>
 * Delegates AI response generation to {@link ChatService}.
 * </p>
 */
@Validated
@RestController
@RequestMapping("/api")
public class ChatController {

    /**
     * Logger instance for structured logging.
     */
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    /**
     * Default AI provider from application properties.
     */
    @Value("${spring.ai.default-provider}")
    private String defaultProvider;

    /**
     * Service responsible for communicating with AI providers.
     */
    private final ChatService chatService;

    /**
     * Constructor-based dependency injection.
     *
     * @param chatService Service that handles AI response generation.
     */
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Enum representing allowed AI providers.
     */
    private enum Provider {
        OLLAMA, OPENAI, GEMINI, ANTHROPIC;

        /**
         * Validate and return enum from string, ignoring case.
         *
         * @param value Provider string
         * @return Provider enum
         * @throws IllegalArgumentException if provider is invalid
         */
        public static Provider fromString(String value) {
            return Arrays.stream(Provider.values())
                    .filter(p -> p.name().equalsIgnoreCase(value))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid AI provider: " + value));
        }
    }

    // -------------------- GET Endpoint --------------------

    /**
     * Handle GET requests for chat interaction.
     *
     * Example:
     * <pre>
     * GET /api/chat?message=Hello&llm=ollama
     * </pre>
     *
     * @param message User message (cannot be blank)
     * @param llm     Optional AI provider name (defaults to configured provider)
     * @return {@link ResponseEntity} containing {@link ChatResponse}
     */
    @GetMapping("/chat")
    public ResponseEntity<ChatResponse> chatGet(
            @NotBlank(message = "Message cannot be empty")
            @RequestParam String message,
            @RequestParam(required = false) String llm) {

        return processChat(message, llm);
    }

    // -------------------- POST Endpoint --------------------

    /**
     * Handle POST requests for chat interaction.
     *
     * Example:
     * <pre>
     * POST /api/chat
     * {
     *   "message": "Hello",
     *   "llm": "ollama"
     * }
     * </pre>
     *
     * @param request {@link ChatRequest} containing the message and optional llm
     * @return {@link ResponseEntity} containing {@link ChatResponse}
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chatPost(@Valid @RequestBody ChatRequest request) {
        return processChat(request.message(), request.llm());
    }

    // -------------------- Private Helpers --------------------

    /**
     * Centralized chat processing logic.
     *
     * @param message User message
     * @param llm     Optional provider string
     * @return {@link ResponseEntity} with AI response
     */
    private ResponseEntity<ChatResponse> processChat(String message, String llm) {

        // Fallback to default provider if not provided
        if (llm == null || llm.isBlank()) {
            log.info("No provider specified. Using default: {}", defaultProvider);
            llm = defaultProvider;
        }

        // Validate provider using enum
        Provider provider = Provider.fromString(llm);

        log.info("Processing chat request. Provider: {}, Message: {}", provider.name(), message);

        // Delegate AI response generation to ChatService
        String response = chatService.getAiResponse(message, provider.name());

        long timestamp = System.currentTimeMillis();

        // Return structured response
        return ResponseEntity.ok(new ChatResponse(response, provider.name(), message, timestamp));
    }

}
