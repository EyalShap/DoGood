package com.dogood.dogoodbackend.domain.externalAIAPI;

import java.util.List;
import java.util.Set;

public interface SkillsAndCategoriesExtractor {
    public SkillsAndCategories getSkillsAndCategories(String name, String description, Set<String> currentSkills, Set<String> currentCategories);
    public void setAI(AI ai);
}
