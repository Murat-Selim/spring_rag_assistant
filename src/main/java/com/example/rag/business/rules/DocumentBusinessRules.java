package com.example.rag.business.rules;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.rag.core.exceptions.types.BusinessException;

@Component
public class DocumentBusinessRules {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024L;

    public void fileMustBeValid(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Uploaded file must not be empty.");
        }
        if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
            throw new BusinessException("Uploaded file must be a PDF.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("Uploaded file exceeds the 10MB size limit.");
        }
    }

    public void textMustNotBeEmpty(String text, String documentName) {
        if (text == null || text.isBlank()) {
            throw new BusinessException("No text could be extracted from '" + documentName + "'.");
        }
    }

}
