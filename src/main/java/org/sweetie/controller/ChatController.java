package org.sweetie.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.sweetie.dto.ChatRequest;
import org.sweetie.dto.ChatResponse;
import org.sweetie.service.ChatService;

@Validated
@RestController
@RequestMapping("/api")
public class ChatController {

    private static final Logger log =
            LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/chat")
    public ResponseEntity<ChatResponse> chatGet(
            @NotBlank(message = "Message cannot be empty")
            @RequestParam String message,
            @RequestParam(required = false) String llm) {

        return processChat(message, llm);
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chatPost(
            @Valid @RequestBody ChatRequest request) {

        return processChat(request.message(), request.llm());
    }

    private ResponseEntity<ChatResponse> processChat(String message, String llm) {

        log.info("Incoming chat request");

        ChatResponse response =
                chatService.processChat(message, llm);

        return ResponseEntity.ok(response);
    }
}