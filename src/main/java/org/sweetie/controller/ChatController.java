package org.sweetie.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RestController
@RequestMapping("/api")
public class ChatController {

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
    public ResponseEntity<?> chatGet(
            @RequestParam String message,
           // @RequestParam (required = false)String llm)
            @RequestParam (defaultValue = "ollama") String llm){

        // validate message input FIRST
        if(message==null || message.isBlank()){
            log.warn("Invalid GET request: message is empty");
            return ResponseEntity
                    .badRequest()
                    .body("Message cannot be empty");
        }

        log.debug("Received GET request with message: {}, llm: {}", message, llm);
        log.info("Received GET request with message: {}", message);


        long timestamp = System.currentTimeMillis();

        try {
            // Delegate response generation to service layer
           String  response = chatService.getAiResponse(message, llm);
           return ResponseEntity.ok(new ChatResponse(response, llm, message, timestamp));
        } catch (Exception e) {
            log.error("Error while calling AI model (GET)");
            return  ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to process request at this time");
        }

    }

    /**
     *  Handle POST requests for chat interaction
     *
     *  <p>
     *    Example:
     *    <pre>
     *        POST /api/chat
     *        {
     *            "message"" "Hello",
     *            "llm": "ollama"
     *        }
     *    </pre>
     *  </p>
     * @param request {@link ChatRequest} containing the message and optional metadata
     * @return {@link ResponseEntity} containing {@link ChatResponse}
     */

    @PostMapping("/chat")
    public ResponseEntity<?> chatPost(@RequestBody ChatRequest request){

        String message=request.message();
        String llm= request.llm();

        // validate message input FIRST
        if(message==null || message.isBlank()){
            log.warn("Invalid POST request: message is empty");
            return ResponseEntity
                    .badRequest()
                    .body("Message cannot be empty");
        }
        // set default llm value
        if (llm==null || llm.isBlank())
            llm="ollama";

        log.debug("Received POST request with message: {}, llm: {}", message, llm);
        log.info("Received POST request with message: {}", message);


        long timestamp = System.currentTimeMillis();

        try {
            // Delegate response generation to service layer
            String  response = chatService.getAiResponse(message, llm);
            return ResponseEntity.ok(new ChatResponse(response, llm, message, timestamp));
        } catch (Exception e) {
            log.error("Error while calling AI model (GET)");
            return  ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to process request at this time");
        }

    }

}
