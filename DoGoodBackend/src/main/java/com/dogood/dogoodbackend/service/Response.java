package com.dogood.dogoodbackend.service;

public class Response<T> {
    private T data;
    private boolean error;
    private String errorString;

    private Response(T data, boolean error, String errorString) {
        this.data = data;
        this.error = error;
        this.errorString = errorString;
    }

    public static <T> Response<T> createResponse(T data, String errorStr) {
        if(errorStr == null || errorStr.isBlank()) {
            return new Response<>(data, false, null);
        }
        return new Response<>(null, true, errorStr);
    }

    public static <T> Response<T> createResponse(String errorStr) {
        return new Response<>(null, true, errorStr);
    }

    public static <T> Response<T> createResponse(T data) {
        return new Response<>(data, false, null);
    }

    public static Response<String> createOK() {
        return new Response<>("OK", false, null);
    }

    public T getData() {
        return this.data;
    }

    public boolean getError() {
        return this.error;
    }

    public String getErrorString() {
        return this.errorString;
    }
}
