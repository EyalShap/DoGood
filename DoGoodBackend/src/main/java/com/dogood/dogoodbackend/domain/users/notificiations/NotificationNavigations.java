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

    public static final String organizationList = "/organizationList";

    public static final String postsList = "/volunteeringPostList";

    public static String organization(int organizationId){
        return String.format("/organization/%d", organizationId);
    }

    public static String volunteeringPost(int id){
        return String.format("/volunteeringPost/%d", id);
    }

    public static String volunteerPost(int id){
        return String.format("/volunteerPost/%d", id);
    }

    public static String requests = "/managerRequestsList";
}
