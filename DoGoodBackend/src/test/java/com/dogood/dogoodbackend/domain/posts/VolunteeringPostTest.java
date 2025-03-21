package com.dogood.dogoodbackend.domain.posts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
/*
class VolunteeringPostTest {
    private VolunteeringPost post;
    private final int postId = 0;
    private final String title = "Postito";
    private final String description = "Postito is a very cool post";
    private final String posterUsername = "Poster";
    private final int volunteeringId = 0;
    private final int organizationId = 0;

    @BeforeEach
    void setUp() {
        this.post = new VolunteeringPost(0, "Title", "Description", "TheDoctor", 0, 0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"P", "PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPost"})
    @NullSource
    @EmptySource
    void givenInvalidTitle_whenEdit_thenThrowException(String invalidTitle) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            post.edit(invalidTitle, description);
        });

        assertEquals(String.format("Invalid post title: %s.\n", invalidTitle), exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"D", "DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDescription"})
    @NullSource
    @EmptySource
    void givenInvalidDescription_whenEdit_thenThrowException(String invalidDescription) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            post.edit(title, invalidDescription);
        });

        assertEquals(String.format("Invalid post description: %s.\n", invalidDescription), exception.getMessage());
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
}*/