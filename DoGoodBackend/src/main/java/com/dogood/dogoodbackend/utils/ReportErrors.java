package com.dogood.dogoodbackend.utils;

import com.dogood.dogoodbackend.domain.reports.ReportKey;

public class ReportErrors {
    public static String makeReportAlreadyExistsError(ReportKey key) {
        return String.format("A report on %s by %s on %s already exists.", key.getReportedId(), key.getReportingUser(), key.getDate().toString());
    }

    public static String makeReportContentAlreadyExistsError() {
        return "This report already exists.";
    }

    public static String makeReportDoesNotExistError(ReportKey key) {
        return String.format("A report on %s by %s on %s does not exist.", key.getReportedId(), key.getReportingUser(), key.getDate().toString());
    }

    public static String makeUserUnauthorizedToMakeActionError(String user, String action) {
        return String.format("The user %s tried to %s report when not authorized.", user, action);
    }

    public static String makeUserTriedToViewReportsError(String user) {
        return String.format("The user %s tried to view reports when not admin.", user);
    }

    public static String makeReportedPostDoesNotExistError(int postId) {
        return String.format("The reported post %d does not exist.", postId);
    }
}
