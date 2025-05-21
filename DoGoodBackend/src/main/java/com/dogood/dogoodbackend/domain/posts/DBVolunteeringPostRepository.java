package com.dogood.dogoodbackend.domain.posts;

import com.dogood.dogoodbackend.domain.organizations.Organization;
import com.dogood.dogoodbackend.jparepos.VolunteeringPostJPA;
import com.dogood.dogoodbackend.utils.OrganizationErrors;
import com.dogood.dogoodbackend.utils.PostErrors;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@Transactional
public class DBVolunteeringPostRepository implements VolunteeringPostRepository{
    private VolunteeringPostJPA jpa;

    public DBVolunteeringPostRepository(VolunteeringPostJPA jpa) {
        this.jpa = jpa;
    }

    public DBVolunteeringPostRepository() {}

    public void setJPA(VolunteeringPostJPA jpa) {
        this.jpa = jpa;
    }

    @Override
    public void clear() {
        jpa.deleteAll();
    }

    @Override
    public int createVolunteeringPost(String title, String description, Set<String> keywords, String posterUsername, int volunteeringId, int organizationId) {
        VolunteeringPost post = new VolunteeringPost(title, description, keywords, posterUsername, volunteeringId, organizationId);
        jpa.save(post);
        return post.getId();
    }

    @Override
    public void removeVolunteeringPost(int postId) {
        if(!jpa.existsById(postId)) {
            throw new IllegalArgumentException(PostErrors.makePostIdDoesNotExistError(postId));
        }
        jpa.deleteById(postId);
    }

    @Override
    public void removePostsByVolunteeringId(int volunteeringId) {
        jpa.deleteByVolunteeringId(volunteeringId);
    }

    @Override
    public void editVolunteeringPost(int postId, String title, String description, Set<String> keywords) {
        VolunteeringPost toEdit = getVolunteeringPost(postId); // will throw exception if does not exist
        toEdit.edit(title, description, keywords);
        jpa.save(toEdit);
    }

    @Override
    public void incNumOfPeopleRequestedToJoin(int postId) {
        VolunteeringPost toInc = getVolunteeringPost(postId); // will throw exception if does not exist
        toInc.incNumOfPeopleRequestedToJoin();
        jpa.save(toInc);
    }

    @Override
    public VolunteeringPost getVolunteeringPost(int postId) {
        Optional<VolunteeringPost> post = jpa.findByIdForUpdate(postId);
        if(!post.isPresent()) {
            throw new IllegalArgumentException(PostErrors.makePostIdDoesNotExistError(postId));
        }

        return post.get();
    }

    @Override
    public List<VolunteeringPost> getAllVolunteeringPosts() {
        return jpa.findAll();
    }

    @Override
    public List<VolunteeringPost> getAllVolunteeringPostsOfVolunteering(int volunteeringId) {
        return jpa.findByVolunteeringId(volunteeringId);
    }

    @Override
    public List<VolunteeringPost> getOrganizationVolunteeringPosts(int organizationId) {
        return jpa.findByOrganizationId(organizationId);
    }

    @Override
    public int getVolunteeringIdByPostId(int postId) {
        return getVolunteeringPost(postId).getVolunteeringId();
    }

}
