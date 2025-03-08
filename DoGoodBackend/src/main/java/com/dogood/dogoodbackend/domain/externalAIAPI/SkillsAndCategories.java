package com.dogood.dogoodbackend.domain.externalAIAPI;

import java.util.List;

public class SkillsAndCategories {
    private List<String> skills;
    private List<String> categories;

    public SkillsAndCategories() {
    }

    public List<String> getSkills() {
        return skills;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }
}
