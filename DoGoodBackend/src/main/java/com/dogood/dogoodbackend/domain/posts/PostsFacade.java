package com.dogood.dogoodbackend.domain.posts;

import com.dogood.dogoodbackend.domain.externalAIAPI.KeywordExtractor;
import com.dogood.dogoodbackend.domain.organizations.OrganizationsFacade;
import com.dogood.dogoodbackend.domain.reports.ReportsFacade;
import com.dogood.dogoodbackend.domain.volunteerings.LocationDTO;
import com.dogood.dogoodbackend.domain.volunteerings.PastExperience;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringDTO;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringFacade;
import com.dogood.dogoodbackend.utils.OrganizationErrors;
import com.dogood.dogoodbackend.utils.PostErrors;
import jakarta.transaction.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Transactional
public class PostsFacade {
    private VolunteeringPostRepository volunteeringPostRepository;
    private VolunteeringFacade volunteeringFacade;
    private OrganizationsFacade organizationsFacade;
    private ReportsFacade reportsFacade;
    private KeywordExtractor keywordExtractor;

    public PostsFacade(VolunteeringPostRepository volunteeringPostRepository, VolunteeringFacade volunteeringFacade, OrganizationsFacade organizationsFacade, KeywordExtractor keywordExtractor) {
        this.volunteeringPostRepository = volunteeringPostRepository;
        this.volunteeringFacade = volunteeringFacade;
        this.organizationsFacade = organizationsFacade;
        this.keywordExtractor = keywordExtractor;
    }

    public void setReportsFacade(ReportsFacade reportsFacade) {
        this.reportsFacade = reportsFacade;
    }

