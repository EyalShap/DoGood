package com.dogood.dogoodbackend.domain.users.notificiations;

public class NotificationNavigations {
    public static final String homepage = "/";
    public static String volunteering(int volunteeringId){
        return "/volunteering/"+volunteeringId;
    }
    public static String volunteeringJoinRequest(int volunteeringId){
        return volunteering(volunteeringId)+"/jrequests";
    }
    public static String volunteeringHourRequest(int volunteeringId){
        return volunteering(volunteeringId)+"/hrrequests";
    }
    public static String volunteeringChat(int volunteeringId){
        return volunteering(volunteeringId)+"/chat";
    }
    public static String volunteerPostVisitorChat(int postId){
        return "/volunteerPost/"+postId+"/chat";
    }
    public static String volunteerPostMemberChat(int postId, String other){
        return "/volunteerPost/"+postId+"/chat/"+other;
    }
    public static final String hoursSummary = "/my-profile";
}
