package com.example.rag.business.concretes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class ChunkingManagerTest {

    private final ChunkingManager chunkingManager = new ChunkingManager();

    @Test
    void emptyTextReturnsNoChunks() {
        assertTrue(chunkingManager.chunk("").isEmpty());
        assertTrue(chunkingManager.chunk(null).isEmpty());
    }

    @Test
    void shortTextReturnsSingleChunk() {
        List<String> chunks = chunkingManager.chunk("hello world");
        assertEquals(1, chunks.size());
        assertEquals("hello world", chunks.get(0));
    }

    @Test
    void longTextProducesOverlappingChunks() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            sb.append((char) ('a' + (i % 26)));
        }
        String text = sb.toString();
        List<String> chunks = chunkingManager.chunk(text);

        assertEquals(4, chunks.size());
        for (String chunk : chunks) {
            assertTrue(chunk.length() <= 700);
        }
        // overlap: chunk1, chunk0'un son 100 karakteriyle baslar
        assertEquals(chunks.get(0).substring(600), chunks.get(1).substring(0, 100));
    }

}
