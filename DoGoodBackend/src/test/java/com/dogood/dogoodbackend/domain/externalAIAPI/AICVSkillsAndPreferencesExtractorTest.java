package com.dogood.dogoodbackend.domain.externalAIAPI;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class AICVSkillsAndPreferencesExtractorTest {
    @Mock
    private AI ai;
    private CVSkillsAndPreferencesExtractor cvSkillsAndPreferencesExtractor;

    @BeforeEach
    void setUp() {
        this.cvSkillsAndPreferencesExtractor = new AICVSkillsAndPreferencesExtractor(ai);
    }

    private byte[] createCV() {
        String text = "John Doe is a highly motivated software developer with experience in Java, Python, and web technologies. He has worked on small-scale web applications and enjoys solving technical challenges. John is a fast learner, a strong team player, and is currently looking for opportunities to contribute to innovative tech projects.";
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Document document = new Document();
        try {
            PdfWriter.getInstance(document, byteArrayOutputStream);
            document.open();
            document.add(new Paragraph(text));
            document.close();
            byte[] pdfBytes = byteArrayOutputStream.toByteArray();
            return pdfBytes;
        }
        catch (DocumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Test
    void givenWorkingAI_whenGetSkillsAndPreferences_thenReturnSkillsAndPreferences() throws JsonProcessingException {
        Map<String, Object> map = new HashMap<>();
        map.put("skills", List.of("skill1", "skill2"));
        map.put("categories", List.of("pref1", "pref2"));
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(map);
        when(ai.sendQuery(anyString())).thenReturn(jsonString);

        byte[] bytes = createCV();
        SkillsAndPreferences res = cvSkillsAndPreferencesExtractor.getSkillsAndPreferences(bytes, null, null);

        SkillsAndPreferences expected = new SkillsAndPreferences(List.of("skill1", "skill2"), List.of("pref1", "pref2"));
        assertEquals(expected, res);
    }

    @Test
    void givenNonWorkingAI_whenGetSkillsAndPreferences_thenReturnEmptyLists() throws JsonProcessingException {
        when(ai.sendQuery(anyString())).thenReturn("error");

        byte[] bytes = createCV();
        SkillsAndPreferences res = cvSkillsAndPreferencesExtractor.getSkillsAndPreferences(bytes, null, null);

        SkillsAndPreferences expected = new SkillsAndPreferences(List.of(), List.of());
        assertEquals(expected, res);
    }

    @Test
    void givenEmptyCV_whenGetSkillsAndPreferences_thenReturnEmptyLists() throws JsonProcessingException {
        when(ai.sendQuery(anyString())).thenReturn("error");

        byte[] bytes = createCV();
        SkillsAndPreferences res = cvSkillsAndPreferencesExtractor.getSkillsAndPreferences(bytes, null, null);

        SkillsAndPreferences expected = new SkillsAndPreferences(List.of(), List.of());
        assertEquals(expected, res);
    }
}