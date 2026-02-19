package org.sweetie.service;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.sweetie.model.LLMType;

@Service
public class ChatService {

    private final ChatClient openAIChatClient;
    private final ChatClient ollamaChatClient;
    private final ChatClient geminiChatClient;
    private final ChatClient anthropicClient;
    @Autowired
    public ChatService(OpenAiChatModel openAiChatModel,
                       OllamaChatModel ollamaChatModel,
                       @Qualifier("geminiChatClient") ChatClient geminiChatClient,
                       AnthropicChatModel anthropicChatModel) {
        this.openAIChatClient = ChatClient.create(openAiChatModel);
        this.ollamaChatClient = ChatClient.create(ollamaChatModel);
        this.geminiChatClient = geminiChatClient;
        this.anthropicClient = ChatClient.create(anthropicChatModel);
    }
    public String getAiResponse(String message, String llmName) {
        var chatClient = getChatModel(LLMType.valueOf(llmName.toUpperCase()));
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
    private ChatClient getChatModel(LLMType llmName) {
        return switch (llmName) {
            case OPENAI -> openAIChatClient;
            case OLLAMA -> ollamaChatClient;
            case GEMINI -> geminiChatClient;
            case ANTHROPIC -> anthropicClient;
        };
    }
}