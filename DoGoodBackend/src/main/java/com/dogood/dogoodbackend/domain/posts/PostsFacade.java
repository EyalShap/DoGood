package com.dogood.dogoodbackend.domain.posts;

import com.dogood.dogoodbackend.domain.externalAIAPI.KeywordExtractor;
import com.dogood.dogoodbackend.domain.organizations.OrganizationsFacade;
import com.dogood.dogoodbackend.domain.volunteerings.LocationDTO;
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

    public void joinVolunteeringRequest(int postId, String actor, String freeText) {
        //TODO: check if user exists and logged in

        int volunteeringId = volunteeringPostRepository.getVolunteeringIdByPostId(postId);
        volunteeringFacade.requestToJoinVolunteering(actor, volunteeringId, freeText);

        Post post = volunteeringPostRepository.getVolunteeringPost(postId);
        post.incNumOfPeopleRequestedToJoin();
    }

    public List<VolunteeringPostDTO> searchByKeywords(String search) {
        List<VolunteeringPost> allPosts = volunteeringPostRepository.getAllVolunteeringPosts();
        Set<String> searchKeywords = keywordExtractor.getKeywords(search);
        List<VolunteeringPostDTO> result = new ArrayList<>();

        for(VolunteeringPost post : allPosts) {
            Set<String> postKeywords = getPostKeywords(post);
            int common = countCommons(searchKeywords, postKeywords);
            if(common >= 1) {
                result.add(new VolunteeringPostDTO(post));
            }
        }
        return result;
    }

    private Set<String> getPostKeywords(VolunteeringPost post) {
        int volunteeringId = post.getVolunteeringId();
        VolunteeringDTO volunteering = volunteeringFacade.getVolunteeringDTO(volunteeringId);

        Set<String> postKeywords = keywordExtractor.getKeywords(post.getTitle() + " " + post.getDescription());
        Set<String> volunteeringKeywords = getVolunteeringKeywords(volunteering);

        postKeywords.addAll(volunteeringKeywords);
        return postKeywords;
    }

    private Set<String> getVolunteeringKeywords(VolunteeringDTO volunteering) {
        int volunteeringId = volunteering.getId();

        Set<String> volunteeringKeywords = keywordExtractor.getKeywords(volunteering.getName() + " " + volunteering.getDescription());
        Set<String> volunteeringCategories = new HashSet<>(volunteeringFacade.getVolunteeringCategories(volunteeringId));
        Set<String> volunteeringSkills = new HashSet<>(volunteeringFacade.getVolunteeringSkills(volunteeringId));

        volunteeringKeywords.addAll(volunteeringCategories);
        volunteeringKeywords.addAll(volunteeringSkills);

        return volunteeringKeywords;
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

    public List<VolunteeringPostDTO> sortByRelevance(String actor) {
        List<VolunteeringPost> allPosts = volunteeringPostRepository.getAllVolunteeringPosts();
        for(VolunteeringPost post: allPosts) {
            post.setRelevance(evaluatePostRelevance(post, actor));
        }

        List<VolunteeringPost> sorted = allPosts.stream()
                .filter(post -> post.getRelevance() != -1)
                .sorted(Comparator.comparingInt(post -> -1 * post.getRelevance()))
                .collect(Collectors.toList());

        return volunteeringPostRepository.getVolunteeringPostDTOs(sorted);
    }

    private int evaluatePostRelevance(VolunteeringPost post, String actor) {
        VolunteeringDTO volunteeringDTO = volunteeringFacade.getVolunteeringDTO(post.getVolunteeringId());

        Set<String> userKeywords = getUserCategories(actor);
        Set<String> userSkills = getUserSkills(actor);
        userKeywords.addAll(userSkills);

        Set<String> postKeywords = getPostKeywords(post);

        int relevance = countCommons(userKeywords, postKeywords) + matchByHistory(actor, volunteeringDTO);
        return relevance;
    }

    private int matchByHistory(String actor, VolunteeringDTO currVolunteering) {
        int match = 0;
        List<VolunteeringDTO> history = getUserVolunteeringHistory(actor);
        Set<String> currKeywords = getVolunteeringKeywords(currVolunteering);

        for(VolunteeringDTO historyVolunteering : history) {
            Set<String> historyKeywords = getVolunteeringKeywords(historyVolunteering);
            match += countCommons(currKeywords, historyKeywords);
        }
        return match;
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

    //TODO: sort by location in beta version

    //TODO: add more parameters
    public List<VolunteeringPostDTO> filterPosts(Set<String> categories, Set<String> skills, Set<String> cities) {
        List<VolunteeringPost> allPosts = volunteeringPostRepository.getAllVolunteeringPosts();
        List<VolunteeringPostDTO> result = new ArrayList<>();

        for(VolunteeringPost post : allPosts) {
            int volunteeringId = post.getVolunteeringId();
            Set<String> volunteeringCategories = new HashSet<>(volunteeringFacade.getVolunteeringCategories(volunteeringId));
            Set<String> volunteeringSkills = new HashSet<>(volunteeringFacade.getVolunteeringSkills(volunteeringId));
            List<LocationDTO> volunteeringLocations = volunteeringFacade.getVolunteeringLocations(volunteeringId);
            Set<String> volunteeringCities = volunteeringLocations.stream().map(locationDTO -> locationDTO.getAddress().getCity()).collect(Collectors.toSet());

            volunteeringCategories.retainAll(categories);
            volunteeringSkills.retainAll(skills);
            volunteeringCities.retainAll(cities);

            if(volunteeringCategories.size() >= 1 && volunteeringSkills.size() >= 1 && volunteeringCities.size() >= 1) {
                result.add(new VolunteeringPostDTO(post));
            }
        }
        return result;
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
