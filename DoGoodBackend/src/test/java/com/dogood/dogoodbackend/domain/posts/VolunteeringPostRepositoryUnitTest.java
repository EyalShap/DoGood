package com.dogood.dogoodbackend.domain.posts;

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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VolunteeringPostRepositoryUnitTest {
    private static MemoryVolunteeringPostRepository memoryVolunteeringPostRepository;
    private static DBVolunteeringPostRepository dbVolunteeringPostRepository;
    private int memPostId1, dbPostId1, postId1, postId2;

    @Mock
    private VolunteeringPost volunteeringPostMock1;

    @Mock
    private VolunteeringPost volunteeringPostMock2;

    @BeforeAll
    static void setUpBeforeAll() {
        memoryVolunteeringPostRepository = new MemoryVolunteeringPostRepository();
        dbVolunteeringPostRepository = new DBVolunteeringPostRepository();
    }

    @BeforeEach
    void setUpBeforeEach() {
        MockitoAnnotations.openMocks(VolunteeringPostRepositoryUnitTest.class);

        memPostId1 = memoryVolunteeringPostRepository.createVolunteeringPost(volunteeringPostMock1);
        dbPostId1 = dbVolunteeringPostRepository.createVolunteeringPost(volunteeringPostMock1);
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
            VolunteeringPost post = repository.getVolunteeringPost(postId);
            repository.removeVolunteeringPost(postId);
        }
        catch (Exception e) {
        }
    }

    static Stream<VolunteeringPostRepository> repoProvider() {
        return Stream.of(memoryVolunteeringPostRepository);
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenNonNullPost_whenCreateVolunteeringPost_thenCreate(VolunteeringPostRepository volunteeringPostRepository) {
        List<VolunteeringPost> expectedBeforeAdd = new ArrayList<>();
        expectedBeforeAdd.add(volunteeringPostMock1);

        List<VolunteeringPost> expectedAfterAdd = new ArrayList<>();
        expectedAfterAdd.add(volunteeringPostMock1);
        expectedAfterAdd.add(volunteeringPostMock2);

        List<VolunteeringPost> resBeforeAdd = volunteeringPostRepository.getAllVolunteeringPosts();
        assertEquals(expectedBeforeAdd, resBeforeAdd);

        this.postId2 = volunteeringPostRepository.createVolunteeringPost(volunteeringPostMock2);
        List<VolunteeringPost> resAfterAdd = volunteeringPostRepository.getAllVolunteeringPosts();
        assertEquals(expectedAfterAdd, resAfterAdd);
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenNullPost_whenCreatePost_thenThrowException(VolunteeringPostRepository volunteeringPostRepository) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            volunteeringPostRepository.createVolunteeringPost(null);
        });
        assertEquals(PostErrors.makePostIsNotValidError(), exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenExistingId_whenRemoveVolunteeringPost_thenRemove(VolunteeringPostRepository volunteeringPostRepository) {
        setIdByRepo(volunteeringPostRepository);

        assertDoesNotThrow(() -> volunteeringPostRepository.removeVolunteeringPost(postId1));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            volunteeringPostRepository.getVolunteeringPost(postId1);
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

        doNothing().when(volunteeringPostMock1).edit(any(), any());
        assertDoesNotThrow(() -> volunteeringPostRepository.editVolunteeringPost(postId1, "Title", "description"));
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenExistingPostAndNonValidFields_whenEditVolunteeringPost_thenThrowException(VolunteeringPostRepository volunteeringPostRepository) {
        setIdByRepo(volunteeringPostRepository);

        doThrow(new IllegalArgumentException()).when(volunteeringPostMock1).edit(any(), any());
        assertThrows(IllegalArgumentException.class, () -> volunteeringPostRepository.editVolunteeringPost(postId1, "Invalid", "Invalid"));
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenNonExistingPost_whenEditVolunteeringPost_thenThrowException(VolunteeringPostRepository volunteeringPostRepository) {
        setIdByRepo(volunteeringPostRepository);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            volunteeringPostRepository.editVolunteeringPost(postId1 + 1, "title", "description");
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(postId1 + 1), exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenExistingId_whenGetVolunteeringPost_thenNoThrownException(VolunteeringPostRepository volunteeringPostRepository) {
        setIdByRepo(volunteeringPostRepository);
        final VolunteeringPost[] post = new VolunteeringPost[1];
        assertDoesNotThrow(() -> post[0] = volunteeringPostRepository.getVolunteeringPost(postId1));
        assertEquals(volunteeringPostMock1, post[0]);
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenNonExistingId_whenGetVolunteeringPost_thenThrowException(VolunteeringPostRepository volunteeringPostRepository) {
        setIdByRepo(volunteeringPostRepository);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            volunteeringPostRepository.getVolunteeringPost(postId1 + 1);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(postId1 + 1), exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void getAllVolunteeringPosts(VolunteeringPostRepository volunteeringPostRepository) {
        List<VolunteeringPost> expected = new ArrayList<>();
        expected.add(volunteeringPostMock1);
        List<VolunteeringPost> res = volunteeringPostRepository.getAllVolunteeringPosts();
        assertEquals(expected, res);
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenOrganizationId_whenGetOrganizationVolunteeringPosts_thenReturn(VolunteeringPostRepository volunteeringPostRepository) {
        List<VolunteeringPost> expected1 = new ArrayList<>();
        expected1.add(volunteeringPostMock1);
        List<VolunteeringPost> expected2 = new ArrayList<>();
        expected2.add(volunteeringPostMock2);
        List<VolunteeringPost> expected3 = new ArrayList<>();

        this.postId2 = volunteeringPostRepository.createVolunteeringPost(volunteeringPostMock2);
        when(volunteeringPostMock1.getOrganizationId()).thenReturn(0);
        when(volunteeringPostMock2.getOrganizationId()).thenReturn(1);

        List<VolunteeringPost> res1 = volunteeringPostRepository.getOrganizationVolunteeringPosts(0);
        List<VolunteeringPost> res2 = volunteeringPostRepository.getOrganizationVolunteeringPosts(1);
        List<VolunteeringPost> res3 = volunteeringPostRepository.getOrganizationVolunteeringPosts(2);

        assertEquals(expected1, res1);
        assertEquals(expected2, res2);
        assertEquals(expected3, res3);
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenExistingId_whenGetVolunteeringIdByPostId_thenReturnVolunteeringId(VolunteeringPostRepository volunteeringPostRepository) {
        setIdByRepo(volunteeringPostRepository);
        when(volunteeringPostMock1.getVolunteeringId()).thenReturn(435);

        assertEquals(435, memoryVolunteeringPostRepository.getVolunteeringIdByPostId(postId1));
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenNonExistingId_whenGetVolunteeringIdByPostId_thenThrowException(VolunteeringPostRepository volunteeringPostRepository) {
        setIdByRepo(volunteeringPostRepository);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            volunteeringPostRepository.getVolunteeringPost(postId1 + 1);
        });

        assertEquals(PostErrors.makePostIdDoesNotExistError(postId1 + 1), exception.getMessage());
    }

    private void setIdByRepo(VolunteeringPostRepository repository) {
        this.postId1 = repository == memoryVolunteeringPostRepository ? memPostId1 : dbPostId1;
    }
}