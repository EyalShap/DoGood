package com.dogood.dogoodbackend.domain.volunteerings;

import com.dogood.dogoodbackend.domain.externalAIAPI.SkillsAndCategories;
import com.dogood.dogoodbackend.domain.externalAIAPI.SkillsAndCategoriesExtractor;
import com.dogood.dogoodbackend.domain.organizations.OrganizationDTO;
import com.dogood.dogoodbackend.domain.organizations.OrganizationsFacade;
import com.dogood.dogoodbackend.domain.posts.PostsFacade;
import com.dogood.dogoodbackend.domain.reports.ReportsFacade;
import com.dogood.dogoodbackend.domain.users.User;
import com.dogood.dogoodbackend.domain.users.UsersFacade;
import com.dogood.dogoodbackend.domain.users.notificiations.NotificationNavigations;
import com.dogood.dogoodbackend.domain.users.notificiations.NotificationSystem;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.*;
import com.dogood.dogoodbackend.pdfformats.PdfFactory;
import com.dogood.dogoodbackend.pdfformats.University;
import com.itextpdf.text.DocumentException;
import jakarta.transaction.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Transactional
public class VolunteeringFacade {
    private VolunteeringRepository repository;
    private final int MINUTES_ALLOWED = 15;
    private final double APPROVAL_PERCENTAGE = 0.75;
    private SchedulingFacade schedulingFacade;
    private OrganizationsFacade organizationFacade;
    private UsersFacade usersFacade;
    private PostsFacade postsFacade;
    private SkillsAndCategoriesExtractor extractor;
    private ReportsFacade reportsFacade;
    private NotificationSystem notificationSystem;


    public VolunteeringFacade(UsersFacade usersFacade, OrganizationsFacade organizationsFacade, VolunteeringRepository repository, SchedulingManager schedulingManager, SkillsAndCategoriesExtractor extractor) {
        this.usersFacade = usersFacade;
        this.schedulingFacade = new SchedulingFacade(schedulingManager);
        this.organizationFacade = organizationsFacade;
        this.repository = repository;
        this.extractor = extractor;
    }

    public void setReportFacade(ReportsFacade reportsFacade) {
        this.reportsFacade = reportsFacade;
    }

    public void setNotificationSystem(NotificationSystem notificationSystem) {
        this.notificationSystem = notificationSystem;
    }

    private boolean isManager(String userId, int organizationId){
        loggedInCheck(userId);
        return organizationFacade.isManager(userId, organizationId);
    }

    public void setPostsFacade(PostsFacade postsFacade) {
        this.postsFacade = postsFacade;
    }

    private void loggedInCheck(String userId){
        //do a logged in check and throw exception if not because i forgot
        if(false){
            throw new IllegalArgumentException("User " + userId + " is not logged in");
        }
    }

    private boolean userExists(String userId){
        loggedInCheck(userId);
        return usersFacade.userExists(userId);
    }

    private boolean isAdmin(String userId){
        return usersFacade.isAdmin(userId);
    }

