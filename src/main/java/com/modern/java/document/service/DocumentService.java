package com.modern.java.document.service;

import com.modern.java.document.entity.BranchEntity;
import com.modern.java.document.entity.CaseDocumentTransactionInfoEntity;
import com.modern.java.document.entity.DocumentEntity;
import com.modern.java.document.entity.UploadDocumentEntity;
import com.modern.java.document.exception.UploadCreditCardDocumentException;
import com.modern.java.document.model.DocumentUploadContext;
import com.modern.java.document.model.UploadDocumentRequest;
import com.modern.java.document.repository.DocumentRepository;
import com.modern.java.document.repository.TransactionDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Service
public class DocumentService {
    private final TransactionDocumentRepository transactionDocumentRepository;
    private final DocumentRepository documentRepository;
    private final FtpServer ftpServer;
    private final UnknownDependencyService unknownDependencyService;

    public DocumentService(TransactionDocumentRepository transactionDocumentRepository,
                           DocumentRepository documentRepository,
                           FtpServer ftpServer, UnknownDependencyService unknownDependencyService) {
        this.transactionDocumentRepository = transactionDocumentRepository;
        this.documentRepository = documentRepository;
        this.ftpServer = ftpServer;
        this.unknownDependencyService = unknownDependencyService;
    }

    public void uploadCreditDocument(UploadDocumentRequest uploadDocumentRequest, MultipartFile file, String hireeNo, String requestBy) {
        Date now = getNow();
        UploadDocumentEntity createOrUpdateTranDoc = this.createOrUpdateTransactionDocument(
                uploadDocumentRequest,
                hireeNo,
                requestBy,
                now);

        int seqNo = createOrUpdateTranDoc.getTotalDocument() + 1;
        DocumentUploadContext documentUploadContext = new DocumentUploadContext(
                file,
                "",
                hireeNo,
                createOrUpdateTranDoc.getDocTransactionSeqNo(),
                seqNo,
                ".jpg");
        documentRepository.save(this.createDocumentFileRecord(
                createOrUpdateTranDoc.getDocTransactionId(),
                seqNo,
                documentUploadContext,
                now,
                requestBy));

        ftpServer.uploadFile(
                documentUploadContext.getRemoteFilePath(),
                documentUploadContext.getHireeDocumentPath(),
                file);
    }

    Date getNow() {
        return new Date();
    }

    UploadDocumentEntity createOrUpdateTransactionDocument(UploadDocumentRequest uploadDocumentRequest, String hireeNo, String requestBy, Date date) {
        List<UploadDocumentEntity> transactionDocuments = this.fetchTransactionDocumentsByCaseNo(uploadDocumentRequest.getCaseNo());
        UploadDocumentEntity currentTransactionDocument = this.getCurrentTransactionDocument(uploadDocumentRequest, transactionDocuments);
        UploadDocumentEntity uploadTransactionDocumentRecord = this.resolveTransactionDocument(
                uploadDocumentRequest,
                currentTransactionDocument,
                hireeNo,
                transactionDocuments.size(),
                requestBy,
                date);
        transactionDocumentRepository.save(uploadTransactionDocumentRecord);
        return uploadTransactionDocumentRecord;
    }

    UploadDocumentEntity getCurrentTransactionDocument(UploadDocumentRequest uploadDocumentRequest, List<UploadDocumentEntity> documents) {
        return documents.stream().filter(document ->
                document.getDocType().equals(uploadDocumentRequest.getDocType())
                        && document.getDocClass().equals(uploadDocumentRequest.getDocClass())
                        && document.getDocStatus().equals("NEW")).findAny().orElse(null);
    }

    List<UploadDocumentEntity> fetchTransactionDocumentsByCaseNo(String caseNo) {
        List<UploadDocumentEntity> documents = transactionDocumentRepository.findByCaseNo(caseNo);
        if (documents == null) {
            throw new UploadCreditCardDocumentException("findByCaseNo not found.");
        }
        return documents;
    }

    UploadDocumentEntity resolveTransactionDocument(UploadDocumentRequest uploadDocumentRequest, UploadDocumentEntity currentDocument, String hireeNo, int sizeOfDocument, String reqBy, Date date) {
        return currentDocument != null
                ? createTransactionDocumentFromRequest(currentDocument, hireeNo, reqBy, date)
                : createNextTransactionDocument(uploadDocumentRequest, hireeNo, sizeOfDocument, reqBy, date);
    }

    private UploadDocumentEntity createNextTransactionDocument(UploadDocumentRequest documentRequest, String hireeNo, int sizeOfDocument, String reqBy, Date date) {
        String caseNo = documentRequest.getCaseNo();
        UploadDocumentEntity document = new UploadDocumentEntity();
        document.setCaseNo(caseNo);
        document.setDocType(
                unknownDependencyService
                        .getDocTypeCode(documentRequest.getDocType()));
        document.setDocClass(
                unknownDependencyService
                        .getDocClassCode(documentRequest.getDocClass()));
        BranchEntity branch = unknownDependencyService.getBranchById(documentRequest.getLocationBranchTeamId());
        document.setDestinationCompanyCode(branch.getCompanyCode());
        document.setDestinationOfficeCode(branch.getBranchCode());
        document.setTotalDocument(1);
        document.setDocStatus("NEW");
        document.setHireeNo(hireeNo);
        document.setCreatedDate(date);
        document.setCreatedBy(reqBy);
        document.setDocTransactionSeqNo(this.generateDocTransactionSeqNo(sizeOfDocument));
        CaseDocumentTransactionInfoEntity caseDocTransInfo = unknownDependencyService.getCaseDocTransInfo(caseNo);
        document.setCampaignCode(caseDocTransInfo.getCampaignCode());
        document.setServiceCode(caseDocTransInfo.getServiceCode());
        document.setBuCode(caseDocTransInfo.getBuCode());
        document.setMktCode(caseDocTransInfo.getMktCode());
        document.setBranchId(caseDocTransInfo.getBranchId());
        return document;
    }

    private UploadDocumentEntity createTransactionDocumentFromRequest(UploadDocumentEntity currentDocument, String hireeNo, String reqBy, Date date) {
        UploadDocumentEntity document = new UploadDocumentEntity();
        document.setDocTransactionId(currentDocument.getDocTransactionId());
        document.setCaseNo(currentDocument.getCaseNo());
        document.setDocType(currentDocument.getDocType());
        document.setDocClass(currentDocument.getDocClass());
        document.setDestinationCompanyCode(currentDocument.getDestinationCompanyCode());
        document.setDestinationOfficeCode(currentDocument.getDestinationOfficeCode());
        document.setTotalDocument(currentDocument.getTotalDocument() + 1);
        document.setDocStatus("NEW");
        document.setHireeNo(hireeNo);
        document.setUpdatedDate(date);
        document.setUpdatedBy(reqBy);
        return document;
    }

    int generateDocTransactionSeqNo(int sizeOfDocument) {
        return sizeOfDocument != 0 ? sizeOfDocument + 1 : 1;
    }

    private DocumentEntity createDocumentFileRecord(long docTransactionId, int seqNo, DocumentUploadContext document, Date currentDate, String createdBy) {
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
