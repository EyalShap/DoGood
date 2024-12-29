package com.dogood.dogoodbackend.api;

public class SearchPostRequest {
    private String search;
    private String actor;

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }
}
