package com.example.rag.business.responses.documentResponse;

public record DocumentUploadResponse(
        String message,
        String documentName,
        int chunks
) {}
