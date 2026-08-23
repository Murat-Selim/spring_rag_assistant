package com.example.rag.business.requests.chatRequest;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank(message = "Question must not be blank.") String question
) {}
