package com.dogood.dogoodbackend.domain.volunteering;

import com.dogood.dogoodbackend.domain.volunteerings.BarcodeHandler;
import com.dogood.dogoodbackend.domain.volunteerings.JoinRequest;
import com.dogood.dogoodbackend.domain.volunteerings.Volunteering;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalTime;

public class VolunteeringUnitTests {
    private Volunteering volunteering;
    private BarcodeHandler barcodeHandler;
    @BeforeEach
    public void setUp() {
        barcodeHandler = Mockito.mock(BarcodeHandler.class);
        volunteering = new Volunteering(0,0,"Test", "Test", barcodeHandler);
    }

    @Test
    public void whenAddJoinRequest_givenJoinRequest_addJoinRequest() {
        JoinRequest joinRequest = Mockito.mock(JoinRequest.class);
        Assertions.assertDoesNotThrow(()->volunteering.addJoinRequest("Jim", joinRequest));
        Assertions.assertEquals(1, volunteering.getPendingJoinRequests().size());
        Assertions.assertTrue(volunteering.getPendingJoinRequests().containsKey("Jim"));
        Assertions.assertEquals(joinRequest, volunteering.getPendingJoinRequests().get("Jim"));
    }

    @Test
    public void whenAddJoinRequest_givenUserAlreadyRequest_throwException() {
        JoinRequest joinRequest = Mockito.mock(JoinRequest.class);
        volunteering.addJoinRequest("Jim", joinRequest);
        Assertions.assertThrows(UnsupportedOperationException.class, ()->volunteering.addJoinRequest("Jim", joinRequest));
        Assertions.assertEquals(1, volunteering.getPendingJoinRequests().size());
    }

    @Test
    public void whenAddGroup_givenVoid_addGroup() {
        Assertions.assertDoesNotThrow(() -> volunteering.addNewGroup());
        Assertions.assertEquals(2, volunteering.getGroups().size());
    }

    @Test
    public void whenRemoveGroup_givenEmptyGroup_removeGroup() {
        int gid = volunteering.addNewGroup();
        Assertions.assertDoesNotThrow(() -> volunteering.removeGroup(gid));
        Assertions.assertEquals(1, volunteering.getGroups().size());
    }

    @Test
    public void whenRemoveGroup_givenNonEmptyGroup_throwException() {
        int gid = volunteering.addNewGroup();
        JoinRequest joinRequest = Mockito.mock(JoinRequest.class);
        volunteering.addJoinRequest("Jim", joinRequest);
        volunteering.approveJoinRequest("Jim", gid);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> volunteering.removeGroup(gid));
        Assertions.assertEquals(2, volunteering.getGroups().size());
    }

    @Test
    public void whenRemoveRange_givenRange_removeRange() {
        int lId = volunteering.addLocation("Test", null);
        int rId = volunteering.addRangeToGroup(0, lId, LocalTime.of(0,0), LocalTime.of(1,0), -1, -1,null, LocalDate.now());
        Assertions.assertDoesNotThrow(() -> volunteering.removeRange(rId));
        Assertions.assertEquals(0, volunteering.getScheduleRanges().size());
    }

    @Test
    public void whenRemoveRange_givenNonExistentRange_throwError() {
        int lId = volunteering.addLocation("Test", null);
        int rId = volunteering.addRangeToGroup(0, lId, LocalTime.of(0,0), LocalTime.of(1,0), -1, -1, null, LocalDate.now());
        Assertions.assertThrows(UnsupportedOperationException.class, () -> volunteering.removeRange(rId+2));
        Assertions.assertEquals(1, volunteering.getScheduleRanges().size());
    }

    @Test
    public void whenCodeValid_givenValidCode_returnTrue(){
        Mockito.when(barcodeHandler.codeValid("TEST")).thenReturn(true);
        Assertions.assertTrue(volunteering.codeValid("TEST"));
    }

    @Test
    public void whenCodeValid_givenInvalidCode_returnFalse(){
        Mockito.when(barcodeHandler.codeValid("TEST")).thenReturn(false);
        Assertions.assertFalse(volunteering.codeValid("TEST"));
    }

    @Test
    public void whenGenerateCode_givenNotConstant_returnCode(){
        Mockito.when(barcodeHandler.generateCode()).thenReturn("TEST");
        Assertions.assertEquals(volunteering.getId()+":TEST",volunteering.generateCode(false));
    }

    @Test
    public void whenGenerateCode_givenConstant_returnCode(){
        Mockito.when(barcodeHandler.generateConstantCode()).thenReturn("TEST");
        Assertions.assertEquals(volunteering.getId()+":TEST",volunteering.generateCode(true));
    }

    @Test
    public void whenClearConstantCodes_givenVoid_callClearCodes(){
       volunteering.clearConstantCodes();
       Mockito.verify(barcodeHandler, Mockito.times(1)).clearConstantCodes();
    }

    @Test
    public void whenApproveJoinRequest_givenValidUser_approveJoinRequest() {
        JoinRequest joinRequest = Mockito.mock(JoinRequest.class);
        volunteering.addJoinRequest("Jim", joinRequest);
        Assertions.assertDoesNotThrow(() -> volunteering.approveJoinRequest("Jim",0));
        Assertions.assertEquals(0, volunteering.getPendingJoinRequests().size());
        Assertions.assertEquals(1, volunteering.getVolunteerToGroup().size());
        Assertions.assertEquals(0, volunteering.getVolunteerGroup("Jim"));
        Assertions.assertEquals(1, volunteering.getGroups().get(0).getUsers().size());
        Assertions.assertEquals("Jim", volunteering.getGroups().get(0).getUsers().get(0));
    }

    @Test
    public void whenApproveJoinRequest_givenUserDidntRequest_throwException() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> volunteering.approveJoinRequest("Jim",0));
        Assertions.assertEquals(0, volunteering.getPendingJoinRequests().size());
        Assertions.assertEquals(0, volunteering.getVolunteerToGroup().size());
    }
}
