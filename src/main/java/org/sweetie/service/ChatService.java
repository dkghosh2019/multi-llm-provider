package org.sweetie.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.sweetie.dto.ChatResponse;
import org.sweetie.exception.AIServiceException;
import org.sweetie.model.LLMType;

import java.util.Map;
import java.util.Optional;

@Service
public class ChatService {

    private static final Logger log =
            LoggerFactory.getLogger(ChatService.class);

    private final Map<LLMType, ChatClient> chatClients;
    private final LLMType defaultProvider;

    public ChatService(
            OpenAiChatModel openAiChatModel,
            OllamaChatModel ollamaChatModel,
            @Qualifier("geminiChatClient") ChatClient geminiChatClient,
            AnthropicChatModel anthropicChatModel,
            @Value("${spring.ai.default-provider}") String defaultProvider) {

        this.defaultProvider = LLMType.valueOf(defaultProvider.toUpperCase());

        this.chatClients = Map.of(
                LLMType.OPENAI, ChatClient.create(openAiChatModel),
                LLMType.OLLAMA, ChatClient.create(ollamaChatModel),
                LLMType.GEMINI, geminiChatClient,
                LLMType.ANTHROPIC, ChatClient.create(anthropicChatModel)
        );
    }

    public ChatResponse processChat(String message, String llmName) {

        LLMType llmType = resolveLlmType(llmName);

        log.info("Routing request to LLM: {}", llmType);
        log.debug("Processing message: {}", message);

        ChatClient chatClient = getChatClient(llmType);

        try {
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

    private LLMType resolveLlmType(String llmName) {

        if (llmName == null || llmName.isBlank()) {
            log.info("No provider specified. Using default: {}", defaultProvider);
            return defaultProvider;
        }

        try {
            return LLMType.valueOf(llmName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Unsupported LLM type: " + llmName
            );
        }
    }

    private ChatClient getChatClient(LLMType llmType) {

        return Optional.ofNullable(chatClients.get(llmType))
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unsupported LLM type: " + llmType));
    }
}