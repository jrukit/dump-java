package com.modern.java.document.controller;

import com.modern.java.document.model.UploadDocumentRequest;
import com.modern.java.document.service.DocumentService;
import com.modern.java.document.service.UnknownDependencyService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {
    @InjectMocks
    private DocumentController documentController;
    @Mock
    private DocumentService documentService;
    @Mock
    private UnknownDependencyService unknownDependencyService;

    @Test
    void shouldBeOkStatus_WhenCallUploadCreditDocument() throws IOException {
        UploadDocumentRequest mockUploadDocumentRequest = new UploadDocumentRequest();
        mockUploadDocumentRequest.setCaseNo("C123456");
        mockUploadDocumentRequest.setDocType("AGREEMENT");
        mockUploadDocumentRequest.setDocClass("LEGAL");
        mockUploadDocumentRequest.setDestinationCompanyCode("COMP001");
        mockUploadDocumentRequest.setDestinationOfficeCode("OFF001");
        mockUploadDocumentRequest.setTotalDocument(1);
        mockUploadDocumentRequest.setDocStatus("PENDING");
        mockUploadDocumentRequest.setCreatedDate(new Date());
        mockUploadDocumentRequest.setCreatedBy("user001");

        MockMultipartFile mockFile = new MockMultipartFile(
                "document",
                "dump.jpg",
                "image/jpeg",
                "Dummy file content".getBytes(StandardCharsets.UTF_8)
        );

        BDDMockito.given(unknownDependencyService.getHireeNo("C123456")).willReturn("HIREE789");

        ResponseEntity<String> actual = documentController.uploadCreditDocument(mockFile, mockUploadDocumentRequest);

        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertEquals("success!", actual.getBody());

        verify(documentService).uploadCreditDocument(mockUploadDocumentRequest, mockFile, "HIREE789", "admin");
    }

    @Test
    void shouldBeBadRequestStatus_WhenCallUploadCreditDocument_WithTxtFileFormat() throws IOException {
        UploadDocumentRequest mockUploadDocumentRequest = new UploadDocumentRequest();
        mockUploadDocumentRequest.setCaseNo("C123456");
        mockUploadDocumentRequest.setDocType("AGREEMENT");
        mockUploadDocumentRequest.setDocClass("LEGAL");
        mockUploadDocumentRequest.setDestinationCompanyCode("COMP001");
        mockUploadDocumentRequest.setDestinationOfficeCode("OFF001");
        mockUploadDocumentRequest.setTotalDocument(1);
        mockUploadDocumentRequest.setDocStatus("PENDING");
        mockUploadDocumentRequest.setCreatedDate(new Date());
        mockUploadDocumentRequest.setCreatedBy("user001");

        MockMultipartFile mockFile = new MockMultipartFile(
                "document",
                "dump.txt",
                "application/txt",
                "Dummy file content".getBytes(StandardCharsets.UTF_8)
        );

        ResponseEntity<String> actual = documentController.uploadCreditDocument(mockFile, mockUploadDocumentRequest);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
        Assertions.assertEquals("isn't JPEG file!", actual.getBody());

        verify(documentService, never()).uploadCreditDocument(mockUploadDocumentRequest, mockFile, "HIREE789", "admin");
    }

    @Test
    void shouldBeBadRequestStatus_WhenCallUploadCreditDocument_WithHireeNumberNotFound() throws IOException {
        UploadDocumentRequest mockUploadDocumentRequest = new UploadDocumentRequest();
        mockUploadDocumentRequest.setCaseNo("C123456");
        mockUploadDocumentRequest.setDocType("AGREEMENT");
        mockUploadDocumentRequest.setDocClass("LEGAL");
        mockUploadDocumentRequest.setDestinationCompanyCode("COMP001");
        mockUploadDocumentRequest.setDestinationOfficeCode("OFF001");
        mockUploadDocumentRequest.setTotalDocument(1);
        mockUploadDocumentRequest.setDocStatus("PENDING");
        mockUploadDocumentRequest.setCreatedDate(new Date());
        mockUploadDocumentRequest.setCreatedBy("user001");

        MockMultipartFile mockFile = new MockMultipartFile(
                "document",
                "dump.jpg",
                "image/jpeg",
                "Dummy file content".getBytes(StandardCharsets.UTF_8)
        );

        BDDMockito.given(unknownDependencyService.getHireeNo("C123456")).willReturn(null);

        ResponseEntity<String> actual = documentController.uploadCreditDocument(mockFile, mockUploadDocumentRequest);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
        Assertions.assertEquals("hiree number not found!", actual.getBody());

        verify(documentService, never()).uploadCreditDocument(mockUploadDocumentRequest, mockFile, "HIREE789", "admin");
    }

    @Test
    void shouldBeRuntimeException_WhenCallUploadCreditDocument_WithCallUploadCreditDocumentThrowsRuntimeException() throws IOException {
        UploadDocumentRequest mockUploadDocumentRequest = new UploadDocumentRequest();
        mockUploadDocumentRequest.setCaseNo("C123456");
        mockUploadDocumentRequest.setDocType("AGREEMENT");
        mockUploadDocumentRequest.setDocClass("LEGAL");
        mockUploadDocumentRequest.setDestinationCompanyCode("COMP001");
        mockUploadDocumentRequest.setDestinationOfficeCode("OFF001");
        mockUploadDocumentRequest.setTotalDocument(1);
        mockUploadDocumentRequest.setDocStatus("PENDING");
        mockUploadDocumentRequest.setCreatedDate(new Date());
        mockUploadDocumentRequest.setCreatedBy("user001");

        MockMultipartFile mockFile = new MockMultipartFile(
                "document",
                "dump.jpg",
                "image/jpeg",
                "Dummy file content".getBytes(StandardCharsets.UTF_8)
        );

        BDDMockito.given(unknownDependencyService.getHireeNo("C123456")).willReturn("HIREE789");

        BDDMockito.willThrow(RuntimeException.class)
                .given(documentService).uploadCreditDocument(mockUploadDocumentRequest, mockFile, "HIREE789", "admin");

        Assertions.assertThrows(RuntimeException.class,
                () -> documentController.uploadCreditDocument(mockFile, mockUploadDocumentRequest));
    }
}