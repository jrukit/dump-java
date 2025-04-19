package com.modern.java.document.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.Date;

@Entity(name = "upload_document")
public class UploadDocumentEntity {
    @Id
    private long docTransactionId;
    private int docTransactionSeqNo;
    private String caseNo;
    private String docType;
    private String docClass;
    private String destinationCompanyCode;
    private String destinationOfficeCode;
    private int totalDocument;
    private String docStatus;
    private String hireNo;
    private Date createdDate;
    private String createdBy;
    private Date updatedDate;
    private String updatedBy;
    private String campaignCode;
    private String serviceCode;
    private String buCode;
    private String mktCode;
    private String branchId;

    public UploadDocumentEntity() {
    }

    // Getters and Setters
    public String getCaseNo() {
        return caseNo;
    }

    public void setCaseNo(String caseNo) {
        this.caseNo = caseNo;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getDocClass() {
        return docClass;
    }

    public void setDocClass(String docClass) {
        this.docClass = docClass;
    }

    public String getDestinationCompanyCode() {
        return destinationCompanyCode;
    }

    public void setDestinationCompanyCode(String destinationCompanyCode) {
        this.destinationCompanyCode = destinationCompanyCode;
    }

    public String getDestinationOfficeCode() {
        return destinationOfficeCode;
    }

    public void setDestinationOfficeCode(String destinationOfficeCode) {
        this.destinationOfficeCode = destinationOfficeCode;
    }

    public int getTotalDocument() {
        return totalDocument;
    }

    public void setTotalDocument(int totalDocument) {
        this.totalDocument = totalDocument;
    }

    public String getDocStatus() {
        return docStatus;
    }

    public void setDocStatus(String docStatus) {
        this.docStatus = docStatus;
    }

    public String getHireeNo() {
        return hireNo;
    }

    public void setHireeNo(String hireeNo) {
        this.hireNo = hireeNo;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public long getDocTransactionId() {
        return docTransactionId;
    }

    public void setDocTransactionId(long docTransactionId) {
        this.docTransactionId = docTransactionId;
    }

    public int getDocTransactionSeqNo() {
        return docTransactionSeqNo;
    }

    public void setDocTransactionSeqNo(int docTransactionSeqNo) {
        this.docTransactionSeqNo = docTransactionSeqNo;
    }

    public String getCampaignCode() {
        return campaignCode;
    }

    public void setCampaignCode(String campaignCode) {
        this.campaignCode = campaignCode;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getBuCode() {
        return buCode;
    }

    public void setBuCode(String buCode) {
        this.buCode = buCode;
    }

    public String getMktCode() {
        return mktCode;
    }

    public void setMktCode(String mktCode) {
        this.mktCode = mktCode;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }
}
