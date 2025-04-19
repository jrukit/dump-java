package com.modern.java.document.service;

import org.springframework.stereotype.Service;

@Service
public class UnknownDependencyService {
    public String getHireeNoOrThrow(String caseNo) {
        return "HIREE789";
    }
    public String getDocTypeCode(String docType) {
        return "AGREEMENT1234567";
    }

    public String getDocClassCode(String docClass) {
        return "LEGAL1234567";
    }

    public BranchEntity getBranchById(String id) {
        BranchEntity branchEntity = new BranchEntity();
        branchEntity.setBranchCode("BranchCode001");
        branchEntity.setCompanyCode("CompanyCode005");
        return branchEntity;
    }

    public CaseDocumentTransactionInfoEntity getCaseDocTransInfo(String caseNo) {
        CaseDocumentTransactionInfoEntity caseDocumentTransactionInfoEntity = new CaseDocumentTransactionInfoEntity();
        caseDocumentTransactionInfoEntity.setServiceCode("Service1234");
        caseDocumentTransactionInfoEntity.setBranchId("Branch1234");
        caseDocumentTransactionInfoEntity.setBuCode("Bu123434");
        caseDocumentTransactionInfoEntity.setCampaignCode("CampaignCode1234");
        caseDocumentTransactionInfoEntity.setMktCode("Mkt1235");
        return caseDocumentTransactionInfoEntity;
    }
}
