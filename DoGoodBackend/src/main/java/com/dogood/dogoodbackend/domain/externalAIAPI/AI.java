package com.dogood.dogoodbackend.domain.externalAIAPI;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface AI {
    public String sendQuery(String prompt);
}
