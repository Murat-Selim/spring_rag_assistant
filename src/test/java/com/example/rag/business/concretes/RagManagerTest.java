package com.example.rag.business.concretes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.rag.business.abstracts.EmbeddingService;
import com.example.rag.dataAccess.abstracts.DocumentChunkRepository;

@ExtendWith(MockitoExtension.class)
class RagManagerTest {

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private com.example.rag.business.rules.RagBusinessRules rules;

    @Mock
    private DocumentChunkRepository repository;

    @InjectMocks
    private RagManager ragManager;

    private DocumentChunkRepository.SimilarChunk chunk(String content) {
        return new DocumentChunkRepository.SimilarChunk() {
            @Override
            public Long getId() {
                return 1L;
            }

            @Override
            public String getDocumentName() {
                return "test.pdf";
            }

            @Override
            public String getContent() {
                return content;
            }

            @Override
            public Double getDistance() {
                return 0.1;
            }
        };
    }

    @Test
    void askReturnsMockAnswerBasedOnRetrievedContext() {
        when(embeddingService.embed(anyString())).thenReturn("[0.1,0.2]");
        when(repository.findSimilarChunks(anyString(), anyInt()))
                .thenReturn(List.of(chunk("Yıllık izin 14 gündür.")));

        var response = ragManager.ask(new com.example.rag.business.requests.chatRequest.ChatRequest("Yıllık izin?"));

        assertTrue(response.answer().contains("Yıllık izin 14 gündür."));
        verify(repository).findSimilarChunks("[0.1,0.2]", 3);
    }

    @Test
    void askThrowsWhenNoChunksFound() {
        when(embeddingService.embed(anyString())).thenReturn("[0.1,0.2]");
        when(repository.findSimilarChunks(anyString(), anyInt())).thenReturn(List.of());
        doThrow(new com.example.rag.core.exceptions.types.BusinessException("No document content available."))
                .when(rules).contextMustExist(any());

        assertThrows(com.example.rag.core.exceptions.types.BusinessException.class,
                () -> ragManager.ask(new com.example.rag.business.requests.chatRequest.ChatRequest("Soru")));
    }

}
