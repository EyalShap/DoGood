package com.dogood.dogoodbackend.domain.posts;

import com.dogood.dogoodbackend.domain.externalAIAPI.KeywordExtractor;
import com.dogood.dogoodbackend.domain.externalAIAPI.SkillsAndCategories;
import com.dogood.dogoodbackend.domain.externalAIAPI.SkillsAndCategoriesExtractor;
import com.dogood.dogoodbackend.domain.organizations.OrganizationDTO;
import com.dogood.dogoodbackend.domain.organizations.OrganizationsFacade;
import com.dogood.dogoodbackend.domain.requests.Request;
import com.dogood.dogoodbackend.domain.requests.RequestObject;
import com.dogood.dogoodbackend.domain.requests.RequestRepository;
import com.dogood.dogoodbackend.domain.reports.ReportsFacade;
import com.dogood.dogoodbackend.domain.users.UsersFacade;
import com.dogood.dogoodbackend.domain.users.notificiations.NotificationNavigations;
import com.dogood.dogoodbackend.domain.users.notificiations.NotificationSystem;
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
    private VolunteerPostRepository volunteerPostRepository;
    private UsersFacade usersFacade;
    private VolunteeringFacade volunteeringFacade;
    private OrganizationsFacade organizationsFacade;
    private ReportsFacade reportsFacade;
    private KeywordExtractor keywordExtractor;
    private SkillsAndCategoriesExtractor skillsAndCategoriesExtractor;
    private RequestRepository requestRepository;
    private NotificationSystem notificationSystem;
    private final int NOTIFY_NEW_POST = 5;

    public PostsFacade(VolunteeringPostRepository volunteeringPostRepository, VolunteerPostRepository volunteerPostRepository, UsersFacade usersFacade, VolunteeringFacade volunteeringFacade, OrganizationsFacade organizationsFacade, KeywordExtractor keywordExtractor, SkillsAndCategoriesExtractor skillsAndCategoriesExtractor, RequestRepository requestRepository) {
        this.volunteeringPostRepository = volunteeringPostRepository;
        this.volunteerPostRepository = volunteerPostRepository;
        this.usersFacade = usersFacade;
        this.volunteeringFacade = volunteeringFacade;
        this.organizationsFacade = organizationsFacade;
        this.keywordExtractor = keywordExtractor;
        this.skillsAndCategoriesExtractor = skillsAndCategoriesExtractor;
        this.requestRepository = requestRepository;
    }

    public void setReportsFacade(ReportsFacade reportsFacade) {
        this.reportsFacade = reportsFacade;
    }

    public void setNotificationSystem(NotificationSystem notificationSystem) {
        this.notificationSystem = notificationSystem;
    }

    public void setOrganizationsFacade(OrganizationsFacade organizationsFacade) {
        this.organizationsFacade = organizationsFacade;
    }

    public void setKeywordExtractor(KeywordExtractor keywordExtractor) {
        this.keywordExtractor = keywordExtractor;
    }

    public void setSkillsAndCategoriesExtractor(SkillsAndCategoriesExtractor skillsAndCategoriesExtractor) {
        this.skillsAndCategoriesExtractor = skillsAndCategoriesExtractor;
    }

    public void setVolunteeringFacade(VolunteeringFacade volunteeringFacade) {
        this.volunteeringFacade = volunteeringFacade;
    }

    // ----------------------------------------------------
    // ---------------- VOLUNTEERING POSTS ----------------

    public int createVolunteeringPost(String title, String description, String posterUsername, int volunteeringId) {
        if(!userExists(posterUsername)){
            throw new IllegalArgumentException("User " + posterUsername + " doesn't exist");
        }
        VolunteeringDTO volunteeringDTO = volunteeringFacade.getVolunteeringDTO(volunteeringId); // check if volunteering exists

        int organizationId = volunteeringFacade.getVolunteeringOrganizationId(volunteeringId);

        // only managers in the organization can post about the organization's volunteering
        if(!organizationsFacade.isManager(posterUsername, organizationId) && !isAdmin(posterUsername)) {
            String organizationName = organizationsFacade.getOrganization(organizationId).getName();
            throw new IllegalArgumentException(OrganizationErrors.makeNonManagerCanNotPreformActionError(posterUsername, organizationName, "post about the organization's volunteering"));
        }

        String volunteeringName = volunteeringDTO.getName();
        String volunteeringDescription = volunteeringDTO.getDescription();
        Set<String> postKeywords = keywordExtractor.getVolunteeringPostKeywords(volunteeringName, volunteeringDescription, title, description);
        int postId = volunteeringPostRepository.createVolunteeringPost(title, description, postKeywords, posterUsername, volunteeringId, organizationId);

        notifyAboutNewVolunteeringPost(postId, posterUsername);

        String message = String.format("The new post \"%s\" was created for the volunteering \"%s\" in your organization \"%s\".", title, volunteeringName, organizationsFacade.getOrganization(organizationId).getName());
        organizationsFacade.notifyManagers(message, NotificationNavigations.volunteeringPost(postId), organizationId);
        return postId;
    }

    private void notifyAboutNewVolunteeringPost(int postId, String actor) {
        VolunteeringPostDTO post = getVolunteeringPost(postId, actor);
        List<String> allUsernames = usersFacade.getAllUsername();
        allUsernames.sort(Comparator.comparing((String username) -> evaluatePostRelevance(post, username)).reversed());

        for(int i = 0; i < Math.min(allUsernames.size(), NOTIFY_NEW_POST); i++) {
            String notifyUsername = allUsernames.get(i);
            int relevance = evaluatePostRelevance(post, notifyUsername);
            if(relevance > 0) {
                String message = String.format("The new post \"%s\" might be relevant for you!", post.getTitle());
                notificationSystem.notifyUser(notifyUsername, message, NotificationNavigations.volunteeringPost(postId));
            }
        }
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
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }

        VolunteeringPost toRemove = volunteeringPostRepository.getVolunteeringPost(postId);

        if(!isAllowedToMakePostAction(actor, toRemove)) {
            throw new IllegalArgumentException(PostErrors.makeUserIsNotAllowedToMakePostActionError(toRemove.title, actor, "remove"));
        }
        reportsFacade.removeVolunteeringPostReports(postId);
        volunteeringPostRepository.removeVolunteeringPost(postId);

        VolunteeringDTO volunteeringDTO = volunteeringFacade.getVolunteeringDTO(toRemove.getVolunteeringId());
        int orgId = toRemove.getOrganizationId();
        OrganizationDTO organizationDTO = organizationsFacade.getOrganization(orgId);
        String message = String.format("The post \"%s\" of the volunteering \"%s\" in your organization \"%s\" was removed", toRemove.getTitle(), volunteeringDTO.getName(), organizationDTO.getName());
        organizationsFacade.notifyManagers(message, NotificationNavigations.organization(orgId), orgId);
    }

    public void removePostsByVolunteeringId(int volunteeringId) {
        volunteeringPostRepository.removePostsByVolunteeringId(volunteeringId);
    }

    public void editVolunteeringPost(int postId, String title, String description, String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }

        VolunteeringPost toEdit = volunteeringPostRepository.getVolunteeringPost(postId);
        String prevTitle = toEdit.getTitle();
        VolunteeringDTO volunteeringDTO = volunteeringFacade.getVolunteeringDTO(toEdit.getVolunteeringId());

        if(!isAllowedToMakePostAction(actor, toEdit)) {
            throw new IllegalArgumentException(PostErrors.makeUserIsNotAllowedToMakePostActionError(toEdit.title, actor, "edit"));
        }
        String volunteeringName = volunteeringDTO.getName();
        String volunteeringDescription = volunteeringDTO.getDescription();
        Set<String> postKeywords = keywordExtractor.getVolunteeringPostKeywords(volunteeringName, volunteeringDescription, title, description);
        volunteeringPostRepository.editVolunteeringPost(postId, title, description, postKeywords);

        int orgId = toEdit.getOrganizationId();
        OrganizationDTO organizationDTO = organizationsFacade.getOrganization(orgId);
        String message = String.format("The post \"%s\" of the volunteering \"%s\" in your organization \"%s\" was edited.", prevTitle, volunteeringName, organizationDTO.getName());
        organizationsFacade.notifyManagers(message, NotificationNavigations.volunteeringPost(postId), orgId);
    }

    public void updateVolunteeringPostsKeywords(int volunteeringId, String actor) {
        for(VolunteeringPostDTO post : getAllVolunteeringPosts(actor)) {
            if(volunteeringId == post.getVolunteeringId()) {
                String postTitle = post.getTitle();
                String postDescription = post.getDescription();

                VolunteeringDTO volunteeringDTO = volunteeringFacade.getVolunteeringDTO(post.getVolunteeringId());
                String volunteeringName = volunteeringDTO.getName();
                String volunteeringDescription = volunteeringDTO.getDescription();

                Set<String> postKeywords = keywordExtractor.getVolunteeringPostKeywords(volunteeringName, volunteeringDescription, postTitle, postDescription);
                volunteeringPostRepository.editVolunteeringPost(post.getId(), postTitle, postDescription, postKeywords);
            }
        }
    }

    public boolean doesVolunteeringPostExist(int postId) {
        try {
            volunteeringPostRepository.getVolunteeringPost(postId);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public boolean doesVolunteerPostExist(int postId) {
        try {
            volunteerPostRepository.getVolunteerPost(postId);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public VolunteeringPostDTO getVolunteeringPost(int postId, String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }

        VolunteeringPost post = volunteeringPostRepository.getVolunteeringPost(postId);
        return new VolunteeringPostDTO(post);
    }

    public VolunteerPostDTO getVolunteerPost(int postId, String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }

        VolunteerPost post = volunteerPostRepository.getVolunteerPost(postId);
        return new VolunteerPostDTO(post);
    }

    public List<VolunteeringPostDTO> getAllVolunteeringPosts(String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }

        List<VolunteeringPost> allPosts = volunteeringPostRepository.getAllVolunteeringPosts();
        return volunteeringPostRepository.getVolunteeringPostDTOs(allPosts);
    }

    public List<VolunteeringPostDTO> getOrganizationVolunteeringPosts(int organizationId, String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }

        List<VolunteeringPost> orgPosts = volunteeringPostRepository.getOrganizationVolunteeringPosts(organizationId);
        return volunteeringPostRepository.getVolunteeringPostDTOs(orgPosts);
    }

    public void joinVolunteeringRequest(int postId, String actor, String freeText) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }

        int volunteeringId = volunteeringPostRepository.getVolunteeringIdByPostId(postId);
        volunteeringFacade.requestToJoinVolunteering(actor, volunteeringId, freeText);

        volunteeringPostRepository.incNumOfPeopleRequestedToJoin(postId);
        // Eyal already notified org managers
    }

    /*public List<VolunteeringPostDTO> searchByKeywords(String search, String actor, List<VolunteeringPostDTO> allPosts) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        if(search == null || search.isBlank()) {
            return volunteeringPostRepository.getVolunteeringPostDTOs();
        }
        search = search.replaceAll("[^a-zA-Z0-9א-ת ]", "").replaceAll("  ", " ").toLowerCase();

        Set<String> searchKeywords = new HashSet<>(Arrays.asList(search.split(" ")));
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

    private Set<String> getVolunteeringPostKeywords(VolunteeringPostDTO post) {
        int volunteeringId = post.getVolunteeringId();
        VolunteeringDTO volunteering = volunteeringFacade.getVolunteeringDTO(volunteeringId);

        //Set<String> postKeywords = keywordExtractor.getKeywords(cleanString(post.getTitle()) + " " + cleanString(post.getDescription()));
        Set<String> postKeywords = post.getKeywords();
        Set<String> volunteeringKeywords = getVolunteeringKeywords(volunteering);

        postKeywords.addAll(volunteeringKeywords);
        postKeywords = postKeywords.stream().map(String::toLowerCase).collect(Collectors.toSet());
        return postKeywords;
    }



    private Set<String> getVolunteeringKeywords(VolunteeringDTO volunteering) {
        int volunteeringId = volunteering.getId();

        //Set<String> volunteeringKeywords = keywordExtractor.getKeywords(cleanString(volunteering.getName()) + " " + cleanString(volunteering.getDescription()));
        Set<String> volunteeringKeywords = new HashSet<>();
        List<String> volunteeringCategories = volunteeringFacade.getVolunteeringCategories(volunteeringId);
        List<String> volunteeringSkills = volunteeringFacade.getVolunteeringSkills(volunteeringId);

        if(volunteeringCategories != null) {
            volunteeringKeywords.addAll(volunteeringCategories);
        }
        if(volunteeringSkills != null) {
            volunteeringKeywords.addAll(volunteeringSkills);
        }

        return volunteeringKeywords;
    }*/

    private int countCommons(Set<String> s1, Set<String> s2) {
        int matching = 0;
        for (String item1 : s1) {
            item1 = item1.toLowerCase().trim();
            for(String item2 : s2) {
                item2 = item2.toLowerCase().trim();
                if (item2.contains(item1)) {
                    matching++;
                }
            }
        }
        return matching;
    }

    private int countCommonsStrict(Set<String> s1, Set<String> s2) {
        int matching = 0;
        for (String item1 : s1) {
            item1 = item1.toLowerCase().trim();
            for(String item2 : s2) {
                item2 = item2.toLowerCase().trim();
                if (item2.equals(item1)) {
                    matching++;
                }
            }
        }
        return matching;
    }

    public List<VolunteeringPostDTO> sortByRelevance(String actor, List<VolunteeringPostDTO> allPosts) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }

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
            Set<String> historyKeywords = new HashSet<>();
            historyKeywords.addAll(historyVolunteering.getCategories());
            historyKeywords.addAll(historyVolunteering.getSkills());
            match += countCommons(currKeywords, historyKeywords);
        }
        return match;
    }

    private Set<String> getVolunteeringKeywords(VolunteeringDTO volunteering) {
        int volunteeringId = volunteering.getId();

        Set<String> volunteeringKeywords = new HashSet<>();
        List<String> volunteeringCategories = getVolunteeringCategories(volunteeringId);
        List<String> volunteeringSkills = getVolunteeringSkills(volunteeringId);

        if(volunteeringCategories != null) {
            volunteeringKeywords.addAll(volunteeringCategories);
        }
        if(volunteeringSkills != null) {
            volunteeringKeywords.addAll(volunteeringSkills);
        }

        return volunteeringKeywords;
    }

    public List<VolunteeringPostDTO> sortByPopularity(String actor, List<VolunteeringPostDTO> allPosts) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }

        List<VolunteeringPostDTO> sorted = allPosts.stream()
                .sorted(Comparator.comparingInt(post -> -1 * volunteeringPostRepository.getVolunteeringPost(post.getId()).evaluatePopularity()))
                .collect(Collectors.toList());

        return sorted;
    }

    // my post = I am poster or I am a manager of the organization
    private boolean isMyPost(VolunteeringPost post, String actor) {
        if(post.getPosterUsername().equals(actor)) {
            return true;
        }
        int volunteeringOrgId = post.getOrganizationId();
        OrganizationDTO org = organizationsFacade.getOrganization(volunteeringOrgId);
        return org.getManagerUsernames().contains(actor);
    }

    //TODO: add more parameters
    public List<VolunteeringPostDTO> filterVolunteeringPosts(Set<String> categories, Set<String> skills, Set<String> cities, Set<String> organizationNames, Set<String> volunteeringNames, String actor, List<Integer> allPostIds, boolean isMyPosts) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }

        List<VolunteeringPostDTO> result = new ArrayList<>();

        for(int postId : allPostIds) {
            VolunteeringPost post = volunteeringPostRepository.getVolunteeringPost(postId);
            int volunteeringId = post.getVolunteeringId();
            Set<String> volunteeringCategories = new HashSet<>(volunteeringFacade.getVolunteeringCategories(volunteeringId));
            Set<String> volunteeringSkills = new HashSet<>(volunteeringFacade.getVolunteeringSkills(volunteeringId));
            List<LocationDTO> volunteeringLocations = volunteeringFacade.getVolunteeringLocations(volunteeringId);
            Set<String> volunteeringCities = volunteeringLocations.stream().map(locationDTO -> locationDTO.getAddress().getCity()).collect(Collectors.toSet());
            String organizationName = organizationsFacade.getOrganization(post.getOrganizationId()).getName();
            String volunteeringName = volunteeringFacade.getVolunteeringDTO(post.getVolunteeringId()).getName();
            Set<String> postKeywords = post.getKeywords();

            boolean matchByCategory = categories.size() > 0 ? countCommons(volunteeringCategories, categories) >= 1 || countCommons(categories, postKeywords) >= 1 : true;
            boolean matchBySkill = skills.size() > 0 ? countCommons(volunteeringSkills, skills) >= 1 || countCommons(skills, postKeywords) >= 1 : true;
            boolean matchByCity = cities.size() > 0 ? countCommonsStrict(volunteeringCities, cities) >= 1 || countCommonsStrict(cities, postKeywords) >= 1 : true;
            boolean matchByOrganization = organizationNames.size() > 0 ? organizationNames.contains(organizationName) : true;
            boolean matchByVolunteering = volunteeringNames.size() > 0 ? volunteeringNames.contains(volunteeringName) : true;
            boolean matchByMyPost = isMyPosts ? isMyPost(post, actor) : true;

            if(matchByCategory && matchBySkill && matchByCity && matchByOrganization && matchByVolunteering && matchByMyPost) {
                result.add(new VolunteeringPostDTO(post));
            }
        }

        return result;
    }

    // my post = I am poster or I am a related user
    private boolean isMyPost(VolunteerPost post, String actor) {
        if(post.getPosterUsername().equals(actor)) {
            return true;
        }
        return post.getRelatedUsers().contains(actor);
    }

    public List<VolunteerPostDTO> filterVolunteerPosts(Set<String> categories, Set<String> skills, String actor, List<Integer> allPosts, boolean isMyPosts) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }

        List<VolunteerPostDTO> result = new ArrayList<>();

        for(int postId : allPosts) {
            VolunteerPost post = volunteerPostRepository.getVolunteerPost(postId);
            Set<String> volunteeringCategories = new HashSet<>(post.getCategories(this));
            Set<String> volunteeringSkills = new HashSet<>(post.getSkills(this));
            Set<String> postKeywords = post.getKeywords();

            boolean matchByCategory = categories.size() > 0 ? countCommons(volunteeringCategories, categories) >= 1 || countCommons(postKeywords, categories) >= 1 : true;
            boolean matchBySkill = skills.size() > 0 ? countCommons(volunteeringSkills, skills) >= 1 || countCommons(postKeywords, skills) >= 1 : true;
            boolean matchByIsMyPost = isMyPosts ? isMyPost(post, actor) : true;

            if(matchByCategory && matchBySkill && matchByIsMyPost) {
                result.add(new VolunteerPostDTO(post));
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

    public List<String> getAllVolunteerPostsCategories() {
        List<VolunteerPost> allPosts = volunteerPostRepository.getAllVolunteerPosts();
        Set<String> allCategories = new HashSet<>();

        for(VolunteerPost post : allPosts) {
            List<String> volunteeringCategories = post.getCategories(this);
            if(volunteeringCategories != null) {
                allCategories.addAll(volunteeringCategories);
            }
        }
        return new ArrayList<>(allCategories);
    }

    public List<String> getAllVolunteerPostsSkills() {
        List<VolunteerPost> allPosts = volunteerPostRepository.getAllVolunteerPosts();
        Set<String> allSkills = new HashSet<>();

        for(VolunteerPost post : allPosts) {
            List<String> volunteeringCategories = post.getSkills(this);
            if(volunteeringCategories != null) {
                allSkills.addAll(volunteeringCategories);
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

    private boolean isAdmin(String user) {
        return usersFacade.isAdmin(user);
    }
    private boolean userExists(String user){
        return usersFacade.userExists(user);
    }

    private Set<String> getUserCategories(String actor) {
        return new HashSet<>(usersFacade.getUserPreferences(actor));
    }

    private Set<String> getUserSkills(String actor) {
        return new HashSet<>(usersFacade.getUserSkills(actor));
    }

    private List<VolunteeringDTO> getUserVolunteeringHistory(String actor) {
        return usersFacade.getUserVolunteeringHistory(actor);
    }

    public List<String> getVolunteeringImages(int volunteeringId) {
        return volunteeringFacade.getVolunteeringImages(volunteeringId);
    }

    // -------------------------------------------------
    // ---------------- VOLUNTEER POSTS ----------------

    public int createVolunteerPost(String title, String description, String posterUsername) {
        if(!userExists(posterUsername)){
            throw new IllegalArgumentException("User " + posterUsername + " doesn't exist");
        }

        Set<String> postKeywords = keywordExtractor.getVolunteerPostKeywords(title, description);
        SkillsAndCategories postSkillsAndCategories = skillsAndCategoriesExtractor.getSkillsAndCategories(title, description, null, null);
        List<String> postSkills = postSkillsAndCategories.getSkills();
        List<String> postCategories = postSkillsAndCategories.getCategories();
        int postId = volunteerPostRepository.createVolunteerPost(title, description, postKeywords, posterUsername, postSkills, postCategories);
        return postId;
    }

    private void notifyRelatedUsers(List<String> users, String postTitle, String postAction, String nav) {
        for(String relatedUser : users) {
            String message = String.format("Your post \"%s\" was %s.", postTitle, postAction);
            notificationSystem.notifyUser(relatedUser, message, nav);
        }
    }

    public void removeVolunteerPost(String actor, int postId) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }

        VolunteerPost toRemove = volunteerPostRepository.getVolunteerPost(postId);

        if(!toRemove.getPosterUsername().equals(actor)) {
            throw new IllegalArgumentException(PostErrors.makeUserIsNotAllowedToMakePostActionError(toRemove.getTitle(), actor, "remove"));
        }
        volunteerPostRepository.removeVolunteerPost(postId);
        reportsFacade.removeVolunteerPostReports(postId);

        notifyRelatedUsers(toRemove.getRelatedUsers(), toRemove.getTitle(), "removed", NotificationNavigations.postsList);
    }

    public void editVolunteerPost(int postId, String title, String description, String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }

        VolunteerPost toEdit = volunteerPostRepository.getVolunteerPost(postId);
        String prevTitle = toEdit.getTitle();

        if(!toEdit.getPosterUsername().equals(actor)) {
            throw new IllegalArgumentException(PostErrors.makeUserIsNotAllowedToMakePostActionError(toEdit.getTitle(), actor, "edit"));
        }

        Set<String> postKeywords = keywordExtractor.getVolunteerPostKeywords(title, description);
        SkillsAndCategories postSkillsAndCategories = skillsAndCategoriesExtractor.getSkillsAndCategories(title, description, null, null);
        List<String> postSkills = postSkillsAndCategories.getSkills();
        List<String> postCategories = postSkillsAndCategories.getCategories();
        volunteerPostRepository.editVolunteerPost(postId, title, description, postKeywords, postSkills, postCategories);

        notifyRelatedUsers(toEdit.getRelatedUsers(), prevTitle, "edited", NotificationNavigations.volunteerPost(postId));
    }

    public void sendAddRelatedUserRequest(int postId, String username, String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        if(!userExists(username)){
            throw new IllegalArgumentException("User " + username + " doesn't exist");
        }

        VolunteerPostDTO post = getVolunteerPost(postId, actor); // check if post exists

        if(post.getRelatedUsers().contains(username)) {
            throw new IllegalArgumentException("User " + username + " is already a related user of the post " + post.getTitle() + ".");
        }

        requestRepository.createRequest(username, actor, postId, RequestObject.VOLUNTEER_POST);

        String title = post.getTitle();
        String message = String.format("%s asked you to join the volunteer post \"%s\".", actor, title);
        notificationSystem.notifyUser(username, message, NotificationNavigations.requests);
    }

    public void handleAddRelatedUserRequest(int postId, String actor, boolean approved) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }

        Request request = requestRepository.getRequest(actor, postId, RequestObject.VOLUNTEER_POST);
        if(approved) {
            volunteerPostRepository.addRelatedUser(postId, actor);
        }
        requestRepository.deleteRequest(actor, postId, RequestObject.VOLUNTEER_POST);

        String title = getVolunteerPost(postId, actor).getTitle();
        String approvedStr = approved ? "approved" : "denied";
        String message = String.format("%s has %s your request to join the post \"%s\".", actor, approvedStr, title);
        notificationSystem.notifyUser(request.getAssignerUsername(), message, NotificationNavigations.volunteerPost(postId));
    }

    public void removeRelatedUser(int postId, String username, String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        String title = getVolunteerPost(postId, actor).getTitle();

        volunteerPostRepository.removeRelatedUser(postId, username, actor);
        List<String> allRelatedUsersButRemoved = getRelatedUsers(postId);

        for(String user : allRelatedUsersButRemoved) {
            String message = String.format("%s was removed from the post \"%s\".", username, title);
            notificationSystem.notifyUser(user, message, NotificationNavigations.volunteerPost(postId));
        }
        notificationSystem.notifyUser(username, String.format("You were removed from the post \"%s\".", title), NotificationNavigations.volunteerPost(postId));
    }

    public boolean hasRelatedUser(int postId, String username) {
        if(!userExists(username)){
            throw new IllegalArgumentException("User " + username + " doesn't exist");
        }
        VolunteerPost post = volunteerPostRepository.getVolunteerPost(postId);
        return post.hasRelatedUser(username);
    }

    public List<String> getRelatedUsers(int postId) {
        VolunteerPost post = volunteerPostRepository.getVolunteerPost(postId);
        return new LinkedList<>(post.getRelatedUsers());
    }

    public void addImage(int postId, String path, String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        volunteerPostRepository.addImage(postId, path, actor);
    }

    public void removeImage(int postId, String path, String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        volunteerPostRepository.removeImage(postId, path, actor);
    }

    public List<VolunteerPostDTO> getAllVolunteerPosts() {
        return volunteerPostRepository.getVolunteerPostDTOs();
    }

    // --------------------------------------------------------------
    // ---------------- GENERIC SEARCH, SORT, FILTER ----------------

    public List<? extends PostDTO> searchByKeywords(String search, String actor, List<PostDTO> allPosts, boolean volunteering) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        if(search == null || search.isBlank()) {
            return volunteering ? volunteeringPostRepository.getVolunteeringPostDTOs() : volunteerPostRepository.getVolunteerPostDTOs();
        }
        search = search.replaceAll("[^a-zA-Z0-9א-ת ]", "").replaceAll("  ", " ").toLowerCase();

        Set<String> searchKeywords = new HashSet<>(Arrays.asList(search.split(" ")));
        List<PostDTO> result = new ArrayList<>();

        for(PostDTO post : allPosts) {
            Set<String> postKeywords = getPostKeywords(post);
            Set<String> postTitleKeywords = new HashSet<>(Arrays.asList(post.getTitle().split(" ")));
            Set<String> postDescriptionKeywords = new HashSet<>(Arrays.asList(post.getDescription().split(" ")));
            postKeywords.addAll(postTitleKeywords);
            postKeywords.addAll(postDescriptionKeywords);

            int common = countCommons(searchKeywords, postKeywords);
            if(common >= 1) {
                result.add(post);
            }
        }
        return result;
    }

    private Set<String> getPostKeywords(PostDTO postDTO) {
        Set<String> postKeywords = postDTO.getKeywords();
        Set<String> skills = new HashSet<>(postDTO.getSkills(this));
        Set<String> categories = new HashSet<>(postDTO.getCategories(this));

        postKeywords.addAll(skills);
        postKeywords.addAll(categories);
        postKeywords = postKeywords.stream().map(String::toLowerCase).collect(Collectors.toSet());
        return postKeywords;
    }

    public List<PostDTO> sortByPostingTime(String actor, List<PostDTO> allPosts) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }

        List<PostDTO> sorted = allPosts.stream()
                .sorted((post1, post2) -> post2.getPostedTime().compareTo(post1.getPostedTime()))
                .collect(Collectors.toList());

        return sorted;
    }

    public List<PostDTO> sortByLastEditTime(String actor, List<PostDTO> allPosts) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }

        List<PostDTO> sorted = allPosts.stream()
                .sorted((post1, post2) -> post2.getLastEditedTime().compareTo(post1.getLastEditedTime()))
                .collect(Collectors.toList());

        return sorted;
    }

    //TODO: sort by location in beta version

    // wrapping volunteering facade function so I can use it from Post
    public List<String> getVolunteeringSkills(int volunteeringId) {
        return volunteeringFacade.getVolunteeringSkills(volunteeringId);
    }

    // wrapping volunteering facade function so I can use it from Post
    public List<String> getVolunteeringCategories(int volunteeringId) {
        return volunteeringFacade.getVolunteeringCategories(volunteeringId);
    }

    public boolean hasPosts(int volunteeringId) {
        //TODO: do this more efficiently
        for(VolunteeringPost p : volunteeringPostRepository.getAllVolunteeringPosts()){
            if(p.getVolunteeringId() == volunteeringId){
                return true;
            }
        }
        return false;
    }

    public List<Request> getUserRequests(String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        return requestRepository.getUserRequests(actor, RequestObject.VOLUNTEER_POST);
    }

    public KeywordExtractor getKeywordExtractor() {
        return this.keywordExtractor;
    }

    public SkillsAndCategoriesExtractor getSkillsAndCategoriesExtractor() {
        return this.skillsAndCategoriesExtractor;
    }

    public RequestRepository getRequestRepository() {
        return requestRepository;
    }

    public void setPoster(int postId, String actor, String newPoster) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        if(!userExists(newPoster)){
            throw new IllegalArgumentException("User " + newPoster + " doesn't exist");
        }
        volunteerPostRepository.setPoster(postId, actor, newPoster);

        VolunteerPost post = volunteerPostRepository.getVolunteerPost(postId);
        String message = String.format("You were set as the poster of the post %s.", post.getTitle());
        notificationSystem.notifyUser(newPoster, message, NotificationNavigations.volunteerPost(postId));
    }

    public void setVolunteerPostSkills(int postId, List<String> skills, String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }

        VolunteerPostDTO post = getVolunteerPost(postId, actor);
        volunteerPostRepository.editVolunteerPost(postId, post.getTitle(), post.getDescription(), post.getKeywords(), skills, post.getCategories());
    }

    public void setVolunteerPostCategories(int postId, List<String> categories, String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }

        VolunteerPostDTO post = getVolunteerPost(postId, actor);
        volunteerPostRepository.editVolunteerPost(postId, post.getTitle(), post.getDescription(), post.getKeywords(), post.getSkills(), categories);
    }
}
