package org.sweetie.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.sweetie.dto.ChatRequest;
import org.sweetie.dto.ChatResponse;
import org.sweetie.service.ChatService;



/**
 * REST controller responsible for handling HTTP requests.
 *
 * <p>
 *     This controller exposes endpoints for interaction with the AI chat Service.
 *     It supports both GET and POST methods for flexibility:
 * </p>
 *
 * <ul>
 *      <li> GET /api/chat - simple query based chat request</li>
 *      <li>POST /api/chat - JSON based chat request</li>
 * </ul>
 * <p>
 *    The controller performs:
 * </p>
 * <ul>
 *     <li>Input validation</li>
 *     <li> Logging for observability</li>
 *     <li>Error handling with proper HTTP status code</li>
 * </ul>
 *
 * <p>
 *   Business logic is delegated to {@link ChatService}
 * </p>
 */
@Validated
@RestController
@RequestMapping("/api")
public class ChatController {

    @Value("${spring.ai.default-provider}")
    private String defaultProvider;


    /**
     * Logger instance for structured logging
     */
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    /**
     *  Service responsible for communicating with the AI  providers
     */
    private final ChatService chatService;

    /**
     *  Coonstructor based dependency injection
     * @param chatService the service that handles AI response generation
     */
    public ChatController(ChatService chatService){
        this.chatService=chatService;
    }

    /**
     *  Handle GET requests for chat interaction
     *
     *  <p>
     *    Example:
     *    <pre>
     *        GET /api/chat?messege=Hello&llm=llama3
     *    </pre>
     *  </p>
     * @param message the user message ( not required)
     * @param llm Optional AI model name
     * @return {@link ResponseEntity} containing {@link ChatResponse}
     */
    @GetMapping("/chat")
    public ResponseEntity<ChatResponse> chatGet(
            @RequestParam String message,
           // @RequestParam (required = false)String llm)
           // @RequestParam (defaultValue = "ollama") String llm
            @RequestParam  String llm)  {

        return processChat(message, llm);

    }

    /**
     *  Handle POST requests for chat interaction
     *
     *  <p>
     *    Example:
     *    <pre>
     *        POST /api/chat
     *        {
     *            "message": "Hello",
     *            "llm": "ollama"
     *        }
     *    </pre>
     *  </p>
     * @param request {@link ChatRequest} containing the message and optional metadata
     * @return {@link ResponseEntity} containing {@link ChatResponse}
     */

    @PostMapping("/chat")
    public ResponseEntity<?> chatPost(@RequestBody ChatRequest request){

       return processChat(request.message(), request.llm());

    }

    private ResponseEntity<ChatResponse> processChat(String message, String llm) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }

        if (llm == null || llm.isBlank()) {
            log.info("default Provider: {}", defaultProvider);
            llm = defaultProvider;
        }

        log.info("Processing chat request. llm={}", llm);

        String response = chatService.getAiResponse(message, llm);
        long timestamp = System.currentTimeMillis();

        return ResponseEntity.ok(new ChatResponse(response, llm, message, timestamp));
    }


}
