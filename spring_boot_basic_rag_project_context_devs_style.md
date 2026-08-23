# Spring Boot Basic RAG Project Context

## 1. Project Goal

Bu proje, Spring Boot kullanarak geliştirilecek basit ve anlaşılır bir **RAG (Retrieval-Augmented Generation) doküman asistanı**dır.

Temel amaç:

1. Kullanıcının PDF doküman yüklemesi
2. PDF içeriğinin metne dönüştürülmesi
3. Metnin küçük parçalara (chunk) ayrılması
4. Her chunk için embedding üretilmesi
5. Embedding verilerinin PostgreSQL + pgvector içinde saklanması
6. Kullanıcı soru sorduğunda sorunun embedding'inin oluşturulması
7. Vector similarity search ile en alakalı chunk'ların bulunması
8. Bulunan chunk'ların LLM'e context olarak verilmesi
9. LLM'in yalnızca ilgili doküman bağlamına dayanarak cevap üretmesi

Bu proje başlangıç seviyesinde tutulacaktır. İlk versiyonda multi-agent, MCP, ERP, Azure deployment, Redis, Kafka veya mikroservis kullanılmayacaktır.

---

# 2. Target Architecture

Bu projede, coding skill'deki katmanlı yaklaşım Java/Spring Boot'a uyarlanacaktır.

Temel yön:

```text
WebAPI → Business → DataAccess → Database
             |
             ├── PDF Processing
             ├── Chunking
             ├── Embedding
             └── RAG / LLM

Entities = ortak veri modelleri
Core     = ortak teknik/configuration bileşenleri
```

Uygulama akışı:

```text
Client / Postman
      |
      v
WebAPI
Controllers
      |
      v
Business
Use Cases / Services
      |
      +-------------------------------+
      |                               |
      v                               v
Document Flow                    Chat / RAG Flow
      |                               |
      v                               v
PDFBox                          Question Embedding
      |                               |
      v                               v
Chunking                       Vector Search
      |                               |
      v                               v
Embedding                      Relevant Chunks
      |                               |
      v                               v
DataAccess                     Prompt + LLM
      |                               |
      +---------------+---------------+
                      |
                      v
             PostgreSQL + pgvector
```

Bu yapı, skill'deki açık katman ayrımı ve `entity → data access → business → API` geliştirme akışının Spring Boot karşılığıdır.

---

# 3. Technology Stack

## Backend

- Java 21
- Spring Boot 3.x
- Spring Web
- Spring Data JPA
- Spring AI
- Maven

## Database

- PostgreSQL
- pgvector

## Document Processing

- Apache PDFBox

## AI

Başlangıç için:

- OpenAI API

Daha sonra alternatif olarak:

- Ollama
- Azure OpenAI

eklenebilir.

## Development / Infrastructure

- Docker
- Docker Compose
- Git
- GitHub
- Postman veya Bruno

---

# 4. Project Name

Önerilen proje adı:

```text
spring-rag-assistant
```

Base package:

```text
com.example.rag
```

---

# 5. Structured Folder Structure

Proje klasör yapısı, verdiğin `Devs` örneğindeki katmanlama yaklaşımına göre düzenlenecektir.

