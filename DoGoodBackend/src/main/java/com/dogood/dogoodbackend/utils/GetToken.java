package com.dogood.dogoodbackend.utils;

import jakarta.servlet.http.HttpServletRequest;

public class GetToken {

    public static String getToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Skip "Bearer " prefix
        }
        return token;
    }
}
