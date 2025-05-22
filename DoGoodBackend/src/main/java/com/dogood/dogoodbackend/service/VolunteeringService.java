package com.dogood.dogoodbackend.service;


import com.dogood.dogoodbackend.domain.organizations.OrganizationDTO;
import com.dogood.dogoodbackend.domain.users.User;
import com.dogood.dogoodbackend.domain.volunteerings.*;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.HourApprovalRequest;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.ScheduleAppointmentDTO;
import com.dogood.dogoodbackend.pdfformats.PdfFactory;
import com.dogood.dogoodbackend.pdfformats.University;
import com.itextpdf.text.DocumentException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

import static java.lang.Thread.sleep;

@Service
@Transactional
public class VolunteeringService {

    private FacadeManager facadeManager;

    @Autowired
    public VolunteeringService(FacadeManager facadeManager) {
        this.facadeManager = facadeManager;

/*
        //frontend testing scenarios
        int orgid = facadeManager.getOrganizationsFacade().createOrganization("OrgOrg",
                "i dont know what to write here this will never be relevant for me",
                "052-0520520",
                "irefuse@this.is.irelevant",
                "TheDoctor");
        int volId = facadeManager.getOrganizationsFacade().createVolunteering(orgid,"Clearing The Backrooms Together",
                "The Backrooms of 72 are mysterious areas, together we can clear them and help them become normal",
                "TheDoctor");
        facadeManager.getOrganizationsFacade().createVolunteering(orgid,"Construction of sleep pods",
                "mmmm sleep pods",
                "TheDoctor");
        facadeManager.getVolunteeringFacade().updateVolunteeringScanDetails("TheDoctor", volId, ScanTypes.DOUBLE_SCAN, ApprovalType.AUTO_FROM_SCAN);


        int locId = facadeManager.getVolunteeringFacade().addVolunteeringLocation("TheDoctor", volId, "The Backrooms", new AddressTuple("B7", "Ben Gurion", "72"));
        int rID = facadeManager.getVolunteeringFacade().addScheduleRangeToGroup("TheDoctor", volId, 0, locId, LocalTime.of(0,0), LocalTime.of(23,59), -1,-1);
        facadeManager.getVolunteeringFacade().updateRangeWeekdays("TheDoctor",volId, 0, locId, rID, new boolean[]{true,true,true,true,true,true,true});
        facadeManager.getVolunteeringFacade().assignVolunteerToLocation("TheDoctor", "EyalShapiro", volId, locId);
        facadeManager.getVolunteeringFacade().makeAppointment("EyalShapiro", volId, 0, locId, rID, LocalTime.of(9,0), LocalTime.of(11,0), null, LocalDate.of(2025, 1, 5));
        facadeManager.getVolunteeringFacade().makeAppointment("EyalShapiro", volId, 0, locId, rID, LocalTime.of(13,0), LocalTime.of(14,0), new boolean[]{false,true,false,false,false,true,false}, null);

        facadeManager.getVolunteeringFacade().requestHoursApproval("EyalShapiro", volId, Date.from(LocalDateTime.of(2025,1,6,12,0).atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(LocalDateTime.of(2025,1,6,14,0).atZone(ZoneId.systemDefault()).toInstant()));
        facadeManager.getVolunteeringFacade().requestHoursApproval("EyalShapiro", volId, Date.from(LocalDateTime.of(2025,1,6,10,0).atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(LocalDateTime.of(2025,1,6,12,0).atZone(ZoneId.systemDefault()).toInstant()));

        facadeManager.getPostsFacade().createVolunteeringPost("post1", "description1", "TheDoctor", volId);
        facadeManager.getVolunteeringFacade().requestToJoinVolunteering("DanaFriedman", 1, "plz i also want join");
        facadeManager.getVolunteeringFacade().acceptUserJoinRequest("TheDoctor", volId, "DanaFriedman", 0);
        int locId2 = facadeManager.getVolunteeringFacade().addVolunteeringLocation("TheDoctor", volId, "The Poolrooms", new AddressTuple("B7", "Ben Gurion", "Sports Centre"));
        int rID2 = facadeManager.getVolunteeringFacade().addScheduleRangeToGroup("TheDoctor", volId, 0, locId2, LocalTime.of(14,0), LocalTime.of(20,0), -1,120);
        facadeManager.getVolunteeringFacade().updateRangeWeekdays("TheDoctor",volId, 0, locId2, rID2, new boolean[]{false,true,false,false,true,true,false});
        facadeManager.getVolunteeringFacade().assignVolunteerToLocation("TheDoctor", "DanaFriedman", volId, locId2);
        int rID3 = facadeManager.getVolunteeringFacade().addScheduleRangeToGroup("TheDoctor", volId, 0, locId2, LocalTime.of(18,0), LocalTime.of(20,0), 60,120);
        facadeManager.getVolunteeringFacade().updateRangeOneTimeDate("TheDoctor", volId, 0, locId2, rID3, LocalDate.of(2025, 1, 14));*/

        /*this.facadeManager.getUsersFacade().register("EyalManager", "123456", "Eyal Manager", "eyalm1000@gmail.com", "0528585519", new Date());
        this.facadeManager.getUsersFacade().register("DanaManager", "123456", "Dana Manager", "dafr@post.bgu.ac.il", "0520391312", new Date());

        int orgId = this.facadeManager.getOrganizationsFacade().createOrganization("Dana Corp", "Company of Dana", "0520391312", "dafr@post.bgu.ac.il", "DanaManager");
        int volId = this.facadeManager.getOrganizationsFacade().createVolunteering(orgId, "Training Puppies", "Help us train the puppies", "DanaManager");
        this.facadeManager.getVolunteeringFacade().addImageToVolunteering("DanaManager", volId, "https://s3.amazonaws.com/cdn-origin-etr.akc.org/wp-content/uploads/2022/07/20112512/American-Eskimo-Dog-puppy-running-outdoors.jpg");
        this.facadeManager.getPostsFacade().createVolunteeringPost("We are training puppies!", "Come join us to help train the puppies", "DanaManager", volId);*/
        ////this.facadeManager.getVolunteeringFacade().updateVolunteeringSkills("DanaManager", volId, List.of("Training", "Animal Care"));
        ////this.facadeManager.getVolunteeringFacade().updateVolunteeringCategories("DanaManager", volId, List.of("Animals", "Sports", "Puppies"));


        // Step 1: Register Managers
        /*this.facadeManager.getUsersFacade().register("AliceManager", "password123", "Alice Manager", "alice.manager@gmail.com", "0521234567", new Date());
        this.facadeManager.getUsersFacade().register("BobManager", "password456", "Bob Manager", "bob.manager@gmail.com", "0529876543", new Date());
        this.facadeManager.getUsersFacade().register("CharlieManager", "password789", "Charlie Manager", "charlie.manager@gmail.com", "0531112233", new Date());
        this.facadeManager.getUsersFacade().register("DinaManager", "password321", "Dina Manager", "dina.manager@gmail.com", "0541239876", new Date());
        this.facadeManager.getUsersFacade().register("EyalManager", "password654", "Eyal Manager", "eyal.manager@gmail.com", "0549873210", new Date());
        this.facadeManager.getUsersFacade().register("DanaManager", "123456", "Dana Manager", "dana.manager@gmail.com", "0549873210", new Date());
        this.facadeManager.getUsersFacade().register("EyalShap", "123456", "אייל שפירו", "eyald@post.bgu.ac.il", "0528585519", Date.from(LocalDate.of(2003,10,19).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        this.facadeManager.getUsersFacade().register("FionaManager", "password555", "Fiona Manager", "fiona.manager@gmail.com", "0539876543", new Date());
        this.facadeManager.getUsersFacade().register("GeorgeManager", "password999", "George Manager", "george.manager@gmail.com", "0523219876", new Date());

// Step 2: Create Organizations
        int orgId1 = this.facadeManager.getOrganizationsFacade().createOrganization("Happy Tails", "Animal Welfare Organization", "0521234567", "contact@happytails.org", "AliceManager");
        int orgId2 = this.facadeManager.getOrganizationsFacade().createOrganization("GreenThumbs", "Environmental Enthusiasts", "0529876543", "info@greenthumbs.com", "BobManager");
        int orgId3 = this.facadeManager.getOrganizationsFacade().createOrganization("FitForLife", "Promoting Healthy Lifestyles", "0531112233", "support@fitforlife.org", "CharlieManager");
        int orgId4 = this.facadeManager.getOrganizationsFacade().createOrganization("Foodies United", "Fighting Hunger Together", "0541239876", "hello@foodiesunited.com", "DinaManager");
        int orgId5 = this.facadeManager.getOrganizationsFacade().createOrganization("Code4Good", "Tech for Social Impact", "0549873210", "support@code4good.org", "EyalManager");
        int orgId6 = this.facadeManager.getOrganizationsFacade().createOrganization("Literacy League", "Promoting Education", "0539876543", "contact@literacyleague.org", "FionaManager");
        int orgId7 = this.facadeManager.getOrganizationsFacade().createOrganization("Senior Supporters", "Supporting the Elderly", "0523219876", "info@seniorsupporters.org", "GeorgeManager");

// Step 3: Add Volunteering Opportunities and Posts

// Organization 1: Happy Tails
        int volId1 = this.facadeManager.getOrganizationsFacade().createVolunteering(orgId1, "Dog Walking", "Walk and care for shelter dogs", "AliceManager");
        this.facadeManager.getVolunteeringFacade().updateVolunteeringSkills("AliceManager", volId1, List.of("Animal Care", "Physical Activity"));
        this.facadeManager.getVolunteeringFacade().updateVolunteeringCategories("AliceManager", volId1, List.of("Animals", "Health"));
        this.facadeManager.getPostsFacade().createVolunteeringPost("Walk Shelter Dogs!", "Join us to keep our furry friends happy and active.", "AliceManager", volId1);
        this.facadeManager.getVolunteeringFacade().addVolunteeringLocation("AliceManager", volId1, "Tel Aviv", new AddressTuple("Tel Aviv", "Tel Aviv", "Tel Aviv"));
        this.facadeManager.getVolunteeringFacade().addVolunteeringLocation("AliceManager", volId1, "Jerus", new AddressTuple("Jerus", "Jerus", "Jerus"));

        int volId2 = this.facadeManager.getOrganizationsFacade().createVolunteering(orgId1, "Cat Cuddling", "Provide love and care to shelter cats", "AliceManager");
        this.facadeManager.getVolunteeringFacade().updateVolunteeringSkills("AliceManager", volId2, List.of("Patience", "Compassion"));
        this.facadeManager.getVolunteeringFacade().updateVolunteeringCategories("AliceManager", volId2, List.of("Animals", "Mental Health"));
        this.facadeManager.getPostsFacade().createVolunteeringPost("Cuddle Shelter Cats!", "Spend quality time with our adorable shelter cats.", "AliceManager", volId2);
        this.facadeManager.getVolunteeringFacade().addVolunteeringLocation("AliceManager", volId2, "Tel Aviv", new AddressTuple("Tel Aviv", "Tel Aviv", "Tel Aviv"));
        this.facadeManager.getVolunteeringFacade().addVolunteeringLocation("AliceManager", volId2, "Beer Sheva", new AddressTuple("Beer Sheva", "Beer Sheva", "Beer Sheva"));

// Organization 2: GreenThumbs
        int volId3 = this.facadeManager.getOrganizationsFacade().createVolunteering(orgId2, "Planting Trees", "Help us plant trees in urban areas", "BobManager");
        this.facadeManager.getVolunteeringFacade().updateVolunteeringSkills("BobManager", volId3, List.of("Gardening", "Teamwork"));
        this.facadeManager.getVolunteeringFacade().updateVolunteeringCategories("BobManager", volId3, List.of("Environment", "Health"));
        this.facadeManager.getPostsFacade().createVolunteeringPost("Plant Trees in the City!", "Be part of a greener future by planting trees with us.", "BobManager", volId3);

        int volId4 = this.facadeManager.getOrganizationsFacade().createVolunteering(orgId2, "Beach Cleanup", "Join us to clean up our local beaches", "BobManager");
        this.facadeManager.getVolunteeringFacade().updateVolunteeringSkills("BobManager", volId4, List.of("Cleaning", "Teamwork"));
        this.facadeManager.getVolunteeringFacade().updateVolunteeringCategories("BobManager", volId4, List.of("Environment", "Social Impact"));
        this.facadeManager.getPostsFacade().createVolunteeringPost("Beach Cleanup Crew Needed!", "Help us protect marine life by cleaning up the beaches.", "BobManager", volId4);

// Organization 3: FitForLife
        int volId5 = this.facadeManager.getOrganizationsFacade().createVolunteering(orgId3, "Yoga Sessions", "Lead yoga sessions for the community", "CharlieManager");
        this.facadeManager.getVolunteeringFacade().updateVolunteeringSkills("CharlieManager", volId5, List.of("Fitness", "Leadership"));
        this.facadeManager.getVolunteeringFacade().updateVolunteeringCategories("CharlieManager", volId5, List.of("Health", "Mental Wellness"));
        this.facadeManager.getPostsFacade().createVolunteeringPost("Lead Yoga Classes!", "Volunteer to bring mindfulness and fitness to our community.", "CharlieManager", volId5);

// Organization 4: Foodies United
        int volId6 = this.facadeManager.getOrganizationsFacade().createVolunteering(orgId4, "Soup Kitchen Helper", "Assist in preparing and serving meals", "DinaManager");
        this.facadeManager.getVolunteeringFacade().updateVolunteeringSkills("DinaManager", volId6, List.of("Cooking", "Teamwork"));
        this.facadeManager.getVolunteeringFacade().updateVolunteeringCategories("DinaManager", volId6, List.of("Community", "Food"));
        this.facadeManager.getPostsFacade().createVolunteeringPost("Help at the Soup Kitchen!", "Join us to provide warm meals to those in need.", "DinaManager", volId6);

// Organization 5: Code4Good
        int volId7 = this.facadeManager.getOrganizationsFacade().createVolunteering(orgId5, "Teach Coding", "Volunteer to teach coding to kids", "EyalManager");
        this.facadeManager.getVolunteeringFacade().updateVolunteeringSkills("EyalManager", volId7, List.of("Programming", "Teaching"));
        this.facadeManager.getVolunteeringFacade().updateVolunteeringCategories("EyalManager", volId7, List.of("Education", "Technology"));
        this.facadeManager.getPostsFacade().createVolunteeringPost("Teach Kids to Code!", "Make a difference by teaching coding skills to children.", "EyalManager", volId7);

// Organization 6: Literacy League
        int volId8 = this.facadeManager.getOrganizationsFacade().createVolunteering(orgId6, "Book Reading Sessions", "Read books to children in libraries", "FionaManager");
        this.facadeManager.getVolunteeringFacade().updateVolunteeringSkills("FionaManager", volId8, List.of("Storytelling", "Patience"));
        this.facadeManager.getVolunteeringFacade().updateVolunteeringCategories("FionaManager", volId8, List.of("Education", "Community"));
        this.facadeManager.getPostsFacade().createVolunteeringPost("Volunteer as a Storyteller!", "Bring stories to life for kids in libraries.", "FionaManager", volId8);

// Organization 7: Senior Supporters
        int volId9 = this.facadeManager.getOrganizationsFacade().createVolunteering(orgId7, "Home Visit Volunteer", "Spend time with lonely seniors", "GeorgeManager");
        this.facadeManager.getVolunteeringFacade().updateVolunteeringSkills("GeorgeManager", volId9, List.of("Compassion", "Communication"));
        this.facadeManager.getVolunteeringFacade().updateVolunteeringCategories("GeorgeManager", volId9, List.of("Community", "Mental Wellness"));
        this.facadeManager.getPostsFacade().createVolunteeringPost("Support the Elderly!", "Visit seniors and brighten their day.", "GeorgeManager", volId9);
// Organization 3: FitForLife
        int volId10 = this.facadeManager.getOrganizationsFacade().createVolunteering(orgId3, "Healthy Cooking Workshops", "Teach community members how to cook healthy meals", "CharlieManager");
        this.facadeManager.getVolunteeringFacade().updateVolunteeringSkills("CharlieManager", volId10, List.of("Cooking", "Teaching"));
        this.facadeManager.getVolunteeringFacade().updateVolunteeringCategories("CharlieManager", volId10, List.of("Health", "Education"));
        this.facadeManager.getPostsFacade().createVolunteeringPost("Teach Healthy Cooking!", "Volunteer to run workshops that promote healthy eating.", "CharlieManager", volId10);

// Organization 4: Foodies United
        int volId11 = this.facadeManager.getOrganizationsFacade().createVolunteering(orgId4, "Food Bank Organizer", "Help organize food donations at the local food bank", "DinaManager");
        this.facadeManager.getVolunteeringFacade().updateVolunteeringSkills("DinaManager", volId11, List.of("Organization", "Compassion"));
        this.facadeManager.getVolunteeringFacade().updateVolunteeringCategories("DinaManager", volId11, List.of("Community", "Food"));
        this.facadeManager.getPostsFacade().createVolunteeringPost("Help Organize Food Donations!", "Support your community by organizing food for those in need.", "DinaManager", volId11);

        int volId12 = this.facadeManager.getOrganizationsFacade().createVolunteering(orgId4, "Meal Delivery Driver", "Deliver meals to families in need", "DinaManager");
        this.facadeManager.getVolunteeringFacade().updateVolunteeringSkills("DinaManager", volId12, List.of("Driving", "Compassion"));
        this.facadeManager.getVolunteeringFacade().updateVolunteeringCategories("DinaManager", volId12, List.of("Community", "Social Impact"));
        this.facadeManager.getPostsFacade().createVolunteeringPost("Deliver Meals to Families!", "Make a direct impact by delivering meals to those in need.", "DinaManager", volId12);

// Organization 5: Code4Good
        int volId13 = this.facadeManager.getOrganizationsFacade().createVolunteering(orgId5, "Coding Mentorship", "Mentor students learning to code", "EyalManager");
        this.facadeManager.getVolunteeringFacade().updateVolunteeringSkills("EyalManager", volId13, List.of("Programming", "Mentorship"));
        this.facadeManager.getVolunteeringFacade().updateVolunteeringCategories("EyalManager", volId13, List.of("Education", "Technology"));
        this.facadeManager.getPostsFacade().createVolunteeringPost("Mentor Aspiring Coders!", "Share your programming skills with the next generation.", "EyalManager", volId13);

        int volId14 = this.facadeManager.getOrganizationsFacade().createVolunteering(orgId5, "App Development Team", "Collaborate on building a charity app", "EyalManager");
        this.facadeManager.getVolunteeringFacade().updateVolunteeringSkills("EyalManager", volId14, List.of("Programming", "Teamwork", "Design"));
        this.facadeManager.getVolunteeringFacade().updateVolunteeringCategories("EyalManager", volId14, List.of("Technology", "Social Impact"));
        this.facadeManager.getPostsFacade().createVolunteeringPost("Build an App for Good!", "Join a team to develop apps that create social impact.", "EyalManager", volId14);

// Organization 6: Literacy League
        int volId15 = this.facadeManager.getOrganizationsFacade().createVolunteering(orgId6, "Reading Buddy", "Help children improve their reading skills", "FionaManager");
        this.facadeManager.getVolunteeringFacade().updateVolunteeringSkills("FionaManager", volId15, List.of("Patience", "Teaching"));
        this.facadeManager.getVolunteeringFacade().updateVolunteeringCategories("FionaManager", volId15, List.of("Education", "Community"));
        this.facadeManager.getPostsFacade().createVolunteeringPost("Be a Reading Buddy!", "Support kids by helping them become confident readers.", "FionaManager", volId15);

        int volId16 = this.facadeManager.getOrganizationsFacade().createVolunteering(orgId6, "Library Organizer", "Organize books and manage library resources", "FionaManager");
        this.facadeManager.getVolunteeringFacade().updateVolunteeringSkills("FionaManager", volId16, List.of("Organization", "Attention to Detail"));
        this.facadeManager.getVolunteeringFacade().updateVolunteeringCategories("FionaManager", volId16, List.of("Education", "Community"));
        this.facadeManager.getPostsFacade().createVolunteeringPost("Organize a Library!", "Help us create a welcoming and organized library space.", "FionaManager", volId16);

// Organization 7: Senior Supporters
        int volId17 = this.facadeManager.getOrganizationsFacade().createVolunteering(orgId7, "Elderly Companion", "Spend time with seniors to combat loneliness", "GeorgeManager");
        this.facadeManager.getVolunteeringFacade().updateVolunteeringSkills("GeorgeManager", volId17, List.of("Compassion", "Listening"));
        this.facadeManager.getVolunteeringFacade().updateVolunteeringCategories("GeorgeManager", volId17, List.of("Community", "Mental Health"));
        this.facadeManager.getPostsFacade().createVolunteeringPost("Be a Companion for Seniors!", "Make a difference in the lives of elderly people by being a friendly companion.", "GeorgeManager", volId17);

        int volId18 = this.facadeManager.getOrganizationsFacade().createVolunteering(orgId7, "Event Coordinator for Seniors", "Organize activities and events for the elderly", "GeorgeManager");
        this.facadeManager.getVolunteeringFacade().updateVolunteeringSkills("GeorgeManager", volId18, List.of("Organization", "Creativity"));
        this.facadeManager.getVolunteeringFacade().updateVolunteeringCategories("GeorgeManager", volId18, List.of("Community", "Social Impact"));
        this.facadeManager.getPostsFacade().createVolunteeringPost("Plan Events for Seniors!", "Bring joy to seniors by organizing fun events.", "GeorgeManager", volId18);

// Organization 1: Happy Tails (Again)
        int volId19 = this.facadeManager.getOrganizationsFacade().createVolunteering(orgId1, "Dog Training Sessions", "Help train shelter dogs to improve adoption chances", "AliceManager");
        this.facadeManager.getVolunteeringFacade().updateVolunteeringSkills("AliceManager", volId19, List.of("Animal Training", "Patience"));
        this.facadeManager.getVolunteeringFacade().updateVolunteeringCategories("AliceManager", volId19, List.of("Animals", "Education"));
        this.facadeManager.getPostsFacade().createVolunteeringPost("Train Shelter Dogs!", "Volunteer to help our shelter dogs find loving homes.", "AliceManager", volId19);
        this.facadeManager.getVolunteeringFacade().addVolunteeringLocation("AliceManager", volId19, "Beer Sheva", new AddressTuple("Beer Sheva", "Beer Sheva", "Beer Sheva"));

        int volId20 = this.facadeManager.getOrganizationsFacade().createVolunteering(orgId1, "Pet Adoption Events", "Assist at events to match pets with families", "AliceManager");
        this.facadeManager.getVolunteeringFacade().updateVolunteeringSkills("AliceManager", volId20, List.of("Event Planning", "Communication"));
        this.facadeManager.getVolunteeringFacade().updateVolunteeringCategories("AliceManager", volId20, List.of("Animals", "Community"));
        this.facadeManager.getPostsFacade().createVolunteeringPost("Help at Adoption Events!", "Join us to find forever homes for our furry friends.", "AliceManager", volId20);

        this.facadeManager.getPostsFacade().createVolunteerPost("Hiii", "HI", "AliceManager");

        facadeManager.getVolunteeringFacade().requestToJoinVolunteering("BobManager", volId1, "plz i want join");
        facadeManager.getVolunteeringFacade().acceptUserJoinRequest("AliceManager", volId1, "BobManager", 0);*/
    }

