package org.sweetie.dto;

public record ChatRequest (
        String message,
        String llm) { }
