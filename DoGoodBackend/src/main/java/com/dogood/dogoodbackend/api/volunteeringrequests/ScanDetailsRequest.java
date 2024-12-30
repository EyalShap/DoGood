package com.dogood.dogoodbackend.api.volunteeringrequests;

import com.dogood.dogoodbackend.domain.volunteerings.ApprovalType;
import com.dogood.dogoodbackend.domain.volunteerings.ScanTypes;

public class ScanDetailsRequest {
    private ScanTypes scanTypes;
    private ApprovalType approvalType;

    public ScanTypes getScanTypes() {
        return scanTypes;
    }

    public void setScanTypes(ScanTypes scanTypes) {
        this.scanTypes = scanTypes;
    }

    public ApprovalType getApprovalType() {
        return approvalType;
    }

    public void setApprovalType(ApprovalType approvalType) {
        this.approvalType = approvalType;
    }
}