    private void checkToken(String token, String username) {
        if (!facadeManager.getAuthFacade().getNameFromToken(token).equals(username)) {
            throw new IllegalArgumentException("Invalid token");
        }
    }

    public Response<String> removeVolunteering(String token, String userId, int volunteeringId) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().removeVolunteering(userId, volunteeringId);
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> generateSkillsAndCategories(String token, String userId, int volunteeringId) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().generateSkillsAndCategories(userId, volunteeringId);
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> updateVolunteering(String token, String userId, int volunteeringId, String name, String description) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().updateVolunteering(userId, volunteeringId, name, description);
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> updateVolunteeringSkills(String token, String userId, int volunteeringId, List<String> skills) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().updateVolunteeringSkills(userId, volunteeringId, skills);
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> updateVolunteeringCategories(String token, String userId, int volunteeringId, List<String> categories) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().updateVolunteeringCategories(userId, volunteeringId, categories);
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> updateVolunteeringScanDetails(String token, String userId, int volunteeringId, ScanTypes scanTypes, ApprovalType approvalType) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().updateVolunteeringScanDetails(userId, volunteeringId, scanTypes, approvalType);
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> scanCode(String token, String userId, String code) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().scanCode(userId, code);
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> makeVolunteeringCode(String token, String userId, int volunteeringId, boolean constant) {
        try {
            checkToken(token, userId);
            String code = facadeManager.getVolunteeringFacade().makeVolunteeringCode(userId, volunteeringId, constant);
            return Response.createResponse(code, null);
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> requestToJoinVolunteering(String token, String userId, int volunteeringId, String freeText) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().requestToJoinVolunteering(userId, volunteeringId, freeText);
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> acceptUserJoinRequest(String token, String userId, int volunteeringId, String joinerId, int groupId) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().acceptUserJoinRequest(userId, volunteeringId, joinerId, groupId);
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> denyUserJoinRequest(String token, String userId, int volunteeringId, String joinerId) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().denyUserJoinRequest(userId, volunteeringId, joinerId);
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> finishVolunteering(String token, String userId, int volunteeringId, String experience) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().finishVolunteering(userId, volunteeringId, experience);
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Integer> addVolunteeringLocation(String token, String userId, int volunteeringId, String name, AddressTuple address) {
        try {
            checkToken(token, userId);
            int locId = facadeManager.getVolunteeringFacade().addVolunteeringLocation(userId, volunteeringId, name, address);
            return Response.createResponse(locId);
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> assignVolunteerToLocation(String token, String userId, String volunteerId, int volunteeringId, int locId) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().assignVolunteerToLocation(userId, volunteerId, volunteeringId, locId);
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> moveVolunteerGroup(String token, String userId, String volunteerId, int volunteeringId, int groupId) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().moveVolunteerGroup(userId, volunteerId, volunteeringId, groupId);
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Integer> createNewGroup(String token, String userId, int volunteeringId) {
        try {
            checkToken(token, userId);
            int groupId = facadeManager.getVolunteeringFacade().createNewGroup(userId, volunteeringId);
            return Response.createResponse(groupId);
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> removeRange(String token, String userId, int volunteeringId, int rID) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().removeRange(userId, volunteeringId, rID);
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> removeGroup(String token, String userId, int volunteeringId, int groupId) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().removeGroup(userId, volunteeringId, groupId);
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> removeLocation(String token, String userId, int volunteeringId, int locId) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().removeLocation(userId, volunteeringId, locId);
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Integer> addScheduleRangeToGroup(String token, String userId, int volunteeringId, int groupId, int locId, int startHour, int startMinute, int endHour, int endMinute, int minimumMinutes, int maximumMinutes, boolean[] weekDays, LocalDate oneTime) {
        try {
            checkToken(token, userId);
            int rangeId = facadeManager.getVolunteeringFacade().addScheduleRangeToGroup(userId, volunteeringId, groupId, locId, LocalTime.of(startHour, startMinute), LocalTime.of(endHour, endMinute), minimumMinutes, maximumMinutes, weekDays, oneTime);
            return Response.createResponse(rangeId);
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> addRestrictionToRange(String token, String userId, int volunteeringId, int groupId, int locId, int rangeId, int startHour, int startMinute, int endHour, int endMinute, int amount) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().addRestrictionToRange(userId, volunteeringId, groupId, locId, rangeId, LocalTime.of(startHour, startMinute), LocalTime.of(endHour, endMinute), amount);
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> removeRestrictionFromRange(String token, String userId, int volunteeringId, int groupId, int locId, int rangeId, int startHour, int startMinute) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().removeRestrictionFromRange(userId, volunteeringId, groupId, locId, rangeId, LocalTime.of(startHour, startMinute));
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> updateRangeWeekdays(String token, String userId, int volunteeringId, int rangeId, boolean[] weekdays) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().updateRangeWeekdays(userId, volunteeringId, rangeId, weekdays);
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> updateRangeOneTimeDate(String token, String userId, int volunteeringId, int rangeId, LocalDate oneTime) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().updateRangeOneTimeDate(userId, volunteeringId, rangeId, oneTime);
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> makeAppointment(String token, String userId, int volunteeringId, int groupId, int locId, int rangeId, int startHour, int startMinute, int endHour, int endMinute, boolean[] weekdays, LocalDate oneTime) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().makeAppointment(userId, volunteeringId, groupId, locId, rangeId, LocalTime.of(startHour, startMinute), LocalTime.of(endHour, endMinute), weekdays, oneTime);
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> cancelAppointment(String token, String userId, int volunteeringId, int startHour, int startMinute) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().cancelAppointment(userId, volunteeringId, LocalTime.of(startHour, startMinute));
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> clearConstantCodes(String token, String userId, int volunteeringId) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().clearConstantCodes(userId, volunteeringId);
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> requestHoursApproval(String token, String userId, int volunteeringId, Date start, Date end) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().requestHoursApproval(userId, volunteeringId, start, end);
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> approveUserHours(String token, String userId, int volunteeringId, String volunteerId, Date start, Date end) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().approveUserHours(userId, volunteeringId, volunteerId, start, end);
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> denyUserHours(String token, String userId, int volunteeringId, String volunteerId, Date start, Date end) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().denyUserHours(userId, volunteeringId, volunteerId, start, end);
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<VolunteeringDTO> getVolunteeringDTO(String token, String userId, int volunteeringId) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().checkViewingPermissions(userId, volunteeringId);
            VolunteeringDTO dto = facadeManager.getVolunteeringFacade().getVolunteeringDTO(volunteeringId);
            return Response.createResponse(dto);
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<VolunteeringDTO>> getVolunteeringsOfUser(String token, String userId) {
        try {
            checkToken(token, userId);
            return Response.createResponse(facadeManager.getVolunteeringFacade().getVolunteeringsOfUser(userId));
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<String>> getVolunteeringSkills(String token, String userId, int volunteeringId) {
        try {
            checkToken(token, userId);
            List<String> skills = facadeManager.getVolunteeringFacade().getVolunteeringSkills(volunteeringId);
            return Response.createResponse(skills);
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<String>> getVolunteeringCategories(String token, String userId, int volunteeringId) {
        try {
            checkToken(token, userId);
            List<String> categs = facadeManager.getVolunteeringFacade().getVolunteeringCategories(volunteeringId);
            return Response.createResponse(categs);
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<LocationDTO>> getVolunteeringLocations(String token, String userId, int volunteeringId) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().checkViewingPermissions(userId, volunteeringId);
            List<LocationDTO> dtos = facadeManager.getVolunteeringFacade().getVolunteeringLocations(volunteeringId);
            return Response.createResponse(dtos);
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<LocationDTO>> getGroupLocations(String token, String userId, int volunteeringId, int groupId) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().checkViewingPermissions(userId, volunteeringId);
            List<LocationDTO> dtos = facadeManager.getVolunteeringFacade().getGroupLocations(volunteeringId, groupId);
            return Response.createResponse(dtos);
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Map<String,LocationDTO>> getGroupLocationMapping(String token, String userId, int volunteeringId, int groupId) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().checkViewingPermissions(userId, volunteeringId);
            Map<String,LocationDTO> dtosMap = facadeManager.getVolunteeringFacade().getGroupLocationMapping(userId, volunteeringId, groupId);
            return Response.createResponse(dtosMap);
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<Integer>> getVolunteeringGroups(String token, String userId, int volunteeringId) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().checkViewingPermissions(userId, volunteeringId);
            List<Integer> groups = facadeManager.getVolunteeringFacade().getVolunteeringGroups(volunteeringId);
            return Response.createResponse(groups);
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Map<String, Integer>> getVolunteeringVolunteers(String token, String userId, int volunteeringId) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().checkViewingPermissions(userId, volunteeringId);
            Map<String, Integer> map = facadeManager.getVolunteeringFacade().getVolunteeringVolunteers(volunteeringId);
            return Response.createResponse(map);
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<GroupDTO> getGroupDTO(String token, String userId, int volunteeringId, int groupId) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().checkViewingPermissions(userId, volunteeringId);
            GroupDTO dto = facadeManager.getVolunteeringFacade().getGroupDTO(volunteeringId, groupId);
            return Response.createResponse(dto);
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<String>> getConstantCodes(String token, String userId, int volunteeringId) {
        try {
            checkToken(token, userId);
            List<String> codes = facadeManager.getVolunteeringFacade().getConstantCodes(userId, volunteeringId);
            return Response.createResponse(codes);
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<String>> getVolunteeringWarnings(String token, String userId, int volunteeringId) {
        try {
            checkToken(token, userId);
            List<String> codes = facadeManager.getVolunteeringFacade().getVolunteeringWarnings(userId, volunteeringId);
            return Response.createResponse(codes);
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<ScheduleAppointmentDTO>> getVolunteerAppointments(String token, String userId, int volunteeringId) {
        try {
            checkToken(token, userId);
            return Response.createResponse(facadeManager.getVolunteeringFacade().getVolunteerAppointments(userId, volunteeringId));
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<ScheduleRangeDTO>> getVolunteerAvailableRanges(String token, String userId, int volunteeringId) {
        try {
            checkToken(token, userId);
            return Response.createResponse(facadeManager.getVolunteeringFacade().getVolunteerAvailableRanges(userId, volunteeringId));
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<ScheduleRangeDTO>> getVolunteeringLocationGroupRanges(String token, String userId, int volunteeringId, int groupId, int locId) {
        try {
            checkToken(token, userId);
            return Response.createResponse(facadeManager.getVolunteeringFacade().getVolunteeringLocationGroupRanges(userId, volunteeringId, groupId, locId));
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<HourApprovalRequest>> getVolunteeringHourRequests(String token, String userId, int volunteeringId) {
        try {
            checkToken(token, userId);
            return Response.createResponse(facadeManager.getVolunteeringFacade().getVolunteeringHourRequests(userId, volunteeringId));
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<JoinRequest>> getVolunteeringJoinRequests(String token, String userId, int volunteeringId) {
        try {
            checkToken(token, userId);
            return Response.createResponse(new LinkedList<>(facadeManager.getVolunteeringFacade().getVolunteeringJoinRequests(userId, volunteeringId).values()));
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Integer> getUserAssignedLocation(String token, String userId, int volunteeringId) {
        try {
            checkToken(token, userId);
            return Response.createResponse(facadeManager.getVolunteeringFacade().getUserAssignedLocation(userId, volunteeringId));
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Integer> getVolunteerGroup(String token, String userId, int volunteeringId) {
        try {
            checkToken(token, userId);
            return Response.createResponse(facadeManager.getVolunteeringFacade().getVolunteerGroup(userId, volunteeringId));
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<LocationDTO> getUserAssignedLocationData(String token, String userId, int volunteeringId) {
        try {
            checkToken(token, userId);
            return Response.createResponse(facadeManager.getVolunteeringFacade().getUserAssignedLocationData(userId, volunteeringId));
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> userHasSettingsPermission(String token, String userId, int volunteeringId) {
        try {
            checkToken(token, userId);
            return Response.createResponse(facadeManager.getVolunteeringFacade().userHasSettingsPermission(userId, volunteeringId));
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<ScanTypes> getVolunteeringScanType(String token, String userId, int volunteeringId) {
        try {
            checkToken(token, userId);
            return Response.createResponse(facadeManager.getVolunteeringFacade().getVolunteeringScanType(userId, volunteeringId));
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<ApprovalType> getVolunteeringApprovalType(String token, String userId, int volunteeringId) {
        try {
            checkToken(token, userId);
            return Response.createResponse(facadeManager.getVolunteeringFacade().getVolunteeringApprovalType(userId, volunteeringId));
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> addImage(String token, String userId, int volunteeringId, String imagePath) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().addImageToVolunteering(userId, volunteeringId, imagePath);
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> removeImage(String token, String userId, int volunteeringId, String imagePath) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().removeImageFromVolunteering(userId, volunteeringId, imagePath);
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> getUserApprovedHoursFormatted(String token, String userId, int volunteeringId, String israeliId) throws DocumentException, IOException {
        try {
            checkToken(token, userId);
            String path = facadeManager.getVolunteeringFacade().getUserApprovedHoursFormatted(userId, volunteeringId, israeliId);
            return Response.createResponse(path, null);
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> getAppointmentsCsv(String token, String userId, int numOfWeeks) throws DocumentException, IOException {
        try {
            checkToken(token, userId);
            String path = facadeManager.getVolunteeringFacade().getAppointmentsCsv(userId, numOfWeeks);
            return Response.createResponse(path, null);
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> disableVolunteeringLocations(String token, String userId, int volunteeringId) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().disableVolunteeringLocations(userId, volunteeringId);
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> updateRequestDescription(String token, String userId, int volunteeringId, Date start, String description) {
        try {
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().updateRequestDescription(userId, volunteeringId,start,description);
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    protected void remindBeforeActivity(){
        facadeManager.getVolunteeringFacade().notifyUpcomingAppointments();
    }
}