```text
spring-rag-assistant/
├── src/main/java/com/example/rag/
│   ├── business/
│   │   ├── abstracts/                  # Service interfaces
│   │   │   ├── DocumentService.java
│   │   │   ├── ChunkingService.java
│   │   │   ├── EmbeddingService.java
│   │   │   └── RagService.java
│   │   │
│   │   ├── concretes/                  # Service implementations
│   │   │   ├── DocumentManager.java
│   │   │   ├── ChunkingManager.java
│   │   │   ├── EmbeddingManager.java
│   │   │   └── RagManager.java
│   │   │
│   │   ├── requests/                   # Request DTOs
│   │   │   ├── chatRequest/
│   │   │   │   └── ChatRequest.java
│   │   │   └── documentRequest/
│   │   │       └── DocumentUploadRequest.java
│   │   │
│   │   ├── responses/                  # Response DTOs
│   │   │   ├── chatResponse/
│   │   │   │   └── ChatResponse.java
│   │   │   └── documentResponse/
│   │   │       └── DocumentUploadResponse.java
│   │   │
│   │   └── rules/                      # Business validation rules
│   │       ├── DocumentBusinessRules.java
│   │       └── RagBusinessRules.java
│   │
│   ├── core/
│   │   ├── exceptions/                 # Global exception handling
│   │   │   └── handlers/
│   │   │       └── GlobalExceptionHandler.java
│   │   │
│   │   ├── mappers/                    # Mapping layer
│   │   │   ├── documentMapper/
│   │   │   │   └── DocumentMapper.java
│   │   │   └── chatMapper/
│   │   │       └── ChatMapper.java
│   │   │
│   │   └── config/                     # Technical configuration
│   │       ├── AiConfig.java
│   │       └── VectorConfig.java
│   │
│   ├── dataAccess/                     # Repository interfaces
│   │   └── abstracts/
│   │       └── DocumentChunkRepository.java
│   │
│   ├── entities/                       # Entity models
│   │   └── concretes/
│   │       └── DocumentChunk.java
│   │
│   ├── webApi/
│   │   └── controllers/                # REST controllers
│   │       ├── DocumentsController.java
│   │       └── ChatController.java
│   │
│   └── RagApplication.java
│
├── src/main/resources/
│   ├── application.yml
│   └── db/
│       └── init.sql
│
├── src/test/java/com/example/rag/
│   └── RagApplicationTests.java
│
├── docker-compose.yml
├── pom.xml
├── .gitignore
├── .env.example
└── README.md
```

Bu yapı tek Spring Boot uygulaması içinde package bazlı **layered monolith** olarak kalacaktır.

---

# 6. Responsibility of Each Layer

## business/abstracts

Servis sözleşmelerini içerir.

```text
business/abstracts/
├── DocumentService.java
├── ChunkingService.java
├── EmbeddingService.java
└── RagService.java
```

Örnek:

```java
public interface RagService {
    ChatResponse ask(ChatRequest request);
}
```

Controller katmanı mümkün olduğunca bu interface'lere bağımlı olur.

---

## business/concretes

Servis interface'lerinin gerçek implementasyonlarını içerir.

```text
business/concretes/
├── DocumentManager.java
├── ChunkingManager.java
├── EmbeddingManager.java
└── RagManager.java
```

Örnek ilişki:

```text
RagService
    |
    v
RagManager
```

`RagManager`, `RagService` interface'ini implemente eder.

---

## business/requests

API veya business use-case'lerine giren verileri taşıyan request DTO'ları içerir.

```text
business/requests/
├── chatRequest/
│   └── ChatRequest.java
└── documentRequest/
    └── DocumentUploadRequest.java
```

Örnek:

```java
public record ChatRequest(
    String question
) {}
```

PDF upload için `MultipartFile` doğrudan controller parametresi olarak da alınabilir. `DocumentUploadRequest` yalnızca ek metadata gerekiyorsa kullanılacaktır.

---

## business/responses

Business katmanından API katmanına dönen response DTO'larını içerir.

```text
business/responses/
├── chatResponse/
│   └── ChatResponse.java
└── documentResponse/
    └── DocumentUploadResponse.java
```

Örnek:

```java
public record ChatResponse(
    String answer
) {}
```

---

## business/rules

Business validation kurallarını içerir.

```text
business/rules/
├── DocumentBusinessRules.java
└── RagBusinessRules.java
```

Örnek sorumluluklar:

### DocumentBusinessRules

- Dosya boş mu?
- Dosya PDF mi?
- Dosya boyutu izin verilen sınırın üzerinde mi?
- PDF'ten metin çıkarılabildi mi?

### RagBusinessRules

- Soru boş mu?
- Database'de sorgulanabilir document chunk var mı?
- Retrieval sonucu context üretilebildi mi?

Bu kurallar controller veya repository içine yazılmamalıdır.

---

## core/exceptions/handlers

Global exception yönetimini içerir.

```text
core/exceptions/handlers/
└── GlobalExceptionHandler.java
```

Amaç API'nin tutarlı hata response'ları döndürmesidir.

Örnek:

