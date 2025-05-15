package com.dogood.dogoodbackend.domain.posts;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface VolunteerPostRepository {
    public void clear();
    public int createVolunteerPost(String title, String description, Set<String> keywords, String posterUsername, List<String> skills, List<String> categories);
    public void removeVolunteerPost(int postId);
    public void editVolunteerPost(int postId, String title, String description, Set<String> keywords, List<String> skills, List<String> categories);
    public void addRelatedUser(int postId, String username);
    public void removeRelatedUser(int postId, String username, String actor);
    public void addImage(int postId, String path, String actor);
    public void removeImage(int postId, String path, String actor);
    public VolunteerPost getVolunteerPost(int postId);
    public List<VolunteerPost> getAllVolunteerPosts();

    public default List<VolunteerPostDTO> getVolunteerPostDTOs(List<VolunteerPost> posts) {
        List<VolunteerPostDTO> volunteerPostDTOs = posts.stream()
                .map(post -> new VolunteerPostDTO(post))
                .collect(Collectors.toList());
        return volunteerPostDTOs;
    }

    public default List<VolunteerPostDTO> getVolunteerPostDTOs() {
        return getVolunteerPostDTOs(getAllVolunteerPosts());
    }
}
