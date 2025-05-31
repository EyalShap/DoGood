package com.dogood.dogoodbackend.domain.volunteering;

import com.dogood.dogoodbackend.domain.volunteerings.Group;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GroupUnitTests {
    private Group group;

    @BeforeEach
    public void setUp() {
        group = new Group(0,0);
    }

    @Test
    public void givenVolunteerNotInGroup_whenAddUser_thenAddVolunteer(){
        group.addUser("Volunteer");
        Assertions.assertEquals(1, group.getUsers().size());
    }

    @Test
    public void givenVolunteerInGroup_whenAddUser_thenThrowException(){
        group.addUser("Volunteer");
        Assertions.assertThrows(IllegalArgumentException.class, ()->group.addUser("Volunteer"));
        Assertions.assertEquals(1, group.getUsers().size());
    }

    @Test
    public void givenVolunteerInGroup_whenRemoveUser_thenRemoveVolunteer(){
        group.addUser("Volunteer");
        group.removeUser("Volunteer");
        Assertions.assertEquals(0, group.getUsers().size());
    }

    @Test
    public void givenVolunteerNotInGroup_whenRemoveUser_thenThrowException(){
        Assertions.assertThrows(IllegalArgumentException.class, ()->group.removeUser("Volunteer"));
        Assertions.assertEquals(0, group.getUsers().size());
    }

    @Test
    public void givenVolunteerInGroup_whenAssignUserToLocation_thenAssignToLocation(){
        group.addUser("Volunteer");
        group.assignUserToLocation("Volunteer",0);
        Assertions.assertEquals(0, group.getAssignedLocation("Volunteer"));
    }

    @Test
    public void givenVolunteerAssignedSameLocation_whenAssignUserToLocation_thenThrowException(){
        group.addUser("Volunteer");
        group.assignUserToLocation("Volunteer",0);
        Assertions.assertThrows(IllegalArgumentException.class, ()->group.assignUserToLocation("Volunteer",0));
    }

    @Test
    public void givenVolunteerNotInGroup_whenAssignUserToLocation_thenThrowException(){
        Assertions.assertThrows(IllegalArgumentException.class, ()->group.assignUserToLocation("Volunteer",0));
    }
}
