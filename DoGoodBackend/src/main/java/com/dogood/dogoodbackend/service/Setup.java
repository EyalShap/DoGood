package com.dogood.dogoodbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class Setup {

    private final boolean DO_SETUP = false;


    @Autowired
    FacadeManager facadeManager;
    @Autowired
    VolunteeringService volunteeringService;
    @Autowired
    UserService userService;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    PostService postService;


    public void setUp(){
        if(!DO_SETUP){
            return;
        }
        facadeManager.getUsersFacade().register("EyalManager","123456","Eyal Manager","eyalm1000@gmail.com","052-0520520",new Date());
        facadeManager.getUsersFacade().register("DanaManager","123456","Dana Manager","dafr@gmail.com","052-0520520",new Date());
        facadeManager.getUsersFacade().register("EyalBGU","123456","אייל שפירו","eyald@post.bgu.ac.il","052-8585519",new Date());
        facadeManager.getUsersFacade().register("EyalTechnion","123456","אייל שפירו","eyald@campus.technion.ac.il","052-8585519",new Date());
        facadeManager.getUsersFacade().register("EyalHaifa","123456","אייל שפירו","eyald@haifa.ac.il","052-8585519",new Date());
        Response<String> danaLogin = userService.login("DanaManager","123456");
        Response<String> bguLogin = userService.login("EyalBGU","123456");
        Response<String> technionLogin = userService.login("EyalTechnion","123456");
        Response<String> haifaLogin = userService.login("EyalHaifa","123456");
        String danaToken = danaLogin.getData();
        String bguToken = bguLogin.getData();
        String technionToken = technionLogin.getData();
        String haifaToken = haifaLogin.getData();

        Response<Integer> danaCreateOrg = organizationService.createOrganization(danaToken,"Strays of Israel", "Organization for helping the stray animals in Israel","052-0520520","dafr@gmail.com","DanaManager");
        int orgId1 = danaCreateOrg.getData();
        Response<Integer> danaCreateVol1 = organizationService.createVolunteering(danaToken, orgId1, "Feed The Kittens", "We feed the many hungry street kittens in Israel","DanaManager");
        Response<Integer> danaCreateVol2 = organizationService.createVolunteering(danaToken, orgId1, "Save The Stray Dogs", "We rescue the stray dogs roaming the streets","DanaManager");
        int volId1 = danaCreateVol1.getData();
        int volId2 = danaCreateVol2.getData();
        Response<Integer> danaCreatePost1 =  postService.createVolunteeringPost(danaToken,"Help us feed the cats!", "Come help us rescue and feed stray cats","DanaManager",volId1);
        Response<Integer> danaCreatePost2 = postService.createVolunteeringPost(danaToken, "Help us save the dogs!", "We are organizing a mission to locate and save stray dogs in various cities in Israel", "DanaManager",volId2);
        int postId1 = danaCreatePost1.getData();
        int postId2 = danaCreatePost2.getData();
        postService.joinVolunteeringRequest(bguToken,postId1,"EyalBGU","");
        postService.joinVolunteeringRequest(technionToken,postId1,"EyalTechnion","");
        postService.joinVolunteeringRequest(haifaToken,postId1,"EyalHaifa","");

        volunteeringService.acceptUserJoinRequest(danaToken, "DanaManager",volId1,"EyalBGU",0);
        volunteeringService.acceptUserJoinRequest(danaToken, "DanaManager",volId1,"EyalTechnion",0);
        volunteeringService.acceptUserJoinRequest(danaToken, "DanaManager",volId1,"EyalHaifa",0);

        LocalDate now = LocalDate.now();
        LocalTime start = LocalTime.of(12,0);
        LocalTime end = LocalTime.of(14,0);
        for(int i = 0; i < 30; i++){
            LocalDate day = now.minusWeeks(i);
            Date startDate = Date.from(day.atTime(start).atZone(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(day.atTime(end).atZone(ZoneId.systemDefault()).toInstant());

            volunteeringService.requestHoursApproval(bguToken, "EyalBGU",volId1,startDate,endDate);
            volunteeringService.requestHoursApproval(technionToken, "EyalTechnion",volId1,startDate,endDate);
            volunteeringService.requestHoursApproval(haifaToken, "EyalHaifa",volId1,startDate,endDate);
            volunteeringService.approveUserHours(danaToken,"DanaManager", volId1,"EyalBGU",startDate,endDate);
            volunteeringService.approveUserHours(danaToken,"DanaManager", volId1,"EyalTechnion",startDate,endDate);
            volunteeringService.approveUserHours(danaToken,"DanaManager", volId1,"EyalHaifa",startDate,endDate);
        }
    }
}