    public int createVolunteering(String userId, int organizationId, String name, String description){
        if(!isManager(userId, organizationId)){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + organizationId);
        }
        Volunteering newVol = repository.addVolunteering(organizationId, name, description);
        return newVol.getId();
    }


    public void generateSkillsAndCategories(String userId, int volunteeringId) {
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        SkillsAndCategories skag = extractor.getSkillsAndCategories(volunteering.getName(), volunteering.getDescription(), new HashSet<>(getAllVolunteeringSkills()), new HashSet<>(getAllVolunteeringCategories()));
        repository.updateVolunteeringSkills(volunteeringId, skag.getSkills());
        repository.updateVolunteeringCategories(volunteeringId, skag.getCategories());
    }


    public void removeVolunteering(String userId, int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isAdmin(userId) && !isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " cannot remove volunteering " + volunteeringId);
        }
        repository.disableVolunteering(volunteeringId);
        postsFacade.removePostsByVolunteeringId(volunteeringId);
        schedulingFacade.removeAppointmentsAndRequestsForVolunteering(volunteeringId);
        organizationFacade.removeVolunteering(volunteering.getOrganizationId(), volunteeringId, userId);
        reportsFacade.removeVolunteeringReports(volunteeringId);
    }


    public void updateVolunteering(String userId, int volunteeringId, String name, String description){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        repository.updateVolunteering(volunteeringId, name, description);
        postsFacade.updateVolunteeringPostsKeywords(volunteeringId, userId);
    }


    public void updateVolunteeringSkills(String userId, int volunteeringId, List<String> skills){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        repository.updateVolunteeringSkills(volunteeringId, skills);
    }


    public void updateVolunteeringCategories(String userId, int volunteeringId, List<String> categories){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        repository.updateVolunteeringCategories(volunteeringId, categories);
    }


    public void updateVolunteeringScanDetails(String userId, int volunteeringId, ScanTypes scanTypes, ApprovalType approvalType){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        volunteering.setScanTypes(scanTypes);
        volunteering.setApprovalType(approvalType);
        repository.updateVolunteeringInDB(volunteering);
    }


    public void addImageToVolunteering(String userId, int volunteeringId, String imagePath){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        volunteering.addImagePath(imagePath);
        repository.updateVolunteeringInDB(volunteering);
    }


    public void removeImageFromVolunteering(String userId, int volunteeringId, String imagePath){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        volunteering.removeImagePath(imagePath);
        repository.updateVolunteeringInDB(volunteering);
    }

    public void scanCode(String userId, String code){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }
        String[] parts = code.replaceAll("\"", "").split(":");
        int volunteeringId = -1;
        try{
            volunteeringId = Integer.parseInt(parts[0]);
        }catch (NumberFormatException e){
            throw new IllegalArgumentException("Invalid volunteering id");
        }
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!volunteering.hasVolunteer(userId)){
            throw new IllegalArgumentException("User " + userId + " is not a volunteer in volunteering " + volunteeringId);
        }
        if(volunteering.getScanTypes() == ScanTypes.NO_SCAN){
            throw new UnsupportedOperationException("Volunteering " + volunteeringId + " does not support QR codes");
        }
        if(!volunteering.codeValid(parts[1])){
            throw new IllegalArgumentException("Invalid code");
        }
        Date first = repository.getFirstVolunteerScan(volunteeringId, userId);
        Date second = new Date();
        if(first != null && (second.getTime() - first.getTime()) >= TimeUnit.DAYS.toMillis(1)){
            first = null;
        }
        DatePair p = null;
        boolean approvalOk = true;
        if(first == null){
            if(volunteering.getScanTypes() == ScanTypes.ONE_SCAN){
                p = schedulingFacade.convertSingleTimeToAppointmentRange(userId, volunteeringId, new Date(), MINUTES_ALLOWED);
            }else{
                if(!schedulingFacade.getHasAppointmentAtRoughStart(userId, volunteeringId,second,MINUTES_ALLOWED)){
                    throw new UnsupportedOperationException("User " + userId + " does not have an appointment for volunteering " + volunteeringId + " starting now");
                }
                repository.recordFirstVolunteerScan(volunteeringId, userId);
                return;
            }
        }else{
            if(volunteering.getScanTypes() != ScanTypes.DOUBLE_SCAN){
                throw new UnsupportedOperationException("Volunteering " + volunteeringId + " does not support double scans");
            }
            p = schedulingFacade.convertRoughRangeToAppointmentRange(userId, volunteeringId, first, second, MINUTES_ALLOWED);
            repository.removeFirstVolunteerScan(volunteeringId, userId);
            long timeDid = TimeUnit.MILLISECONDS.toMinutes(second.getTime() - first.getTime());
            long timeRequired = TimeUnit.MILLISECONDS.toMinutes(p.getEnd().getTime() - p.getStart().getTime());
            approvalOk = timeDid >= timeRequired*APPROVAL_PERCENTAGE;
        }
        schedulingFacade.addHourApprovalRequest(userId, volunteeringId, p.getStart(), p.getEnd());
        if(volunteering.getApprovalType() == ApprovalType.AUTO_FROM_SCAN && approvalOk){
            schedulingFacade.approveUserHours(userId, volunteeringId, p.getStart(), p.getEnd()); //yipee
        }else{
            organizationFacade.notifyManagers("New hour approval request from " + userId, NotificationNavigations.volunteeringHourRequest(volunteeringId),volunteering.getOrganizationId());
        }
    }

    public String makeVolunteeringCode(String userId, int volunteeringId, boolean constant){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId() + " of volunteering " + volunteeringId);
        }
        String code = volunteering.generateCode(constant);
        repository.updateVolunteeringInDB(volunteering);
        return code;
    }


    public void requestToJoinVolunteering(String userId, int volunteeringId, String freeText){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(volunteering.hasVolunteer(userId)){
            throw new IllegalArgumentException("User " + userId + " is already a volunteer in volunteering " + volunteeringId);
        }
        if(isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is already a manager in organization of volunteering " + volunteeringId);
        }
        volunteering.addJoinRequest(userId, new JoinRequest(userId, freeText));
        repository.updateVolunteeringInDB(volunteering);
        organizationFacade.notifyManagers(userId+ " has requested to join volunteering " + volunteering.getName(), NotificationNavigations.volunteeringJoinRequest(volunteeringId),volunteering.getOrganizationId());
    }


    public void acceptUserJoinRequest(String userId, int volunteeringId, String joinerId, int groupId){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }
        if(!userExists(joinerId)){
            throw new IllegalArgumentException("User " + joinerId + " does not exist");
        }
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId() + " of volunteering " + volunteeringId);
        }
        if(volunteering.hasVolunteer(joinerId)){
            throw new IllegalArgumentException("User " + joinerId + " is already a volunteer in volunteering " + volunteeringId);
        }
        volunteering.approveJoinRequest(joinerId, groupId);
        usersFacade.addUserVolunteering(joinerId, volunteeringId);
        repository.updateVolunteeringInDB(volunteering);
        notificationSystem.notifyUser(joinerId,"You have been accepted to volunteering " + volunteering.getName() + ".",NotificationNavigations.volunteering(volunteeringId));
    }


    public void denyUserJoinRequest(String userId, int volunteeringId, String joinerId){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }
        if(!userExists(joinerId)){
            throw new IllegalArgumentException("User " + joinerId + " does not exist");
        }
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId() + " of volunteering " + volunteeringId);
        }
        if(volunteering.hasVolunteer(joinerId)){
            throw new IllegalArgumentException("User " + joinerId + " is already a volunteer in volunteering " + volunteeringId);
        }
        volunteering.denyJoinRequest(joinerId);
        repository.updateVolunteeringInDB(volunteering);
        notificationSystem.notifyUser(joinerId,"You have been denied from volunteering " + volunteering.getName() + ".",NotificationNavigations.homepage);
    }


    public void finishVolunteering(String userId, int volunteeringId, String experience){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!volunteering.hasVolunteer(userId)){
            throw new IllegalArgumentException("User " + userId + " is not a volunteer in volunteering " + volunteeringId);
        }
        volunteering.leaveVolunteering(userId, new PastExperience(userId, experience, new Date()));
        schedulingFacade.userLeave(volunteeringId, userId);
        usersFacade.addUserVolunteeringHistory(userId, volunteering.getDTO());
        usersFacade.removeUserVolunteering(userId, volunteeringId);
        repository.updateVolunteeringInDB(volunteering);
        organizationFacade.notifyManagers(userId + " has left volunteering " + volunteering.getName(), NotificationNavigations.volunteering(volunteeringId), volunteering.getOrganizationId());
    }


    public int addVolunteeringLocation(String userId, int volunteeringId, String name, AddressTuple address){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        int locId = volunteering.addLocation(name, address);
        repository.updateVolunteeringInDB(volunteering);
        return locId;
    }


    public void assignVolunteerToLocation(String userId, String volunteerId, int volunteeringId, int locId){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }

        if(!userExists(volunteerId)){
            throw new IllegalArgumentException("User " + volunteerId + " does not exist");
        }
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!volunteering.hasVolunteer(volunteerId)){
            throw new IllegalArgumentException("User " + volunteerId + " is not a volunteer in volunteering " + volunteeringId);
        }
        if(!userId.equals(volunteerId) && !isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " cannot assign " + volunteerId + " to a location");
        }
        volunteering.assignVolunteerToLocation(volunteerId, locId);
        repository.updateVolunteeringInDB(volunteering);
    }


    public void moveVolunteerGroup(String userId, String volunteerId, int volunteeringId, int groupId){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }

        if(!userExists(volunteerId)){
            throw new IllegalArgumentException("User " + volunteerId + " does not exist");
        }
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        if(!volunteering.hasVolunteer(volunteerId)){
            throw new IllegalArgumentException("User " + volunteerId + " is not a volunteer in volunteering " + volunteeringId);
        }
        volunteering.moveVolunteerToNewGroup(volunteerId, groupId);
        repository.updateVolunteeringInDB(volunteering);
        notificationSystem.notifyUser(volunteerId, "You have been moved to group " + groupId + " in volunteering " + volunteering.getName(), NotificationNavigations.volunteering(volunteeringId));
    }


    public int createNewGroup(String userId, int volunteeringId){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }

        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        int groupId = volunteering.addNewGroup();
        repository.updateVolunteeringInDB(volunteering);
        return groupId;
    }


    public void removeGroup(String userId, int volunteeringId, int groupId){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }

        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        volunteering.removeGroup(groupId);
        repository.updateVolunteeringInDB(volunteering);
    }

    public void removeRange(String userId, int volunteeringId, int rID) {
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }

        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        volunteering.removeRange(rID);
        schedulingFacade.removeAppointmentsOfRange(volunteeringId, rID);
        repository.updateVolunteeringInDB(volunteering);
    }


    public void removeLocation(String userId, int volunteeringId, int locId){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }

        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        List<Integer> rangesToRemove = volunteering.getRangeIdsForLocation(locId);
        volunteering.removeLocation(locId);
        for(Integer rangeId : rangesToRemove){
            schedulingFacade.removeAppointmentsOfRange(volunteeringId, rangeId);
        }
        repository.updateVolunteeringInDB(volunteering);
    }


    public int addScheduleRangeToGroup(String userId, int volunteeringId, int groupId, int locId, LocalTime startTime, LocalTime endTime, int minimumMinutes, int maximumMinutes, boolean[] weekDays, LocalDate oneTime){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }

        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        int rangeId = volunteering.addRangeToGroup(groupId, locId, startTime, endTime, minimumMinutes, maximumMinutes, weekDays, oneTime);
        if(weekDays == null && oneTime == null){
            throw new IllegalArgumentException("Week days and One time cannot both be null");
        }
        repository.updateVolunteeringInDB(volunteering);
        return rangeId;
    }

    public void addRestrictionToRange(String userId, int volunteeringId, int groupId, int locId, int rangeId, LocalTime startTime, LocalTime endTime, int amount){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }

        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        volunteering.addRestrictionToRange(groupId, locId, rangeId, new RestrictionTuple(startTime, endTime, amount));
        repository.updateVolunteeringInDB(volunteering);
    }

    public void removeRestrictionFromRange(String userId, int volunteeringId, int groupId, int locId, int rangeId, LocalTime startTime){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }

        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        volunteering.removeRestrictionFromRange(groupId, locId, rangeId, startTime);
        repository.updateVolunteeringInDB(volunteering);
    }

    public void updateRangeWeekdays(String userId, int volunteeringId, int rangeId, boolean[] weekdays){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }

        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        volunteering.updateRangeWeekdays(rangeId, weekdays);
        repository.updateVolunteeringInDB(volunteering);
    }


    public void updateRangeOneTimeDate(String userId, int volunteeringId, int rangeId, LocalDate oneTime){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }

        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        volunteering.updateRangeOneTimeDate(rangeId, oneTime);
        repository.updateVolunteeringInDB(volunteering);
    }


    public void makeAppointment(String userId, int volunteeringId, int groupId, int locId, int rangeId, LocalTime start, LocalTime end, boolean[] weekdays, LocalDate oneTime){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }

        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!volunteering.hasVolunteer(userId)){
            throw new IllegalArgumentException("User " + userId + " is not a volunteer in volunteering " + volunteeringId);
        }
        ScheduleRange range = volunteering.getScheduleRange(groupId, locId, rangeId);
        schedulingFacade.makeAppointment(userId, volunteeringId, range, start, end, weekdays, oneTime);
    }

    public void cancelAppointment(String userId, int volunteeringId, LocalTime start){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!volunteering.hasVolunteer(userId)){
            throw new IllegalArgumentException("User " + userId + " is not a volunteer in volunteering " + volunteeringId);
        }
        schedulingFacade.cancelAppointment(userId, volunteeringId, start);
    }

    public void clearConstantCodes(String userId, int volunteeringId){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }

        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        volunteering.clearConstantCodes();
        repository.updateVolunteeringInDB(volunteering);
    }


    public void requestHoursApproval(String userId, int volunteeringId, Date start, Date end){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!volunteering.hasVolunteer(userId)){
            throw new IllegalArgumentException("User " + userId + " is not a volunteer in volunteering " + volunteeringId);
        }
        schedulingFacade.addHourApprovalRequest(userId, volunteeringId, start, end);
        organizationFacade.notifyManagers("New hour approval request from " + userId, NotificationNavigations.volunteeringHourRequest(volunteeringId),volunteering.getOrganizationId());
    }


    public void approveUserHours(String userId, int volunteeringId, String volunteerId, Date start, Date end){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }
        if(!userExists(volunteerId)){
            throw new IllegalArgumentException("User " + volunteerId + " does not exist");
        }
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId() + " of volunteering " + volunteeringId);
        }
        if(!volunteering.hasVolunteer(volunteerId)){
            throw new IllegalArgumentException("User " + volunteerId + " is not a volunteer in volunteering " + volunteeringId);
        }
        schedulingFacade.approveUserHours(volunteerId, volunteeringId, start, end);
        notificationSystem.notifyUser(volunteerId, "Your hour request from in volunteering " + volunteering.getName()+ " has been approved", NotificationNavigations.hoursSummary);
    }


    public void denyUserHours(String userId, int volunteeringId, String volunteerId, Date start, Date end){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }
        if(!userExists(volunteerId)){
            throw new IllegalArgumentException("User " + volunteerId + " does not exist");
        }
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId() + " of volunteering " + volunteeringId);
        }
        if(!volunteering.hasVolunteer(volunteerId)){
            throw new IllegalArgumentException("User " + volunteerId + " is not a volunteer in volunteering " + volunteeringId);
        }
        schedulingFacade.denyUserHours(volunteerId, volunteeringId, start, end);
        notificationSystem.notifyUser(volunteerId, "Your hour request from in volunteering " + volunteering.getName()+ " has been denied", NotificationNavigations.hoursSummary);
    }


    public VolunteeringDTO getVolunteeringDTO(int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return volunteering.getDTO();
    }

    public List<VolunteeringDTO> getVolunteeringsOfUser(String username){
        List<Volunteering> volunteerings = repository.getAllVolunteerings();
        return volunteerings.stream()
                .filter(volunteering -> volunteering.hasVolunteer(username) || organizationFacade.isManager(username, volunteering.getOrganizationId()))
                .map(volunteering -> volunteering.getDTO()).toList();
    }


    public List<String> getVolunteeringSkills(int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return volunteering.getSkills();
    }


    public List<String> getVolunteeringCategories(int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return volunteering.getCategories();
    }


    public int getVolunteeringOrganizationId(int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return volunteering.getOrganizationId();
    }


    public List<LocationDTO> getVolunteeringLocations(int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return volunteering.getLocationDTOs();
    }

    public List<LocationDTO> getGroupLocations(int volunteeringId, int groupId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return volunteering.getGroupLocations(groupId);
    }

    public List<Integer> getVolunteeringGroups(int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return new LinkedList<>(volunteering.getGroups().keySet());
    }


    public Map<String,Integer> getVolunteeringVolunteers(int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return volunteering.getVolunteerToGroup();
    }

    public GroupDTO getGroupDTO(int volunteeringId, int groupId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return volunteering.getGroupDTO(groupId);
    }


    public List<HourApprovalRequest> getUserApprovedHours(String userId, List<Integer> volunteeringIds){
        return schedulingFacade.getUserApprovedHours(userId,volunteeringIds);
    }

    public String getUserApprovedHoursFormatted(String userId, int volunteeringId, String israeliId) throws DocumentException, IOException {
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        User userData = usersFacade.getUser(userId);
        OrganizationDTO orgData = organizationFacade.getOrganization(volunteering.getOrganizationId());
        PdfFactory factory = new PdfFactory();
        String[] nameSplit = userData.getName().split(" "); //TODO: make this better
        String email = userData.getEmails().get(0); //TODO: make this better
        return factory.createFormat(University.getUniversity(email),
                orgData.getName(),
                nameSplit[0],
                nameSplit.length > 1 ? nameSplit[1] : "",
                israeliId,
                userData.getPhone(),
                email,
                getUserApprovedHours(userId, List.of(volunteeringId)));
    }

    public String getAppointmentsCsv(String userId, int numOfWeeks) throws IOException {
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }
        User userData = usersFacade.getUser(userId);
        String csv = "Subject,Start Date,Start Time,End Time\n";
        List<ScheduleAppointmentDTO> scheduleAppointments = schedulingFacade.getUserAppointments(userId,userData.getVolunteeringIds());
        for(ScheduleAppointmentDTO scheduleAppointmentDTO : scheduleAppointments){
            csv += scheduleAppointmentDTO.toCsv(getVolunteeringDTO(scheduleAppointmentDTO.getVolunteeringId()).getName(),numOfWeeks);
        }
        Files.createDirectories(Paths.get("./"+userId));
        String path = "./"+userId + "/"+"export-"+userId+".csv";
        PrintWriter csvWriter = new PrintWriter(path);
        csvWriter.print(csv);
        csvWriter.close();
        return path;
    }


    public List<ScheduleAppointmentDTO> getVolunteerAppointments(String userId, int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!volunteering.hasVolunteer(userId)){
            throw new IllegalArgumentException("User " + userId + " is not a volunteer in volunteering " + volunteeringId);
        }
        return schedulingFacade.getUserAppointments(userId, volunteeringId);
    }


    public List<ScheduleRangeDTO> getVolunteerAvailableRanges(String userId, int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!volunteering.hasVolunteer(userId)){
            throw new IllegalArgumentException("User " + userId + " is not a volunteer in volunteering " + volunteeringId);
        }
        return volunteering.getVolunteerAvailableRanges(userId);
    }

    public List<ScheduleRangeDTO> getVolunteeringLocationGroupRanges(String userId, int volunteeringId, int groupId, int locId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        checkViewingPermissions(userId, volunteeringId);
        return volunteering.getLocationGroupRanges(groupId,locId);
    }


    public List<HourApprovalRequest> getVolunteeringHourRequests(String userId, int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId() + " of volunteering " + volunteeringId);
        }
        return schedulingFacade.getHourApprovalRequests(volunteeringId);
    }


    public Map<String, JoinRequest> getVolunteeringJoinRequests(String userId, int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId() + " of volunteering " + volunteeringId);
        }
        return volunteering.getPendingJoinRequests();
    }


    public List<String> getConstantCodes(String userId, int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId() + " of volunteering " + volunteeringId);
        }
        return volunteering.getConstantCodes();
    }

    public List<String> getVolunteeringWarnings(String userId, int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId() + " of volunteering " + volunteeringId);
        }
        List<String> warnings = volunteering.getWarnings();
        if(!postsFacade.hasPosts(volunteeringId)){
            warnings.add("Volunteering isn't published. Use \"Post Volunteering\" so users can find and join your volunteering.");
        }
        return warnings;
    }

    public void checkViewingPermissions(String userId, int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!volunteering.hasVolunteer(userId) && !isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " has no permission to view " + " volunteering with id " + volunteeringId);
        }
    }


    public boolean getHasVolunteer(String userId, int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return volunteering.hasVolunteer(userId);
    }


    public int getUserAssignedLocation(String userId, int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return volunteering.getAssignedLocation(userId);
    }

    public int getVolunteerGroup(String userId, int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return volunteering.getVolunteerGroup(userId);
    }

    public LocationDTO getUserAssignedLocationData(String userId, int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return volunteering.getAssignedLocationData(userId);
    }


    public List<PastExperience> getVolunteeringPastExperiences(int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return volunteering.getPastExperiences();
    }

    public boolean userHasSettingsPermission(String userId, int volunteeringId) {
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return isManager(userId, volunteering.getOrganizationId());
    }

    public ScanTypes getVolunteeringScanType(String userId, int volunteeringId) {
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return volunteering.getScanTypes();
    }

    public ApprovalType getVolunteeringApprovalType(String userId, int volunteeringId) {
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return volunteering.getApprovalType();
    }

    public List<String> getVolunteeringImages(int volunteeringId) {
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return volunteering.getImagePaths();
    }

    public List<String> getAllVolunteeringSkills() {
        List<String> all = new LinkedList<>();
        for(Volunteering v : repository.getAllVolunteerings()){
            all.addAll(v.getSkills());
        }
        return all;
    }

    public List<String> getAllVolunteeringCategories() {
        List<String> all = new LinkedList<>();
        for(Volunteering v : repository.getAllVolunteerings()){
            all.addAll(v.getCategories());
        }
        return all;
    }

    public void disableVolunteeringLocations(String userId, int volunteeringId) {
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        volunteering.disableLocations();
        repository.updateVolunteeringInDB(volunteering);
    }

    public List<String> getVolunteeringChatMembers(int volunteeringId){
        List<String> members = new LinkedList<>();
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        members.addAll(volunteering.getVolunteerToGroup().keySet());
        members.addAll(organizationFacade.getOrganization(volunteering.getOrganizationId()).getManagerUsernames());
        return members;
    }
}
