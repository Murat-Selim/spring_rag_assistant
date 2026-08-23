package com.example.rag.dataAccess.abstracts;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.rag.entities.concretes.DocumentChunk;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    @Modifying
    @Query(value = """
            INSERT INTO document_chunks (document_name, content, embedding)
            VALUES (:documentName, :content, CAST(:embedding AS vector))
            """, nativeQuery = true)
    void insertChunk(@Param("documentName") String documentName,
                     @Param("content") String content,
                     @Param("embedding") String embedding);

    @Query(value = """
            SELECT c.id          AS id,
                   c.document_name AS documentName,
                   c.content     AS content,
                   c.embedding <=> CAST(:queryEmbedding AS vector) AS distance
            FROM document_chunks c
            ORDER BY c.embedding <=> CAST(:queryEmbedding AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<SimilarChunk> findSimilarChunks(@Param("queryEmbedding") String queryEmbedding,
                                         @Param("limit") int limit);

    interface SimilarChunk {
        Long getId();

        String getDocumentName();

        String getContent();

        Double getDistance();
    }

}
