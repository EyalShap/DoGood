package com.dogood.dogoodbackend.domain.externalAIAPI;

import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkillsAndCategories that = (SkillsAndCategories) o;
        return Objects.equals(skills, that.skills) && Objects.equals(categories, that.categories);
    }

    @Override
    public int hashCode() {
        return Objects.hash(skills, categories);
    }
}
