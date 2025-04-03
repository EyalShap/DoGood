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

public class BGUPdfFormat implements PdfFormat{
    final int FIRST_NAME_X = 380;
    final int NAME_ID_Y = 565;
    final int LAST_NAME_X = 235;
    final int ID_X = 105;
    final int PHONE_Y = 540;
    final int PHONE_3DIGITS_X = 375;
    final int PHONE_REST_X = 410;
    final int ORGNAME_SUMHOURS_Y = 520;
    final int ORGNAME_X = 330;
    final int SUM_HOURS_X = 190;
    final int DATE_X = 460;
    final int STARTHOUR_X = 410;
    final int ENDHOUR_X = 355;
    final int TOTAL_X = 300;
    final int PAGE1_ROWS = 10;
    final int PAGE1_ROW_Y = 415;
    final int PAGE2_ROW_Y = 730;
    final int ROW_HEIGHT = 34;

    final int FONT_SIZE = 12;
    private int current_row ;

    private PdfStamper stamper;
    private PdfReader reader;
    private String outputPath;

    final BaseFont bf = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H,true);

    public BGUPdfFormat(String username) throws IOException, DocumentException {
        current_row = 0;
        InputStream pdf = getClass().getClassLoader().getResourceAsStream("templates/bgu.pdf");
        reader = new PdfReader(pdf);
        outputPath = "./"+username + "/"+"bgu"+username+".pdf";
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
        PdfContentByte over = stamper.getOverContent(1);

        addText(FIRST_NAME_X, NAME_ID_Y, firstName, over);
        return this;
    }

    @Override
    public PdfFormat addLastName(String lastName) {
        PdfContentByte over = stamper.getOverContent(1);

        addText(LAST_NAME_X, NAME_ID_Y, lastName, over);
        return this;
    }

    @Override
    public PdfFormat addId(String id) {
        PdfContentByte over = stamper.getOverContent(1);

        addText(ID_X, NAME_ID_Y, id, over);
        return this;
    }

    @Override
    public PdfFormat addPhoneNumber(String phoneNumber) {
        PdfContentByte over = stamper.getOverContent(1);

        String first3Digits = phoneNumber.substring(0,3);
        String rest = phoneNumber.substring(3).replaceAll("-","");

        addText(PHONE_3DIGITS_X, PHONE_Y, first3Digits, over);
        addText(PHONE_REST_X, PHONE_Y, rest, over);
        return this;
    }

    @Override
    public PdfFormat addOrgName(String orgName) {
        PdfContentByte over = stamper.getOverContent(1);

        addText(ORGNAME_X, ORGNAME_SUMHOURS_Y, orgName, over);
        return this;
    }

    @Override
    public PdfFormat addSumHours(String sumHours) {
        PdfContentByte over = stamper.getOverContent(1);

        addText(SUM_HOURS_X, ORGNAME_SUMHOURS_Y, sumHours, over);
        return this;
    }

    @Override
    public PdfFormat addApprovedHours(HourApprovalRequest approvedHours) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat hourFormat = new SimpleDateFormat("HH:mm");
        String date = dateFormat.format(approvedHours.getEndTime());
        String start = hourFormat.format(approvedHours.getStartTime());
        String end = hourFormat.format(approvedHours.getEndTime());
        int y;
        PdfContentByte over;
        if(current_row >= PAGE1_ROWS){
            over = stamper.getOverContent(2);
            y = PAGE2_ROW_Y-ROW_HEIGHT*(current_row-PAGE1_ROWS);
        }else{
            over = stamper.getOverContent(1);
            y = PAGE1_ROW_Y-ROW_HEIGHT*current_row;
        }
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
