package com.modern.java.document.service;

import com.modern.java.document.entity.BranchEntity;
import com.modern.java.document.entity.CaseDocumentTransactionInfoEntity;
import com.modern.java.document.entity.DocumentEntity;
import com.modern.java.document.entity.UploadDocumentEntity;
import com.modern.java.document.model.DocumentUploadContext;
import com.modern.java.document.model.UploadDocumentRequest;
import com.modern.java.document.repository.DocumentRepository;
import com.modern.java.document.repository.TransactionDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class DocumentService {
    private final TransactionDocumentRepository transactionDocumentRepository;
    private final DocumentRepository documentRepository;
    private final FtpServer ftpServer;
    private  final UnknownDependencyService unknownDependencyService;

    public DocumentService(TransactionDocumentRepository transactionDocumentRepository,
                           DocumentRepository documentRepository,
                           FtpServer ftpServer, UnknownDependencyService unknownDependencyService) {
        this.transactionDocumentRepository = transactionDocumentRepository;
        this.documentRepository = documentRepository;
        this.ftpServer = ftpServer;
        this.unknownDependencyService = unknownDependencyService;
    }

    public void uploadCreditDocument(UploadDocumentRequest uploadDocumentRequest, MultipartFile file, String requestBy) throws IOException {
        String jpgFileFormat = this.getJpgFormatOrThrow(file.getContentType());
        String hireeNo = unknownDependencyService.getHireeNoOrThrow(uploadDocumentRequest.getCaseNo());
        Date currentDate = getCurrentDate();
        UploadDocumentEntity uploadDocument = this.updateTransactionDocument(
                uploadDocumentRequest,
                hireeNo,
                requestBy,
                currentDate);

        int seqNo = uploadDocument.getTotalDocument() + 1;
        DocumentUploadContext documentUploadContext = new DocumentUploadContext(
                file,
                "",
                hireeNo,
                uploadDocument.getDocTransactionSeqNo(),
                seqNo,
                jpgFileFormat);
        documentRepository.save(this.createDocumentEntity(
                uploadDocument.getDocTransactionId(),
                seqNo,
                documentUploadContext,
                currentDate,
                requestBy));

        ftpServer.uploadFile(
                documentUploadContext.getRemoteFilePath(),
                documentUploadContext.getHireeDocumentPath(),
                file);
    }

    String getJpgFormatOrThrow(String contentType) {
        if (Objects.equals(contentType, "image/jpeg")) {
            return ".jpg";
        } else {
            throw new RuntimeException("isn't JPEG file!");
        }
    }

    Date getCurrentDate() {
        return new Date();
    }

    UploadDocumentEntity updateTransactionDocument(UploadDocumentRequest documentRequest, String hireeNo, String requestBy, Date date) {
        List<UploadDocumentEntity> transactionDocuments = this.fetchTransactionDocumentsByCaseNo(documentRequest.getCaseNo());
        UploadDocumentEntity currentTransactionDocument = this.getCurrentTransactionDocument(documentRequest, transactionDocuments);
        UploadDocumentEntity uploadDocumentRequest = this.generateTransactionDocument(
                documentRequest,
                currentTransactionDocument,
                hireeNo,
                transactionDocuments.size(),
                requestBy,
                date);
        transactionDocumentRepository.save(uploadDocumentRequest);
        return uploadDocumentRequest;
    }

    UploadDocumentEntity getCurrentTransactionDocument(UploadDocumentRequest documentRequest, List<UploadDocumentEntity> documents) {
        return documents.stream().filter(document ->
                document.getDocType().equals(documentRequest.getDocType())
                        && document.getDocClass().equals(documentRequest.getDocClass())
                        && document.getDocStatus().equals("NEW")).findAny().orElse(null);
    }

    List<UploadDocumentEntity> fetchTransactionDocumentsByCaseNo(String caseNo) {
        List<UploadDocumentEntity> transactionDocuments = transactionDocumentRepository.findByCaseNo(caseNo);
        if (transactionDocuments == null) {
            throw new RuntimeException("findByCaseNo not found.");
        }
        return transactionDocuments;
    }

    UploadDocumentEntity generateTransactionDocument(UploadDocumentRequest documentRequest, UploadDocumentEntity currentDocument, String hireeNo, int sizeOfDocument, String reqBy, Date date) {
        return currentDocument != null
                ? generateUploadDocumentWithCurrentDocument(currentDocument, hireeNo, reqBy, date)
                : generateUploadDocumentWithDocumentRequest(documentRequest, hireeNo, sizeOfDocument, reqBy, date);
    }

    private UploadDocumentEntity generateUploadDocumentWithDocumentRequest(UploadDocumentRequest documentRequest, String hireeNo, int sizeOfDocument, String reqBy, Date date) {
        String caseNo = documentRequest.getCaseNo();
        UploadDocumentEntity uploadDocument = new UploadDocumentEntity();
        uploadDocument.setCaseNo(caseNo);
        uploadDocument.setDocType(
                unknownDependencyService
                        .getDocTypeCode(documentRequest.getDocType()));
        uploadDocument.setDocClass(
                unknownDependencyService
                        .getDocClassCode(documentRequest.getDocClass()));
        BranchEntity branch = unknownDependencyService.getBranchById(documentRequest.getLocationBranchTeamId());
        uploadDocument.setDestinationCompanyCode(branch.getCompanyCode());
        uploadDocument.setDestinationOfficeCode(branch.getBranchCode());
        uploadDocument.setTotalDocument(1);
        uploadDocument.setDocStatus("NEW");
        uploadDocument.setHireeNo(hireeNo);
        uploadDocument.setCreatedDate(date);
        uploadDocument.setCreatedBy(reqBy);
        uploadDocument.setDocTransactionSeqNo(this.generateDocTransactionSeqNo(sizeOfDocument));
        CaseDocumentTransactionInfoEntity caseDocTransInfo = unknownDependencyService.getCaseDocTransInfo(caseNo);
        uploadDocument.setCampaignCode(caseDocTransInfo.getCampaignCode());
        uploadDocument.setServiceCode(caseDocTransInfo.getServiceCode());
        uploadDocument.setBuCode(caseDocTransInfo.getBuCode());
        uploadDocument.setMktCode(caseDocTransInfo.getMktCode());
        uploadDocument.setBranchId(caseDocTransInfo.getBranchId());
        return uploadDocument;
    }

    private UploadDocumentEntity generateUploadDocumentWithCurrentDocument(UploadDocumentEntity currentDocument, String hireeNo, String reqBy, Date date) {
        UploadDocumentEntity uploadDocument = new UploadDocumentEntity();
        uploadDocument.setDocTransactionId(currentDocument.getDocTransactionId());
        uploadDocument.setCaseNo(currentDocument.getCaseNo());
        uploadDocument.setDocType(currentDocument.getDocType());
        uploadDocument.setDocClass(currentDocument.getDocClass());
        uploadDocument.setDestinationCompanyCode(currentDocument.getDestinationCompanyCode());
        uploadDocument.setDestinationOfficeCode(currentDocument.getDestinationOfficeCode());
        uploadDocument.setTotalDocument(currentDocument.getTotalDocument() + 1);
        uploadDocument.setDocStatus("NEW");
        uploadDocument.setHireeNo(hireeNo);
        uploadDocument.setUpdatedDate(date);
        uploadDocument.setUpdatedBy(reqBy);
        return uploadDocument;
    }

    int generateDocTransactionSeqNo(int sizeOfDocument) {
        return sizeOfDocument != 0 ? sizeOfDocument + 1 : 1;
    }

    private DocumentEntity createDocumentEntity(long docTransactionId, int seqNo, DocumentUploadContext document, Date currentDate, String createdBy) {
        DocumentEntity documentEntity = new DocumentEntity();
        documentEntity.setDocTransactionId(docTransactionId);
        documentEntity.setSeqNo(seqNo);
        documentEntity.setDocPath(document.getCpoDocumentPath());
        documentEntity.setDocName(document.getDocumentName());
        documentEntity.setOriginalFileName(document.getFile().getOriginalFilename());
        documentEntity.setOriginalFileType(document.getFile().getContentType());
        documentEntity.setCreatedDate(currentDate);
        documentEntity.setCreatedBy(createdBy);
        return documentEntity;
    }
}
