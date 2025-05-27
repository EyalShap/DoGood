package com.dogood.dogoodbackend.domain.posts;

import com.dogood.dogoodbackend.jparepos.VolunteeringPostJPA;
import com.dogood.dogoodbackend.utils.PostErrors;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
abstract class AbstractVolunteeringPostRepositoryTest {
    protected VolunteeringPostRepository repository;

    private int postId;
    private final String title = "Postito";
    private final String description = "Postito is a very cool post";
    private final Set<String> keywords = Set.of("Animals", "Fruits");
    private final String posterUsername = "Poster";
    private final int volunteeringId = 0;
    private final int organizationId = 0;

    protected abstract VolunteeringPostRepository createRepository();

    @BeforeEach
    void setup() {
        repository = createRepository();
        postId = repository.createVolunteeringPost(title, description, new HashSet<>(keywords), posterUsername, volunteeringId, organizationId);
    }

    @AfterEach
    void tearDown() {
        repository.clear();
    }

    private boolean verifyPostFields(String title, String description, Set<String> keywords) {
        VolunteeringPost post = repository.getVolunteeringPostForRead(postId);
        return verifyPostFields(post, title, description, keywords);
    }

    private boolean verifyPostFields(VolunteeringPost post, String title, String description, Set<String> keywords) {
        return post.getTitle().equals(title) &&
                post.getDescription().equals(description) &&
                post.getKeywords().equals(keywords);
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
    void givenValidFields_whenCreateVolunteeringPost_thenCreate(String title, String description, Set<String> keywords) {
        List<VolunteeringPost> allPostsBefore = repository.getAllVolunteeringPosts();
        VolunteeringPost post1 = repository.getVolunteeringPostForRead(postId);
        List<VolunteeringPost> expectedBefore = List.of(post1);
        assertEquals(expectedBefore, allPostsBefore);

        int postId2 = repository.createVolunteeringPost(title, description, keywords, posterUsername, 0, 0);
        VolunteeringPost post2 = repository.getVolunteeringPostForRead(postId2);

        List<VolunteeringPost> allPostsAfter = repository.getAllVolunteeringPosts();
        List<VolunteeringPost> expectedAfter = List.of(post1, post2);
        assertEquals(new HashSet<>(expectedAfter), new HashSet<>(allPostsAfter));
    }

    @Test
    void givenInvalidFields_whenCreateVolunteeringPost_thenThrowException() {
        List<VolunteeringPost> allPostsBefore = repository.getAllVolunteeringPosts();
        VolunteeringPost post1 = repository.getVolunteeringPostForRead(postId);
        List<VolunteeringPost> expected = List.of(post1);
        assertEquals(expected, allPostsBefore);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.createVolunteeringPost("", description, keywords, posterUsername, 0, 0);
        });
        assertEquals("Invalid post title: .\n", exception.getMessage());

