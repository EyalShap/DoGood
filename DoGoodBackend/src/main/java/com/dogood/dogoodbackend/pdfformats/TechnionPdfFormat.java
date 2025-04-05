package com.dogood.dogoodbackend.pdfformats;

import com.dogood.dogoodbackend.domain.volunteerings.scheduling.HourApprovalRequest;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class TechnionPdfFormat implements PdfFormat{
    final int ADJUST_Y_BY = 27;

    final int NAME_X = 340;
    final int NAME_Y = 590 - ADJUST_Y_BY;
    final int ID_Y = 575 - ADJUST_Y_BY;
    final int ID_X = 360;
    final int ORGNAME_Y = 560 - ADJUST_Y_BY;
    final int ORGNAME_X = 355;
    final int DATE_X = 435;
    final int STARTHOUR_X = 345;
    final int ENDHOUR_X = 250;
    final int TOTAL_X = 170;
    final int PAGE1_ROW_Y = 490 - ADJUST_Y_BY;
    final int ROW_HEIGHT = 26;


    final int FONT_SIZE = 12;
    private int current_row ;

    private PdfStamper stamper;
    private PdfReader reader;
    private String outputPath;
    private byte[] signature;

    private String fullName = "";

    final BaseFont bf = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H,true);

    public TechnionPdfFormat(String username, byte[] signature) throws IOException, DocumentException {
        current_row = 0;
        InputStream pdf = getClass().getClassLoader().getResourceAsStream("templates/tech.pdf");
        reader = new PdfReader(pdf);
        this.signature = signature == null ? signature : signature.clone();
        outputPath = "./"+username + "/"+"tech"+username+".pdf";
        stamper = new PdfStamper(reader, new FileOutputStream(outputPath));
    }

    private void addText(int x, int y, String text, PdfContentByte over){
        over.beginText();
        if(text.charAt(0) >= 'א' && text.charAt(0) <= 'ת'){
            text = (new StringBuilder()).append(text).reverse().toString();
        }
        over.setFontAndSize(bf, FONT_SIZE);
        over.setTextMatrix(x, y);
        over.showText(text);
        over.endText();
    }

    @Override
    public PdfFormat addFirstName(String firstName) {
        this.fullName += firstName;
        return this;
    }

    @Override
    public PdfFormat addLastName(String lastName) {
        this.fullName += " "+ lastName;
        PdfContentByte over = stamper.getOverContent(1);

        addText(NAME_X, NAME_Y, this.fullName, over);
        return this;
    }

    @Override
    public PdfFormat addId(String id) {
        PdfContentByte over = stamper.getOverContent(1);

        addText(ID_X, ID_Y, id, over);
        return this;
    }

    @Override
    public PdfFormat addPhoneNumber(String phoneNumber) {
        return this;
    }

    @Override
    public PdfFormat addOrgName(String orgName) {
        PdfContentByte over = stamper.getOverContent(1);

        addText(ORGNAME_X, ORGNAME_Y, orgName, over);
        return this;
    }

    @Override
    public PdfFormat addSumHours(String sumHours) {
        return this;
    }

    @Override
    public PdfFormat addApprovedHours(HourApprovalRequest approvedHours) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat hourFormat = new SimpleDateFormat("HH:mm");
        String date = dateFormat.format(approvedHours.getEndTime());
        String start = hourFormat.format(approvedHours.getStartTime());
        String end = hourFormat.format(approvedHours.getEndTime());
        int y = PAGE1_ROW_Y-ROW_HEIGHT*current_row;
        PdfContentByte over = stamper.getOverContent(1);
        addText(DATE_X, y, date, over);
        addText(STARTHOUR_X, y, start, over);
        addText(ENDHOUR_X, y, end, over);
        double totalHours = approvedHours.getTotalHours();
        String totalHoursString = Math.floor(totalHours) == totalHours ? ""+(int)totalHours : String.format("%.01f", totalHours);
        addText(TOTAL_X, y, totalHoursString, over);
        current_row++;
        return this;
    }

    @Override
    public PdfFormat addEmail(String email) {
        return this;
    }

    @Override
    public String finish() throws DocumentException, IOException {
        stamper.close();
        reader.close();
        return outputPath;
    }
}
