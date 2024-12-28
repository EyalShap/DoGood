package com.dogood.dogoodbackend.domain.posts;

public class SearchPost {
    private Post post;
    private int relevance;

    public SearchPost(Post post, int relevance) {
        this.post = post;
        this.relevance = relevance;
    }

    public Post getPost() {
        return post;
    }

    public int getRelevance() {
        return relevance;
    }

    public void setRelevance(int relevance) {
        this.relevance = relevance;
    }
}
