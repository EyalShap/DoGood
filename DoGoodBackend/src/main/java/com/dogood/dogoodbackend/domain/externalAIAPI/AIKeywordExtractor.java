package com.dogood.dogoodbackend.domain.externalAIAPI;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AIKeywordExtractor implements KeywordExtractor{
    private AI ai;
    private final String prompt = "I will give you a volunteering name and description, and the title and description of the volunteering's advertisement. Please extract all relevant keywords and their synonyms and related words. Please add a lot of words. Display the result as a comma-separated list. The volunteering name is: %s. The volunteering description is: %s. The advertisement title is: %s. The advertisement description is: %s.";

    public AIKeywordExtractor(AI ai) {
        this.ai = ai;
    }

    @Override
    public Set<String> getKeywords(String volunteeringName, String volunteeringDescription, String postTitle, String postDescription) {
        try {
            String finalPrompt = String.format(prompt, volunteeringName, volunteeringDescription, postTitle, postDescription);
            String result = ai.sendQuery(finalPrompt);
            String[] keywords = result.split(", ");
            return new HashSet<>(List.of(keywords));
        }
        catch (Exception e) {
            return new HashSet<>(Arrays.asList((volunteeringName + " " + volunteeringDescription + " " + postTitle + " " + postDescription).split(" ")));
        }
    }
}
