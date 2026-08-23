package com.example.rag.business.abstracts;

import org.springframework.web.multipart.MultipartFile;

import com.example.rag.business.responses.documentResponse.DocumentUploadResponse;

public interface DocumentService {

    DocumentUploadResponse upload(MultipartFile file);

}
