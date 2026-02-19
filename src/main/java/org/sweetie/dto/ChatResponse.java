package org.sweetie.dto;

public record ChatResponse (
        String response,
        String llm,
        String originalMessage,
        long timeStamp
){}
