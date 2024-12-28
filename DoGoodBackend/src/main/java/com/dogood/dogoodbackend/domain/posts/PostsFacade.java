package com.dogood.dogoodbackend.domain.posts;

import com.dogood.dogoodbackend.domain.externalAIAPI.KeywordExtractor;
import com.dogood.dogoodbackend.domain.organizations.OrganizationsFacade;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringDTO;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringFacade;
import com.dogood.dogoodbackend.utils.OrganizationErrors;
import com.dogood.dogoodbackend.utils.PostErrors;

import java.util.*;
import java.util.stream.Collectors;

public class PostsFacade {
    private VolunteeringPostRepository volunteeringPostRepository;
    private VolunteeringFacade volunteeringFacade;
    private OrganizationsFacade organizationsFacade;
    private KeywordExtractor keywordExtractor;

    public PostsFacade(VolunteeringPostRepository volunteeringPostRepository, VolunteeringFacade volunteeringFacade, OrganizationsFacade organizationsFacade, KeywordExtractor keywordExtractor) {
        this.volunteeringPostRepository = volunteeringPostRepository;
        this.volunteeringFacade = volunteeringFacade;
        this.organizationsFacade = organizationsFacade;
        this.keywordExtractor = keywordExtractor;
    }

    public int createVolunteeringPost(String title, String description, String posterUsername, int volunteeringId) {
        //TODO: check if user exists and logged in

        int organizationId = volunteeringFacade.getVolunteeringOrganizationId(volunteeringId);

        // only managers in the organization can post about the organization's volunteering
        if(!organizationsFacade.isManager(posterUsername, organizationId) && !isAdmin(posterUsername)) {
            String organizationName = organizationsFacade.getOrganization(organizationId).getName();
            throw new IllegalArgumentException(OrganizationErrors.makeNonManagerCanNotPreformActionError(posterUsername, organizationName, "post about the organization's volunteering"));
        }

        return volunteeringPostRepository.createVolunteeringPost(title, description, posterUsername, volunteeringId, organizationId);
    }

    private boolean isAllowedToMakePostAction(String actor, VolunteeringPost post) {
        if(isAdmin(actor)) {
            return true;
        }

        int organizationId = volunteeringFacade.getVolunteeringOrganizationId(post.getVolunteeringId());
        if(organizationsFacade.isManager(actor, organizationId)) {
            return true;
        }

        return post.getPosterUsername().equals(actor);
    }

    public void removeVolunteeringPost(int postId, String actor) {
        //TODO: check if user exists and logged in

        VolunteeringPost toRemove = volunteeringPostRepository.getVolunteeringPost(postId);

        if(!isAllowedToMakePostAction(actor, toRemove)) {
            throw new IllegalArgumentException(PostErrors.makeUserIsNotAllowedToMakePostActionError(postId, actor, "remove"));
        }
        volunteeringPostRepository.removeVolunteeringPost(postId);
    }

    public void editVolunteeringPost(int postId, String title, String description, String actor) {
        //TODO: check if user exists and logged in

        VolunteeringPost toEdit = volunteeringPostRepository.getVolunteeringPost(postId);

        if(!isAllowedToMakePostAction(actor, toEdit)) {
            throw new IllegalArgumentException(PostErrors.makeUserIsNotAllowedToMakePostActionError(postId, actor, "edit"));
        }
        volunteeringPostRepository.editVolunteeringPost(postId, title, description);
    }