```json
{
  "message": "Uploaded file must be a PDF.",
  "timestamp": "..."
}
```

---

## core/mappers

Entity / request / response dönüşümlerini içerir.

```text
core/mappers/
├── documentMapper/
│   └── DocumentMapper.java
└── chatMapper/
    └── ChatMapper.java
```

İlk MVP'de mapping çok basitse manuel mapping kullanılabilir.

Proje büyüdüğünde MapStruct eklenebilir.

---

## core/config

Teknik configuration sınıflarını içerir.

```text
core/config/
├── AiConfig.java
└── VectorConfig.java
```

Örnek sorumluluklar:

- Spring AI client ayarları
- Embedding model configuration
- Vector / pgvector configuration
- AI ile ilgili bean tanımları

---

## dataAccess/abstracts

Repository interface'lerini içerir.

```text
dataAccess/abstracts/
└── DocumentChunkRepository.java
```

Sorumlulukları:

- DocumentChunk kaydetmek
- DocumentChunk sorgulamak
- pgvector similarity search yapmak

Örnek:

```java
public interface DocumentChunkRepository
        extends JpaRepository<DocumentChunk, Long> {

}
```

Business logic repository içinde bulunmaz.

---

## entities/concretes

Database entity modellerini içerir.

```text
entities/concretes/
└── DocumentChunk.java
```

İlk MVP için ana entity:

```text
DocumentChunk

id
documentName
content
embedding
createdAt
```

---

## webApi/controllers

REST endpoint'lerini içerir.

```text
webApi/controllers/
├── DocumentsController.java
└── ChatController.java
```

### DocumentsController

```text
POST /api/documents
```

Akış:

```text
DocumentsController
        |
        v
DocumentService
        |
        v
DocumentManager
```

### ChatController

```text
POST /api/chat
```

Akış:

```text
ChatController
      |
      v
RagService
      |
      v
RagManager
```

Controller business logic içermez.

---

# 7. Dependency Direction

Ana bağımlılık akışı:

```text
webApi
   |
   v
business/abstracts
   |
   v
business/concretes
   |
   +-------------------+
   |                   |
   v                   v
business/rules     dataAccess/abstracts
                       |
                       v
                PostgreSQL + pgvector
```

Entity ve ortak teknik bileşenler:

```text
entities/concretes
        ^
        |
business + dataAccess

core
 ^
 |
ortak teknik ihtiyaçlar
```

Request / Response akışı:

```text
HTTP Request
    |
    v
business/requests
    |
    v
Controller
    |
    v
Service Interface
    |
    v
Manager
    |
    v
business/responses
    |
    v
HTTP Response
```

Temel kurallar:

```text
Controller repository çağırmaz.

Controller doğrudan Manager yerine Service interface kullanır.

Manager business orchestration yapar.

Manager ihtiyaç duyduğu diğer servislerde interface'e bağımlı olur.

Repository yalnızca data access sorumluluğu taşır.

Business kuralları rules package'inde tutulur.

Entity doğrudan API response olarak döndürülmez.

Request ve response modelleri business altında ayrı tutulur.
```

---

# 8. Data Model

İlk MVP için tek entity yeterlidir.

## DocumentChunk

Alanlar:

```text
id
documentName
content
embedding
createdAt
```

Mantıksal gösterim:

```java
DocumentChunk

Long id
String documentName
String content
Vector embedding
LocalDateTime createdAt
```

İlk versiyonda ayrı `Document` entity oluşturmak zorunlu değildir.

Proje büyütülürse:

```text
Document
   |
   +--- DocumentChunk
   +--- DocumentChunk
   +--- DocumentChunk
```

ilişkisine geçilebilir.

---

# 9. Database Structure

PostgreSQL üzerinde pgvector extension aktif edilmelidir.

Örnek SQL:

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

İlk tablo:

```sql
CREATE TABLE IF NOT EXISTS document_chunks (
    id BIGSERIAL PRIMARY KEY,
    document_name VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    embedding VECTOR(1536),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

> Not: `VECTOR(1536)` değeri kullanılan embedding modeline göre değişebilir.

---

# 10. Vector Similarity Search

Kullanıcı sorusunun embedding'i oluşturulduktan sonra database üzerinde en yakın chunk'lar aranır.

Mantık:

```sql
SELECT
    id,
    document_name,
    content,
    embedding <=> CAST(:queryEmbedding AS vector) AS distance
