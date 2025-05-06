package com.dogood.dogoodbackend.domain.posts;


import com.dogood.dogoodbackend.utils.PostErrors;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public interface VolunteeringPostRepository {
    public void clear();
    public int createVolunteeringPost(String title, String description, Set<String> keywords, String posterUsername, int volunteeringId, int organizationId);
    public void removeVolunteeringPost(int postId);
    public void removePostsByVolunteeringId(int volunteeringId);
    public void editVolunteeringPost(int postId, String title, String description, Set<String> keywords);
    public void incNumOfPeopleRequestedToJoin(int postId);
    public VolunteeringPost getVolunteeringPost(int postId);
    public List<VolunteeringPost> getAllVolunteeringPosts();
    public List<VolunteeringPost> getOrganizationVolunteeringPosts(int organizationId);
    public int getVolunteeringIdByPostId(int postId);

    public default List<VolunteeringPostDTO> getVolunteeringPostDTOs(List<VolunteeringPost> posts) {
        List<VolunteeringPostDTO> volunteeringPostDTO = posts.stream()
                .map(post -> new VolunteeringPostDTO(post))
                .collect(Collectors.toList());
        return volunteeringPostDTO;
    }

    public default List<VolunteeringPostDTO> getVolunteeringPostDTOs() {
        return getVolunteeringPostDTOs(getAllVolunteeringPosts());
    }

}
