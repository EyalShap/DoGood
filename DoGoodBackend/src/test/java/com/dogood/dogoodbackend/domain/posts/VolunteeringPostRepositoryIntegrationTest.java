package com.dogood.dogoodbackend.domain.posts;

import com.dogood.dogoodbackend.domain.organizations.Organization;
import com.dogood.dogoodbackend.jparepos.OrganizationJPA;
import com.dogood.dogoodbackend.jparepos.VolunteeringPostJPA;
import com.dogood.dogoodbackend.utils.PostErrors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.dogood.dogoodbackend.utils.ValidateFields.isValidText;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class VolunteeringPostRepositoryIntegrationTest {
    private static MemoryVolunteeringPostRepository memoryVolunteeringPostRepository;
    private static DBVolunteeringPostRepository dbVolunteeringPostRepository;
    private int memPostId, dbPostId, postId, postId2;
    private VolunteeringPost memVolunteeringPost, dbVolunteeringPost;
    private final String title = "Title";
    private final String description = "Description";
    private final int volunteeringId = 0;
    private final int organizationId = 0;
    private final String actor1 = "TheDoctor";

    @Autowired
    private ApplicationContext applicationContext;
    private VolunteeringPostJPA volunteeringPostJPA;

    @BeforeAll
    static void setUpBeforeAll() {
        memoryVolunteeringPostRepository = new MemoryVolunteeringPostRepository();
        dbVolunteeringPostRepository = new DBVolunteeringPostRepository();
    }

    @BeforeEach
    void setUpBeforeEach() {
        VolunteeringPostJPA volunteeringPostJPA = applicationContext.getBean(VolunteeringPostJPA.class);
        dbVolunteeringPostRepository.setJPA(volunteeringPostJPA);
        volunteeringPostJPA.deleteAll();

        this.memPostId = memoryVolunteeringPostRepository.createVolunteeringPost(title, description, actor1, volunteeringId, organizationId);
        this.memVolunteeringPost = new VolunteeringPost(memPostId, title, description, actor1, volunteeringId, organizationId);

        this.dbPostId = dbVolunteeringPostRepository.createVolunteeringPost(title, description, actor1, volunteeringId, organizationId);
        this.dbVolunteeringPost = new VolunteeringPost(dbPostId, title, description, actor1, volunteeringId, organizationId);
    }

    @AfterEach
    void afterEach() {
        removePostAfterEach(memPostId, memoryVolunteeringPostRepository);
        removePostAfterEach(dbPostId, dbVolunteeringPostRepository);
        removePostAfterEach(postId2, memoryVolunteeringPostRepository);
        removePostAfterEach(postId2, dbVolunteeringPostRepository);
    }

    private void removePostAfterEach(int postId, VolunteeringPostRepository repository) {
        try {
            VolunteeringPost post = repository.getVolunteeringPost(postId);
            repository.removeVolunteeringPost(postId);
        }
        catch (Exception e) {
        }
    }

    static Stream<VolunteeringPostRepository> repoProvider() {
        return Stream.of(memoryVolunteeringPostRepository, dbVolunteeringPostRepository);
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenNonNullPost_whenCreateVolunteeringPost_thenCreate(VolunteeringPostRepository volunteeringPostRepository) {
        VolunteeringPost post1 = getPostByRepo(volunteeringPostRepository);


        List<VolunteeringPost> expectedBeforeAdd = new ArrayList<>();
        expectedBeforeAdd.add(post1);

        List<VolunteeringPost> resBeforeAdd = volunteeringPostRepository.getAllVolunteeringPosts();
        assertEquals(expectedBeforeAdd, resBeforeAdd);

        this.postId2 = volunteeringPostRepository.createVolunteeringPost("Blah", "Blah", actor1, 1, 2);
        VolunteeringPost post2 = new VolunteeringPost(this.postId2, "Blah", "Blah", actor1, 1, 2);
        List<VolunteeringPost> expectedAfterAdd = new ArrayList<>();
        expectedAfterAdd.add(post1);
        expectedAfterAdd.add(post2);

        List<VolunteeringPost> resAfterAdd = volunteeringPostRepository.getAllVolunteeringPosts();
        assertEquals(expectedAfterAdd, resAfterAdd);
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenExistingId_whenRemoveVolunteeringPost_thenRemove(VolunteeringPostRepository volunteeringPostRepository) {
        setIdByRepo(volunteeringPostRepository);

        assertDoesNotThrow(() -> volunteeringPostRepository.removeVolunteeringPost(postId));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            volunteeringPostRepository.getVolunteeringPost(postId);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(postId), exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenNonExistingId_whenRemoveVolunteeringPost_thenThrowException(VolunteeringPostRepository volunteeringPostRepository) {
        setIdByRepo(volunteeringPostRepository);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            volunteeringPostRepository.removeVolunteeringPost(postId + 1);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(postId + 1), exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenExistingPostAndValidFields_whenEditVolunteeringPost_thenEdit(VolunteeringPostRepository volunteeringPostRepository) {
        setIdByRepo(volunteeringPostRepository);

        assertDoesNotThrow(() -> volunteeringPostRepository.editVolunteeringPost(postId, "Title", "description"));

        VolunteeringPost expected = new VolunteeringPost(postId, "Title", "description", actor1, volunteeringId, organizationId);
        assertEquals(expected, volunteeringPostRepository.getVolunteeringPost(postId));
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenExistingPostAndNonValidFields_whenEditVolunteeringPost_thenThrowException(VolunteeringPostRepository volunteeringPostRepository) {
        setIdByRepo(volunteeringPostRepository);
        VolunteeringPost post = volunteeringPostRepository == memoryVolunteeringPostRepository ? memVolunteeringPost : dbVolunteeringPost;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            volunteeringPostRepository.editVolunteeringPost(postId, "", "");
        });
        StringBuilder expected = new StringBuilder();
        expected.append("Invalid post title: .\n").append("Invalid post description: .\n");

        assertEquals(expected.toString(), exception.getMessage());
        assertEquals(post, volunteeringPostRepository.getVolunteeringPost(postId));
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenNonExistingPost_whenEditVolunteeringPost_thenThrowException(VolunteeringPostRepository volunteeringPostRepository) {
        setIdByRepo(volunteeringPostRepository);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            volunteeringPostRepository.editVolunteeringPost(postId + 1, "title", "description");
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(postId + 1), exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenExistingId_whenGetVolunteeringPost_thenNoThrownException(VolunteeringPostRepository volunteeringPostRepository) {
        setIdByRepo(volunteeringPostRepository);
        VolunteeringPost expectedPost = getPostByRepo(volunteeringPostRepository);
        final VolunteeringPost[] post = new VolunteeringPost[1];
        assertDoesNotThrow(() -> post[0] = volunteeringPostRepository.getVolunteeringPost(postId));
        assertEquals(expectedPost, post[0]);
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenNonExistingId_whenGetVolunteeringPost_thenThrowException(VolunteeringPostRepository volunteeringPostRepository) {
        setIdByRepo(volunteeringPostRepository);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            volunteeringPostRepository.getVolunteeringPost(postId + 1);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(postId + 1), exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void getAllVolunteeringPosts(VolunteeringPostRepository volunteeringPostRepository) {
        VolunteeringPost expectedPost = getPostByRepo(volunteeringPostRepository);
        List<VolunteeringPost> expected = new ArrayList<>();
        expected.add(expectedPost);
        List<VolunteeringPost> res = volunteeringPostRepository.getAllVolunteeringPosts();
        assertEquals(expected, res);
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenOrganizationId_whenGetOrganizationVolunteeringPosts_thenReturn(VolunteeringPostRepository volunteeringPostRepository) {
        this.postId2 = volunteeringPostRepository.createVolunteeringPost("Blah", "Blah", actor1, 1, 1);
        VolunteeringPost post2 = new VolunteeringPost(this.postId2, "Blah", "Blah", actor1, 1, 1);

        List<VolunteeringPost> res1 = volunteeringPostRepository.getOrganizationVolunteeringPosts(0);
        List<VolunteeringPost> res2 = volunteeringPostRepository.getOrganizationVolunteeringPosts(1);
        List<VolunteeringPost> res3 = volunteeringPostRepository.getOrganizationVolunteeringPosts(2);

        VolunteeringPost expectedPost1 = getPostByRepo(volunteeringPostRepository);
        List<VolunteeringPost> expected1 = new ArrayList<>();
        expected1.add(expectedPost1);
        List<VolunteeringPost> expected2 = new ArrayList<>();
        expected2.add(post2);
        List<VolunteeringPost> expected3 = new ArrayList<>();

        assertEquals(expected1, res1);
        assertEquals(expected2, res2);
        assertEquals(expected3, res3);
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenExistingId_whenGetVolunteeringIdByPostId_thenReturnVolunteeringId(VolunteeringPostRepository volunteeringPostRepository) {
        setIdByRepo(volunteeringPostRepository);

        assertEquals(volunteeringId, volunteeringPostRepository.getVolunteeringIdByPostId(postId));
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenNonExistingId_whenGetVolunteeringIdByPostId_thenThrowException(VolunteeringPostRepository volunteeringPostRepository) {
        setIdByRepo(volunteeringPostRepository);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            volunteeringPostRepository.getVolunteeringPost(postId + 1);
        });

        assertEquals(PostErrors.makePostIdDoesNotExistError(postId + 1), exception.getMessage());
    }

    private void setIdByRepo(VolunteeringPostRepository repository) {
        this.postId = repository == memoryVolunteeringPostRepository ? memPostId : dbPostId;
    }

    private VolunteeringPost getPostByRepo(VolunteeringPostRepository repository) {
        return repository == memoryVolunteeringPostRepository ? memVolunteeringPost : dbVolunteeringPost;
    }
}