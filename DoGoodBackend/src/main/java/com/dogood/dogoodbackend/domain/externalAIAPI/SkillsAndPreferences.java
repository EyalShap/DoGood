package com.dogood.dogoodbackend.domain.externalAIAPI;

import java.util.List;
import java.util.Objects;

public class SkillsAndPreferences {
    private List<String> skills;
    private List<String> preferences;

    public SkillsAndPreferences() {}

    public SkillsAndPreferences(List<String> skills, List<String> preferences) {
        this.skills = skills;
        this.preferences = preferences;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<String> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<String> preferences) {
        this.preferences = preferences;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkillsAndPreferences that = (SkillsAndPreferences) o;
        return Objects.equals(skills, that.skills) && Objects.equals(preferences, that.preferences);
    }

    @Override
    public int hashCode() {
        return Objects.hash(skills, preferences);
    }
}
