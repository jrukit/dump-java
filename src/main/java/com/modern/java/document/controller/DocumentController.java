package com.modern.java.document.controller;

import com.modern.java.document.exception.InvalidRequestException;
import com.modern.java.document.model.UploadDocumentRequest;
import com.modern.java.document.service.DocumentService;
import com.modern.java.document.service.UnknownDependencyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@RestController
public class DocumentController {
    private final DocumentService documentService;
    private final UnknownDependencyService unknownDependencyService;

    public DocumentController(DocumentService documentService, UnknownDependencyService unknownDependencyService) {
        this.documentService = documentService;
        this.unknownDependencyService = unknownDependencyService;
    }

    @PostMapping(value = "/v1/credit-documents/upload")
    public ResponseEntity<String> uploadCreditDocument(
            @RequestPart("document") MultipartFile file,
            @RequestPart("payload") UploadDocumentRequest payload
    ) {
        try {
            if (!Objects.equals(file.getContentType(), "image/jpeg")) {
                throw new InvalidRequestException("isn't JPEG file!");
            }
            String hireeNo = unknownDependencyService.getHireeNo(payload.getCaseNo());
            if (hireeNo == null) {
                throw new InvalidRequestException("hiree number not found!");
            }

            documentService.uploadCreditDocument(payload, file, hireeNo, "admin");
            return ResponseEntity.ok().body("success!");
        } catch (InvalidRequestException ie) {
            return ResponseEntity.badRequest().body(ie.getMessage());
        }
    }
}
