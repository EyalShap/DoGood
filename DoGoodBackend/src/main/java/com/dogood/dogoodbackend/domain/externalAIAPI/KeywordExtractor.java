package com.dogood.dogoodbackend.domain.externalAIAPI;

import java.util.Set;

public interface KeywordExtractor {
    public Set<String> getVolunteeringPostKeywords(String volunteeringName, String volunteeringDescription, String postTitle, String postDescription);
    public Set<String> getVolunteerPostKeywords(String postTitle, String postDescription);
    public void setAI(AI ai);
}
