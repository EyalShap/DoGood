package com.dogood.dogoodbackend.pdfformats;

import com.dogood.dogoodbackend.domain.volunteerings.scheduling.HourApprovalRequest;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class HaifaPdfFormat implements PdfFormat{
    final int FUCKED_UP_X_BY=30;
    final int FUCKED_UP_Y_BY=20;


    final int FIRST_NAME_X = 330+FUCKED_UP_X_BY;
    final int INFO_Y = 545+FUCKED_UP_Y_BY;
    final int LAST_NAME_X = 410 + FUCKED_UP_X_BY;
    final int ID_X = 220 + FUCKED_UP_X_BY;
    final int PHONE_X = 90 + FUCKED_UP_X_BY;
    final int EMAIL_Y = 520 + FUCKED_UP_Y_BY;
    final int EMAIL_X = 90 + FUCKED_UP_X_BY;
    final int SUM_HOURS_X = 155 + FUCKED_UP_X_BY;
    final int SUM_HOURS_Y = 190;
    final int DATE_X = 400 + FUCKED_UP_X_BY;
    final int DAY_X = 340 + FUCKED_UP_X_BY;
    final int STARTHOUR_X = 270 + FUCKED_UP_X_BY;
    final int ENDHOUR_X = 200 + FUCKED_UP_X_BY;
    final int TOTAL_X = 155 + FUCKED_UP_X_BY;
    final int PAGE1_ROW_Y = 475 + FUCKED_UP_Y_BY;
    final int ROW_HEIGHT = 17;

    final int FONT_SIZE = 12;
    private int current_row ;

    private PdfStamper stamper;
    private PdfReader reader;
    private String outputPath;

    final String[] days = new String[]{"ראשון", "שני", "שלישי", "רביעי", "חמישי","שישי","שבת"};

    final BaseFont bf = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H,true);

    public HaifaPdfFormat(String username) throws IOException, DocumentException {
        current_row = 0;
        File pdf = ResourceUtils.getFile("classpath:templates/haifa.pdf");
        reader = new PdfReader(new FileInputStream(pdf));
        outputPath = "./"+username + "/"+"haifa"+username+".pdf";
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

        addText(FIRST_NAME_X, INFO_Y, firstName, over);
        return this;
    }

    @Override
    public PdfFormat addLastName(String lastName) {
        PdfContentByte over = stamper.getOverContent(1);

        addText(LAST_NAME_X, INFO_Y, lastName, over);
        return this;
    }

    @Override
    public PdfFormat addId(String id) {
        PdfContentByte over = stamper.getOverContent(1);

        addText(ID_X, INFO_Y, id, over);
        return this;
    }

    @Override
    public PdfFormat addPhoneNumber(String phoneNumber) {
        PdfContentByte over = stamper.getOverContent(1);

        addText(PHONE_X, INFO_Y, phoneNumber, over);
        return this;
    }

    @Override
    public PdfFormat addOrgName(String orgName) {
        return this;
    }

    @Override
    public PdfFormat addSumHours(String sumHours) {
        PdfContentByte over = stamper.getOverContent(1);

        addText(SUM_HOURS_X, SUM_HOURS_Y, sumHours, over);
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
        Calendar c = Calendar.getInstance();
        c.setTime(approvedHours.getStartTime());
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        addText(DATE_X, y, date, over);
        addText(DAY_X,y,days[dayOfWeek-1],over);
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
        PdfContentByte over = stamper.getOverContent(1);

        addText(EMAIL_X, EMAIL_Y, email, over);
        return this;
    }

    @Override
    public String finish() throws DocumentException, IOException {
        stamper.close();
        reader.close();
        return outputPath;
    }
}
