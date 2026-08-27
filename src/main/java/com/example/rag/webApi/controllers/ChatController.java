package com.example.rag.webApi.controllers;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import com.example.rag.business.abstracts.RagService;
import com.example.rag.business.requests.chatRequest.ChatRequest;
import com.example.rag.business.responses.chatResponse.ChatResponse;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Document-based question answering")
public class ChatController {

    private final RagService ragService;

    @PostMapping
    @Operation(summary = "Ask a question", description = "Answers a question using uploaded document context")
    public ResponseEntity<ChatResponse> ask(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(ragService.ask(request));
    }

}
