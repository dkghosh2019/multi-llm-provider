package org.sweetie.aichat.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.sweetie.aichat.dto.ChatResponse;
import org.sweetie.aichat.exception.AIServiceException;
import org.sweetie.aichat.model.LLMType;

import java.util.Map;
import java.util.Optional;

/**
 * Service class responsible for routing chat messages to different AI providers
 * and returning responses.
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final Map<LLMType, ChatClient> chatClients;
    private final LLMType defaultProvider;

    /**
     * Constructor initializes available AI clients and default provider.
     *
     * @param openAiChatModel OpenAI model
     * @param ollamaChatModel Ollama model
     * @param geminiChatClient Gemini model
     * @param anthropicChatModel Anthropic model
     * @param defaultProviderName default AI provider from configuration
     */
    public ChatService(
            OpenAiChatModel openAiChatModel,
            OllamaChatModel ollamaChatModel,
            @Qualifier("geminiChatClient") ChatClient geminiChatClient,
            AnthropicChatModel anthropicChatModel,
            @Value("${spring.ai.default-provider}") String defaultProviderName) {

        this.defaultProvider = LLMType.valueOf(defaultProviderName.toUpperCase());

        // Map LLM types to their respective clients
        this.chatClients = Map.of(
                LLMType.OPENAI, ChatClient.create(openAiChatModel),
                LLMType.OLLAMA, ChatClient.create(ollamaChatModel),
                LLMType.GEMINI, geminiChatClient,
                LLMType.ANTHROPIC, ChatClient.create(anthropicChatModel)
        );
    }

    /**
     * Processes a chat message by routing it to the appropriate AI provider.
     *
     * @param message the chat message
     * @param llmName optional AI provider name
     * @return ChatResponse containing AI response and metadata
     */
    public ChatResponse processChat(String message, String llmName) {

        LLMType llmType = resolveLlmType(llmName);

        log.info("Routing request to LLM: {}", llmType);
        log.debug("Processing message: {}", message);

        // Get the corresponding chat client
        ChatClient chatClient = getChatClient(llmType);

        try {
            // Send the message to the AI client and get the response
            String response = chatClient.prompt()
                    .user(message)
                    .call()
                    .content();

            return new ChatResponse(
                    response,
                    llmType.name(),
                    message,
                    System.currentTimeMillis()
            );

        } catch (Exception ex) {
            log.error("Error calling LLM {}", llmType, ex);
            throw new AIServiceException(
                    "AI service is unavailable",
                    ex
            );
        }
    }

    /**
     * Resolves the LLM type based on user input or default provider.
     *
     * @param llmName optional AI provider name
     * @return resolved LLMType
     */
    private LLMType resolveLlmType(String llmName) {

        if (llmName == null || llmName.isBlank()) {
            log.info("No provider specified. Using default: {}", defaultProvider);
            return defaultProvider;
        }

        try {
            return LLMType.valueOf(llmName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported LLM type: " + llmName);
        }
    }

    /**
     * Retrieves the ChatClient for a given LLM type.
     *
     * @param llmType the AI provider type
     * @return ChatClient associated with the provider
     */
    private ChatClient getChatClient(LLMType llmType) {

        return Optional.ofNullable(chatClients.get(llmType))
                .orElseThrow(() ->
                        new IllegalArgumentException("Unsupported LLM type: " + llmType));
    }
}