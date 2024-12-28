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
    public Set<String> getKeywords(String str) {
        String[] split = str.split("\\s+");
        Set<String> keywords = Arrays.stream(split)
                .filter(word -> !stopwords.contains(word))
                .collect(Collectors.toSet());
        return keywords;
    }
}