FROM document_chunks
ORDER BY embedding <=> CAST(:queryEmbedding AS vector)
LIMIT 3;
```

Buradaki:

```text
LIMIT 3
```

ilk MVP'de LLM'e en alakalı 3 chunk'ın gönderileceği anlamına gelir.

---

# 11. API Design

## Document Upload

Endpoint:

```http
POST /api/documents
```

Content-Type:

```text
multipart/form-data
```

Field:

```text
file
```

Örnek response:

```json
{
  "message": "Document processed successfully",
  "documentName": "company-policy.pdf",
  "chunks": 18
}
```

---

## Chat

Endpoint:

```http
POST /api/chat
```

Request:

```json
{
  "question": "Yıllık izin kaç gündür?"
}
```

Response:

```json
{
  "answer": "Dokümana göre yıllık izin süresi 14 gündür."
}
```

İleride response içine source eklenebilir:

```json
{
  "answer": "Dokümana göre yıllık izin süresi 14 gündür.",
  "sources": [
    "company-policy.pdf"
  ]
}
```

---

# 12. Basic RAG Prompt

LLM'e doğrudan yalnızca soru gönderilmemelidir.

Sistem aşağıdaki yapıda prompt oluşturmalıdır:

```text
You are a document assistant.

Answer the user's question only by using the supplied context.

If the answer cannot be found in the context, say that the information
could not be found in the uploaded document.

Context:

{retrieved_chunks}

Question:

{user_question}
```

Bu sayede LLM'in doküman dışı bilgi üretmesi azaltılır.

---

# 13. Spring Initializr Setup

Spring Initializr üzerinden yeni proje oluşturulurken:

```text
Project:
Maven

Language:
Java

Spring Boot:
3.x

Group:
com.example

Artifact:
rag

Name:
spring-rag-assistant

Package name:
com.example.rag

Packaging:
Jar

Java:
21
```

Başlangıç dependency'leri:

```text
Spring Web

Spring Data JPA

PostgreSQL Driver

Validation
```

Daha sonra manuel olarak:

```text
Spring AI

Apache PDFBox
```

eklenecektir.

---

# 14. pom.xml Dependencies

Projenin ihtiyaç duyduğu ana dependency grupları:

```xml
<dependencies>

    <!-- REST API -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- PostgreSQL -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- PDF -->
    <dependency>
        <groupId>org.apache.pdfbox</groupId>
        <artifactId>pdfbox</artifactId>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>

</dependencies>
```

Spring AI dependency ve BOM sürümü proje oluşturulduğu tarihte kullanılan güncel Spring AI sürümüne göre eklenmelidir.

---

# 15. Docker Compose

PostgreSQL + pgvector local ortamda Docker ile çalıştırılabilir.

Örnek:

```yaml
services:

  postgres:
    image: pgvector/pgvector:pg16

    container_name: rag-postgres

    environment:
      POSTGRES_DB: ragdb
      POSTGRES_USER: raguser
      POSTGRES_PASSWORD: ragpassword

    ports:
      - "5432:5432"

    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

Çalıştırmak için:

```bash
docker compose up -d
```

Kontrol:

```bash
docker ps
```

---

# 16. application.yml

Örnek yapı:

```yaml
spring:

  datasource:
    url: jdbc:postgresql://localhost:5432/ragdb
    username: raguser
    password: ragpassword

  jpa:
    hibernate:
      ddl-auto: update

    properties:
      hibernate:
        format_sql: true

  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

ai:

  openai:
    api-key: ${OPENAI_API_KEY}
```

API key doğrudan source code içine yazılmamalıdır.

---

# 17. Environment Variable

Local development:

```bash
export OPENAI_API_KEY=your_key_here
```

veya IDE environment variables kullanılabilir.

Repository içine gerçek key commit edilmemelidir.

`.env.example`:

```text
OPENAI_API_KEY=
```

