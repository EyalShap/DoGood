package com.dogood.dogoodbackend.domain.externalAIAPI;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProxyKeywordExtractor implements KeywordExtractor{
    private Set<?> stopwords;

    public ProxyKeywordExtractor() {
        this.stopwords = EnglishAnalyzer.getDefaultStopSet();
    }

    @Override
    public Set<String> getVolunteeringPostKeywords(String volunteeringName, String volunteeringDescription, String postTitle, String postDescription) {
        String str = volunteeringName + " " + volunteeringDescription + " " + postTitle + " " + postDescription;
        return getKeywords(str);
    }

    @Override
    public Set<String> getVolunteerPostKeywords(String postTitle, String postDescription) {
        String str = postTitle + " " + postDescription;
        return getKeywords(str);
    }

    @Override
    public void setAI(AI ai) {

    }

    private Set<String> getKeywords(String str) {
        String[] split = str.split("\\s+");
        Set<String> keywords = Arrays.stream(split)
                .filter(word -> !stopwords.contains(word))
                .collect(Collectors.toSet());
        return keywords;
    }
}
