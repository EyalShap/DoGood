package com.dogood.dogoodbackend.acceptance.nonfunctional;

import java.util.Map;

public interface ParametricUseCaseExecutor {
    public boolean execute(Map<String,String> parameters);
}
