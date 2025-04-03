package com.dogood.dogoodbackend.domain.externalAIAPI;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class AICVSkillsAndPreferencesExtractor implements CVSkillsAndPreferencesExtractor{
    private AI ai;
    private ObjectMapper objectMapper;
    private final String basicPrompt = "I am going to give you a cv. Please extract the skills this person has and what job categories they might be intrested in. %s Please output only a list of skills and a list of preferences, always in English, like this: \"{skills: [...],\ncategories: [...]}\" (you should return a json with a \"skill\" list field and a \"categories\" list field.). Make sure each skill/categories has the first letter capitalized only.\nIf for any reason you are unable to get the skills and categories, SEND A JSON WITH EMPTY ARRAYS, do NOT TRY TO ANSWER THE USER, do NOT COMMUNICATE WITH THE USER.\nThank you. Here is the cv: %s.";
    private final String currentSkillsAndPreferences = "I will give you the current available skills and categories. Use them as a guide to the style of skills/preferences expected. You can create new skills/categories or use existing ones in the list. The current skills are: %s. The current categories are: %s.";

    public AICVSkillsAndPreferencesExtractor(AI ai) {
        this.ai = ai;
        this.objectMapper = new ObjectMapper();
    }

    private String createPrompt(String cvString, Set<String> currentSkills, Set<String> currentPreferences) {
        String skills = (currentSkills == null || currentSkills.size() == 0) ? "None" : "[" + String.join(", ", currentSkills) + "]";
        String preferences = (currentPreferences == null || currentPreferences.size() == 0) ? "None" : "[" + String.join(", ", currentPreferences) + "]";
        String listPrompt = String.format(currentSkillsAndPreferences, skills, preferences);
        return String.format(basicPrompt, listPrompt, cvString);
    }

    private String getCvString(byte[] cvBytes) {
        try {
            // Convert byte[] to InputStream
            ByteArrayInputStream inputStream = new ByteArrayInputStream(cvBytes);

            // Load PDF document from InputStream
            PDDocument document = PDDocument.load(inputStream);

            // Extract text from PDF
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String extractedText = pdfStripper.getText(document);
            document.close();

            // Print extracted text
            return extractedText;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public SkillsAndPreferences getSkillsAndPreferences(byte[] cv, Set<String> currentSkills, Set<String> currentPreferences) {
        try {
            String cvString = getCvString(cv);
            if(cvString.equals("")) {
                throw new Exception();
            }
            String prompt = createPrompt(cvString, currentSkills, currentPreferences);
            String result = ai.sendQuery(prompt);
            result = result.replace("```json", "");
            result = result.replace("```", "");
            result = result.replace("\\n", "");
            result = result.replace("\\", "");
            SkillsAndCategories skillsAndCategories = objectMapper.readValue(result, SkillsAndCategories.class);
            return new SkillsAndPreferences(skillsAndCategories.getSkills(), skillsAndCategories.getCategories());
        }
        catch (Exception e) {
            SkillsAndPreferences skillsAndPreferences = new SkillsAndPreferences();
            skillsAndPreferences.setPreferences(new LinkedList<>());
            skillsAndPreferences.setSkills(new LinkedList<>());
            return skillsAndPreferences;
        }
    }
}
