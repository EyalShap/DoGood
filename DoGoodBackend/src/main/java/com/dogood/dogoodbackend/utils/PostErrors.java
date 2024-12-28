package com.dogood.dogoodbackend.utils;

public class PostErrors {
    public static String makePostIdAlreadyExistsError(int postId) {
        return String.format("A post with id %d already exists." ,postId);
    }

    public static String makePostIdDoesNotExistError(int postId) {
        return String.format("A post with id %d does not exist." ,postId);
    }

    public static String makeUserIsNotAllowedToMakePostActionError(int postId, String username, String action) {
        return String.format("The user %s is not allowed to %s post %d.", username, postId, action);
    }

    public static String makeThereIsNoPostForVolunteeringError(int volunteeringId) {
        return String.format("There is no post for volunteering with id %d.", volunteeringId);
    }
}
