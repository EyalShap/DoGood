package com.dogood.dogoodbackend.domain.posts;

import com.dogood.dogoodbackend.utils.PostErrors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class VolunteerPostTest {
    private VolunteerPost post;
    private final int postId = 0;
    private final String title = "Postito";
    private final String description = "Postito is a very cool post";
    private final Set<String> keywords = Set.of("Animals", "Fruits");
    private final String posterUsername = "Poster";
    private final List<String> skills = List.of("Planting", "Teamwork");
    private final List<String> categories = List.of("Environment", "Sustainability");
    private final List<String> relatedUsers = List.of(posterUsername);

    @Mock
    private PostsFacade postsFacade;

    @BeforeEach
    void setUp() {
        this.post = new VolunteerPost(postId, title, description, keywords, posterUsername, skills, categories);
    }

    private boolean verifyPostFields(String title, String description, Set<String> keywords) {
        return this.post.getTitle().equals(title) &&
                this.post.getDescription().equals(description) &&
                this.post.getKeywords().equals(keywords);
    }

    private static Stream<Arguments> validInputs() {
        return Stream.of(
                Arguments.of("We Can Help with Animals", "We will take care for rescued dogs.", Set.of("Animals", "Care", "Dogs")),
                Arguments.of("Food Drive", "We are organizing a local food drive event.", Set.of("Food", "Community", "Event")),
                Arguments.of("Beach Cleanup", "We can clean up the beach and protect marine life.", Set.of("Environment", "Cleanup", "Ocean")),
                Arguments.of("Tutoring Kids", "We are tutoring kids in math and reading.", Set.of("Education", "Kids", "Tutoring")),
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
    void givenNewUser_whenAddUser_thenAdd() {
        String newUsername = "Moshe";
        List<String> expectedUsers = List.of(posterUsername, newUsername);

        assertEquals(relatedUsers, this.post.getRelatedUsers());
        this.post.addUser(newUsername);
        assertEquals(expectedUsers, this.post.getRelatedUsers());
    }

    @Test
    void givenPoster_whenAddUser_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            post.addUser(posterUsername);
        });

        String expectedErr = PostErrors.makeUserIsRelatedToPost(posterUsername, this.title, true);
        assertEquals(expectedErr, exception.getMessage());
        assertEquals(relatedUsers, this.post.getRelatedUsers());
    }

    @Test
    void givenExistingUser_whenAddUser_thenThrowException() {
        String newUsername = "Moshe";
        post.addUser(newUsername);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            post.addUser(newUsername);
        });

        String expectedErr = PostErrors.makeUserIsRelatedToPost(newUsername, this.title, true);
        assertEquals(expectedErr, exception.getMessage());

        List<String> expectedUsers = List.of(posterUsername, newUsername);
        assertEquals(expectedUsers, this.post.getRelatedUsers());
    }

    @Test
    void givenExistingUserByPoster_whenRemoveUser_thenRemove() {
        String newUsername = "Moshe";
        post.addUser(newUsername);

        List<String> expectedUsers = List.of(posterUsername, newUsername);
        assertEquals(expectedUsers, this.post.getRelatedUsers());

        post.removeUser(newUsername, posterUsername);
        assertEquals(relatedUsers, this.post.getRelatedUsers());
    }

    @Test
    void givenExistingUserByNonPoster_whenRemoveUser_thenThrowException() {
        String newUsername = "Moshe";
        String nonPoster = "Ofer";
        post.addUser(newUsername);

        List<String> expectedUsers = List.of(posterUsername, newUsername);
        assertEquals(expectedUsers, this.post.getRelatedUsers());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            post.removeUser(newUsername, nonPoster);
        });
        String expectedErr = PostErrors.makeUserIsNotAllowedToMakePostActionError(this.title, nonPoster, "remove user from");
        assertEquals(expectedErr, exception.getMessage());
        assertEquals(expectedUsers, this.post.getRelatedUsers());
    }

    @Test
    void givenPoster_whenRemoveUser_thenThrowException() {
        assertEquals(relatedUsers, this.post.getRelatedUsers());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            post.removeUser(posterUsername, posterUsername);
        });
        String expectedErr = PostErrors.makePosterCanNotBeRemovedFromPost(posterUsername, this.title);
        assertEquals(expectedErr, exception.getMessage());
        assertEquals(relatedUsers, this.post.getRelatedUsers());
    }

    @Test
    void givenNonExistingUser_whenRemoveUser_thenThrowException() {
        String newUsername = "Moshe";
        assertEquals(relatedUsers, this.post.getRelatedUsers());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            post.removeUser(newUsername, posterUsername);
        });
        String expectedErr = PostErrors.makeUserIsRelatedToPost(newUsername, this.title, false);
        assertEquals(expectedErr, exception.getMessage());
        assertEquals(relatedUsers, this.post.getRelatedUsers());
    }

    @Test
    void givenExistingUserByPoster_whenSetPoster_thenSet() {
        String newUsername = "Moshe";
        post.addUser(newUsername);

        assertEquals(posterUsername, post.getPosterUsername());
        post.setPoster(posterUsername, newUsername);
        assertEquals(newUsername, post.getPosterUsername());
    }

    @Test
    void givenNonExistingUser_whenSetPoster_thenSet() {
        String newUsername = "Moshe";

        assertEquals(posterUsername, post.getPosterUsername());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            post.setPoster(posterUsername, newUsername);
        });
        String expectedErr = PostErrors.makeUserIsRelatedToPost(newUsername, this.title, false);
        assertEquals(expectedErr, exception.getMessage());
        assertEquals(posterUsername, post.getPosterUsername());
    }

    @Test
    void givenNewImageByPoster_whenAddImage_thenAdd() {
        String newPath = "dummyPath";
        List<String> expected = List.of(newPath);

        assertEquals(new ArrayList<String>(), post.getImages());
        post.addImage(posterUsername, newPath);
        assertEquals(expected, post.getImages());
    }

    @Test
    void givenNewImageByRelatedUser_whenAddImage_thenAdd() {
        String newUser = "Moshe";
        post.addUser(newUser);

        String newPath = "dummyPath";
        List<String> expected = List.of(newPath);

        assertEquals(new ArrayList<String>(), post.getImages());
        post.addImage(newUser, newPath);
        assertEquals(expected, post.getImages());
    }

    @Test
    void givenNonRelatedUser_whenAddImage_thenThrowException() {
        String newUser = "Moshe";
        String newPath = "dummyPath";

        assertEquals(new ArrayList<String>(), post.getImages());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            post.addImage(newUser, newPath);
        });
        String expectedErr = PostErrors.makeUserIsNotAllowedToMakePostActionError(this.title, newUser, "add image to");
        assertEquals(expectedErr, exception.getMessage());

        assertEquals(new ArrayList<String>(), post.getImages());
    }

    @ParameterizedTest
    @ValueSource(strings = {"dummyPath", "\"dummyPath\""})
    void givenExistingImage_whenAddImage_thenThrowException(String existingPath) {
        String newPath = "dummyPath";
        post.addImage(posterUsername, newPath);

        List<String> expected = List.of(newPath);
        assertEquals(expected, post.getImages());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            post.addImage(posterUsername, existingPath);
        });
        String expectedErr = PostErrors.makeImagePathExists(newPath, posterUsername, true);
        assertEquals(expectedErr, exception.getMessage());

        assertEquals(expected, post.getImages());
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void givenInvalidImage_whenAddImage_thenThrowException(String invalidPath) {
        assertEquals(new ArrayList<String>(), post.getImages());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            post.addImage(posterUsername, invalidPath);
        });
        String expectedErr = PostErrors.makeImagePathIsNotValid(invalidPath);
        assertEquals(expectedErr, exception.getMessage());

        assertEquals(new ArrayList<String>(), post.getImages());
    }

    @Test
    void givenExistingImageByPoster_whenRemoveImage_thenRemove() {
        String newPath = "dummyPath";
        post.addImage(posterUsername, newPath);

        List<String> expected = List.of(newPath);
        assertEquals(expected, post.getImages());

        post.removeImage(posterUsername, newPath);

        assertEquals(new ArrayList<>(), post.getImages());
    }

    @Test
    void givenExistingImageByExistingUser_whenRemoveImage_thenRemove() {
        String newUser = "Moshe";
        post.addUser(newUser);
        String newPath = "dummyPath";
        post.addImage(posterUsername, newPath);

        List<String> expected = List.of(newPath);
        assertEquals(expected, post.getImages());

        post.removeImage(newUser, newPath);

        assertEquals(new ArrayList<>(), post.getImages());
    }

    @Test
    void givenNonExistingUser_whenRemoveImage_thenThrowException() {
        String newUser = "Moshe";
        String newPath = "dummyPath";
        post.addImage(posterUsername, newPath);

        List<String> expected = List.of(newPath);
        assertEquals(expected, post.getImages());
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            post.removeImage(newUser, newPath);
        });
        String expectedErr = PostErrors.makeUserIsNotAllowedToMakePostActionError(this.title, newUser, "remove image from");
        assertEquals(expectedErr, exception.getMessage());
        assertEquals(expected, post.getImages());
    }

    @Test
    void givenNonExistingImage_whenRemoveImage_thenThrowException() {
        String newPath = "dummyPath";

        assertEquals(new ArrayList<>(), post.getImages());
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            post.removeImage(posterUsername, newPath);
        });
        String expectedErr = PostErrors.makeImagePathExists(newPath, posterUsername, false);
        assertEquals(expectedErr, exception.getMessage());
        assertEquals(new ArrayList<>(), post.getImages());
    }

    @Test
    void getSkills() {
        assertEquals(skills, post.getSkills(this.postsFacade));
    }

    @Test
    void getCategories() {
        assertEquals(categories, post.getCategories(this.postsFacade));
    }

    @Test
    void givenPoster_whenHasRelatedUser_thenReturnTrue() {
        assertTrue(post.hasRelatedUser(posterUsername));
    }

    @Test
    void givenExistingUser_whenHasRelatedUser_thenReturnTrue() {
        String newUsername = "Moshe";
        post.addUser(newUsername);

        assertTrue(post.hasRelatedUser(newUsername));
    }

    @Test
    void givenNonExistingUser_whenHasRelatedUser_thenReturnFalse() {
        String newUsername = "Moshe";
        assertFalse(post.hasRelatedUser(newUsername));
    }
}