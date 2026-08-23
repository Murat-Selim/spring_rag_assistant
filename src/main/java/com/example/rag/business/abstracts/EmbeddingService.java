package com.example.rag.business.abstracts;

public interface EmbeddingService {

    // Metni pgvector literal formatina cevirir: "[0.123,-0.456,...]"
    String embed(String text);

}
