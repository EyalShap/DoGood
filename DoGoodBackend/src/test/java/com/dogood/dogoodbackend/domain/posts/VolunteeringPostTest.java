package com.dogood.dogoodbackend.domain.posts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VolunteeringPostTest {
    private VolunteeringPost post;
    private final int postId = 0;
    private final String title = "Postito";
    private final String description = "Postito is a very cool post";
    private final Set<String> keywords = Set.of("Animals", "Fruits");
    private final String posterUsername = "Poster";
    private final int volunteeringId = 0;
    private final int organizationId = 0;

    @Mock
    private PostsFacade postsFacade;
    private final List<String> mockSkills = List.of("Planting", "Teamwork");
    private final List<String> mockCategories = List.of("Environment", "Sustainability");


    @BeforeEach
    void setUp() {
        this.post = new VolunteeringPost(postId, title, description, keywords, posterUsername, volunteeringId, organizationId);
    }

    private boolean verifyPostFields(String title, String description, Set<String> keywords) {
        return this.post.getTitle().equals(title) &&
                this.post.getDescription().equals(description) &&
                this.post.getKeywords().equals(keywords);
    }

    private static Stream<Arguments> validInputs() {
        return Stream.of(
                Arguments.of("Help with Animals", "We need volunteers to care for rescued dogs.", Set.of("Animals", "Care", "Dogs")),
                Arguments.of("Food Drive", "Join us in organizing a local food drive event.", Set.of("Food", "Community", "Event")),
                Arguments.of("Beach Cleanup", "Help clean up the beach and protect marine life.", Set.of("Environment", "Cleanup", "Ocean")),
                Arguments.of("Tutoring Kids", "Volunteer to tutor kids in math and reading.", Set.of("Education", "Kids", "Tutoring")),
                Arguments.of("Art Therapy", "Support art therapy workshops for mental health.", Set.of("Art", "Health", "Therapy"))
        );
    }

    @ParameterizedTest
    @MethodSource("validInputs")
    void givenValidFields_whenEdit_thenEdit(String newTitle, String newDescription, Set<String> newKeywords) {
        post.edit(newTitle, newDescription, newKeywords);
        assertTrue(verifyPostFields(newTitle, newDescription, newKeywords));
    }

    @ParameterizedTest
    @ValueSource(strings = {"P", "PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPost"})
    @NullSource
    @EmptySource
    void givenInvalidTitle_whenEdit_thenThrowException(String invalidTitle) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            post.edit(invalidTitle, description, keywords);
        });

        assertEquals(String.format("Invalid post title: %s.\n", invalidTitle), exception.getMessage());
        assertTrue(verifyPostFields(this.title, this.description, this.keywords));
    }

    @ParameterizedTest
    @ValueSource(strings = {"D", "DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDescription"})
    @NullSource
    @EmptySource
    void givenInvalidDescription_whenEdit_thenThrowException(String invalidDescription) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            post.edit(title, invalidDescription, keywords);
        });

        assertEquals(String.format("Invalid post description: %s.\n", invalidDescription), exception.getMessage());
        assertTrue(verifyPostFields(this.title, this.description, this.keywords));
    }

    @Test
    void incNumOfPeopleRequestedToJoin() {
        assertEquals(0, post.getNumOfPeopleRequestedToJoin());
        post.incNumOfPeopleRequestedToJoin();
        assertEquals(1, post.getNumOfPeopleRequestedToJoin());
    }

    @Test
    void evaluatePopularity() {
        assertEquals(0, post.evaluatePopularity());
        post.incNumOfPeopleRequestedToJoin();
        assertEquals(1, post.evaluatePopularity());
    }

    @Test
    void getSkills() {
        when(this.postsFacade.getVolunteeringSkills(volunteeringId)).thenReturn(mockSkills);
        assertEquals(mockSkills, this.post.getSkills(this.postsFacade));
    }

    @Test
    void getCategories() {
        when(this.postsFacade.getVolunteeringCategories(volunteeringId)).thenReturn(mockCategories);
        assertEquals(mockCategories, this.post.getCategories(this.postsFacade));
    }
}