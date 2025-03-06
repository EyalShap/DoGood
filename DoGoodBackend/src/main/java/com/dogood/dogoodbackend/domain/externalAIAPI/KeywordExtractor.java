package com.dogood.dogoodbackend.domain.externalAIAPI;

import java.util.Set;

public interface KeywordExtractor {
    public Set<String> getKeywords(String volunteeringName, String volunteeringDescription, String postTitle, String postDescription);
}
