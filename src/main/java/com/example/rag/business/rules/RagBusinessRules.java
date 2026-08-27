package com.example.rag.business.rules;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.rag.core.exceptions.types.BusinessException;
import com.example.rag.dataAccess.abstracts.DocumentChunkRepository;

@Component
public class RagBusinessRules {

    public void questionMustBeValid(String question) {
        if (question == null || question.isBlank()) {
            throw new BusinessException("Question must not be blank.");
        }
    }

    public void contextMustExist(List<DocumentChunkRepository.SimilarChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            throw new BusinessException(
                    "No document content available. Please upload a document first.");
        }
    }

}
