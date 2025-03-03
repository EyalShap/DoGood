package com.dogood.dogoodbackend.pdfformats;

import com.dogood.dogoodbackend.domain.volunteerings.scheduling.HourApprovalRequest;
import com.itextpdf.text.DocumentException;

import java.io.IOException;

public interface PdfFormat {
    public PdfFormat addFirstName(String firstName);
    public PdfFormat addLastName(String lastName);
    public PdfFormat addId(String id);
    public PdfFormat addPhoneNumber(String phoneNumber);
    public PdfFormat addOrgName(String orgName);
    public PdfFormat addSumHours(String sumHours);
    public PdfFormat addApprovedHours(HourApprovalRequest approvedHours);
    public PdfFormat addEmail(String email);
    public String finish() throws DocumentException, IOException;
}
