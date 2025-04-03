package com.dogood.dogoodbackend.domain.externalAIAPI;

import java.util.List;

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
}
