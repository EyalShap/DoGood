package com.dogood.dogoodbackend.domain.externalAIAPI;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class AISkillsAndCategoriesExtractor implements SkillsAndCategoriesExtractor {
    private AI ai;
    private ObjectMapper objectMapper;
    private final String basicPrompt = "I am going to give you the Name and description of a Volunteering Gig. Please analyze the required skills and possible categories for the volunteering gig from its name and the contents of its description. %s Please output only a list of skills and a list of categories, like this: \"{skills: [...],\ncategories: [...]}\" (you should return a json with a \"skill\" list field and a \"categories\" list field.). Make sure each skill/categories has the first letter capitalized only.\nIf for any reason you are unable to get the skills and preferences, SEND A JSON WITH EMPTY ARRAYS, do NOT TRY TO ANSWER THE USER, do NOT COMMUNICATE WITH THE USER.\nThank you. Here is the name of the volunteering: %s. Here is the description of the volunteering: %s";
    private final String currentSkillsAndCategories = "I will give you the current available skills and categories. Use them as a guide to the style of skills/categories expected. You can create new skills/categories or use existing ones in the list. The current skills are: %s. The current categories are: %s.";

    public AISkillsAndCategoriesExtractor(AI ai) {
        this.ai = ai;
        this.objectMapper = new ObjectMapper();
    }

    private String createPrompt(String name, String description, Set<String> currentSkills, Set<String> currentCategories) {
        String skills = (currentSkills == null || currentSkills.size() == 0) ? "None" : "[" + String.join(", ", currentSkills) + "]";
        String categories = (currentCategories == null || currentCategories.size() == 0) ? "None" : "[" + String.join(", ", currentCategories) + "]";
        String listPrompt = String.format(currentSkillsAndCategories, skills, categories);
        return String.format(basicPrompt,listPrompt, name, description);
    }

    private List<String> parseList(String listString) {
        listString = listString.replace("(", "").replace(")", "");
        String[] items = listString.split(", ");
        return Arrays.asList(items);
    }

    @Override
    public SkillsAndCategories getSkillsAndCategories(String name, String description, Set<String> currentSkills, Set<String> currentCategories) {
        try {
            String prompt = createPrompt(name, description, currentSkills, currentCategories);
            String result = ai.sendQuery(String.format(prompt, name, description));
            result = result.replace("```json", "");
            result = result.replace("```", "");
            result = result.replace("\\n", "");
            result = result.replace("\\", "");
            return objectMapper.readValue(result, SkillsAndCategories.class);
        }
        catch (Exception e) {
            SkillsAndCategories skillsAndCategories = new SkillsAndCategories();
            skillsAndCategories.setCategories(new LinkedList<>());
            skillsAndCategories.setSkills(new LinkedList<>());
            return skillsAndCategories;
        }
    }
}
