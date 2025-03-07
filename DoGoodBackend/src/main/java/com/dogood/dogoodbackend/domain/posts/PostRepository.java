package com.dogood.dogoodbackend.domain.posts;

import java.util.List;

public interface PostRepository<T> {
    public T getPost(int postId);
    public void removePost(int postId);
    public List<T> getAllPosts();
}