---

# 18. PDF Processing Flow

PDF yüklenince:

```text
MultipartFile
      |
      v
PDDocument
      |
      v
PDFTextStripper
      |
      v
String text
      |
      v
ChunkingService
```

Pseudo code:

```java
public String extractText(MultipartFile file) {

    try (PDDocument document =
             PDDocument.load(file.getInputStream())) {

        PDFTextStripper stripper =
            new PDFTextStripper();

        return stripper.getText(document);
    }
}
```

PDFBox sürümüne göre API değişebilir. Kod güncel sürüme göre uyarlanmalıdır.

---

# 19. Chunking Logic

İlk MVP'de basit karakter tabanlı chunking yeterlidir.

Pseudo code:

```java
public List<String> chunk(String text) {

    int chunkSize = 700;
    int overlap = 100;

    List<String> chunks =
        new ArrayList<>();

    int start = 0;

    while (start < text.length()) {

        int end =
            Math.min(start + chunkSize, text.length());

        chunks.add(
            text.substring(start, end)
        );

        start += chunkSize - overlap;
    }

    return chunks;
}
```

İleride token-based veya semantic chunking'e geçilebilir.

---

# 20. Complete Upload Flow

```text
POST /api/documents

        |
        v

WebAPI / DocumentController

        |
        v

Business / DocumentService

        |
        +--> PDF text extraction
        |
        +--> ChunkingService
        |
        +--> EmbeddingService
        |
        +--> DocumentChunkRepository
        |
        v

PostgreSQL + pgvector
```

Pseudo logic:

```text
receive PDF

extract text

create chunks

for every chunk:

    generate embedding

    create DocumentChunk

    save chunk

return processed chunk count
```

---

# 21. Complete Chat Flow

```text
POST /api/chat

      |
      v

WebAPI / ChatController

      |
      v

Business / RagService

      |
      +--> Embed question
      |
      +--> Vector search
      |
      +--> Retrieve top 3 chunks
      |
      +--> Build prompt
      |
      +--> Send prompt to LLM
      |
      v

Return answer
```

---

# 22. MVP Development Order

Projeyi tek seferde geliştirmemek gerekir.

## Phase 1 — Spring Boot Setup

Amaç:

Spring Boot API ayağa kalksın.

Yapılacaklar:

```text
Spring Initializr

Folder structure

application.yml

Health/test endpoint

Git repository
```

---

## Phase 2 — PostgreSQL

Amaç:

Spring Boot PostgreSQL'e bağlansın.

Yapılacaklar:

```text
docker-compose.yml

PostgreSQL

pgvector extension

DocumentChunk entity

Repository
```

---

## Phase 3 — PDF Upload

Amaç:

PDF dosyası API üzerinden alınsın.

Yapılacaklar:

```text
POST /api/documents

MultipartFile

PDFBox

Text extraction
```

Bu aşamada henüz AI kullanılmayabilir.

---

## Phase 4 — Chunking

Amaç:

PDF text küçük parçalara ayrılsın.

Yapılacaklar:

```text
ChunkingService

500-800 character chunk

100 character overlap
```

---

## Phase 5 — Embedding

Amaç:

Her chunk embedding'e dönüştürülsün.

Akış:

```text
Chunk

  |

Embedding Model

  |

Vector
```

Vector PostgreSQL içinde kaydedilir.

---

## Phase 6 — Semantic Search

Amaç:

Soruyla alakalı document chunk'ları bulmak.

Akış:

```text
Question

Embedding

pgvector search

Top 3 chunks
```

---

## Phase 7 — RAG

Amaç:

Bulunan chunk'ları LLM'e context olarak göndermek.

Akış:

```text
Question

+

Top 3 chunks

+

System Prompt

      |

      v

     LLM

      |

      v

    Answer
```

---

## Phase 8 — Basic Testing

Test edilmesi gereken ana senaryolar:

```text
PDF upload çalışıyor mu?

PDF text doğru okunuyor mu?

Chunks doğru oluşuyor mu?

Embedding database'e kaydoluyor mu?

Question embedding üretiliyor mu?

Vector search alakalı chunk buluyor mu?

LLM yalnızca context'e göre cevap veriyor mu?
```

