package com.dogood.dogoodbackend.utils;

public class PostErrors {
    public static String makePostIdAlreadyExistsError(int postId) {
        return String.format("A post with id %d already exists." ,postId);
    }

    public static String makePostIdDoesNotExistError(int postId) {
        return String.format("A post with id %d does not exist." ,postId);
    }


}
