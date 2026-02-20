package org.sweetie.aichat.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.sweetie.aichat.dto.ChatRequest;
import org.sweetie.aichat.dto.ChatResponse;
import org.sweetie.aichat.service.ChatService;

/**
 * REST controller for handling chat requests.
 * Supports both GET and POST endpoints for AI chat interactions.
 */
@Validated
@RestController
@RequestMapping("/api")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;

    /**
     * Constructor-based dependency injection for ChatService.
     *
     * @param chatService the service handling chat logic
     */
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Handles GET requests for chat.
     *
     * @param message the chat message from the user, cannot be blank
     * @param llm optional AI provider name (e.g., "openai", "ollama")
     * @return ResponseEntity containing ChatResponse
     */
    @GetMapping("/chat")
    public ResponseEntity<ChatResponse> chatGet(
            @NotBlank(message = "Message cannot be empty")
            @RequestParam String message,
            @RequestParam(required = false) String llm) {

        return processChat(message, llm);
    }

    /**
     * Handles POST requests for chat.
     *
     * @param request the chat request containing message and optional LLM provider
     * @return ResponseEntity containing ChatResponse
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chatPost(
            @Valid @RequestBody ChatRequest request) {

        return processChat(request.message(), request.llm());
    }

    /**
     * Internal helper method to process chat requests.
     *
     * @param message the chat message
     * @param llm optional AI provider name
     * @return ResponseEntity containing ChatResponse
     */
    private ResponseEntity<ChatResponse> processChat(String message, String llm) {

        log.info("Incoming chat request");

        // Delegate actual chat processing to ChatService
        ChatResponse response = chatService.processChat(message, llm);

        return ResponseEntity.ok(response);
    }
}