---

# 23. Git Strategy

Basit branch yapısı yeterlidir.

```text
main

develop

feature/pdf-upload

feature/embedding

feature/rag
```

Commit örnekleri:

```text
feat: add document upload endpoint

feat: implement PDF text extraction

feat: add document chunking service

feat: integrate embedding generation

feat: add pgvector similarity search

feat: implement RAG question answering
```

---

# 24. What Will NOT Be Added Initially

İlk MVP sırasında aşağıdakiler eklenmemelidir:

```text
Microservices

Kafka

Redis

Kubernetes

Multi-agent

MCP

ERP integration

D365

Fine tuning

Azure deployment

Complex authentication

Frontend application
```

Amaç önce RAG mantığını çalışan küçük bir sistem üzerinden öğrenmektir.

---

# 25. Possible Future Versions

MVP tamamlandıktan sonra proje aşağıdaki sırayla büyütülebilir.

## V2

```text
Multiple documents

Document IDs

Source references

Conversation history
```

## V3

```text
Ollama

Local open-source LLM

Local embedding model
```

## V4

```text
Dockerize Spring Boot

GitHub Actions CI/CD
```

## V5

```text
Azure OpenAI

Azure deployment
```

## V6

```text
MCP

Basic agent

External tools
```

Bu geliştirmeler MVP bittikten sonra yapılmalıdır.

---

# 26. Learning Goals

Bu proje tamamlandığında aşağıdaki kavramların pratik karşılığı öğrenilmiş olacaktır:

```text
LLM

Embedding

Vector

Vector similarity search

RAG

Chunking

Prompt context

Hallucination reduction

REST API

Spring Boot

JPA

PostgreSQL

pgvector

Docker

Environment variables

Git / GitHub
```

---

# 27. Expected Interview Explanation

Proje tamamlandığında şu şekilde anlatılabilmelidir:

> Spring Boot kullanarak doküman tabanlı basit bir RAG sistemi geliştirdim. Kullanıcının yüklediği PDF dokümanlarını Apache PDFBox ile metne dönüştürüp chunk'lara ayırıyorum. Her chunk için embedding oluşturup PostgreSQL pgvector üzerinde saklıyorum. Kullanıcı soru sorduğunda sorunun embedding'ini oluşturup vector similarity search ile en alakalı doküman parçalarını buluyorum. Daha sonra bu parçaları context olarak LLM'e göndererek dokümana dayalı cevap oluşturuyorum.

---

# 28. Definition of Done — MVP

Aşağıdakilerin tamamı çalışıyorsa MVP tamamlanmıştır:

- [ ] Spring Boot uygulaması çalışıyor
- [ ] PostgreSQL Docker üzerinden çalışıyor
- [ ] pgvector aktif
- [ ] PDF yüklenebiliyor
- [ ] PDF text çıkarılabiliyor
- [ ] Text chunk'lara ayrılıyor
- [ ] Chunk embedding'leri oluşturuluyor
- [ ] Embedding'ler database'e kaydediliyor
- [ ] Kullanıcı soru sorabiliyor
- [ ] Question embedding oluşturuluyor
- [ ] Vector search çalışıyor
- [ ] Top-K chunk bulunuyor
- [ ] Chunk'lar LLM context'ine ekleniyor
- [ ] LLM cevap döndürüyor
- [ ] Context içinde bilgi yoksa sistem bunu açıkça belirtiyor

---

# 29. Core Principle

Bu projenin amacı mümkün olan en karmaşık AI sistemini oluşturmak değildir.

Amaç aşağıdaki pipeline'ı gerçekten anlamak ve çalıştırmaktır:

```text
DOCUMENT
   |
   v
TEXT
   |
   v
CHUNKS
   |
   v
EMBEDDINGS
   |
   v
VECTOR DATABASE
   |
   v
RETRIEVAL
   |
   v
CONTEXT
   |
   v
LLM
   |
   v
ANSWER
```

Bu temel oturduktan sonra agent, MCP, local LLM, Azure ve diğer ileri konular projeye kontrollü şekilde eklenebilir.