    public int createVolunteeringPost(String title, String description, String posterUsername, int volunteeringId) {
        //TODO: check if user exists and logged in
        volunteeringFacade.getVolunteeringDTO(volunteeringId); // check if volunteering exists

        int organizationId = volunteeringFacade.getVolunteeringOrganizationId(volunteeringId);

        // only managers in the organization can post about the organization's volunteering
        if(!organizationsFacade.isManager(posterUsername, organizationId) && !isAdmin(posterUsername)) {
            String organizationName = organizationsFacade.getOrganization(organizationId).getName();
            throw new IllegalArgumentException(OrganizationErrors.makeNonManagerCanNotPreformActionError(posterUsername, organizationName, "post about the organization's volunteering"));
        }

        int postId = volunteeringPostRepository.createVolunteeringPost(title, description, posterUsername, volunteeringId, organizationId);
        return postId;
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

    @Transactional
    public void removeVolunteeringPost(int postId, String actor) {
        //TODO: check if user exists and logged in

        VolunteeringPost toRemove = volunteeringPostRepository.getVolunteeringPost(postId);

        if(!isAllowedToMakePostAction(actor, toRemove)) {
            throw new IllegalArgumentException(PostErrors.makeUserIsNotAllowedToMakePostActionError(postId, actor, "remove"));
        }
        reportsFacade.removePostReports(postId);
        volunteeringPostRepository.removeVolunteeringPost(postId);
    }

    public void removePostsByVolunteeringId(int volunteeringId) {
        volunteeringPostRepository.removePostsByVolunteeringId(volunteeringId);
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

    public VolunteeringPostDTO getVolunteeringPost(int postId, String actor) {
        //TODO: check if user exists and logged in

        VolunteeringPost post = volunteeringPostRepository.getVolunteeringPost(postId);
        return new VolunteeringPostDTO(post);
    }

    public List<VolunteeringPostDTO> getAllVolunteeringPosts(String actor) {
        //TODO: check if user exists and logged in

        List<VolunteeringPost> allPosts = volunteeringPostRepository.getAllVolunteeringPosts();
        return volunteeringPostRepository.getVolunteeringPostDTOs(allPosts);
    }

    public List<VolunteeringPostDTO> getOrganizationVolunteeringPosts(int organizationId, String actor) {
        //TODO: check if user exists and logged in

        List<VolunteeringPost> orgPosts = volunteeringPostRepository.getOrganizationVolunteeringPosts(organizationId);
        return volunteeringPostRepository.getVolunteeringPostDTOs(orgPosts);
    }

    public void joinVolunteeringRequest(int postId, String actor, String freeText) {
        //TODO: check if user exists and logged in

        int volunteeringId = volunteeringPostRepository.getVolunteeringIdByPostId(postId);
        volunteeringFacade.requestToJoinVolunteering(actor, volunteeringId, freeText);

        volunteeringPostRepository.incNumOfPeopleRequestedToJoin(postId);
    }

    public List<VolunteeringPostDTO> searchByKeywords(String search, String actor, List<VolunteeringPostDTO> allPosts) {
        //TODO: check if user exists and logged in
        if(search == null || search.isBlank()) {
            return volunteeringPostRepository.getVolunteeringPostDTOs();
        }
        search = search.replaceAll("[^a-zA-Z0-9 ]", "");

        Set<String> searchKeywords = keywordExtractor.getKeywords(search).stream().map(keyword -> keyword.toLowerCase()).collect(Collectors.toSet());
        List<VolunteeringPostDTO> result = new ArrayList<>();

        for(VolunteeringPostDTO post : allPosts) {
            Set<String> postKeywords = getPostKeywords(post).stream().map(keyword -> keyword.toLowerCase()).collect(Collectors.toSet());;
            int common = countCommons(searchKeywords, postKeywords);
            if(common >= 1) {
                result.add(post);
            }
        }
        return result;
    }

    private Set<String> getPostKeywords(VolunteeringPostDTO post) {
        int volunteeringId = post.getVolunteeringId();
        VolunteeringDTO volunteering = volunteeringFacade.getVolunteeringDTO(volunteeringId);

        Set<String> postKeywords = keywordExtractor.getKeywords(cleanString(post.getTitle()) + " " + cleanString(post.getDescription()));
        Set<String> volunteeringKeywords = getVolunteeringKeywords(volunteering);

        postKeywords.addAll(volunteeringKeywords);
        return postKeywords;
    }

    private Set<String> getVolunteeringKeywords(VolunteeringDTO volunteering) {
        int volunteeringId = volunteering.getId();

        Set<String> volunteeringKeywords = keywordExtractor.getKeywords(cleanString(volunteering.getName()) + " " + cleanString(volunteering.getDescription()));
        List<String> volunteeringCategories = volunteeringFacade.getVolunteeringCategories(volunteeringId);
        List<String> volunteeringSkills = volunteeringFacade.getVolunteeringSkills(volunteeringId);

        if(volunteeringCategories != null) {
            volunteeringKeywords.addAll(volunteeringCategories);
        }
        if(volunteeringSkills != null) {
            volunteeringKeywords.addAll(volunteeringSkills);
        }

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

    private String cleanString(String str) {
        return str.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase();
    }

    public List<VolunteeringPostDTO> sortByRelevance(String actor, List<VolunteeringPostDTO> allPosts) {
        //TODO: check if user exists and logged in

        for(VolunteeringPostDTO post: allPosts) {
            post.setRelevance(evaluatePostRelevance(post, actor));
        }

        List<VolunteeringPostDTO> sorted = allPosts.stream()
                .filter(post -> post.getRelevance() != -1)
                .sorted(Comparator.comparingInt(post -> -1 * post.getRelevance()))
                .collect(Collectors.toList());

        return sorted;
    }

    private int evaluatePostRelevance(VolunteeringPostDTO post, String actor) {
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

    public List<VolunteeringPostDTO> sortByPopularity(String actor, List<VolunteeringPostDTO> allPosts) {
        //TODO: check if user exists and logged in

        List<VolunteeringPostDTO> sorted = allPosts.stream()
                .sorted(Comparator.comparingInt(post -> -1 * volunteeringPostRepository.getVolunteeringPost(post.getId()).evaluatePopularity()))
                .collect(Collectors.toList());

        return sorted;
    }

    public List<VolunteeringPostDTO> sortByPostingTime(String actor, List<VolunteeringPostDTO> allPosts) {
        //TODO: check if user exists and logged in

        List<VolunteeringPostDTO> sorted = allPosts.stream()
                .sorted((post1, post2) -> post2.getPostedTime().compareTo(post1.getPostedTime()))
                .collect(Collectors.toList());

        return sorted;
    }

    public List<VolunteeringPostDTO> sortByLastEditTime(String actor, List<VolunteeringPostDTO> allPosts) {
        //TODO: check if user exists and logged in

        List<VolunteeringPostDTO> sorted = allPosts.stream()
                .sorted((post1, post2) -> post2.getLastEditedTime().compareTo(post1.getLastEditedTime()))
                .collect(Collectors.toList());

        return sorted;
    }

    //TODO: sort by location in beta version

    //TODO: add more parameters
    public List<VolunteeringPostDTO> filterPosts(Set<String> categories, Set<String> skills, Set<String> cities, Set<String> organizationNames, Set<String> volunteeringNames, String actor, List<VolunteeringPostDTO> allPosts) {
        //TODO: check if user exists and logged in

        List<VolunteeringPostDTO> result = new ArrayList<>();

        for(VolunteeringPostDTO post : allPosts) {
            int volunteeringId = post.getVolunteeringId();
            Set<String> volunteeringCategories = new HashSet<>(volunteeringFacade.getVolunteeringCategories(volunteeringId));
            Set<String> volunteeringSkills = new HashSet<>(volunteeringFacade.getVolunteeringSkills(volunteeringId));
            List<LocationDTO> volunteeringLocations = volunteeringFacade.getVolunteeringLocations(volunteeringId);
            Set<String> volunteeringCities = volunteeringLocations.stream().map(locationDTO -> locationDTO.getAddress().getCity()).collect(Collectors.toSet());
            String organizationName = organizationsFacade.getOrganization(post.getOrganizationId()).getName();
            String volunteeringName = volunteeringFacade.getVolunteeringDTO(post.getVolunteeringId()).getName();

            boolean matchByCategory = true;
            boolean matchBySkill = true;
            boolean matchByCity = true;
            boolean matchByOrganization = organizationNames.size() > 0 ? organizationNames.contains(organizationName) : true;
            boolean matchByVolunteering = volunteeringNames.size() > 0 ? volunteeringNames.contains(volunteeringName) : true;

            if(categories.size() > 0) {
                volunteeringCategories.retainAll(categories);
                matchByCategory = volunteeringCategories.size() >= 1;
            }
            if(skills.size() > 0) {
                volunteeringSkills.retainAll(skills);
                matchBySkill = volunteeringSkills.size() >= 1;
            }
            if(cities.size() > 0) {
                volunteeringCities.retainAll(cities);
                matchByCity = volunteeringCities.size() >= 1;
            }

            if(matchByCategory && matchBySkill && matchByCity && matchByOrganization && matchByVolunteering) {
                result.add(post);
            }
        }
        return result;
    }

    public List<String> getAllPostsCategories() {
        List<VolunteeringPost> allPosts = volunteeringPostRepository.getAllVolunteeringPosts();
        Set<String> allCategories = new HashSet<>();

        for(VolunteeringPost post : allPosts) {
            List<String> volunteeringCategories = volunteeringFacade.getVolunteeringCategories(post.getVolunteeringId());
            if(volunteeringCategories != null) {
                allCategories.addAll(volunteeringCategories);
            }
        }
        return new ArrayList<>(allCategories);
    }

    public List<String> getAllPostsSkills() {
        List<VolunteeringPost> allPosts = volunteeringPostRepository.getAllVolunteeringPosts();
        Set<String> allSkills = new HashSet<>();

        for(VolunteeringPost post : allPosts) {
            List<String> volunteeringSkills = volunteeringFacade.getVolunteeringSkills(post.getVolunteeringId());
            if(volunteeringSkills != null) {
                allSkills.addAll(volunteeringSkills);
            }
        }
        return new ArrayList<>(allSkills);
    }

    public List<String> getAllPostsCities() {
        List<VolunteeringPost> allPosts = volunteeringPostRepository.getAllVolunteeringPosts();
        Set<String> allCities = new HashSet<>();

        for(VolunteeringPost post : allPosts) {
            List<LocationDTO> volunteeringLocations = volunteeringFacade.getVolunteeringLocations(post.getVolunteeringId());
            Set<String> volunteeringCities = volunteeringLocations.stream().map(locationDTO -> locationDTO.getAddress().getCity()).collect(Collectors.toSet());

            if(volunteeringCities != null) {
                allCities.addAll(volunteeringCities);
            }
        }
        return new ArrayList<>(allCities);
    }

    public List<String> getAllPostsOrganizations() {
        List<VolunteeringPost> allPosts = volunteeringPostRepository.getAllVolunteeringPosts();
        Set<String> allOrganizations = new HashSet<>();

        for(VolunteeringPost post : allPosts) {
            String organizationName = organizationsFacade.getOrganization(post.getOrganizationId()).getName();
            allOrganizations.add(organizationName);
        }
        return new ArrayList<>(allOrganizations);
    }

    public List<String> getAllPostsVolunteerings() {
        List<VolunteeringPost> allPosts = volunteeringPostRepository.getAllVolunteeringPosts();
        Set<String> allVolunteerings = new HashSet<>();

        for(VolunteeringPost post : allPosts) {
            String volunteeringName = volunteeringFacade.getVolunteeringDTO(post.getVolunteeringId()).getName();
            allVolunteerings.add(volunteeringName);
        }
        return new ArrayList<>(allVolunteerings);
    }

    public List<PastExperience> getPostPastExperiences(int postId) {
        int volunteeringId = volunteeringPostRepository.getVolunteeringIdByPostId(postId);
        return volunteeringFacade.getVolunteeringPastExperiences(volunteeringId);
    }

    public String getVolunteeringName(int volunteeringId) {
        return volunteeringFacade.getVolunteeringDTO(volunteeringId).getName();
    }

    // TODO: remove when users facade is implemented
    private boolean isAdmin(String username) {
        return false;
    }

    // TODO: remove when users facade is implemented
    private Set<String> getUserCategories(String actor) {
        return new HashSet<>();
    }

    // TODO: remove when users facade is implemented
    private Set<String> getUserSkills(String actor) {
        return new HashSet<>();
    }

    // TODO: remove when users facade is implemented
    private List<VolunteeringDTO> getUserVolunteeringHistory(String actor) {
        return new ArrayList<>();
    }



}
