package com.dogood.dogoodbackend.domain.externalAIAPI;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest

class SkillsAndCategoriesExtractorTest {
    @Mock
    private AI ai;
    private SkillsAndCategoriesExtractor skillsAndCategoriesExtractor;

    @BeforeEach
    void setUp() {
        this.skillsAndCategoriesExtractor = new AISkillsAndCategoriesExtractor(ai);
    }

    @Test
    void givenWorkingAI_whenGetSkillsAndCategories_thenReturnSkillsAndCategories() throws JsonProcessingException {
        Map<String, Object> map = new HashMap<>();
        map.put("skills", List.of("skill1", "skill2"));
        map.put("categories", List.of("cat1", "cat2"));
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(map);

        when(ai.sendQuery(anyString())).thenReturn(jsonString);

        SkillsAndCategories expected = new SkillsAndCategories();
        expected.setSkills(List.of("skill1", "skill2"));
        expected.setCategories(List.of("cat1", "cat2"));

        SkillsAndCategories res = skillsAndCategoriesExtractor.getSkillsAndCategories("name", "description", null, null);

        assertEquals(expected, res);
    }

    @Test
    void givenNotWorkingAI_whenGetSkillsAndCategories_thenReturnEmptyLists() throws JsonProcessingException {
        when(ai.sendQuery(anyString())).thenReturn("error");

        SkillsAndCategories expected = new SkillsAndCategories();
        expected.setSkills(List.of());
        expected.setCategories(List.of());

        SkillsAndCategories res = skillsAndCategoriesExtractor.getSkillsAndCategories("name", "description", null, null);

        assertEquals(expected, res);
    }
}