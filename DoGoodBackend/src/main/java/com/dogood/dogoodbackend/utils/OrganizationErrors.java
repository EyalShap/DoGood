package com.dogood.dogoodbackend.utils;

public class OrganizationErrors {

    public static String makeNonManagerCanNotPreformActionError(String username, String organizationName, String action) {
        return String.format("The user %s is not a manager in organization %s and therefore can not %s.", username, organizationName, action);
    }

    public static String makeUserIsAlreadyAManagerError(String username, String organizationName) {
        return String.format("The user %s is already a manager in organization %s.", username, organizationName);
    }

    public static String makeNonFounderCanNotPreformActionError(String username, String organizationName, String action) {
        return String.format("The user %s is not the founder of organization %s and therefore can not %s.", username, organizationName, action);
    }

    public static String makeFounderCanNotResignError(String username, String organizationName) {
        return String.format("The user %s is the founder of organization %s and therefore can not resign.", username, organizationName);
    }

    public static String makeFounderCanNotBeRemovedError(String username, String organizationName) {
        return String.format("The user %s is the founder of organization %s and therefore can not be removed.", username, organizationName);
    }

    public static String makeUserIsNotAManagerError(String username, String organizationName) {
        return String.format("The user %s is not a manager in organization %s.", username, organizationName);
    }

    public static String makeVolunteeringAlreadyExistsError(int volunteeringId, String organizationName) {
        return String.format("The volunteering %d already exists in organization %s.", volunteeringId, organizationName);
    }

    public static String makeVolunteeringDoesNotExistsError(int volunteeringId, String organizationName) {
        return String.format("The volunteering %d does not exist in organization %s.", volunteeringId, organizationName);
    }

    public static String makeAssignManagerRequestAlreadyExistsError(String assignee, int organizationId) {
        return String.format("The user %s is already requested to become a manager of organization %d.", assignee, organizationId);
    }

    public static String makeAssignManagerRequestDoesNotExistError(String assignee, int organizationId) {
        return String.format("The user %s is not requested to become a manager of organization %d.", assignee, organizationId);
    }

    public static String makeOrganizationIdAlreadyExistsError(int organizationId) {
        return String.format("An organization with id %d already exists." ,organizationId);
    }

    public static String makeOrganizationIdDoesNotExistError(int organizationId) {
        return String.format("An organization with id %d does not exist." ,organizationId);
    }

    public static String makeInvalidOrganizationError() {
        return "This organization is invalid.";
    }

    public static String makeUserIsVolunteerInTheOrganizationError(String username, String organizationName) {
        return String.format("The user %s can not be a manager of organization %s since he is a volunteering in the organization.", username, organizationName);
    }
}
