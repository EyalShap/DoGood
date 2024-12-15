package com.dogood.dogoodbackend.utils;

public class ReportErrors {
    public static String makeReportIdAlreadyExistsError(int reportId) {
        return String.format("A report with id %d already exists.", reportId);
    }

    public static String makeReportContentAlreadyExistsError() {
        return "This report already exists.";
    }

    public static String makeReportDoesNotExistError(int reportId){
        return String.format("A report with id %d does not exist.", reportId);
    }

    public static String makeUserUnauthorizedToMakeActionError(int userId, int reportId, String action) {
        return String.format("The user %d tried to %s report with id %d when not authorized.", userId, action, reportId);
    }

    public static String makeUserTriedToViewReportsError(int userId) {
        return String.format("The user %d tried to view reports when not admin.", userId);
    }

    public static String makeReportedPostDoesNotExistError(int postId) {
        return String.format("The reported post %d does not exist.", postId);
    }
}
