package com.modern.java.document.service;

import com.modern.java.document.entity.BranchEntity;
import com.modern.java.document.entity.CaseDocumentTransactionInfoEntity;
import com.modern.java.document.entity.DocumentEntity;
import com.modern.java.document.entity.UploadDocumentEntity;
import com.modern.java.document.exception.UploadCreditCardDocumentException;
import com.modern.java.document.model.UploadDocumentRequest;
import com.modern.java.document.repository.DocumentRepository;
import com.modern.java.document.repository.TransactionDocumentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static helper.DeepEqMatcher.deepEq;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {
    @InjectMocks
    @Spy
    private DocumentService documentService;
    @Mock
    private TransactionDocumentRepository transactionDocumentRepository;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private FtpServer ftpServer;
    @Mock
    private UnknownDependencyService unknownDependencyService;
    private final static Date MOCK_CURRENT_DATE = new Date();

    @Nested
    class UploadCreditDocumentTest {
        @Test
        void shouldCallUpdateTransactionDocumentAndCallSaveDocumentAndCallUploadFile() throws IOException {
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

            BDDMockito.given(documentService.getNow()).willReturn(MOCK_CURRENT_DATE);

            UploadDocumentEntity mockUploadDoc = new UploadDocumentEntity();
            mockUploadDoc.setDocTransactionId(1L);
            mockUploadDoc.setCaseNo("C000003");
            mockUploadDoc.setDocType("AGREEMENT");
            mockUploadDoc.setDocClass("LEGAL");
            mockUploadDoc.setDestinationCompanyCode("COMP001");
            mockUploadDoc.setDestinationOfficeCode("OFF001");
            mockUploadDoc.setTotalDocument(3);
            mockUploadDoc.setDocStatus("NEW");
            mockUploadDoc.setHireeNo("HIREE789");
            mockUploadDoc.setUpdatedDate(MOCK_CURRENT_DATE);
            mockUploadDoc.setUpdatedBy("user001");
            BDDMockito.willReturn(mockUploadDoc).given(documentService)
                    .createOrUpdateTransactionDocument(
                            mockUploadDocumentRequest,
                            "HIREE789",
                            "user001",
                            MOCK_CURRENT_DATE
                    );

            documentService.uploadCreditDocument(mockUploadDocumentRequest, mockFile, "HIREE789", "user001");

            DocumentEntity mockDocumentEntity = new DocumentEntity();
            mockDocumentEntity.setDocTransactionId(1L);
            mockDocumentEntity.setSeqNo(4);
            mockDocumentEntity.setDocPath("HIREE789/HIREE789_000_004");
            mockDocumentEntity.setDocName("HIREE789_000_004");
            mockDocumentEntity.setOriginalFileName("dump.jpg");
            mockDocumentEntity.setOriginalFileType("image/jpeg");
            mockDocumentEntity.setCreatedDate(MOCK_CURRENT_DATE);
            mockDocumentEntity.setCreatedBy("user001");
            verify(documentRepository).save(deepEq(mockDocumentEntity));

            verify(ftpServer).uploadFile("/HIREE789/HIREE789_000_004.jpg", "/HIREE789", mockFile);
        }

        @Test
        void shouldBeUploadCreditCardDocumentException_WithCallUpdateTransactionDocumentThrowsUploadCreditCardDocumentException() {
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

            BDDMockito.willThrow(UploadCreditCardDocumentException.class).given(documentService).createOrUpdateTransactionDocument(any(), anyString(), any(), any());

            Assertions.assertThrows(UploadCreditCardDocumentException.class,
                    () -> documentService.uploadCreditDocument(mockUploadDocumentRequest, mockFile, "HIRE789", "user001"));

            verify(documentRepository, never()).save(any());
            verify(ftpServer, never()).uploadFile(anyString(), anyString(), any());
        }

        @Test
        void shouldBeDataAccessResourceFailureException_WithCallSaveDocumentThrowsDataAccessResourceFailureException() {
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

            BDDMockito.given(documentService.getNow()).willReturn(MOCK_CURRENT_DATE);

            UploadDocumentEntity mockUploadDoc = new UploadDocumentEntity();
            mockUploadDoc.setDocTransactionId(1L);
            mockUploadDoc.setCaseNo("C000003");
            mockUploadDoc.setDocType("AGREEMENT");
            mockUploadDoc.setDocClass("LEGAL");
            mockUploadDoc.setDestinationCompanyCode("COMP001");
            mockUploadDoc.setDestinationOfficeCode("OFF001");
            mockUploadDoc.setTotalDocument(3);
            mockUploadDoc.setDocStatus("NEW");
            mockUploadDoc.setHireeNo("HIREE789");
            mockUploadDoc.setUpdatedDate(MOCK_CURRENT_DATE);
            mockUploadDoc.setUpdatedBy("user001");
            BDDMockito.willReturn(mockUploadDoc).given(documentService)
                    .createOrUpdateTransactionDocument(
                            mockUploadDocumentRequest,
                            "HIRE789",
                            "user001",
                            MOCK_CURRENT_DATE
                    );

            BDDMockito.willThrow(DataAccessResourceFailureException.class).given(documentRepository).save(any());

            Assertions.assertThrows(DataAccessResourceFailureException.class,
                    () -> documentService.uploadCreditDocument(mockUploadDocumentRequest, mockFile, "HIRE789", "user001"));

            verify(ftpServer, never()).uploadFile(anyString(), anyString(), any());
        }

        @Test
        void shouldThrowsUploadCreditCardDocumentException_WithCallUploadDocumentToSFTPThrowsUploadCreditCardDocumentException() {
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

            BDDMockito.given(documentService.getNow()).willReturn(MOCK_CURRENT_DATE);

            UploadDocumentEntity mockUploadDoc = new UploadDocumentEntity();
            mockUploadDoc.setDocTransactionId(1L);
            mockUploadDoc.setCaseNo("C000003");
            mockUploadDoc.setDocType("AGREEMENT");
            mockUploadDoc.setDocClass("LEGAL");
            mockUploadDoc.setDestinationCompanyCode("COMP001");
            mockUploadDoc.setDestinationOfficeCode("OFF001");
            mockUploadDoc.setTotalDocument(3);
            mockUploadDoc.setDocStatus("NEW");
            mockUploadDoc.setHireeNo("HIREE789");
            mockUploadDoc.setUpdatedDate(MOCK_CURRENT_DATE);
            mockUploadDoc.setUpdatedBy("user001");
            BDDMockito.willReturn(mockUploadDoc).given(documentService)
                    .createOrUpdateTransactionDocument(
                            mockUploadDocumentRequest,
                            "HIREE789",
                            "user001",
                            MOCK_CURRENT_DATE
                    );
            BDDMockito.given(ftpServer.uploadFile("/HIREE789/HIREE789_000_004.jpg", "/HIREE789", mockFile)).willThrow(UploadCreditCardDocumentException.class);

            Assertions.assertThrows(UploadCreditCardDocumentException.class,
                    () -> documentService.uploadCreditDocument(mockUploadDocumentRequest, mockFile, "HIREE789", "user001"));

            verify(documentRepository).save(any());
        }
    }

    @Nested
    class CreateOrUpdateTransactionDocument {
        @Test
        void shouldBeUploadDocumentEntity() throws ParseException {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

            UploadDocumentRequest mockUploadDocumentRequest = new UploadDocumentRequest();
            mockUploadDocumentRequest.setCaseNo("C000003");
            mockUploadDocumentRequest.setDocType("AGREEMENT");
            mockUploadDocumentRequest.setDocClass("LEGAL");
            mockUploadDocumentRequest.setDestinationCompanyCode("COMP001");
            mockUploadDocumentRequest.setDestinationOfficeCode("OFF001");
            mockUploadDocumentRequest.setTotalDocument(1);
            mockUploadDocumentRequest.setDocStatus("NEW");
            mockUploadDocumentRequest.setCreatedDate(formatter.parse("01-12-2020"));
            mockUploadDocumentRequest.setCreatedBy("user001");

            UploadDocumentEntity mockTransactionDocWithPendingStatus = new UploadDocumentEntity();
            mockTransactionDocWithPendingStatus.setCaseNo("C000001");
            mockTransactionDocWithPendingStatus.setDocType("AGREEMENT");
            mockTransactionDocWithPendingStatus.setDocClass("LEGAL");
            mockTransactionDocWithPendingStatus.setDestinationCompanyCode("COMP001");
            mockTransactionDocWithPendingStatus.setDestinationOfficeCode("OFF001");
            mockTransactionDocWithPendingStatus.setTotalDocument(2);
            mockTransactionDocWithPendingStatus.setDocStatus("PENDING");
            mockTransactionDocWithPendingStatus.setHireeNo("HIREE789");
            mockTransactionDocWithPendingStatus.setCreatedDate(formatter.parse("26-09-2019"));
            mockTransactionDocWithPendingStatus.setCreatedBy("user001");
            UploadDocumentEntity mockTransactionDocWithNewStatus = new UploadDocumentEntity();
            mockTransactionDocWithNewStatus.setCaseNo("C000002");
            mockTransactionDocWithNewStatus.setDocType("AGREEMENT");
            mockTransactionDocWithNewStatus.setDocClass("LEGAL");
            mockTransactionDocWithNewStatus.setDestinationCompanyCode("COMP001");
            mockTransactionDocWithNewStatus.setDestinationOfficeCode("OFF001");
            mockTransactionDocWithNewStatus.setTotalDocument(2);
            mockTransactionDocWithNewStatus.setDocStatus("NEW");
            mockTransactionDocWithNewStatus.setHireeNo("HIREE789");
            mockTransactionDocWithNewStatus.setCreatedDate(formatter.parse("26-09-2019"));
            mockTransactionDocWithNewStatus.setCreatedBy("user001");
            List<UploadDocumentEntity> mockTransactionDocuments = List.of(mockTransactionDocWithPendingStatus, mockTransactionDocWithNewStatus);
            BDDMockito.given(documentService.fetchTransactionDocumentsByCaseNo("C000003")).willReturn(mockTransactionDocuments);

            BDDMockito.given(documentService.getCurrentTransactionDocument(mockUploadDocumentRequest, mockTransactionDocuments)).willReturn(mockTransactionDocWithNewStatus);

            UploadDocumentEntity mockGenerateTransactionDoc = new UploadDocumentEntity();
            mockGenerateTransactionDoc.setCaseNo("C000003");
            mockGenerateTransactionDoc.setDocType("AGREEMENT");
            mockGenerateTransactionDoc.setDocClass("LEGAL");
            mockGenerateTransactionDoc.setDestinationCompanyCode("COMP001");
            mockGenerateTransactionDoc.setDestinationOfficeCode("OFF001");
            mockGenerateTransactionDoc.setTotalDocument(3);
            mockGenerateTransactionDoc.setDocStatus("NEW");
            mockGenerateTransactionDoc.setHireeNo("HIREE789");
            mockGenerateTransactionDoc.setUpdatedDate(MOCK_CURRENT_DATE);
            mockGenerateTransactionDoc.setUpdatedBy("user001");
            BDDMockito.given(documentService.resolveTransactionDocument(
                    mockUploadDocumentRequest,
                    mockTransactionDocWithNewStatus,
                    "HIRE789",
                    2,
                    "user001",
                    MOCK_CURRENT_DATE)).willReturn(mockGenerateTransactionDoc);

            UploadDocumentEntity actual = documentService.createOrUpdateTransactionDocument(mockUploadDocumentRequest, "HIRE789", "user001", MOCK_CURRENT_DATE);

            Assertions.assertEquals("C000003", actual.getCaseNo());
            Assertions.assertEquals("AGREEMENT", actual.getDocType());
            Assertions.assertEquals("LEGAL", actual.getDocClass());
            Assertions.assertEquals("COMP001", actual.getDestinationCompanyCode());
            Assertions.assertEquals("OFF001", actual.getDestinationOfficeCode());
            Assertions.assertEquals(2 + 1, actual.getTotalDocument());
            Assertions.assertEquals("NEW", actual.getDocStatus());
            Assertions.assertEquals("HIREE789", actual.getHireeNo());
            Assertions.assertEquals(MOCK_CURRENT_DATE, actual.getUpdatedDate());
            Assertions.assertEquals("user001", actual.getUpdatedBy());

            verify(transactionDocumentRepository).save(mockGenerateTransactionDoc);
        }

        @Test
        void shouldBeUploadCreditCardDocumentException_WithCallFetchTransactionDocumentsByCaseNoThrowsUploadCreditCardDocumentException() throws ParseException {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

            UploadDocumentRequest mockUploadDocumentRequest = new UploadDocumentRequest();
            mockUploadDocumentRequest.setCaseNo("C000003");
            mockUploadDocumentRequest.setDocType("AGREEMENT");
            mockUploadDocumentRequest.setDocClass("LEGAL");
            mockUploadDocumentRequest.setDestinationCompanyCode("COMP001");
            mockUploadDocumentRequest.setDestinationOfficeCode("OFF001");
            mockUploadDocumentRequest.setTotalDocument(1);
            mockUploadDocumentRequest.setDocStatus("PENDING");
            mockUploadDocumentRequest.setCreatedDate(formatter.parse("01-12-2020"));
            mockUploadDocumentRequest.setCreatedBy("user001");

            BDDMockito.given(documentService.fetchTransactionDocumentsByCaseNo("C000003")).willThrow(UploadCreditCardDocumentException.class);

            Assertions.assertThrows(UploadCreditCardDocumentException.class,
                    () -> documentService.createOrUpdateTransactionDocument(mockUploadDocumentRequest,
                            "HIRE789",
                            "user001",
                            MOCK_CURRENT_DATE));

            verify(transactionDocumentRepository, never()).save(any());
        }

        @Test
        void shouldBeRuntimeException_WithCallGenerateTransactionDocThrowsRuntimeException() throws ParseException {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

            UploadDocumentRequest mockUploadDocumentRequest = new UploadDocumentRequest();
            mockUploadDocumentRequest.setCaseNo("C000003");
            mockUploadDocumentRequest.setDocType("AGREEMENT");
            mockUploadDocumentRequest.setDocClass("LEGAL");
            mockUploadDocumentRequest.setDestinationCompanyCode("COMP001");
            mockUploadDocumentRequest.setDestinationOfficeCode("OFF001");
            mockUploadDocumentRequest.setTotalDocument(1);
            mockUploadDocumentRequest.setDocStatus("NEW");
            mockUploadDocumentRequest.setCreatedDate(formatter.parse("01-12-2020"));
            mockUploadDocumentRequest.setCreatedBy("user001");

            UploadDocumentEntity mockTransactionDocWithPendingStatus = new UploadDocumentEntity();
            mockTransactionDocWithPendingStatus.setCaseNo("C000001");
            mockTransactionDocWithPendingStatus.setDocType("AGREEMENT");
            mockTransactionDocWithPendingStatus.setDocClass("LEGAL");
            mockTransactionDocWithPendingStatus.setDestinationCompanyCode("COMP001");
            mockTransactionDocWithPendingStatus.setDestinationOfficeCode("OFF001");
            mockTransactionDocWithPendingStatus.setTotalDocument(2);
            mockTransactionDocWithPendingStatus.setDocStatus("PENDING");
            mockTransactionDocWithPendingStatus.setHireeNo("HIREE789");
            mockTransactionDocWithPendingStatus.setCreatedDate(formatter.parse("26-09-2019"));
            mockTransactionDocWithPendingStatus.setCreatedBy("user001");
            UploadDocumentEntity mockTransactionDocWithNewStatus = new UploadDocumentEntity();
            mockTransactionDocWithNewStatus.setCaseNo("C000002");
            mockTransactionDocWithNewStatus.setDocType("AGREEMENT");
            mockTransactionDocWithNewStatus.setDocClass("LEGAL");
            mockTransactionDocWithNewStatus.setDestinationCompanyCode("COMP001");
            mockTransactionDocWithNewStatus.setDestinationOfficeCode("OFF001");
            mockTransactionDocWithNewStatus.setTotalDocument(2);
            mockTransactionDocWithNewStatus.setDocStatus("NEW");
            mockTransactionDocWithNewStatus.setHireeNo("HIREE789");
            mockTransactionDocWithNewStatus.setCreatedDate(formatter.parse("26-09-2019"));
            mockTransactionDocWithNewStatus.setCreatedBy("user001");

            List<UploadDocumentEntity> mockTransactionDocuments = List.of(mockTransactionDocWithPendingStatus, mockTransactionDocWithNewStatus);

            BDDMockito.given(documentService.fetchTransactionDocumentsByCaseNo("C123456")).willReturn(List.of(mockTransactionDocWithPendingStatus, mockTransactionDocWithNewStatus));
            BDDMockito.given(documentService.getCurrentTransactionDocument(mockUploadDocumentRequest, mockTransactionDocuments)).willReturn(mockTransactionDocWithNewStatus);
            BDDMockito.given(documentService.resolveTransactionDocument(
                    mockUploadDocumentRequest,
                    mockTransactionDocWithNewStatus,
                    "HIREE789",
                    2,
                    "user001",
                    MOCK_CURRENT_DATE)).willThrow(RuntimeException.class);

            Assertions.assertThrows(RuntimeException.class,
                    () -> documentService.createOrUpdateTransactionDocument(mockUploadDocumentRequest,
                            "HIRE789",
                            "user001",
                            MOCK_CURRENT_DATE));

            verify(transactionDocumentRepository, never()).save(any());
        }

        @Test
        void shouldBeDataAccessResourceFailureException_WithCallSaveThrowsDataAccessResourceFailureException() throws ParseException {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

            UploadDocumentRequest mockUploadDocumentRequest = new UploadDocumentRequest();
            mockUploadDocumentRequest.setCaseNo("C000003");
            mockUploadDocumentRequest.setDocType("AGREEMENT");
            mockUploadDocumentRequest.setDocClass("LEGAL");
            mockUploadDocumentRequest.setDestinationCompanyCode("COMP001");
            mockUploadDocumentRequest.setDestinationOfficeCode("OFF001");
            mockUploadDocumentRequest.setTotalDocument(1);
            mockUploadDocumentRequest.setDocStatus("PENDING");
            mockUploadDocumentRequest.setCreatedDate(formatter.parse("01-12-2020"));
            mockUploadDocumentRequest.setCreatedBy("user001");

            UploadDocumentEntity mockTransactionDocWithPendingStatus = new UploadDocumentEntity();
            mockTransactionDocWithPendingStatus.setCaseNo("C000001");
            mockTransactionDocWithPendingStatus.setDocType("AGREEMENT");
            mockTransactionDocWithPendingStatus.setDocClass("LEGAL");
            mockTransactionDocWithPendingStatus.setDestinationCompanyCode("COMP001");
            mockTransactionDocWithPendingStatus.setDestinationOfficeCode("OFF001");
            mockTransactionDocWithPendingStatus.setTotalDocument(2);
            mockTransactionDocWithPendingStatus.setDocStatus("PENDING");
            mockTransactionDocWithPendingStatus.setHireeNo("HIREE789");
            mockTransactionDocWithPendingStatus.setCreatedDate(formatter.parse("26-09-2019"));
            mockTransactionDocWithPendingStatus.setCreatedBy("user001");

            UploadDocumentEntity mockTransactionDocWithNewStatus = new UploadDocumentEntity();
            mockTransactionDocWithNewStatus.setCaseNo("C000002");
            mockTransactionDocWithNewStatus.setDocType("AGREEMENT");
            mockTransactionDocWithNewStatus.setDocClass("LEGAL");
            mockTransactionDocWithNewStatus.setDestinationCompanyCode("COMP001");
            mockTransactionDocWithNewStatus.setDestinationOfficeCode("OFF001");
            mockTransactionDocWithNewStatus.setTotalDocument(2);
            mockTransactionDocWithNewStatus.setDocStatus("NEW");
            mockTransactionDocWithNewStatus.setHireeNo("HIREE789");
            mockTransactionDocWithNewStatus.setCreatedDate(formatter.parse("26-09-2019"));
            mockTransactionDocWithNewStatus.setCreatedBy("user001");
            List<UploadDocumentEntity> mockTransactionDocuments = List.of(mockTransactionDocWithPendingStatus, mockTransactionDocWithNewStatus);
            BDDMockito.given(documentService.fetchTransactionDocumentsByCaseNo("C000003")).willReturn(mockTransactionDocuments);

            BDDMockito.given(documentService.getCurrentTransactionDocument(mockUploadDocumentRequest, mockTransactionDocuments)).willReturn(mockTransactionDocWithNewStatus);

            UploadDocumentEntity mockGenerateTransactionDoc = new UploadDocumentEntity();
            mockGenerateTransactionDoc.setCaseNo("C000003");
            mockGenerateTransactionDoc.setDocType("AGREEMENT");
            mockGenerateTransactionDoc.setDocClass("LEGAL");
            mockGenerateTransactionDoc.setDestinationCompanyCode("COMP001");
            mockGenerateTransactionDoc.setDestinationOfficeCode("OFF001");
            mockGenerateTransactionDoc.setTotalDocument(3);
            mockGenerateTransactionDoc.setDocStatus("NEW");
            mockGenerateTransactionDoc.setHireeNo("HIREE789");
            mockGenerateTransactionDoc.setUpdatedDate(MOCK_CURRENT_DATE);
            mockGenerateTransactionDoc.setUpdatedBy("user001");
            BDDMockito.given(documentService.resolveTransactionDocument(
                    mockUploadDocumentRequest,
                    mockTransactionDocWithNewStatus,
                    "HIRE789",
                    2,
                    "user001",
                    MOCK_CURRENT_DATE)).willReturn(mockGenerateTransactionDoc);
            BDDMockito.willThrow(DataAccessResourceFailureException.class).given(transactionDocumentRepository).save(mockGenerateTransactionDoc);

            Assertions.assertThrows(DataAccessResourceFailureException.class,
                    () -> documentService.createOrUpdateTransactionDocument(mockUploadDocumentRequest,
                            "HIRE789",
                            "user001",
                            MOCK_CURRENT_DATE));
        }
    }

    @Nested
    class FetchTransactionDocumentsByCaseNoTest {
        @Test
        void shouldBeTransactionDocuments() throws ParseException {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            UploadDocumentEntity mockTransactionDocWithPendingStatus = new UploadDocumentEntity();
            mockTransactionDocWithPendingStatus.setCaseNo("C000001");
            mockTransactionDocWithPendingStatus.setDocType("AGREEMENT");
            mockTransactionDocWithPendingStatus.setDocClass("LEGAL");
            mockTransactionDocWithPendingStatus.setDestinationCompanyCode("COMP001");
            mockTransactionDocWithPendingStatus.setDestinationOfficeCode("OFF001");
            mockTransactionDocWithPendingStatus.setTotalDocument(2);
            mockTransactionDocWithPendingStatus.setDocStatus("PENDING");
            mockTransactionDocWithPendingStatus.setHireeNo("HIREE789");
            mockTransactionDocWithPendingStatus.setCreatedDate(formatter.parse("26-09-2019"));
            mockTransactionDocWithPendingStatus.setCreatedBy("user001");
            UploadDocumentEntity mockTransactionDocWithNewStatus = new UploadDocumentEntity();
            mockTransactionDocWithNewStatus.setCaseNo("C000002");
            mockTransactionDocWithNewStatus.setDocType("AGREEMENT");
            mockTransactionDocWithNewStatus.setDocClass("LEGAL");
            mockTransactionDocWithNewStatus.setDestinationCompanyCode("COMP001");
            mockTransactionDocWithNewStatus.setDestinationOfficeCode("OFF001");
            mockTransactionDocWithNewStatus.setTotalDocument(2);
            mockTransactionDocWithNewStatus.setDocStatus("NEW");
            mockTransactionDocWithNewStatus.setHireeNo("HIREE789");
            mockTransactionDocWithNewStatus.setCreatedDate(formatter.parse("26-09-2019"));
            mockTransactionDocWithNewStatus.setCreatedBy("user001");
            List<UploadDocumentEntity> mockTransactionDocuments = List.of(mockTransactionDocWithPendingStatus, mockTransactionDocWithNewStatus);

            BDDMockito.given(transactionDocumentRepository.findByCaseNo("C000002")).willReturn(mockTransactionDocuments);

            Assertions.assertEquals(mockTransactionDocuments, documentService.fetchTransactionDocumentsByCaseNo("C000002"));
        }

        @Test
        void shouldBeUploadCreditCardDocumentException_WithCallFindByCaseNoNotFoundData() {
            BDDMockito.given(transactionDocumentRepository.findByCaseNo("C000002")).willReturn(null);

            Assertions.assertThrows(UploadCreditCardDocumentException.class, () -> documentService.fetchTransactionDocumentsByCaseNo("C000002"));
        }

        @Test
        void shouldBeDataAccessResourceFailureException_WithCallFindByCaseNoThrowsDataAccessResourceFailureException() {
            BDDMockito.given(transactionDocumentRepository.findByCaseNo("C000002")).willThrow(DataAccessResourceFailureException.class);

            Assertions.assertThrows(DataAccessResourceFailureException.class, () -> documentService.fetchTransactionDocumentsByCaseNo("C000002"));
        }
    }

    @Nested
    class GetCurrentTransactionDocument {
        @Test
        void shouldBeCurrentTransactionDocument() throws ParseException {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            UploadDocumentEntity mockTransactionDocWithPendingStatus = new UploadDocumentEntity();
            mockTransactionDocWithPendingStatus.setCaseNo("C000001");
            mockTransactionDocWithPendingStatus.setDocType("AGREEMENT");
            mockTransactionDocWithPendingStatus.setDocClass("LEGAL");
            mockTransactionDocWithPendingStatus.setDestinationCompanyCode("COMP001");
            mockTransactionDocWithPendingStatus.setDestinationOfficeCode("OFF001");
            mockTransactionDocWithPendingStatus.setTotalDocument(2);
            mockTransactionDocWithPendingStatus.setDocStatus("PENDING");
            mockTransactionDocWithPendingStatus.setHireeNo("HIREE789");
            mockTransactionDocWithPendingStatus.setCreatedDate(formatter.parse("26-09-2019"));
            mockTransactionDocWithPendingStatus.setCreatedBy("user001");
            UploadDocumentEntity mockTransactionDocWithNewStatus = new UploadDocumentEntity();
            mockTransactionDocWithNewStatus.setCaseNo("C000002");
            mockTransactionDocWithNewStatus.setDocType("AGREEMENT");
            mockTransactionDocWithNewStatus.setDocClass("LEGAL");
            mockTransactionDocWithNewStatus.setDestinationCompanyCode("COMP001");
            mockTransactionDocWithNewStatus.setDestinationOfficeCode("OFF001");
            mockTransactionDocWithNewStatus.setTotalDocument(2);
            mockTransactionDocWithNewStatus.setDocStatus("NEW");
            mockTransactionDocWithNewStatus.setHireeNo("HIREE789");
            mockTransactionDocWithNewStatus.setCreatedDate(formatter.parse("26-09-2019"));
            mockTransactionDocWithNewStatus.setCreatedBy("user001");
            List<UploadDocumentEntity> mockTransactionDocuments = List.of(mockTransactionDocWithPendingStatus, mockTransactionDocWithNewStatus);

            UploadDocumentRequest mockUploadDocumentRequest = new UploadDocumentRequest();
            mockUploadDocumentRequest.setCaseNo("C123456");
            mockUploadDocumentRequest.setDocType("AGREEMENT");
            mockUploadDocumentRequest.setDocClass("LEGAL");
            mockUploadDocumentRequest.setDestinationCompanyCode("COMP001");
            mockUploadDocumentRequest.setDestinationOfficeCode("OFF001");
            mockUploadDocumentRequest.setTotalDocument(1);
            mockUploadDocumentRequest.setDocStatus("NEW");
            mockUploadDocumentRequest.setCreatedDate(new Date());
            mockUploadDocumentRequest.setCreatedBy("user001");
            Assertions.assertEquals(mockTransactionDocWithNewStatus, documentService.getCurrentTransactionDocument(mockUploadDocumentRequest, mockTransactionDocuments));
        }

        @Test
        void shouldBeNull_WithDocTypeDoesNotMatch() throws ParseException {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            UploadDocumentEntity mockTransactionDocWithPendingStatus = new UploadDocumentEntity();
            mockTransactionDocWithPendingStatus.setCaseNo("C000001");
            mockTransactionDocWithPendingStatus.setDocType("AGREEMENT");
            mockTransactionDocWithPendingStatus.setDocClass("LEGAL");
            mockTransactionDocWithPendingStatus.setDestinationCompanyCode("COMP001");
            mockTransactionDocWithPendingStatus.setDestinationOfficeCode("OFF001");
            mockTransactionDocWithPendingStatus.setTotalDocument(2);
            mockTransactionDocWithPendingStatus.setDocStatus("PENDING");
            mockTransactionDocWithPendingStatus.setHireeNo("HIREE789");
            mockTransactionDocWithPendingStatus.setCreatedDate(formatter.parse("26-09-2019"));
            mockTransactionDocWithPendingStatus.setCreatedBy("user001");
            UploadDocumentEntity mockTransactionDocWithNewStatus = new UploadDocumentEntity();
            mockTransactionDocWithNewStatus.setCaseNo("C000002");
            mockTransactionDocWithNewStatus.setDocType("AGREEMENT");
            mockTransactionDocWithNewStatus.setDocClass("LEGAL");
            mockTransactionDocWithNewStatus.setDestinationCompanyCode("COMP001");
            mockTransactionDocWithNewStatus.setDestinationOfficeCode("OFF001");
            mockTransactionDocWithNewStatus.setTotalDocument(2);
            mockTransactionDocWithNewStatus.setDocStatus("PENDING");
            mockTransactionDocWithNewStatus.setHireeNo("HIREE789");
            mockTransactionDocWithNewStatus.setCreatedDate(formatter.parse("26-09-2019"));
            mockTransactionDocWithNewStatus.setCreatedBy("user001");
            List<UploadDocumentEntity> mockTransactionDocuments = List.of(mockTransactionDocWithPendingStatus, mockTransactionDocWithNewStatus);

            UploadDocumentRequest mockUploadDocumentRequest = new UploadDocumentRequest();
            mockUploadDocumentRequest.setCaseNo("C123456");
            mockUploadDocumentRequest.setDocType("AGREEMENT");
            mockUploadDocumentRequest.setDocClass("LEGAL");
            mockUploadDocumentRequest.setDestinationCompanyCode("COMP001");
            mockUploadDocumentRequest.setDestinationOfficeCode("OFF001");
            mockUploadDocumentRequest.setTotalDocument(1);
            mockUploadDocumentRequest.setDocStatus("NEW");
            mockUploadDocumentRequest.setCreatedDate(new Date());
            mockUploadDocumentRequest.setCreatedBy("user001");
            Assertions.assertNull(documentService.getCurrentTransactionDocument(mockUploadDocumentRequest, mockTransactionDocuments));
        }

        @Test
        void shouldBeNull_WithDocClassDoesNotMatch() throws ParseException {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            UploadDocumentEntity mockTransactionDocWithPendingStatus = new UploadDocumentEntity();
            mockTransactionDocWithPendingStatus.setCaseNo("C000001");
            mockTransactionDocWithPendingStatus.setDocType("AGREEMENT");
            mockTransactionDocWithPendingStatus.setDocClass("ANOTHER_CLASS");
            mockTransactionDocWithPendingStatus.setDestinationCompanyCode("COMP001");
            mockTransactionDocWithPendingStatus.setDestinationOfficeCode("OFF001");
            mockTransactionDocWithPendingStatus.setTotalDocument(2);
            mockTransactionDocWithPendingStatus.setDocStatus("PENDING");
            mockTransactionDocWithPendingStatus.setHireeNo("HIREE789");
            mockTransactionDocWithPendingStatus.setCreatedDate(formatter.parse("26-09-2019"));
            mockTransactionDocWithPendingStatus.setCreatedBy("user001");
            UploadDocumentEntity mockTransactionDocWithNewStatus = new UploadDocumentEntity();
            mockTransactionDocWithNewStatus.setCaseNo("C000002");
            mockTransactionDocWithNewStatus.setDocType("AGREEMENT");
            mockTransactionDocWithNewStatus.setDocClass("ANOTHER_CLASS");
            mockTransactionDocWithNewStatus.setDestinationCompanyCode("COMP001");
            mockTransactionDocWithNewStatus.setDestinationOfficeCode("OFF001");
            mockTransactionDocWithNewStatus.setTotalDocument(2);
            mockTransactionDocWithNewStatus.setDocStatus("NEW");
            mockTransactionDocWithNewStatus.setHireeNo("HIREE789");
            mockTransactionDocWithNewStatus.setCreatedDate(formatter.parse("26-09-2019"));
            mockTransactionDocWithNewStatus.setCreatedBy("user001");
            List<UploadDocumentEntity> mockTransactionDocuments = List.of(mockTransactionDocWithPendingStatus, mockTransactionDocWithNewStatus);

            UploadDocumentRequest mockUploadDocumentRequest = new UploadDocumentRequest();
            mockUploadDocumentRequest.setCaseNo("C123456");
            mockUploadDocumentRequest.setDocType("AGREEMENT");
            mockUploadDocumentRequest.setDocClass("LEGAL");
            mockUploadDocumentRequest.setDestinationCompanyCode("COMP001");
            mockUploadDocumentRequest.setDestinationOfficeCode("OFF001");
            mockUploadDocumentRequest.setTotalDocument(1);
            mockUploadDocumentRequest.setDocStatus("NEW");
            mockUploadDocumentRequest.setCreatedDate(new Date());
            mockUploadDocumentRequest.setCreatedBy("user001");
            Assertions.assertNull(documentService.getCurrentTransactionDocument(mockUploadDocumentRequest, mockTransactionDocuments));
        }

        @Test
        void shouldBeNull_WithDocStatusDoesNotMatch() throws ParseException {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            UploadDocumentEntity mockTransactionDocWithPendingStatus = new UploadDocumentEntity();
            mockTransactionDocWithPendingStatus.setCaseNo("C000001");
            mockTransactionDocWithPendingStatus.setDocType("AGREEMENT");
            mockTransactionDocWithPendingStatus.setDocClass("LEGAL");
            mockTransactionDocWithPendingStatus.setDestinationCompanyCode("COMP001");
            mockTransactionDocWithPendingStatus.setDestinationOfficeCode("OFF001");
            mockTransactionDocWithPendingStatus.setTotalDocument(2);
            mockTransactionDocWithPendingStatus.setDocStatus("PENDING");
            mockTransactionDocWithPendingStatus.setHireeNo("HIREE789");
            mockTransactionDocWithPendingStatus.setCreatedDate(formatter.parse("26-09-2019"));
            mockTransactionDocWithPendingStatus.setCreatedBy("user001");
            List<UploadDocumentEntity> mockTransactionDocuments = List.of(mockTransactionDocWithPendingStatus);

            UploadDocumentRequest mockUploadDocumentRequest = new UploadDocumentRequest();
            mockUploadDocumentRequest.setCaseNo("C123456");
            mockUploadDocumentRequest.setDocType("AGREEMENT");
            mockUploadDocumentRequest.setDocClass("LEGAL");
            mockUploadDocumentRequest.setDestinationCompanyCode("COMP001");
            mockUploadDocumentRequest.setDestinationOfficeCode("OFF001");
            mockUploadDocumentRequest.setTotalDocument(1);
            mockUploadDocumentRequest.setDocStatus("NEW");
            mockUploadDocumentRequest.setCreatedDate(new Date());
            mockUploadDocumentRequest.setCreatedBy("user001");
            Assertions.assertNull(documentService.getCurrentTransactionDocument(mockUploadDocumentRequest, mockTransactionDocuments));
        }
    }

    @Nested
    class ResolveTransactionDocument {
        @Test
        void shouldBeTransactionDocument_WithCurrentTransactionExists() {
            UploadDocumentRequest mockUploadDocumentRequest = new UploadDocumentRequest();
            mockUploadDocumentRequest.setCaseNo("C000001");
            mockUploadDocumentRequest.setDocType("AGREEMENT");
            mockUploadDocumentRequest.setDocClass("LEGAL");
            mockUploadDocumentRequest.setDestinationCompanyCode("COMP002");
            mockUploadDocumentRequest.setDestinationOfficeCode("OFF005");
            mockUploadDocumentRequest.setTotalDocument(1);
            mockUploadDocumentRequest.setDocStatus("NEW");
            mockUploadDocumentRequest.setCreatedDate(MOCK_CURRENT_DATE);
            mockUploadDocumentRequest.setCreatedBy("user002");

            UploadDocumentEntity currentTransactionDocument = new UploadDocumentEntity();
            currentTransactionDocument.setCaseNo("C000003");
            currentTransactionDocument.setDocType("AGREEMENT");
            currentTransactionDocument.setDocClass("LEGAL");
            currentTransactionDocument.setDestinationCompanyCode("COMP001");
            currentTransactionDocument.setDestinationOfficeCode("OFF001");
            currentTransactionDocument.setTotalDocument(2);
            currentTransactionDocument.setDocStatus("NEW");
            currentTransactionDocument.setHireeNo("HIREE789");
            currentTransactionDocument.setUpdatedDate(MOCK_CURRENT_DATE);
            currentTransactionDocument.setUpdatedBy("user001");

            UploadDocumentEntity expectedTransactionDocument = new UploadDocumentEntity();
            expectedTransactionDocument.setCaseNo("C000003");
            expectedTransactionDocument.setDocType("AGREEMENT");
            expectedTransactionDocument.setDocClass("LEGAL");
            expectedTransactionDocument.setDestinationCompanyCode("COMP001");
            expectedTransactionDocument.setDestinationOfficeCode("OFF001");
            expectedTransactionDocument.setTotalDocument(3);
            expectedTransactionDocument.setDocStatus("NEW");
            expectedTransactionDocument.setHireeNo("HIREE789");
            expectedTransactionDocument.setUpdatedDate(MOCK_CURRENT_DATE);
            expectedTransactionDocument.setUpdatedBy("user001");

            assertThat(documentService.resolveTransactionDocument(
                    mockUploadDocumentRequest,
                    currentTransactionDocument,
                    "HIREE789",
                    3,
                    "user001",
                    MOCK_CURRENT_DATE)).usingRecursiveComparison().isEqualTo(expectedTransactionDocument);
        }

        @Test
        void shouldBeTransactionDocumentFromUploadDocumentRequest_WithCurrentTransactionDoesNotExists() {
            UploadDocumentRequest mockUploadDocumentRequest = new UploadDocumentRequest();
            mockUploadDocumentRequest.setCaseNo("C000001");
            mockUploadDocumentRequest.setDocType("AGREEMENT");
            mockUploadDocumentRequest.setDocClass("LEGAL");
            mockUploadDocumentRequest.setDestinationCompanyCode("COMP001");
            mockUploadDocumentRequest.setDestinationOfficeCode("OFF001");
            mockUploadDocumentRequest.setTotalDocument(1);
            mockUploadDocumentRequest.setDocStatus("NEW");
            mockUploadDocumentRequest.setCreatedDate(MOCK_CURRENT_DATE);
            mockUploadDocumentRequest.setCreatedBy("user0001");
            mockUploadDocumentRequest.setLocationBranchTeamId("LocationBranchTeam1234");

            BDDMockito.given(unknownDependencyService.getDocTypeCode("AGREEMENT")).willReturn("AGREEMENT1234567");
            BDDMockito.given(unknownDependencyService.getDocClassCode("LEGAL")).willReturn("LEGAL1234567");
            BranchEntity branchEntity = new BranchEntity();
            branchEntity.setBranchCode("BranchCode001");
            branchEntity.setCompanyCode("CompanyCode005");
            BDDMockito.given(unknownDependencyService.getBranchById("LocationBranchTeam1234")).willReturn(branchEntity);

            CaseDocumentTransactionInfoEntity caseDocumentTransactionInfoEntity = new CaseDocumentTransactionInfoEntity();
            caseDocumentTransactionInfoEntity.setServiceCode("Service1234");
            caseDocumentTransactionInfoEntity.setBranchId("Branch1234");
            caseDocumentTransactionInfoEntity.setBuCode("Bu123434");
            caseDocumentTransactionInfoEntity.setCampaignCode("CampaignCode1234");
            caseDocumentTransactionInfoEntity.setMktCode("Mkt1235");
            BDDMockito.given(unknownDependencyService.getCaseDocTransInfo("C000001")).willReturn(caseDocumentTransactionInfoEntity);

            UploadDocumentEntity expectedTransactionDocument = new UploadDocumentEntity();
            expectedTransactionDocument.setCaseNo("C000001");
            expectedTransactionDocument.setDocType("AGREEMENT1234567");
            expectedTransactionDocument.setDocClass("LEGAL1234567");
            expectedTransactionDocument.setDestinationCompanyCode("CompanyCode005");
            expectedTransactionDocument.setDestinationOfficeCode("BranchCode001");
            expectedTransactionDocument.setTotalDocument(1);
            expectedTransactionDocument.setDocStatus("NEW");
            expectedTransactionDocument.setHireeNo("HIREE789");
            expectedTransactionDocument.setCreatedDate(MOCK_CURRENT_DATE);
            expectedTransactionDocument.setCreatedBy("user001");
            expectedTransactionDocument.setDocTransactionSeqNo(1 + 3);
            expectedTransactionDocument.setServiceCode("Service1234");
            expectedTransactionDocument.setBranchId("Branch1234");
            expectedTransactionDocument.setBuCode("Bu123434");
            expectedTransactionDocument.setCampaignCode("CampaignCode1234");
            expectedTransactionDocument.setMktCode("Mkt1235");
            assertThat(documentService.resolveTransactionDocument(
                    mockUploadDocumentRequest,
                    null,
                    "HIREE789",
                    3,
                    "user001",
                    MOCK_CURRENT_DATE)).usingRecursiveComparison().isEqualTo(expectedTransactionDocument);
        }
    }

    @Nested
    class GenerateDocTransactionSeqNo {
        @Test
        void shouldBeOne_WithSizeOfDocumentIsZero() {
            Assertions.assertEquals(1, documentService.generateDocTransactionSeqNo(0));
        }

        @Test
        void shouldBeTwo_WithSizeOfDocumentIsOne() {
            Assertions.assertEquals(2, documentService.generateDocTransactionSeqNo(1));
        }
    }
}