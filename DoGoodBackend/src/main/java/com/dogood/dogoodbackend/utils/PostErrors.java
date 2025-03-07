package com.dogood.dogoodbackend.utils;

public class PostErrors {
    public static String makePostIdAlreadyExistsError(int postId) {
        return String.format("A post with id %d already exists." ,postId);
    }

    public static String makePostIdDoesNotExistError(int postId) {
        return String.format("A post with id %d does not exist." ,postId);
    }

    public static String makeUserIsNotAllowedToMakePostActionError(String title, String username, String action) {
        return String.format("The user %s is not allowed to %s post \"%s\".", username, action, title);
    }

    public static String makeThereIsNoPostForVolunteeringError(int volunteeringId) {
        return String.format("There is no post for volunteering with id %d.", volunteeringId);
    }

    public static String makePostIsNotValidError() {
        return "The given post is not valid.";
    }


    public static String makeVolunteeringIdDoesNotExistError(int volunteeringId) {
        return String.format("An volunteering with id %d does not exist." ,volunteeringId);
    }

    public static String makeUserIsRelatedToPost(String username, String title, boolean related) {
        String relatedStr = related ? "already" : "not";
        return String.format("The user %s is %s related to the post \"%s\".", username, relatedStr, title);
    }

    public static String makePosterCanNotBeRemovedFromPost(String username, String title) {
        return String.format("The user %s is the poster of the post \"%s\" and therefore can not be removed.", username, title);
    }

    public static String makeImagePathExists(String path, String title, boolean exist) {
        String existStr = exist ? "already" : "does not";
        String sStr = exist ? "s" : "";
        return String.format("The image %s %s exist%sStr in the post \"%s\".", path, existStr, sStr, title);
    }
}
