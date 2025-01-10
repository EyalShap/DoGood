package com.dogood.dogoodbackend.domain.posts;

import com.dogood.dogoodbackend.domain.externalAIAPI.ProxyKeywordExtractor;
import com.dogood.dogoodbackend.domain.organizations.*;
import com.dogood.dogoodbackend.domain.volunteerings.AddressTuple;
import com.dogood.dogoodbackend.domain.volunteerings.MemoryVolunteeringRepository;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringFacade;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringRepository;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.MemorySchedulingManager;
import com.dogood.dogoodbackend.utils.OrganizationErrors;
import com.dogood.dogoodbackend.utils.PostErrors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

public class PostsFacadeMemoryIntegrationTest {
    private PostsFacade postsFacade;
    private int postId;
    private final String title1 = "Title1";
    private final String description1 = "Description1";
    private int volunteeringId1, volunteeringId2;
    private int organizationId1;
    private final String title2 = "Title2";
    private final String description2 = "Description2";
    private final String actor1 = "TheDoctor";
    private final String actor2 = "NotTheDoctor";
    private VolunteeringPostRepository volunteeringPostRepository;
    private RequestRepository requestRepository;
    private OrganizationRepository organizationRepository;
    private VolunteeringRepository volunteeringRepository;
    private VolunteeringFacade volunteeringFacade;
    private OrganizationsFacade organizationsFacade;

    @BeforeEach
    void setUp() {
        this.volunteeringPostRepository = new MemoryVolunteeringPostRepository();
        this.requestRepository = new MemoryRequestRepository();
        this.organizationRepository = new MemoryOrganizationRepository();
        this.volunteeringRepository = new MemoryVolunteeringRepository();

        this.organizationsFacade = new OrganizationsFacade(organizationRepository, requestRepository);
        this.volunteeringFacade = new VolunteeringFacade(organizationsFacade, volunteeringRepository, new MemorySchedulingManager());
        this.postsFacade = new PostsFacade(volunteeringPostRepository, volunteeringFacade, organizationsFacade, new ProxyKeywordExtractor());

        this.organizationsFacade.setVolunteeringFacade(volunteeringFacade);

        this.organizationId1 = this.organizationsFacade.createOrganization("Organization1", "Description1", "0547960995", "org@gmail.com", actor1);
        this.volunteeringId1 = this.volunteeringFacade.createVolunteering(actor1, organizationId1, "Volunteering", "Description");
        this.postId = this.postsFacade.createVolunteeringPost(title1, description1, actor1, volunteeringId1);
    }

    @Test
    void givenValidFieldsAndOrganizationManager_whenCreateVolunteeringPost_thenCreate() {
        assertEquals(1, postsFacade.getAllVolunteeringPosts(actor1).size());

        int newPostId = volunteeringPostRepository.getNextVolunteeringPostId();
        int resId = postsFacade.createVolunteeringPost(title2, description2, actor1, volunteeringId1);

        assertEquals(newPostId, resId);
        assertEquals(2, postsFacade.getAllVolunteeringPosts(actor1).size());
    }

