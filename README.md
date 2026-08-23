# Spring RAG Assistant

Spring Boot ile geliştirilen basit ve anlaşılır bir **RAG (Retrieval-Augmented Generation) doküman asistanı**.

Kullanıcı PDF yükler → metin çıkarılır → chunk'lara ayrılır → her chunk için embedding üretilir → PostgreSQL + pgvector içinde saklanır. Kullanıcı soru sorduğunda sorunun embedding'i oluşturulur, vector similarity search ile en alakalı chunk'lar bulunur ve LLM'e context olarak verilir.

## Teknolojiler

| Katman | Teknoloji |
|---|---|
| Dil / Framework | Java 17, Spring Boot 3.5 |
| Web / Data | Spring Web, Spring Data JPA, Validation |
| Veritabanı | PostgreSQL 16 + pgvector |
| PDF | Apache PDFBox 3 |
| AI | OpenAI (planlanan) — şimdilik mock embedding |
| Altyapı | Docker Compose, Maven Wrapper |

## Mimari

```text
webApi/controllers        → REST endpoint'ler
business/abstracts        → Service interface'leri
business/concretes        → Service implementasyonları (Manager)
business/requests         → Request DTO'ları
business/responses        → Response DTO'ları
business/rules            → Business validation kuralları
core/                     → Global exception handling, config
dataAccess/abstracts      → Repository interface'leri
entities/concretes        → Entity modelleri
```

Pipeline:

```text
DOCUMENT → TEXT → CHUNKS → EMBEDDINGS → VECTOR DB → RETRIEVAL → CONTEXT → LLM → ANSWER
```

## API

| Method | Endpoint | Açıklama |
|---|---|---|
| GET | `/api/health` | Servis durumu |
| POST | `/api/documents` | PDF yükleme (`multipart/form-data`, field: `file`) |
| POST | `/api/chat` | Soru sorma (`application/json`) |

### Örnekler

```bash
# PDF yükleme
curl -X POST http://localhost:8080/api/documents \
  -F "file=@dokuman.pdf;type=application/pdf"

# Soru sorma
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "Yillik izin kac gundur?"}'
```

Upload response:

```json
{
  "message": "Document processed successfully",
  "documentName": "dokuman.pdf",
  "chunks": 18
}
```

Chat response:

```json
{
  "answer": "Dokümana göre yıllık izin süresi 14 gündür."
}
```

## Kurulum ve Çalıştırma

### Gereksinimler

- Java 17+
- Docker (PostgreSQL/pgvector için)

### Adımlar

1. PostgreSQL + pgvector'ı başlat:

   ```bash
   docker compose up -d
   ```

2. `src/main/resources/application.yml` içinde:
   - `datasource` bloğundaki yorumları kaldır
   - `spring.autoconfigure.exclude` bloğunu sil

3. Uygulamayı başlat:

   ```bash
   ./mvnw spring-boot:run        # Linux/macOS
   .\mvnw.cmd spring-boot:run    # Windows
   ```

### Environment Variables

OpenAI entegrasyonu aktifleştiğinde:

```bash
OPENAI_API_KEY=your_key_here
```

`.env.example` dosyasını `.env` olarak kopyalayıp kullanabilirsiniz. Gerçek key asla repoya commit edilmez.

## Testler

```bash
./mvnw test                    # Linux/macOS
.\mvnw.cmd test                # Windows
```

## Durum

- [x] Phase 1 — Spring Boot kurulumu, health endpoint
- [x] Phase 2 — Entity + pgvector repository (DB bağlantısı son adımda aktive edilecek)
- [x] Phase 3 — PDF upload + text extraction
- [x] Phase 4 — Chunking (700 karakter / 100 overlap)
- [x] Phase 5 — Embedding (mock; OpenAI key gelince gerçek modele geçecek)
- [x] Phase 6 — Vector similarity search (top 3)
- [x] Phase 7 — RAG akışı (mock LLM cevabı)
- [ ] Son entegrasyon testi (Docker + gerçek DB ile uçtan uca)

## Notlar

- `EmbeddingManager` şu an **deterministik mock** üretir; `OPENAI_API_KEY` geldiğinde gerçek embedding modeliyle değiştirilecektir.
- `RagManager` içindeki `generateAnswer` şimdilik retrieval sonucunu döner; LLM entegrasyonuyla dokümana dayalı gerçek cevap üretilecektir.
