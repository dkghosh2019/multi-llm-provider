package org.sweetie.model;

public enum LLMType {
    OPENAI("openai"),
    OLLAMA("ollama"),
    GEMINI("gemini"),
    ANTHROPIC("anthropic");

    private final String value;

    LLMType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}