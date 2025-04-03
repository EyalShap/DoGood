package com.dogood.dogoodbackend.domain.externalAIAPI;

import java.util.Set;

public interface CVSkillsAndPreferencesExtractor {
    public SkillsAndPreferences getSkillsAndPreferences(byte[] cv, Set<String> currentSkills, Set<String> currentPreferences);

}
