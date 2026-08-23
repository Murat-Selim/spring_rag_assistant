package com.example.rag.business.concretes;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.rag.business.abstracts.ChunkingService;

@Service
public class ChunkingManager implements ChunkingService {

    private static final int CHUNK_SIZE = 700;
    private static final int OVERLAP = 100;

    @Override
    public List<String> chunk(String text) {
        String normalized = text == null ? "" : text.strip();
        if (normalized.isEmpty()) {
            return List.of();
        }
        if (normalized.length() <= CHUNK_SIZE) {
            return List.of(normalized);
        }

        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < normalized.length()) {
            int end = Math.min(start + CHUNK_SIZE, normalized.length());
            chunks.add(normalized.substring(start, end));
            start += CHUNK_SIZE - OVERLAP;
        }
        return chunks;
    }

}
