package com.modern.java.document.entity;

public class CaseDocumentTransactionInfoEntity {
    private String campaignCode;
    private String serviceCode;
    private String buCode;
    private String mktCode;
    private String branchId;

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
