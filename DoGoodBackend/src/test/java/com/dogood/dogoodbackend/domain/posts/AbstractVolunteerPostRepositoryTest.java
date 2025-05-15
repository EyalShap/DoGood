package com.dogood.dogoodbackend.domain.posts;

import com.dogood.dogoodbackend.jparepos.VolunteeringPostJPA;
import com.dogood.dogoodbackend.utils.PostErrors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
abstract class AbstractVolunteerPostRepositoryTest {
    public VolunteerPostRepository repository;

    private int postId;
    private final String title = "Postito";
    private final String description = "Postito is a very cool post";
    private final Set<String> keywords = Set.of("Animals", "Fruits");
    private final String posterUsername = "Poster";
    private final List<String> skills = List.of("Driving");
    private final List<String> categories = List.of("Healthcare");
    private final List<String> relatedUsers = List.of(posterUsername);

    protected abstract VolunteerPostRepository createRepository();

    @BeforeEach
    public void setup() {
        repository = createRepository();
        repository.clear();
        postId = repository.createVolunteerPost(title, description, keywords, posterUsername, skills, categories);
    }

    public boolean verifyPostFields(String title, String description, Set<String> keywords, List<String> skills, List<String> categories) {
        VolunteerPost post = repository.getVolunteerPost(postId);
        return verifyPostFields(post, title, description, keywords, skills, categories);
    }

    public boolean verifyPostFields(VolunteerPost post, String title, String description, Set<String> keywords, List<String> skills, List<String> categories) {
        return post.getTitle().equals(title) &&
                post.getDescription().equals(description) &&
                new HashSet<>(post.getKeywords()).equals(new HashSet<>(keywords)) &&
                new HashSet<>(post.getSkills(null)).equals(new HashSet<>(skills)) &&
                new HashSet<>(post.getCategories(null)).equals(new HashSet<>(categories));
    }

    public static Stream<Arguments> validInputs() {
        return Stream.of(
                Arguments.of(
                        "Help with Animals",
                        "Offering support in caring for rescued dogs, including feeding, walking, and emotional support.",
                        Set.of("Animals", "Care", "Dogs"),
                        List.of("Animal Handling", "Empathy", "Basic Veterinary Care"),
                        List.of("Animal Welfare", "Community Service")
                ),
                Arguments.of(
                        "Food Drive",
                        "Organizing and running a local food drive to support families in need.",
                        Set.of("Food", "Community", "Event"),
                        List.of("Organization", "Communication", "Logistics"),
                        List.of("Social Impact", "Community Service")
                ),
                Arguments.of(
                        "Beach Cleanup",
                        "Leading beach cleanup activities to promote environmental responsibility.",
                        Set.of("Environment", "Cleanup", "Ocean"),
                        List.of("Team Leadership", "Environmental Awareness", "Physical Stamina"),
                        List.of("Environment", "Sustainability")
                ),
                Arguments.of(
                        "Tutoring Kids",
                        "Providing tutoring sessions for children in subjects like math and reading.",
                        Set.of("Education", "Kids", "Tutoring"),
                        List.of("Teaching", "Patience", "Subject Knowledge"),
                        List.of("Education", "Youth Development")
                ),
                Arguments.of(
                        "Art Therapy",
                        "Facilitating art therapy workshops to promote mental well-being.",
                        Set.of("Art", "Health", "Therapy"),
                        List.of("Creativity", "Listening Skills", "Psychological Sensitivity"),
                        List.of("Mental Health", "Creative Arts")
                )
        );
    }

    @ParameterizedTest
    @MethodSource("validInputs")
    public void givenValidFields_whenCreateVolunteerPost_thenCreate(String title, String description, Set<String> keywords, List<String> skills, List<String> categories) {
        List<VolunteerPost> allPostsBefore = repository.getAllVolunteerPosts();
        VolunteerPost post1 = repository.getVolunteerPost(postId);
        assertEquals(1, allPostsBefore.size());
        assertEquals(post1, allPostsBefore.get(0));

        int postId2 = repository.createVolunteerPost(title, description, keywords, posterUsername, skills, categories);
        VolunteerPost post2 = repository.getVolunteerPost(postId2);

        Map<Integer, VolunteerPost> allPostsAfter = new HashMap<>();
        for(VolunteerPost post : repository.getAllVolunteerPosts()) {
            allPostsAfter.put(post.getId(), post);
        }

        assertEquals(2, allPostsAfter.size());
        assertEquals(post1, allPostsAfter.getOrDefault(postId, null));
        assertEquals(post2, allPostsAfter.getOrDefault(postId2, null));
    }