        List<VolunteeringPost> allPostsAfter = repository.getAllVolunteeringPosts();
        assertEquals(expected, allPostsAfter);
    }

    @Test
    void givenExistingId_whenRemoveVolunteeringPost_thenRemove() {
        assertDoesNotThrow(() -> repository.getVolunteeringPostForRead(postId));

        repository.removeVolunteeringPost(postId);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.getVolunteeringPostForRead(postId);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(postId), exception.getMessage());
    }

    @Test
    void givenNonExistingId_whenRemoveVolunteeringPost_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.removeVolunteeringPost(postId + 1);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(postId + 1), exception.getMessage());
    }

    @Test
    void givenVolunteeringId_whenRemovePostsByVolunteeringId_thenRemove() {
        VolunteeringPost post1 = repository.getVolunteeringPostForRead(postId);

        int postId2 = repository.createVolunteeringPost("Title2", "Description2", Set.of("keyword21", "keyword22"), posterUsername, volunteeringId, organizationId);
        VolunteeringPost post2 = repository.getVolunteeringPostForRead(postId2);

        int postId3 = repository.createVolunteeringPost("Title3", "Description3", Set.of("keyword31", "keyword32"), posterUsername, volunteeringId + 1, organizationId);
        VolunteeringPost post3 = repository.getVolunteeringPostForRead(postId3);

        List<VolunteeringPost> expectedBefore = List.of(post1, post2, post3);
        List<VolunteeringPost> allPostsBefore = repository.getAllVolunteeringPosts();
        assertEquals(new HashSet<>(expectedBefore), new HashSet<>(allPostsBefore));

        repository.removePostsByVolunteeringId(volunteeringId);

        List<VolunteeringPost> expectedAfter = List.of(post3);
        List<VolunteeringPost> allPostsAfter = repository.getAllVolunteeringPosts();
        assertEquals(new HashSet<>(expectedAfter), new HashSet<>(allPostsAfter));

        repository.removePostsByVolunteeringId(volunteeringId + 1);

        expectedAfter = new ArrayList<>();
        allPostsAfter = repository.getAllVolunteeringPosts();
        assertEquals(new HashSet<>(expectedAfter), new HashSet<>(allPostsAfter));
    }

    @Test
    void givenExistingId_whenIncNumOfPeopleRequestedToJoin_thenInc() {
        int requestsBefore = repository.getVolunteeringPostForRead(postId).getNumOfPeopleRequestedToJoin();
        repository.incNumOfPeopleRequestedToJoin(postId);
        int requestsAfter = repository.getVolunteeringPostForRead(postId).getNumOfPeopleRequestedToJoin();
        assertEquals(1, requestsAfter - requestsBefore);
    }

    @Test
    void givenNonExistingId_whenIncNumOfPeopleRequestedToJoin_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.incNumOfPeopleRequestedToJoin(postId + 1);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(postId + 1), exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("validInputs")
    void givenExistingPostAndValidFields_whenEditVolunteeringPost_thenEdit(String newTitle, String newDescription, Set<String> newKeywords) {
        assertDoesNotThrow(() -> repository.editVolunteeringPost(postId, newTitle, newDescription, new HashSet<>(newKeywords)));
        assertTrue(verifyPostFields(newTitle, newDescription, newKeywords));
    }

    @Test
    void givenExistingPostAndNonValidFields_whenEditVolunteeringPost_thenThrowException() {
        assertThrows(IllegalArgumentException.class, () -> repository.editVolunteeringPost(postId, "", "", null));
        assertTrue(verifyPostFields(title, description, keywords));
    }

    @Test
    void givenNonExistingPost_whenEditVolunteeringPost_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.editVolunteeringPost(postId + 1, "title", "description", new HashSet<>());
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(postId + 1), exception.getMessage());
    }

    @Test
    void givenExistingId_whengetVolunteeringPostForRead_thenNoThrownException() {
        final VolunteeringPost[] post = new VolunteeringPost[1];
        assertDoesNotThrow(() -> post[0] = repository.getVolunteeringPostForRead(postId));
        assertTrue(verifyPostFields(post[0], title, description, keywords));
    }

    @Test
    void givenNonExistingId_whengetVolunteeringPostForRead_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.getVolunteeringPostForRead(postId + 1);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(postId + 1), exception.getMessage());
    }

    @Test
    void getAllVolunteeringPosts() {
        VolunteeringPost post1 = repository.getVolunteeringPostForRead(postId);

        int postId2 = repository.createVolunteeringPost("Title2", "Description2", Set.of("keyword21", "keyword22"), posterUsername, volunteeringId, organizationId);
        VolunteeringPost post2 = repository.getVolunteeringPostForRead(postId2);

        int postId3 = repository.createVolunteeringPost("Title3", "Description3", Set.of("keyword31", "keyword32"), posterUsername, volunteeringId, organizationId);
        VolunteeringPost post3 = repository.getVolunteeringPostForRead(postId3);

        List<VolunteeringPost> expected = List.of(post1, post2, post3);
        List<VolunteeringPost> res = repository.getAllVolunteeringPosts();
        assertEquals(new HashSet<>(expected), new HashSet<>(res));
    }

    @Test
    void givenOrganizationId_whenGetOrganizationVolunteeringPosts_thenReturn() {
        VolunteeringPost post1 = repository.getVolunteeringPostForRead(postId);

        int postId2 = repository.createVolunteeringPost("Title2", "Description2", Set.of("keyword21", "keyword22"), posterUsername, volunteeringId, organizationId);
        VolunteeringPost post2 = repository.getVolunteeringPostForRead(postId2);

        int postId3 = repository.createVolunteeringPost("Title3", "Description3", Set.of("keyword31", "keyword32"), posterUsername, volunteeringId, organizationId + 1);
        VolunteeringPost post3 = repository.getVolunteeringPostForRead(postId3);

        List<VolunteeringPost> expected1 = List.of(post1, post2);
        List<VolunteeringPost> expected2 = List.of(post3);
        List<VolunteeringPost> expected3 = new ArrayList<>();

        List<VolunteeringPost> res1 = repository.getOrganizationVolunteeringPosts(organizationId);
        List<VolunteeringPost> res2 = repository.getOrganizationVolunteeringPosts(organizationId + 1);
        List<VolunteeringPost> res3 = repository.getOrganizationVolunteeringPosts(organizationId + 2);

        assertEquals(expected1, res1);
        assertEquals(expected2, res2);
        assertEquals(expected3, res3);
    }

    @Test
    void givenExistingId_whenGetVolunteeringIdByPostId_thenReturnVolunteeringId() {
        int postId2 = repository.createVolunteeringPost("Title2", "Description2", Set.of("keyword21", "keyword22"), posterUsername, volunteeringId + 1, organizationId);
        int postId3 = repository.createVolunteeringPost("Title3", "Description3", Set.of("keyword31", "keyword32"), posterUsername, volunteeringId + 2, organizationId);

        assertEquals(volunteeringId, repository.getVolunteeringIdByPostId(postId));
        assertEquals(volunteeringId + 1, repository.getVolunteeringIdByPostId(postId2));
        assertEquals(volunteeringId + 2, repository.getVolunteeringIdByPostId(postId3));
    }

    @Test
    void givenNonExistingId_whenGetVolunteeringIdByPostId_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.getVolunteeringPostForRead(postId + 1);
        });

        assertEquals(PostErrors.makePostIdDoesNotExistError(postId + 1), exception.getMessage());
    }
}
