package com.example.rag.business.concretes;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.rag.business.abstracts.EmbeddingService;
import com.example.rag.business.abstracts.RagService;
import com.example.rag.business.requests.chatRequest.ChatRequest;
import com.example.rag.business.responses.chatResponse.ChatResponse;
import com.example.rag.business.rules.RagBusinessRules;
import com.example.rag.dataAccess.abstracts.DocumentChunkRepository;

@Service
public class RagManager implements RagService {

    private static final int TOP_K = 3;

    private final EmbeddingService embeddingService;
    private final RagBusinessRules rules;
    private final DocumentChunkRepository repository;

    public RagManager(EmbeddingService embeddingService,
                      RagBusinessRules rules,
                      DocumentChunkRepository repository) {
        this.embeddingService = embeddingService;
        this.rules = rules;
        this.repository = repository;
    }

    @Override
    public ChatResponse ask(ChatRequest request) {
        rules.questionMustBeValid(request.question());

        String questionEmbedding = embeddingService.embed(request.question());
        List<DocumentChunkRepository.SimilarChunk> relevantChunks =
                repository.findSimilarChunks(questionEmbedding, TOP_K);
        rules.contextMustExist(relevantChunks);

        String context = buildContext(relevantChunks);
        String prompt = buildPrompt(context, request.question());

        return new ChatResponse(generateAnswer(prompt, relevantChunks));
    }

    private String buildContext(List<DocumentChunkRepository.SimilarChunk> chunks) {
        StringBuilder sb = new StringBuilder();
        for (DocumentChunkRepository.SimilarChunk chunk : chunks) {
            sb.append(chunk.getContent()).append("\n\n");
        }
        return sb.toString().strip();
    }

    private String buildPrompt(String context, String question) {
        // OpenAI key geldiginde bu prompt dogrudan LLM'e gonderilecek
        return """
                You are a document assistant.

                Answer the user's question only by using the supplied context.

                If the answer cannot be found in the context, say that the information
                could not be found in the uploaded document.

                Context:

                %s

                Question:

                %s
                """.formatted(context, question);
    }

    // MOCK LLM - OpenAI key gelene kadar retrieval sonucunu dondurur.
    private String generateAnswer(String prompt,
                                  List<DocumentChunkRepository.SimilarChunk> chunks) {
        StringBuilder sb = new StringBuilder("[Mock answer based on retrieved context]\n\n");
        for (DocumentChunkRepository.SimilarChunk chunk : chunks) {
            sb.append("- ").append(chunk.getContent(), 0, Math.min(200, chunk.getContent().length()))
                    .append("...\n");
        }
        return sb.toString().strip();
    }

}
