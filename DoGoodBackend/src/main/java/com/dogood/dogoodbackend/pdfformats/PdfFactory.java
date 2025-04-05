package com.dogood.dogoodbackend.pdfformats;

import com.dogood.dogoodbackend.domain.volunteerings.scheduling.HourApprovalRequest;
import com.itextpdf.text.DocumentException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class PdfFactory {
    public String createFormat(String username, University uni, String orgName, String firstName, String lastName, String id, String phone, String email, List<HourApprovalRequest> approvedHoursList, byte[] signature) throws DocumentException, IOException {
        if(uni == University.ELSE){
            throw new UnsupportedOperationException("University not supported");
        }
        PdfFormat format;
        Files.createDirectories(Paths.get("./"+approvedHoursList.get(0).getUserId()));
        if(uni == University.BGU){
            format = new BGUPdfFormat(username,signature);
        }else if(uni == University.TECH){
            format = new TechnionPdfFormat(username,signature);
        }else{
            format = new HaifaPdfFormat(username,signature);
        }
        double sumHours = 0;
        for(HourApprovalRequest hours : approvedHoursList){
            sumHours += hours.getTotalHours();
        }
        String sumHoursString = Math.floor(sumHours) == sumHours ? ""+(int)sumHours : String.format("%.01f", sumHours);
        format.addFirstName(firstName).addLastName(lastName).addId(id).addPhoneNumber(phone).addOrgName(orgName).addEmail(email).addSumHours(sumHoursString);
        for(HourApprovalRequest hours : approvedHoursList){
            format.addApprovedHours(hours);
        }
        return format.finish();
    }
}
