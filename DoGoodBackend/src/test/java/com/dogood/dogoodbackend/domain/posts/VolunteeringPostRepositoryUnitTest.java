package com.dogood.dogoodbackend.domain.posts;

import com.dogood.dogoodbackend.jparepos.VolunteeringPostJPA;
import com.dogood.dogoodbackend.utils.PostErrors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class VolunteeringPostRepositoryUnitTest {
    // MAY BE DELETED BECAUSE IT IS JUST THE SAME AS INTEGRATION
    private static MemoryVolunteeringPostRepository memoryVolunteeringPostRepository;
    private static DBVolunteeringPostRepository dbVolunteeringPostRepository;
    private int memPostId1, dbPostId1, postId1, postId2;

    @Mock
    private VolunteeringPost volunteeringPostMock1;

    @Mock
    private VolunteeringPost volunteeringPostMock2;

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
        MockitoAnnotations.openMocks(VolunteeringPostRepositoryUnitTest.class);

        VolunteeringPostJPA volunteeringPostJPA = applicationContext.getBean(VolunteeringPostJPA.class);
        dbVolunteeringPostRepository.setJPA(volunteeringPostJPA);
        volunteeringPostJPA.deleteAll();

        memPostId1 = memoryVolunteeringPostRepository.createVolunteeringPost("Title", "Description", null, "TheDoctor", 0, 0);
        dbPostId1 = dbVolunteeringPostRepository.createVolunteeringPost("Title", "Description", null,"TheDoctor", 0, 0);
    }

    @AfterEach
    void afterEach() {
        removePostAfterEach(memPostId1, memoryVolunteeringPostRepository);
        removePostAfterEach(dbPostId1, dbVolunteeringPostRepository);
        removePostAfterEach(postId2, memoryVolunteeringPostRepository);
        removePostAfterEach(postId2, dbVolunteeringPostRepository);
    }

    private void removePostAfterEach(int postId, VolunteeringPostRepository repository) {
        try {
            VolunteeringPost post = repository.getVolunteeringPostForRead(postId);
            repository.removeVolunteeringPost(postId);
        }
        catch (Exception e) {
        }
    }

    static Stream<VolunteeringPostRepository> repoProvider() {
        return Stream.of(memoryVolunteeringPostRepository, dbVolunteeringPostRepository);
    }

    /*@ParameterizedTest
    @MethodSource("repoProvider")
    void givenNonNullPost_whenCreateVolunteeringPost_thenCreate(VolunteeringPostRepository volunteeringPostRepository) {
        List<VolunteeringPost> expectedBeforeAdd = new ArrayList<>();
        expectedBeforeAdd.add(volunteeringPostMock1);

        List<VolunteeringPost> expectedAfterAdd = new ArrayList<>();
        expectedAfterAdd.add(volunteeringPostMock1);
        expectedAfterAdd.add(volunteeringPostMock2);

        List<VolunteeringPost> resBeforeAdd = volunteeringPostRepository.getAllVolunteeringPosts();
        assertEquals(expectedBeforeAdd, resBeforeAdd);

        this.postId2 = volunteeringPostRepository.createVolunteeringPost("Title2", "Description2", "TheDoctor", 0, 0);
        List<VolunteeringPost> resAfterAdd = volunteeringPostRepository.getAllVolunteeringPosts();
        assertEquals(expectedAfterAdd, resAfterAdd);
    }*/

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenExistingId_whenRemoveVolunteeringPost_thenRemove(VolunteeringPostRepository volunteeringPostRepository) {
        setIdByRepo(volunteeringPostRepository);

        assertDoesNotThrow(() -> volunteeringPostRepository.removeVolunteeringPost(postId1));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            volunteeringPostRepository.getVolunteeringPostForRead(postId1);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(postId1), exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenNonExistingId_whenRemoveVolunteeringPost_thenThrowException(VolunteeringPostRepository volunteeringPostRepository) {
        setIdByRepo(volunteeringPostRepository);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            volunteeringPostRepository.removeVolunteeringPost(postId1 + 1);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(postId1 + 1), exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenExistingPostAndValidFields_whenEditVolunteeringPost_thenEdit(VolunteeringPostRepository volunteeringPostRepository) {
        setIdByRepo(volunteeringPostRepository);

        assertDoesNotThrow(() -> volunteeringPostRepository.editVolunteeringPost(postId1, "Title", "description", null));
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenExistingPostAndNonValidFields_whenEditVolunteeringPost_thenThrowException(VolunteeringPostRepository volunteeringPostRepository) {
        setIdByRepo(volunteeringPostRepository);

        assertThrows(IllegalArgumentException.class, () -> volunteeringPostRepository.editVolunteeringPost(postId1, "", "", null));
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenNonExistingPost_whenEditVolunteeringPost_thenThrowException(VolunteeringPostRepository volunteeringPostRepository) {
        setIdByRepo(volunteeringPostRepository);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            volunteeringPostRepository.editVolunteeringPost(postId1 + 1, "title", "description", null);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(postId1 + 1), exception.getMessage());
    }

    // The same test as integration
    /*@ParameterizedTest
    @MethodSource("repoProvider")
    void givenExistingId_whengetVolunteeringPostForRead_thenNoThrownException(VolunteeringPostRepository volunteeringPostRepository) {
        setIdByRepo(volunteeringPostRepository);
        final VolunteeringPost[] post = new VolunteeringPost[1];
        assertDoesNotThrow(() -> post[0] = volunteeringPostRepository.getVolunteeringPostForRead(postId1));
        assertEquals(volunteeringPostMock1, post[0]);
    }*/

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenNonExistingId_whengetVolunteeringPostForRead_thenThrowException(VolunteeringPostRepository volunteeringPostRepository) {
        setIdByRepo(volunteeringPostRepository);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            volunteeringPostRepository.getVolunteeringPostForRead(postId1 + 1);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(postId1 + 1), exception.getMessage());
    }

    /*@ParameterizedTest
    @MethodSource("repoProvider")
    void getAllVolunteeringPosts(VolunteeringPostRepository volunteeringPostRepository) {
        List<VolunteeringPost> expected = new ArrayList<>();
        expected.add(volunteeringPostMock1);
        List<VolunteeringPost> res = volunteeringPostRepository.getAllVolunteeringPosts();
        assertEquals(expected, res);
    }*/

    /*@ParameterizedTest
    @MethodSource("repoProvider")
    void givenOrganizationId_whenGetOrganizationVolunteeringPosts_thenReturn(VolunteeringPostRepository volunteeringPostRepository) {
        List<VolunteeringPost> expected1 = new ArrayList<>();
        expected1.add(volunteeringPostMock1);
        List<VolunteeringPost> expected2 = new ArrayList<>();
        expected2.add(volunteeringPostMock2);
        List<VolunteeringPost> expected3 = new ArrayList<>();

        this.postId2 = volunteeringPostRepository.createVolunteeringPost("Title2", "Description2", "TheDoctor", 0, 0);
        when(volunteeringPostMock1.getOrganizationId()).thenReturn(0);
        when(volunteeringPostMock2.getOrganizationId()).thenReturn(1);

        List<VolunteeringPost> res1 = volunteeringPostRepository.getOrganizationVolunteeringPosts(0);
        List<VolunteeringPost> res2 = volunteeringPostRepository.getOrganizationVolunteeringPosts(1);
        List<VolunteeringPost> res3 = volunteeringPostRepository.getOrganizationVolunteeringPosts(2);

        assertEquals(expected1, res1);
        assertEquals(expected2, res2);
        assertEquals(expected3, res3);
    }*/

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenExistingId_whenGetVolunteeringIdByPostId_thenReturnVolunteeringId(VolunteeringPostRepository volunteeringPostRepository) {
        setIdByRepo(volunteeringPostRepository);

        assertEquals(0, volunteeringPostRepository.getVolunteeringIdByPostId(postId1));
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenNonExistingId_whenGetVolunteeringIdByPostId_thenThrowException(VolunteeringPostRepository volunteeringPostRepository) {
        setIdByRepo(volunteeringPostRepository);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            volunteeringPostRepository.getVolunteeringPostForRead(postId1 + 1);
        });

        assertEquals(PostErrors.makePostIdDoesNotExistError(postId1 + 1), exception.getMessage());
    }

    private void setIdByRepo(VolunteeringPostRepository repository) {
        this.postId1 = repository == memoryVolunteeringPostRepository ? memPostId1 : dbPostId1;
    }
}