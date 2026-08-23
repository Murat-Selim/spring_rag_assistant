package com.example.rag.business.concretes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EmbeddingManagerTest {

    private final EmbeddingManager embeddingManager = new EmbeddingManager();

    @Test
    void sameTextProducesSameVector() {
        String v1 = embeddingManager.embed("Yıllık izin kaç gündür?");
        String v2 = embeddingManager.embed("Yıllık izin kaç gündür?");
        assertEquals(v1, v2);
    }

    @Test
    void differentTextProducesDifferentVector() {
        String v1 = embeddingManager.embed("Yıllık izin kaç gündür?");
        String v2 = embeddingManager.embed("Fatura ödeme tarihi nedir?");
        assertNotEquals(v1, v2);
    }

    @Test
    void vectorLiteralHasCorrectFormatAndDimensions() {
        String literal = embeddingManager.embed("hello world example");
        assertTrue(literal.startsWith("["));
        assertTrue(literal.endsWith("]"));
        assertEquals(EmbeddingManager.DIMENSIONS, literal.split(",").length);
    }

}
