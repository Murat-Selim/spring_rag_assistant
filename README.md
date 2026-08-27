# Spring RAG Assistant

A simple, easy-to-understand **RAG (Retrieval-Augmented Generation) document assistant** built with Spring Boot.

The user uploads a PDF → text is extracted → split into chunks → an embedding is generated for each chunk → stored in PostgreSQL + pgvector. When the user asks a question, the question's embedding is generated, the most relevant chunks are found via vector similarity search, and provided to the LLM as context.

## Technologies

| Layer | Technology |
|---|---|
| Language / Framework | Java 17, Spring Boot 3.5 |
| Web / Data | Spring Web, Spring Data JPA, Validation |
| Database | PostgreSQL 16 + pgvector |
| PDF | Apache PDFBox 3 |
| AI | OpenAI (planned) — mock embedding for now |
| Infrastructure | Docker Compose, Maven Wrapper |

## Architecture

```text
webApi/controllers        → REST endpoints
business/abstracts        → Service interfaces
business/concretes        → Service implementations (Managers)
business/requests         → Request DTOs
business/responses        → Response DTOs
business/rules            → Business validation rules
core/                     → Global exception handling, config
dataAccess/abstracts      → Repository interfaces
entities/concretes        → Entity models
```

Pipeline:

```text
DOCUMENT → TEXT → CHUNKS → EMBEDDINGS → VECTOR DB → RETRIEVAL → CONTEXT → LLM → ANSWER
```

## API

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/health` | Service status |
| POST | `/api/documents` | PDF upload (`multipart/form-data`, field: `file`) |
| POST | `/api/chat` | Ask a question (`application/json`) |

Swagger UI: `http://localhost:8080/swagger-ui.html`

OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### Examples

```bash
# Upload a PDF
curl -X POST http://localhost:8080/api/documents \
  -F "file=@document.pdf;type=application/pdf"

# Ask a question
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "How many annual leave days do I have?"}'
```

Upload response:

```json
{
  "message": "Document processed successfully",
  "documentName": "document.pdf",
  "chunks": 18
}
```

Chat response:

```json
{
  "answer": "According to the document, the annual leave period is 14 days."
}
```

## Setup and Running

### Requirements

- Java 17+
- Docker (for PostgreSQL/pgvector)

### Steps

1. Start PostgreSQL + pgvector:

   ```bash
   docker compose up -d
   ```

2. Start the application:

   ```bash
   ./mvnw spring-boot:run        # Linux/macOS
   .\mvnw.cmd spring-boot:run    # Windows
   ```

### Environment Variables

```bash
OPENAI_API_KEY=your_key_here
```

Never commit a real key to the repository.

## Tests

```bash
./mvnw test                    # Linux/macOS
.\mvnw.cmd test                # Windows
```

## Status

- [x] Phase 1 — Spring Boot setup, health endpoint
- [x] Phase 2 — Entity + pgvector repository (DB connection will be activated in the final step)
- [x] Phase 3 — PDF upload + text extraction
- [x] Phase 4 — Chunking (700 characters / 100 overlap)
- [x] Phase 5 — Embedding (mock; will switch to a real model once the OpenAI key is available)
- [x] Phase 6 — Vector similarity search (top 3)
- [x] Phase 7 — RAG flow (mock LLM answer)
- [x] Swagger/OpenAPI integration
- [ ] Final integration test (end-to-end with Docker + real DB)

## Notes

- `EmbeddingManager` currently produces a **deterministic mock**; it will be replaced with a real embedding model once `OPENAI_API_KEY` is available.
- `generateAnswer` inside `RagManager` currently returns the retrieval result; with LLM integration it will produce real document-based answers.
