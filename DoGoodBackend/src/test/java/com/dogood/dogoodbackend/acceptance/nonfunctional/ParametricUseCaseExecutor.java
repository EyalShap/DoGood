package com.dogood.dogoodbackend.acceptance.nonfunctional;

import java.util.Map;
import java.util.concurrent.Future;

@FunctionalInterface
public interface ParametricUseCaseExecutor {
    public Future<Boolean> execute(Map<String,String> parameters);
}