    public boolean doesExist(int postId) {
        try {
            volunteeringPostRepository.getVolunteeringPost(postId);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public VolunteeringPostDTO getVolunteeringPost(int postId) {
        VolunteeringPost post = volunteeringPostRepository.getVolunteeringPost(postId);
        return new VolunteeringPostDTO(post);
    }

    public List<VolunteeringPostDTO> getAllVolunteeringPosts() {
        List<VolunteeringPost> allPosts = volunteeringPostRepository.getAllVolunteeringPosts();
        return volunteeringPostRepository.getVolunteeringPostDTOs(allPosts);
    }

    public List<VolunteeringPostDTO> getOrganizationVolunteeringPosts(int organizationId) {
        List<VolunteeringPost> orgPosts = volunteeringPostRepository.getOrganizationVolunteeringPosts(organizationId);
        return volunteeringPostRepository.getVolunteeringPostDTOs(orgPosts);
    }

    public int getVolunteeringIdByPostId(int postId) {
        return volunteeringPostRepository.getVolunteeringIdByPostId(postId);
    }

    public void joinVolunteeringRequest(int postId, String actor, String freeText) {
        //TODO: check if user exists and logged in

        int volunteeringId = getVolunteeringIdByPostId(postId);
        volunteeringFacade.requestToJoinVolunteering(actor, volunteeringId, freeText);

        Post post = volunteeringPostRepository.getVolunteeringPost(postId);
        post.incNumOfPeopleRequestedToJoin();
    }

    private int countCommons(Set<String> s1, Set<String> s2) {
        int matching = 0;
        for (String item : s1) {
            if (s2.contains(item)) {
                matching++;
            }
        }
        return matching;
    }

    private int matchByKeywords(String search, Set<String> postKeywords) {
        if(search == null || search.isBlank()) {
            return 0;
        }
        Set<String> searchKeywords = keywordExtractor.getKeywords(search);
        return countCommons(searchKeywords, postKeywords);
    }

    private int matchByCategories(Set<String> postKeywords, String actor, int volunteeringId) {
        Set<String> userCategories = getUserCategories(actor);
        Set<String> volunteeringCategories = new HashSet<>(volunteeringFacade.getVolunteeringCategories(volunteeringId));
        volunteeringCategories.addAll(postKeywords);
        return countCommons(userCategories, volunteeringCategories);
    }

    private int matchBySkills(Set<String> postKeywords, String actor, int volunteeringId) {
        Set<String> userSkills = getUserSkills(actor);
        Set<String> volunteeringSkills = new HashSet<>(volunteeringFacade.getVolunteeringSkills(volunteeringId));
        volunteeringSkills.addAll(postKeywords);
        return countCommons(userSkills, volunteeringSkills);
    }

    private int evaluateVolunteeringSimilarity(Set<String> currVolunteeringKeywords, Set<String> currVolunteeringCategories, Set<String> currVolunteeringSkills, VolunteeringDTO otherVolunteering) {
        int otherVolunteeringId = otherVolunteering.getId();

        Set<String> otherVolunteeringKeywords = keywordExtractor.getKeywords(otherVolunteering.getName() + " " + otherVolunteering.getDescription());

        Set<String> otherVolunteeringCategories = new HashSet<>(volunteeringFacade.getVolunteeringCategories(otherVolunteeringId));

        Set<String> otherVolunteeringSkills = new HashSet<>(volunteeringFacade.getVolunteeringSkills(otherVolunteeringId));

        return countCommons(currVolunteeringKeywords, otherVolunteeringKeywords)
                + countCommons(currVolunteeringCategories, otherVolunteeringCategories)
                + countCommons(currVolunteeringSkills, otherVolunteeringSkills);
    }

    private int matchByHistory(String actor, int currVolunteeringId, Set<String> currVolunteeringKeywords, Set<String> currVolunteeringCategories, Set<String> currVolunteeringSkills) {
        int match = 0;
        List<VolunteeringDTO> history = getUserVolunteeringHistory(actor);
        VolunteeringDTO currVolunteering = volunteeringFacade.getVolunteeringDTO(currVolunteeringId);

        for(VolunteeringDTO historyVolunteering : history) {
            match += evaluateVolunteeringSimilarity(currVolunteeringKeywords, currVolunteeringCategories, currVolunteeringSkills, historyVolunteering);
        }
        return match;
    }

    private int evaluatePostRelevance(VolunteeringPost post, String search, String actor) {
        int volunteeringId = post.getVolunteeringId();

        // combining all the words from keywords, categories, skills to avoid duplicates
        Set<String> searchKeywords = (search == null || search.isBlank()) ? keywordExtractor.getKeywords(search) : new HashSet<>();
        Set<String> postKeywords = keywordExtractor.getKeywords(post.getTitle() + " " + post.getDescription());

        Set<String> volunteeringKeywords = keywordExtractor.getKeywords(post.getTitle() + " " + post.getDescription());

        Set<String> userCategories = getUserCategories(actor);
        Set<String> volunteeringCategories = new HashSet<>(volunteeringFacade.getVolunteeringCategories(volunteeringId));

        Set<String> userSkills = getUserSkills(actor);
        Set<String> volunteeringSkills = new HashSet<>(volunteeringFacade.getVolunteeringSkills(volunteeringId));

        Set<String> userSet = searchKeywords;
        userSet.addAll(userCategories);
        userSet.addAll(userSkills);

        Set<String> postSet = postKeywords;
        postSet.addAll(volunteeringKeywords);
        postSet.addAll(volunteeringCategories);
        postSet.addAll(volunteeringSkills);

        int relevance = countCommons(userSet, postSet) + matchByHistory(actor, volunteeringId, volunteeringKeywords, volunteeringCategories, volunteeringSkills);
        return relevance;
    }

    private int getRelevanceFromMap(Map<VolunteeringPost, Integer> postWithRelevance) {
        // only one item in map
        for(Integer rel: postWithRelevance.values()) {
            return rel;
        }
        return 0;
    }

    private VolunteeringPost getPostFromMap(Map<VolunteeringPost, Integer> postWithRelevance) {
        // only one item in map
        for(VolunteeringPost post: postWithRelevance.keySet()) {
            return post;
        }
        return null;
    }

    public List<VolunteeringPostDTO> searchByKeywords(String search, String actor) {
        List<VolunteeringPost> allPosts = volunteeringPostRepository.getAllVolunteeringPosts();

        List<VolunteeringPost> filteredSortedList = allPosts.stream()
                .map(post -> Map.of(post, evaluatePostRelevance(post, search, actor)))
                .filter(postWithRelevance -> getRelevanceFromMap(postWithRelevance) >= 1)
                .sorted(Comparator.comparingInt(postWithRelevance -> -1 * getRelevanceFromMap(postWithRelevance)))
                .map(postWithRelevance -> getPostFromMap(postWithRelevance))
                .collect(Collectors.toList());

        return volunteeringPostRepository.getVolunteeringPostDTOs(filteredSortedList);
    }

    public List<VolunteeringPostDTO> sortByRelevance(String actor) {
        List<VolunteeringPost> allPosts = volunteeringPostRepository.getAllVolunteeringPosts();

        List<VolunteeringPost> sorted = allPosts.stream()
                .sorted(Comparator.comparingInt(post -> -1 * evaluatePostRelevance(post, null, actor)))
                .collect(Collectors.toList());

        return volunteeringPostRepository.getVolunteeringPostDTOs(sorted);
    }

    public List<VolunteeringPostDTO> sortByPopularity() {
        List<VolunteeringPost> allPosts = volunteeringPostRepository.getAllVolunteeringPosts();

        List<VolunteeringPost> sorted = allPosts.stream()
                .sorted(Comparator.comparingInt(post -> -1 * post.evaluatePopularity()))
                .collect(Collectors.toList());

        return volunteeringPostRepository.getVolunteeringPostDTOs(sorted);
    }

    public List<VolunteeringPostDTO> sortByPostingTime() {
        List<VolunteeringPost> allPosts = volunteeringPostRepository.getAllVolunteeringPosts();

        List<VolunteeringPost> sorted = allPosts.stream()
                .sorted((post1, post2) -> post2.getPostedTime().compareTo(post1.getPostedTime()))
                .collect(Collectors.toList());

        return volunteeringPostRepository.getVolunteeringPostDTOs(sorted);
    }

    public List<VolunteeringPostDTO> sortByLastEditTime() {
        List<VolunteeringPost> allPosts = volunteeringPostRepository.getAllVolunteeringPosts();

        List<VolunteeringPost> sorted = allPosts.stream()
                .sorted((post1, post2) -> post2.getLastEditedTime().compareTo(post1.getLastEditedTime()))
                .collect(Collectors.toList());

        return volunteeringPostRepository.getVolunteeringPostDTOs(sorted);
    }

    //TODO: sort by location

    /*public List<VolunteeringPostDTO> filterPosts(Set<String> categories, Set<String> skills, location, hour, available now, ) {

    }*/

    public List<VolunteeringPostDTO> filterPosts(Set<String> categories, Set<String> skills) {

    }

    // TODO: remove when users facade is implemented
    private boolean isAdmin(String username) {
        return false;
    }

    // TODO: remove when users facade is implemented
    private Set<String> getUserCategories(String actor) {
        return null;
    }

    // TODO: remove when users facade is implemented
    private Set<String> getUserSkills(String actor) {
        return null;
    }

    // TODO: remove when users facade is implemented
    private List<VolunteeringDTO> getUserVolunteeringHistory(String actor) {
        return null;
    }

}
