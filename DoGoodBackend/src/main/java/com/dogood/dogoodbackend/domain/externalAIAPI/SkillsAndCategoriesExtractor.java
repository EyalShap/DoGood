package com.dogood.dogoodbackend.domain.externalAIAPI;

import java.util.List;
import java.util.Set;

public interface SkillsAndCategoriesExtractor {
    public List<String>[] getSkillsAndCategories(String name, String description, List<String> currentSkills, List<String> currentCategories);
}
