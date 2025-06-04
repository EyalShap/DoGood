package com.dogood.dogoodbackend.domain.externalAIAPI;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class KeywordExtractorTest {
    @Mock
    private AI ai;
    private KeywordExtractor keywordExtractor;

    @BeforeEach
    void setUp() {
        this.keywordExtractor = new AIKeywordExtractor(ai);
    }

    @Test
    void givenWorkingAI_whenGetVolunteeringPostKeywords_thenReturnKeywords() {
        when(ai.sendQuery(anyString())).thenReturn("keyword1, keyword2, keyword3");

        Set<String> expected = Set.of("keyword1", "keyword2", "keyword3");
        Set<String> res = keywordExtractor.getVolunteeringPostKeywords("Volunteering", "Description", "Title", "Description");
        assertEquals(expected, res);
    }

    @Test
    void givenNotWorkingAI_whenGetVolunteeringPostKeywords_thenReturnEmpty() {
        when(ai.sendQuery(anyString())).thenThrow(new IllegalArgumentException(""));

        Set<String> expected = Set.of();
        Set<String> res = keywordExtractor.getVolunteeringPostKeywords("Volunteering", "VolunteeringDescription", "Title", "PostDescription");
        assertEquals(expected, res);
    }

    @Test
    void givenWorkingAI_getVolunteerPostKeywords_thenReturnKeywords() {
        when(ai.sendQuery(anyString())).thenReturn("keyword1, keyword2, keyword3");

        Set<String> expected = Set.of("keyword1", "keyword2", "keyword3");
        Set<String> res = keywordExtractor.getVolunteerPostKeywords("Title", "Description");
        assertEquals(expected, res);
    }

    @Test
    void givenNonWorkingAI_getVolunteerPostKeywords_thenReturnKeywords() {
        when(ai.sendQuery(anyString())).thenThrow(new IllegalArgumentException(""));

        Set<String> expected = Set.of();
        Set<String> res = keywordExtractor.getVolunteerPostKeywords("Title", "Description");
        assertEquals(expected, res);
    }
}