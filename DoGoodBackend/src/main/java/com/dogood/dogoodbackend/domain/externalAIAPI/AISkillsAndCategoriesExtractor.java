package com.dogood.dogoodbackend.domain.externalAIAPI;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class AISkillsAndCategoriesExtractor implements SkillsAndCategoriesExtractor {
    private AI ai;
    private ObjectMapper objectMapper;
    private final String basicPrompt = "Given the following volunteering name description, please extract the required skills and volunteering categories. The required format is (list of skills)&(list of categories). The volunteering name is: %s. The volunteering description is: %s. ";
    private final String currentSkillsAndCategories = "I will give you the current available skills and categories, so if one of the skills or categories already exists take it from there and don't create new ones. The current skills are: %s. The current categories are: %s.";

    public AISkillsAndCategoriesExtractor(AI ai) {
        this.ai = ai;
        this.objectMapper = new ObjectMapper();
    }

    private String createPrompt(String name, String description, List<String> currentSkills, List<String> currentCategories) {
        String prompt = String.format(basicPrompt, name, description);
        String skills = (currentSkills == null || currentSkills.size() == 0) ? "None" : "[" + String.join(", ", currentSkills) + "]";
        String categories = (currentCategories == null || currentCategories.size() == 0) ? "None" : "[" + String.join(", ", currentCategories) + "]";
        if(!skills.equals("None") || !categories.equals("None")) {
            prompt += String.format(currentSkillsAndCategories, skills, categories);
        }
        return prompt;
    }

    private List<String> parseList(String listString) {
        listString = listString.replace("(", "").replace(")", "");
        String[] items = listString.split(", ");
        return Arrays.asList(items);
    }

    @Override
    public List<String>[] getSkillsAndCategories(String name, String description, List<String> currentSkills, List<String> currentCategories) {
        List<String>[] res = new List[2];

        try {
            String prompt = createPrompt(name, description, currentSkills, currentCategories);
            String result = ai.sendQuery(String.format(prompt, name, description));
            String[] skillsAndCategories = result.split("&");
            List<String> skills = parseList(skillsAndCategories[0]);
            List<String> categories = parseList(skillsAndCategories[1]);

            res[0] = skills;
            res[1] = categories;
            return res;
        }
        catch (Exception e) {
            res[0] = new ArrayList<>();
            res[1] = new ArrayList<>();
            return res;
        }
    }
}
