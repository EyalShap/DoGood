package com.dogood.dogoodbackend.domain.posts;


import com.dogood.dogoodbackend.utils.PostErrors;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface VolunteeringPostRepository {
    public int createVolunteeringPost(String title, String description, String posterUsername, int volunteeringId, int organizationId);
    public void removeVolunteeringPost(int postId);
    public void editVolunteeringPost(int postId, String title, String description);
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
