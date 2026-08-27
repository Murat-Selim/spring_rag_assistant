package com.example.rag.business.concretes;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import com.example.rag.business.abstracts.ChunkingService;
import com.example.rag.business.abstracts.DocumentService;
import com.example.rag.business.abstracts.EmbeddingService;
import com.example.rag.business.responses.documentResponse.DocumentUploadResponse;
import com.example.rag.business.rules.DocumentBusinessRules;
import com.example.rag.dataAccess.abstracts.DocumentChunkRepository;

@Service
@RequiredArgsConstructor
public class DocumentManager implements DocumentService {

    private final DocumentBusinessRules rules;
    private final ChunkingService chunkingService;
    private final EmbeddingService embeddingService;
    private final DocumentChunkRepository repository;

    @Override
    @Transactional
    public DocumentUploadResponse upload(MultipartFile file) {
        rules.fileMustBeValid(file);

        String text = extractText(file);
        rules.textMustNotBeEmpty(text, file.getOriginalFilename());

        List<String> chunks = chunkingService.chunk(text);

        for (String chunk : chunks) {
            repository.insertChunk(file.getOriginalFilename(), chunk, embeddingService.embed(chunk));
        }

        return new DocumentUploadResponse(
                "Document processed successfully",
                file.getOriginalFilename(),
                chunks.size()
        );
    }

    private String extractText(MultipartFile file) {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read PDF: " + file.getOriginalFilename(), e);
        }
    }

}
