package com.example.rag.business.concretes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import com.example.rag.business.abstracts.ChunkingService;
import com.example.rag.business.abstracts.EmbeddingService;
import com.example.rag.business.rules.DocumentBusinessRules;
import com.example.rag.dataAccess.abstracts.DocumentChunkRepository;

class DocumentManagerTest {

    @Test
    void uploadStoresChunksWithVectorCastRepositoryMethod() throws Exception {
        DocumentBusinessRules rules = mock(DocumentBusinessRules.class);
        ChunkingService chunking = mock(ChunkingService.class);
        EmbeddingService embedding = mock(EmbeddingService.class);
        DocumentChunkRepository repository = mock(DocumentChunkRepository.class);
        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename()).thenReturn("test.pdf");
        when(file.getBytes()).thenReturn(pdfBytes());
        when(chunking.chunk(any())).thenReturn(List.of("chunk"));
        when(embedding.embed("chunk")).thenReturn("[0.1]");

        DocumentManager manager = new DocumentManager(rules, chunking, embedding, repository);
        manager.upload(file);

        verify(repository).insertChunk("test.pdf", "chunk", "[0.1]");
        verify(repository, never()).save(any());
    }

    private byte[] pdfBytes() throws IOException {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(50, 700);
                stream.showText("document text");
                stream.endText();
            }
            document.save(output);
            return output.toByteArray();
        }
    }
}
