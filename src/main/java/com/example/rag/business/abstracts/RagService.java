package com.example.rag.business.abstracts;

import com.example.rag.business.requests.chatRequest.ChatRequest;
import com.example.rag.business.responses.chatResponse.ChatResponse;

public interface RagService {

    ChatResponse ask(ChatRequest request);

}