    @Test
    void givenInvalidFields_whenCreateVolunteeringPost_thenThrowException() {
        assertEquals(1, postsFacade.getAllVolunteeringPosts(actor1).size());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.createVolunteeringPost("", "", actor1, volunteeringId1);
        });

        StringBuilder expected = new StringBuilder();
        expected.append("Invalid post title: .\n").append("Invalid post description: .\n");

        assertEquals(expected.toString(), exception.getMessage());
        assertEquals(1, postsFacade.getAllVolunteeringPosts(actor1).size());
    }

    @Test
    void givenNonExistingVolunteering_whenCreateVolunteeringPost_thenThrowException() {
        assertEquals(1, postsFacade.getAllVolunteeringPosts(actor1).size());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.createVolunteeringPost(title1, description1, actor1, volunteeringId1 + 1);
        });

        assertEquals("Volunteering with id " + (volunteeringId1 + 1) + " does not exist", exception.getMessage());
        assertEquals(1, postsFacade.getAllVolunteeringPosts(actor1).size());
    }

    @Test
    void givenNonOrganizationManager_whenCreateVolunteeringPost_thenThrowException() {
        assertEquals(1, postsFacade.getAllVolunteeringPosts(actor1).size());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.createVolunteeringPost(title1, description1, actor2, volunteeringId1);
        });

        assertEquals(OrganizationErrors.makeNonManagerCanNotPreformActionError(actor2, "Organization1", "post about the organization's volunteering"), exception.getMessage());
        assertEquals(1, postsFacade.getAllVolunteeringPosts(actor1).size());
    }

    @Test
    void givenExistingIdAndManager_whenRemoveVolunteeringPost_thenRemove() {
        assertEquals(1, postsFacade.getAllVolunteeringPosts(actor1).size());
        postsFacade.removeVolunteeringPost(postId, actor1);
        assertEquals(0, postsFacade.getAllVolunteeringPosts(actor1).size());
    }

    @Test
    void givenExistingIdAndNonManager_whenRemoveVolunteeringPost_thenThrowException() {
        assertEquals(1, postsFacade.getAllVolunteeringPosts(actor1).size());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.removeVolunteeringPost(postId, actor2);
        });
        assertEquals(PostErrors.makeUserIsNotAllowedToMakePostActionError(postId, actor2, "remove"), exception.getMessage());
        assertEquals(1, postsFacade.getAllVolunteeringPosts(actor1).size());
    }

    @Test
    void givenNonExistingId_whenRemoveVolunteeringPost_thenThrowException() {
        assertEquals(1, postsFacade.getAllVolunteeringPosts(actor1).size());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.removeVolunteeringPost(postId + 1, actor1);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(postId + 1), exception.getMessage());
        assertEquals(1, postsFacade.getAllVolunteeringPosts(actor1).size());
    }

    @Test
    void givenExistingIdAndManagerAndValidFields_whenEditVolunteeringPost_thenEdit() {
        VolunteeringPostDTO postBefore = postsFacade.getVolunteeringPost(postId, actor1);
        assertEquals(title1, postBefore.getTitle());
        assertEquals(description1, postBefore.getDescription());

        postsFacade.editVolunteeringPost(postId, title2, description2, actor1);

        VolunteeringPostDTO postAfter = postsFacade.getVolunteeringPost(postId, actor1);
        assertEquals(title2, postAfter.getTitle());
        assertEquals(description2, postAfter.getDescription());
    }

    @Test
    void givenNonManager_whenEditOrganization_thenThrowException() {
        VolunteeringPostDTO postBefore = postsFacade.getVolunteeringPost(postId, actor1);
        assertEquals(title1, postBefore.getTitle());
        assertEquals(description1, postBefore.getDescription());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.editVolunteeringPost(postId, title2, description2, actor2);
        });
        assertEquals(PostErrors.makeUserIsNotAllowedToMakePostActionError(postId, actor2, "edit"), exception.getMessage());

        VolunteeringPostDTO postAfter = postsFacade.getVolunteeringPost(postId, actor1);
        assertEquals(title1, postAfter.getTitle());
        assertEquals(description1, postAfter.getDescription());
    }

    @Test
    void givenNonExistingId_whenEditOrganization_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.editVolunteeringPost(postId + 1, title2, description2, actor1);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(postId + 1), exception.getMessage());
    }

    @Test
    void givenInvalidFields_whenEditOrganization_thenThrowException() {
        VolunteeringPostDTO postBefore = postsFacade.getVolunteeringPost(postId, actor1);
        assertEquals(title1, postBefore.getTitle());
        assertEquals(description1, postBefore.getDescription());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.editVolunteeringPost(postId, "", "", actor1);
        });
        StringBuilder expected = new StringBuilder();
        expected.append("Invalid post title: .\n").append("Invalid post description: .\n");
        assertEquals(expected.toString(), exception.getMessage());

        VolunteeringPostDTO postAfter = postsFacade.getVolunteeringPost(postId, actor1);
        assertEquals(title1, postAfter.getTitle());
        assertEquals(description1, postAfter.getDescription());
    }

    @Test
    void doesExist() {
        assertTrue(postsFacade.doesExist(postId));
        assertFalse(postsFacade.doesExist(postId + 1));
    }

    @Test
    void givenExistingId_whenGetVolunteeringPost_thenReturnVolunteeringPost() {
        VolunteeringPost expected = new VolunteeringPost(postId, title1, description1, actor1, volunteeringId1, organizationId1);

        LocalDateTime date = LocalDateTime.of(2025, 1, 1, 10, 0);
        try (MockedStatic<LocalDateTime> mocked = mockStatic(LocalDateTime.class)) {
            mocked.when(LocalDateTime::now).thenReturn(date);

            assertEquals(new VolunteeringPostDTO(expected), postsFacade.getVolunteeringPost(postId, actor1));
        }
    }

    @Test
    void givenNonExistingId_whenGetVolunteeringPost_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.getVolunteeringPost(postId + 1, actor1);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(postId + 1), exception.getMessage());
    }

    @Test
    void getAllVolunteeringPosts() {
        int newPostId = volunteeringPostRepository.getNextVolunteeringPostId();
        int resId = postsFacade.createVolunteeringPost(title2, description2, actor1, volunteeringId1);

        List<VolunteeringPostDTO> expected = new ArrayList<>();
        LocalDateTime date = LocalDateTime.of(2025, 1, 1, 10, 0);
        try (MockedStatic<LocalDateTime> mocked = mockStatic(LocalDateTime.class)) {
            mocked.when(LocalDateTime::now).thenReturn(date);

            expected.add(new VolunteeringPostDTO(new VolunteeringPost(postId, title1, description1, actor1, volunteeringId1, organizationId1)));
            expected.add(new VolunteeringPostDTO(new VolunteeringPost(newPostId, title2, description2, actor1, volunteeringId1, organizationId1)));
        }
        assertEquals(expected, postsFacade.getAllVolunteeringPosts(actor1));
    }

    @Test
    void givenExistingPosts_whenGetOrganizationVolunteeringPosts_thenReturnPosts() {
        List<VolunteeringPostDTO> expected = new ArrayList<>();
        LocalDateTime date = LocalDateTime.of(2025, 1, 1, 10, 0);
        try (MockedStatic<LocalDateTime> mocked = mockStatic(LocalDateTime.class)) {
            mocked.when(LocalDateTime::now).thenReturn(date);

            expected.add(new VolunteeringPostDTO(new VolunteeringPost(postId, title1, description1, actor1, volunteeringId1, organizationId1)));
        }
        assertEquals(expected, postsFacade.getOrganizationVolunteeringPosts(organizationId1, actor1));
    }

    @Test
    void givenNoExistingPosts_whenGetOrganizationVolunteeringPosts_thenReturnEmptyList() {
        List<VolunteeringPostDTO> expected = new ArrayList<>();
        assertEquals(expected, postsFacade.getOrganizationVolunteeringPosts(organizationId1 + 1, actor1));
    }

    @Test
    void givenExistingPostId_whenJoinVolunteeringRequest_thenSendRequest() {
        //checking the request does not exist
        Exception exception = assertThrows(UnsupportedOperationException.class, () -> {
            volunteeringFacade.denyUserJoinRequest(actor1, volunteeringId1, actor2);
        });
        assertEquals("There is no pending join request for user " + actor2, exception.getMessage());

        assertEquals(0, postsFacade.getVolunteeringPost(postId, actor1).getNumOfPeopleRequestedToJoin());

        postsFacade.joinVolunteeringRequest(postId, actor2, "I really want to join this volunteering!");

        //checking if the request exists
        assertDoesNotThrow(() -> volunteeringFacade.denyUserJoinRequest(actor1, volunteeringId1, actor2));
        assertEquals(1, postsFacade.getVolunteeringPost(postId, actor1).getNumOfPeopleRequestedToJoin());
    }

    @Test
    void givenNonExistingPostId_whenJoinVolunteeringRequest_thenThrowException() {
        //checking the request does not exist
        Exception exception = assertThrows(UnsupportedOperationException.class, () -> {
            volunteeringFacade.denyUserJoinRequest(actor1, volunteeringId1, actor2);
        });
        assertEquals("There is no pending join request for user " + actor2, exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.joinVolunteeringRequest(postId + 1, actor2, "I really want to join this volunteering!");
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(postId + 1), exception.getMessage());

        //checking the request does not exist
        exception = assertThrows(UnsupportedOperationException.class, () -> {
            volunteeringFacade.denyUserJoinRequest(actor1, volunteeringId1, actor2);
        });
        assertEquals("There is no pending join request for user " + actor2, exception.getMessage());
    }

    @Test
    void givenRequestByVolunteer_whenJoinVolunteeringRequest_thenThrowException() {
        postsFacade.joinVolunteeringRequest(postId, actor2, "I really want to join this volunteering!");
        volunteeringFacade.acceptUserJoinRequest(actor1, volunteeringId1, actor2, 0);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.joinVolunteeringRequest(postId, actor2, "I really want to join this volunteering!");
        });
        assertEquals("User " + actor2 + " is already a volunteer in volunteering " + volunteeringId1, exception.getMessage());
    }

    @Test
    void givenDoubleRequest_whenJoinVolunteeringRequest_thenThrowException() {
        postsFacade.joinVolunteeringRequest(postId, actor2, "I really want to join this volunteering!");

        Exception exception = assertThrows(UnsupportedOperationException.class, () -> {
            postsFacade.joinVolunteeringRequest(postId, actor2, "I really want to join this volunteering!");
        });
        assertEquals("There is already a join request for this user!", exception.getMessage());
    }

    @Test
    void searchByKeywords() {
        List<VolunteeringPostDTO> allPosts = createPostsForSearchAndPost();
        VolunteeringPostDTO post1 = allPosts.get(0);
        VolunteeringPostDTO post2 = allPosts.get(1);
        VolunteeringPostDTO post3 = allPosts.get(2);

        List<VolunteeringPostDTO> expectedBananaCarrot = List.of(post1, post2);
        List<VolunteeringPostDTO> expectedPearLemon = List.of(post1, post2, post3);
        List<VolunteeringPostDTO> expectedApple = List.of(post1, post3);
        List<VolunteeringPostDTO> expectedOrange = List.of(post2);
        List<VolunteeringPostDTO> expectedPineaplle = List.of();

        assertEquals(expectedBananaCarrot, postsFacade.searchByKeywords("bAnana CaRRoT", actor1, allPosts));
        assertEquals(expectedPearLemon, postsFacade.searchByKeywords("Pear lemon", actor1, allPosts));
        assertEquals(expectedApple, postsFacade.searchByKeywords("Apple", actor1, allPosts));
        assertEquals(expectedOrange, postsFacade.searchByKeywords("orange", actor1, allPosts));
        assertEquals(expectedPineaplle, postsFacade.searchByKeywords("pineapple", actor1, allPosts));
    }

    @Test
    void sortByRelevance() {
        //TODO when user is implemented
    }

    @Test
    void sortByPopularity() {
        List<VolunteeringPostDTO> allPosts = createPostsForSearchAndPost();
        VolunteeringPostDTO post1 = allPosts.get(0);
        VolunteeringPostDTO post2 = allPosts.get(1);
        VolunteeringPostDTO post3 = allPosts.get(2);

        // post1 popularity = 1
        postsFacade.joinVolunteeringRequest(post1.getId(), "user1", "hi");

        // post2 popularity = 3
        postsFacade.joinVolunteeringRequest(post2.getId(), "user2", "hi");
        postsFacade.joinVolunteeringRequest(post2.getId(), "user3", "hi");
        postsFacade.joinVolunteeringRequest(post2.getId(), "user4", "hi");

        // post3 popularity = 2
        postsFacade.joinVolunteeringRequest(post3.getId(), "user5", "hi");
        postsFacade.joinVolunteeringRequest(post3.getId(), "user6", "hi");

        List<VolunteeringPostDTO> expectedSorted = List.of(post2, post3, post1);
        assertEquals(expectedSorted, postsFacade.sortByPopularity(actor1, allPosts));
    }

    @Test
    void sortByPostingTime() {
        List<VolunteeringPostDTO> allPosts = createPostsForSearchAndPost();
        VolunteeringPostDTO post1 = allPosts.get(0);
        VolunteeringPostDTO post2 = allPosts.get(1);
        VolunteeringPostDTO post3 = allPosts.get(2);

        post1.setPostedTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        post2.setPostedTime(LocalDateTime.of(2023, 1, 1, 10, 0));
        post3.setPostedTime(LocalDateTime.of(2025, 1, 1, 10, 0));

        List<VolunteeringPostDTO> expectedSorted = List.of(post3, post1, post2);
        assertEquals(expectedSorted, postsFacade.sortByPostingTime(actor1, allPosts));
    }

    @Test
    void sortByLastEditTime() {
        List<VolunteeringPostDTO> allPosts = createPostsForSearchAndPost();
        VolunteeringPostDTO post1 = allPosts.get(0);
        VolunteeringPostDTO post2 = allPosts.get(1);
        VolunteeringPostDTO post3 = allPosts.get(2);

        post1.setLastEditedTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        post2.setLastEditedTime(LocalDateTime.of(2023, 1, 1, 10, 0));
        post3.setLastEditedTime(LocalDateTime.of(2024, 1, 1, 10, 0));

        List<VolunteeringPostDTO> expectedSorted = List.of(post1, post3, post2);
        assertEquals(expectedSorted, postsFacade.sortByLastEditTime(actor1, allPosts));
    }

    @Test
    void filterPosts() {
        List<VolunteeringPostDTO> allPosts = createPostsForSearchAndPost();
        VolunteeringPostDTO post1 = allPosts.get(0);
        VolunteeringPostDTO post2 = allPosts.get(1);
        VolunteeringPostDTO post3 = allPosts.get(2);

        List<VolunteeringPostDTO> expected0 = allPosts;
        List<VolunteeringPostDTO> res0 = postsFacade.filterPosts(new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>(), actor1, allPosts);
        assertEquals(expected0, res0);

        List<VolunteeringPostDTO> expected1 = List.of(post2);
        List<VolunteeringPostDTO> res1 = postsFacade.filterPosts(new HashSet<>(), new HashSet<>(), Set.of("Jerusalem"), new HashSet<>(), new HashSet<>(), actor1, allPosts);
        assertEquals(expected1, res1);

        List<VolunteeringPostDTO> expected2 = List.of();
        List<VolunteeringPostDTO> res2 = postsFacade.filterPosts(Set.of("Lemon", "Cherry"), Set.of("Lettuce"), new HashSet<>(), Set.of("Cherry"), new HashSet<>(), actor1, allPosts);
        assertEquals(expected2, res2);

        List<VolunteeringPostDTO> expected3 = List.of(post1, post3);
        List<VolunteeringPostDTO> res3 = postsFacade.filterPosts(new HashSet<>(), Set.of("Lettuce", "Tomato"), new HashSet<>(), new HashSet<>(), Set.of("Volunteering"), actor1, allPosts);
        assertEquals(expected3, res3);
    }

    @Test
    void getAllPostsCategories() {
        createPostsForSearchAndPost();
        List<String> expectedCategories = List.of("Lemon", "Lettuce", "Cherry", "Orange");
        assertEquals(new HashSet<>(expectedCategories), new HashSet<>(postsFacade.getAllPostsCategories()));
    }

    @Test
    void getAllPostsSkills() {
        createPostsForSearchAndPost();
        List<String> expectedCategories = List.of("Pear", "Lettuce");
        assertEquals(new HashSet<>(expectedCategories), new HashSet<>(postsFacade.getAllPostsSkills()));
    }

    @Test
    void getAllPostsCities() {
        createPostsForSearchAndPost();

        List<String> expectedCities = List.of("Tel Aviv", "Beer Sheva", "Arad", "Jerusalem");
        assertEquals(new HashSet<>(expectedCities), new HashSet<>(postsFacade.getAllPostsCities()));
    }

    @Test
    void getAllPostsOrganizations() {
        createPostsForSearchAndPost();
        List<String> expectedOrganization = List.of("Organization1");
        assertEquals(new HashSet<>(expectedOrganization), new HashSet<>(postsFacade.getAllPostsOrganizations()));
    }

    @Test
    void getAllPostsVolunteerings() {
        createPostsForSearchAndPost();
        List<String> expectedVolunteerings = List.of("Volunteering", "Cherry");
        assertEquals(new HashSet<>(expectedVolunteerings), new HashSet<>(postsFacade.getAllPostsVolunteerings()));
    }

    private List<VolunteeringPostDTO> createPostsForSearchAndPost() {
        this.volunteeringId2 = volunteeringFacade.createVolunteering(actor1, organizationId1, "Cherry", "Lemon");

        VolunteeringPostDTO post1 = new VolunteeringPostDTO(new VolunteeringPost(postId + 1, "Banana", "Apple, Tomato: Carrot", actor1, volunteeringId1, organizationId1));
        VolunteeringPostDTO post2 = new VolunteeringPostDTO(new VolunteeringPost(postId + 2, "Carrot", "Orange, Banana[Cherry", actor1, volunteeringId2, organizationId1));
        VolunteeringPostDTO post3 = new VolunteeringPostDTO(new VolunteeringPost(postId + 3, "Cherry", "Apple, Lettuce+Carrot", actor1, volunteeringId1, organizationId1));

        postsFacade.createVolunteeringPost("Banana", "Apple, Tomato: Carrot", actor1, volunteeringId1);
        postsFacade.createVolunteeringPost("Carrot", "Orange, Banana[Cherry", actor1, volunteeringId2);
        postsFacade.createVolunteeringPost("Cherry", "Apple, Lettuce+Lemon", actor1, volunteeringId1);
        volunteeringFacade.updateVolunteeringSkills(actor1, volunteeringId1, List.of("Pear", "Lettuce"));
        volunteeringFacade.updateVolunteeringCategories(actor1, volunteeringId1, List.of("Lemon", "Lettuce"));
        volunteeringFacade.updateVolunteeringSkills(actor1, volunteeringId2, List.of("Pear"));
        volunteeringFacade.updateVolunteeringCategories(actor1, volunteeringId2, List.of("Cherry", "Orange"));
        volunteeringFacade.addVolunteeringLocation(actor1, volunteeringId1, "Location1", new AddressTuple("Tel Aviv", "Street1", "Address1"));
        volunteeringFacade.addVolunteeringLocation(actor1, volunteeringId1, "Location2", new AddressTuple("Beer Sheva", "Street2", "Address2"));
        volunteeringFacade.addVolunteeringLocation(actor1, volunteeringId1, "Location3", new AddressTuple("Arad", "Street3", "Address3"));
        volunteeringFacade.addVolunteeringLocation(actor1, volunteeringId2, "Location4", new AddressTuple("Beer Sheva", "Street4", "Address4"));
        volunteeringFacade.addVolunteeringLocation(actor1, volunteeringId2, "Location5", new AddressTuple("Jerusalem", "Street5", "Address5"));

        List<VolunteeringPostDTO> expectedBananaCarrot = List.of(post1, post2);
        List<VolunteeringPostDTO> expectedPearLemon = List.of(post1, post2, post3);
        List<VolunteeringPostDTO> expectedApple = List.of(post1, post3);
        List<VolunteeringPostDTO> expectedOrange = List.of(post2);
        List<VolunteeringPostDTO> expectedPineaplle = List.of();

        List<VolunteeringPostDTO> allPosts = List.of(post1, post2, post3);
        assertEquals(expectedBananaCarrot, postsFacade.searchByKeywords("bAnana CaRRoT", actor1, allPosts));
        assertEquals(expectedPearLemon, postsFacade.searchByKeywords("Pear lemon", actor1, allPosts));
        assertEquals(expectedApple, postsFacade.searchByKeywords("Apple", actor1, allPosts));
        assertEquals(expectedOrange, postsFacade.searchByKeywords("orange", actor1, allPosts));
        assertEquals(expectedPineaplle, postsFacade.searchByKeywords("pineapple", actor1, allPosts));

        return allPosts;
    }
}
