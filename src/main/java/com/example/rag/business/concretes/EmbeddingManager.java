package com.example.rag.business.concretes;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.example.rag.business.abstracts.EmbeddingService;

// MOCK implementation - OpenAI key geldiginde gercek embedding modeli ile degistirilecek.
// Deterministik hashing: ayni metin -> ayni vektor, benzer kelimeler -> yakin vektorler.
@Service
public class EmbeddingManager implements EmbeddingService {

    static final int DIMENSIONS = 1536;

    @Override
    public String embed(String text) {
        double[] vector = new double[DIMENSIONS];
        for (String token : tokenize(text)) {
            int index = Math.floorMod(token.hashCode(), DIMENSIONS);
            vector[index] += 1.0;
        }
        normalize(vector);
        return toVectorLiteral(vector);
    }

    private List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return List.of(text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{Nd}]+"))
                .stream()
                .filter(t -> t.length() > 2)
                .toList();
    }

    private void normalize(double[] vector) {
        double sum = 0;
        for (double v : vector) {
            sum += v * v;
        }
        double norm = Math.sqrt(sum);
        if (norm == 0) {
            return;
        }
        for (int i = 0; i < vector.length; i++) {
            vector[i] /= norm;
        }
    }

    private String toVectorLiteral(double[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(String.format(Locale.ROOT, "%.6f", vector[i]));
        }
        return sb.append(']').toString();
    }

}
