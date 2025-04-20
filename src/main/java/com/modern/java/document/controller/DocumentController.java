package com.modern.java.document.controller;

import com.modern.java.document.model.UploadDocumentRequest;
import com.modern.java.document.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class DocumentController {
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(value = "/v1/credit-documents/upload")
    public ResponseEntity<String> uploadCreditDocument(
            @RequestPart("document") MultipartFile file,
            @RequestPart("payload") UploadDocumentRequest payload
    ) throws IOException {
        documentService.uploadCreditDocument(payload, file, "admin");
        return ResponseEntity.ok().body("success!");
    }
}
