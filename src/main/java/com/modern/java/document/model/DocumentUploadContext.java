package com.modern.java.document.model;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

public class DocumentUploadContext {
    private final MultipartFile file;
    private final String hireeDocumentPath;
    private final String documentName;
    private final String remoteFilePath;
    private final String cpoDocumentPath;

    public DocumentUploadContext(MultipartFile file, String basePath, String hireeNo, int docTransactionSeqNo, int seqNo, String fileType) {
        this.file = file;
        this.documentName = createCpoDocumentName(hireeNo, docTransactionSeqNo, seqNo);
        this.hireeDocumentPath = basePath + "/" + hireeNo;
        this.remoteFilePath = getHireeDocumentPath() + "/" + getDocumentName() + fileType;
        this.cpoDocumentPath = hireeNo + "/" + getDocumentName();
    }

    private String createCpoDocumentName(String hireeNo, int documentTransactionSeqNo, int seqNo) {
        return hireeNo + "_" + StringUtils.leftPad(String.valueOf(documentTransactionSeqNo), 3, "0") + "_" + StringUtils.leftPad(String.valueOf(seqNo), 3, "0");
    }

    public MultipartFile getFile() {
        return file;
    }

    public String getHireeDocumentPath() {
        return hireeDocumentPath;
    }

    public String getDocumentName() {
        return documentName;
    }

    public String getRemoteFilePath() {
        return remoteFilePath;
    }

    public String getCpoDocumentPath() {
        return cpoDocumentPath;
    }
}
