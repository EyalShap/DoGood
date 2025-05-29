package com.dogood.dogoodbackend.pdfformats;

import com.dogood.dogoodbackend.domain.volunteerings.scheduling.HourApprovalRequest;
import com.itextpdf.text.DocumentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Service
public class PdfFactory {
    @Value("${font.arial.location}")
    private String fontLocation;

    public String createFormat(String username, University uni, String orgName, String firstName, String lastName, String id, String phone, String email, List<HourApprovalRequest> approvedHoursList, byte[] signature) throws DocumentException, IOException {
        if(uni == University.ELSE){
            throw new UnsupportedOperationException("University not supported");
        }
        PdfFormat format;
        Files.createDirectories(Paths.get("./"+username));
        if(uni == University.BGU){
            format = new BGUPdfFormat(username,signature,fontLocation);
        }else if(uni == University.TECH){
            format = new TechnionPdfFormat(username,signature,fontLocation);
        }else{
            format = new HaifaPdfFormat(username,signature,fontLocation);
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