    @Test
    public void givenInvalidFields_whenCreateVolunteerPost_thenThrowException() {
        List<VolunteerPost> allPostsBefore = repository.getAllVolunteerPosts();
        VolunteerPost post1 = repository.getVolunteerPost(postId);
        List<VolunteerPost> expected = List.of(post1);
        assertEquals(expected, allPostsBefore);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.createVolunteerPost("", description, keywords, posterUsername, skills, categories);
        });
        assertEquals("Invalid post title: .\n", exception.getMessage());

        List<VolunteerPost> allPostsAfter = repository.getAllVolunteerPosts();
        assertIterableEquals(expected, allPostsAfter);
    }

    @Test
    public void givenExistingId_whenRemoveVolunteerPost_thenRemove() {
        assertDoesNotThrow(() -> repository.getVolunteerPost(postId));

        repository.removeVolunteerPost(postId);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.getVolunteerPost(postId);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(postId), exception.getMessage());
    }

    @Test
    public void givenNonExistingId_whenRemoveVolunteerPost_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.removeVolunteerPost(postId + 1);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(postId + 1), exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("validInputs")
    public void givenExistingPostAndValidFields_whenEditVolunteerPost_thenEdit(String newTitle, String newDescription, Set<String> newKeywords, List<String> newSkills, List<String> newCategories) {
        assertDoesNotThrow(() -> repository.editVolunteerPost(postId, newTitle, newDescription, newKeywords, newSkills, newCategories));
        assertTrue(verifyPostFields(newTitle, newDescription, newKeywords, skills, categories));
    }

    @Test
    public void givenExistingPostAndNonValidFields_whenEditVolunteerPost_thenThrowException() {
        assertThrows(IllegalArgumentException.class, () -> repository.editVolunteerPost(postId, "", "", null, null, null));
        assertTrue(verifyPostFields(title, description, keywords, skills, categories));
    }

    @Test
    public void givenNonExistingPost_whenEditVolunteerPost_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.editVolunteerPost(postId + 1, "title", "description", new HashSet<>(), new ArrayList<>(), new ArrayList<>());
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(postId + 1), exception.getMessage());
    }

    @Test
    public void givenExistingPostNewUserByPoster_whenAddRelatedUser_thenAdd() {
        String newUsername = "Moshe";
        List<String> expectedUsers = List.of(posterUsername, newUsername);

        assertEquals(relatedUsers, repository.getVolunteerPost(postId).getRelatedUsers());
        repository.addRelatedUser(postId, newUsername);
        assertEquals(expectedUsers, repository.getVolunteerPost(postId).getRelatedUsers());
    }

    @Test
    public void givenNonExistingPost_whenAddRelatedUser_thenThrowException() {
        String newUsername = "Moshe";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.addRelatedUser(postId + 1, newUsername);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(postId + 1), exception.getMessage());
    }

    @Test
    public void givenPoster_whenAddRelatedUser_thenThrowException() {
        assertEquals(relatedUsers, repository.getVolunteerPost(postId).getRelatedUsers());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.addRelatedUser(postId, posterUsername);
        });

        String expectedErr = PostErrors.makeUserIsRelatedToPost(posterUsername, this.title, true);
        assertEquals(expectedErr, exception.getMessage());
        assertEquals(relatedUsers, repository.getVolunteerPost(postId).getRelatedUsers());
    }

    @Test
    public void givenExistingUser_whenAddRelatedUser_thenThrowException() {
        String newUsername = "Moshe";
        repository.addRelatedUser(postId, newUsername);

        List<String> expectedUsers = List.of(posterUsername, newUsername);
        assertEquals(expectedUsers, repository.getVolunteerPost(postId).getRelatedUsers());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.addRelatedUser(postId, newUsername);
        });

        String expectedErr = PostErrors.makeUserIsRelatedToPost(newUsername, this.title, true);
        assertEquals(expectedErr, exception.getMessage());

        assertEquals(expectedUsers, repository.getVolunteerPost(postId).getRelatedUsers());
    }

    @Test
    public void givenExistingUserByPoster_whenRemoveRelatedUser_thenRemove() {
        String newUsername = "Moshe";
        repository.addRelatedUser(postId, newUsername);

        List<String> expectedUsers = List.of(posterUsername, newUsername);
        assertEquals(expectedUsers, repository.getVolunteerPost(postId).getRelatedUsers());

        repository.removeRelatedUser(postId, newUsername, posterUsername);
        assertEquals(relatedUsers, repository.getVolunteerPost(postId).getRelatedUsers());
    }

    @Test
    public void givenNonExistingPost_whenRemoveRelatedUser_thenThrowException() {
        String newUsername = "Moshe";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.removeRelatedUser(postId + 1, newUsername, posterUsername);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(postId + 1), exception.getMessage());
    }

    @Test
    public void givenExistingUserByNonPoster_whenRemoveRelatedUser_thenThrowException() {
        String newUsername = "Moshe";
        String nonPoster = "Ofer";
        repository.addRelatedUser(postId, newUsername);

        List<String> expectedUsers = List.of(posterUsername, newUsername);
        assertEquals(expectedUsers, repository.getVolunteerPost(postId).getRelatedUsers());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.removeRelatedUser(postId, newUsername, nonPoster);
        });
        String expectedErr = PostErrors.makeUserIsNotAllowedToMakePostActionError(this.title, nonPoster, "remove user from");
        assertEquals(expectedErr, exception.getMessage());
        assertEquals(expectedUsers, repository.getVolunteerPost(postId).getRelatedUsers());
    }

    @Test
    public void givenPoster_whenRemoveRelatedUser_thenThrowException() {
        assertEquals(relatedUsers, repository.getVolunteerPost(postId).getRelatedUsers());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.removeRelatedUser(postId, posterUsername, posterUsername);
        });
        String expectedErr = PostErrors.makePosterCanNotBeRemovedFromPost(posterUsername, this.title);
        assertEquals(expectedErr, exception.getMessage());
        assertEquals(relatedUsers, repository.getVolunteerPost(postId).getRelatedUsers());
    }

    @Test
    public void givenNonExistingUser_whenRemoveRelatedUser_thenThrowException() {
        String newUsername = "Moshe";
        assertEquals(relatedUsers, repository.getVolunteerPost(postId).getRelatedUsers());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.removeRelatedUser(postId, newUsername, posterUsername);
        });
        String expectedErr = PostErrors.makeUserIsRelatedToPost(newUsername, this.title, false);
        assertEquals(expectedErr, exception.getMessage());
        assertEquals(relatedUsers, repository.getVolunteerPost(postId).getRelatedUsers());
    }

    @Test
    public void givenNewImageByPoster_whenAddImage_thenAdd() {
        String newPath = "dummyPath";
        List<String> expected = List.of(newPath);

        assertEquals(new ArrayList<String>(), repository.getVolunteerPost(postId).getImages());
        repository.addImage(postId, newPath, posterUsername);
        assertEquals(expected, repository.getVolunteerPost(postId).getImages());
    }

    @Test
    public void givenNewImageByRelatedUser_whenAddImage_thenAdd() {
        String newUser = "Moshe";
        repository.addRelatedUser(postId, newUser);

        String newPath = "dummyPath";
        List<String> expected = List.of(newPath);

        assertEquals(new ArrayList<String>(), repository.getVolunteerPost(postId).getImages());
        repository.addImage(postId, newPath, newUser);
        assertEquals(expected, repository.getVolunteerPost(postId).getImages());
    }

    @Test
    public void givenNonExistingPost_whenAddImage_thenThrowException() {
        String newPath = "dummyPath";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.addImage(postId + 1, newPath, posterUsername);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(postId + 1), exception.getMessage());
    }

    @Test
    public void givenNonRelatedUser_whenAddImage_thenThrowException() {
        String newUser = "Moshe";
        String newPath = "dummyPath";

        assertEquals(new ArrayList<String>(), repository.getVolunteerPost(postId).getImages());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.addImage(postId, newPath, newUser);
        });
        String expectedErr = PostErrors.makeUserIsNotAllowedToMakePostActionError(this.title, newUser, "add image to");
        assertEquals(expectedErr, exception.getMessage());

        assertEquals(new ArrayList<String>(), repository.getVolunteerPost(postId).getImages());
    }

    @ParameterizedTest
    @ValueSource(strings = {"dummyPath", "\"dummyPath\""})
    public void givenExistingImage_whenAddImage_thenThrowException(String existingPath) {
        String newPath = "dummyPath";
        repository.addImage(postId, newPath, posterUsername);

        List<String> expected = List.of(newPath);
        assertEquals(expected, repository.getVolunteerPost(postId).getImages());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.addImage(postId, existingPath, posterUsername);
        });
        String expectedErr = PostErrors.makeImagePathExists(newPath, posterUsername, true);
        assertEquals(expectedErr, exception.getMessage());

        assertEquals(expected, repository.getVolunteerPost(postId).getImages());
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    public void givenInvalidImage_whenAddImage_thenThrowException(String invalidPath) {
        assertEquals(new ArrayList<String>(), repository.getVolunteerPost(postId).getImages());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.addImage(postId, invalidPath, posterUsername);
        });
        String expectedErr = PostErrors.makeImagePathIsNotValid(invalidPath);
        assertEquals(expectedErr, exception.getMessage());

        assertEquals(new ArrayList<String>(), repository.getVolunteerPost(postId).getImages());
    }

    @Test
    public void givenExistingImageByPoster_whenRemoveImage_thenRemove() {
        String newPath = "dummyPath";
        repository.addImage(postId, newPath, posterUsername);

        List<String> expected = List.of(newPath);
        assertEquals(expected, repository.getVolunteerPost(postId).getImages());

        repository.removeImage(postId, newPath, posterUsername);

        assertEquals(new ArrayList<>(), repository.getVolunteerPost(postId).getImages());
    }

    @Test
    public void givenExistingImageByExistingUser_whenRemoveImage_thenRemove() {
        String newUser = "Moshe";
        repository.addRelatedUser(postId, newUser);
        String newPath = "dummyPath";
        repository.addImage(postId, newPath, posterUsername);

        List<String> expected = List.of(newPath);
        assertEquals(expected, repository.getVolunteerPost(postId).getImages());

        repository.removeImage(postId, newPath, newUser);

        assertEquals(new ArrayList<>(), repository.getVolunteerPost(postId).getImages());
    }

    @Test
    public void givenNonExistingPost_whenRemoveImage_thenThrowException() {
        String newPath = "dummyPath";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.removeImage(postId + 1, newPath, posterUsername);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(postId + 1), exception.getMessage());
    }

    @Test
    public void givenNonExistingUser_whenRemoveImage_thenThrowException() {
        String newUser = "Moshe";
        String newPath = "dummyPath";
        repository.addImage(postId, newPath, posterUsername);

        List<String> expected = List.of(newPath);
        assertEquals(expected, repository.getVolunteerPost(postId).getImages());
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.removeImage(postId, newPath, newUser);
        });
        String expectedErr = PostErrors.makeUserIsNotAllowedToMakePostActionError(this.title, newUser, "remove image from");
        assertEquals(expectedErr, exception.getMessage());
        assertEquals(expected, repository.getVolunteerPost(postId).getImages());
    }

    @Test
    public void givenNonExistingImage_whenRemoveImage_thenThrowException() {
        String newPath = "dummyPath";

        assertEquals(new ArrayList<>(), repository.getVolunteerPost(postId).getImages());
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.removeImage(postId, newPath, posterUsername);
        });
        String expectedErr = PostErrors.makeImagePathExists(newPath, posterUsername, false);
        assertEquals(expectedErr, exception.getMessage());
        assertEquals(new ArrayList<>(), repository.getVolunteerPost(postId).getImages());
    }

    @Test
    public void givenExistingId_whenGetVolunteerPost_thenNoThrownException() {
        final VolunteerPost[] post = new VolunteerPost[1];
        assertDoesNotThrow(() -> post[0] = repository.getVolunteerPost(postId));
        assertTrue(verifyPostFields(post[0], title, description, keywords, skills, categories));
    }

    @Test
    public void givenNonExistingId_whenGetVolunteerPost_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.getVolunteerPost(postId + 1);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(postId + 1), exception.getMessage());
    }

    @Test
    public void getAllVolunteerPosts() {
        VolunteerPost post1 = repository.getVolunteerPost(postId);

        int postId2 = repository.createVolunteerPost("Title2", "Description2", Set.of("keyword21", "keyword22"), posterUsername, List.of("skill21", "skill22"), List.of("cat21", "cat22"));
        VolunteerPost post2 = repository.getVolunteerPost(postId2);

        int postId3 = repository.createVolunteerPost("Title3", "Description3", Set.of("keyword31", "keyword32"), posterUsername, List.of("skill31", "skill32"), List.of("cat31", "cat32"));
        VolunteerPost post3 = repository.getVolunteerPost(postId3);

        Map<Integer, VolunteerPost> res = new HashMap<>();
        for(VolunteerPost post : repository.getAllVolunteerPosts()) {
            res.put(post.getId(), post);
        }

        assertEquals(3, repository.getAllVolunteerPosts().size());
        assertEquals(post1, res.getOrDefault(postId, null));
        assertEquals(post2, res.getOrDefault(postId2, null));
        assertEquals(post3, res.getOrDefault(postId3, null));
    }

    @Test
    public void givenNonExistingId_whenGetVolunteeringIdByPostId_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.getVolunteerPost(postId + 1);
        });

        assertEquals(PostErrors.makePostIdDoesNotExistError(postId + 1), exception.getMessage());
    }
}
