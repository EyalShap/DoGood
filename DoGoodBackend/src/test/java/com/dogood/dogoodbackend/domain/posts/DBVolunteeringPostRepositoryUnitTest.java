package com.dogood.dogoodbackend.domain.posts;

import com.dogood.dogoodbackend.domain.volunteerings.Volunteering;
import com.dogood.dogoodbackend.jparepos.VolunteeringPostJPA;
import jakarta.transaction.Transactional;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.*;
@Transactional
public class DBVolunteeringPostRepositoryUnitTest extends AbstractVolunteeringPostRepositoryTest{

    private VolunteeringPostJPA mockJpa;
    private static int nextId = 0;
    private Map<Integer, VolunteeringPost> posts;

    @Override
    protected VolunteeringPostRepository createRepository() {
        DBVolunteeringPostRepository repo = new DBVolunteeringPostRepository();
        mockJpa = Mockito.mock(VolunteeringPostJPA.class);
        repo.setJPA(mockJpa);
        posts = new HashMap<>();
        setBehavior();
        return repo;
    }

    private void setBehavior() {
        Mockito.when(mockJpa.save(Mockito.any(VolunteeringPost.class))).thenAnswer(new Answer<VolunteeringPost>() {
            @Override
            public VolunteeringPost answer(InvocationOnMock invocation) {
                VolunteeringPost post = invocation.getArgument(0);
                post.setId(nextId);
                posts.put(nextId, post);
                nextId++;
                return post;
            }
        });

        Mockito.when(mockJpa.existsById(Mockito.anyInt())).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) {
                Integer id = invocation.getArgument(0);
                return posts.containsKey(id);
            }
        });

        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                Integer id = invocation.getArgument(0);
                posts.remove(id);
                return null;
            }
        }).when(mockJpa).deleteById(Mockito.anyInt());

        Mockito.when(mockJpa.findById(Mockito.anyInt())).thenAnswer(new Answer<Optional<VolunteeringPost>>() {
            @Override
            public Optional<VolunteeringPost> answer(InvocationOnMock invocation) {
                Integer id = invocation.getArgument(0);
                if (posts.containsKey(id)) {
                    return Optional.of(posts.get(id));
                }
                return Optional.empty();
            }
        });

        Mockito.when(mockJpa.findByIdForUpdate(Mockito.anyInt())).thenAnswer(new Answer<Optional<VolunteeringPost>>() {
            @Override
            public Optional<VolunteeringPost> answer(InvocationOnMock invocation) {
                Integer id = invocation.getArgument(0);
                if (posts.containsKey(id)) {
                    return Optional.of(posts.get(id));
                }
                return Optional.empty();
            }
        });

        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                posts = new HashMap<>();
                nextId = 0;
                return null;
            }
        }).when(mockJpa).deleteAll();

        Mockito.when(mockJpa.findAll()).thenAnswer(new Answer<List<VolunteeringPost>>() {
            @Override
            public List<VolunteeringPost> answer(InvocationOnMock invocation) {
                return new ArrayList<>(posts.values());
            }
        });

        Mockito.when(mockJpa.findByOrganizationId(Mockito.anyInt())).thenAnswer(new Answer<List<VolunteeringPost>>() {
            @Override
            public List<VolunteeringPost> answer(InvocationOnMock invocation) {
                Integer organizationId = invocation.getArgument(0);
                List<VolunteeringPost> res = new ArrayList<>();
                for(VolunteeringPost post : posts.values()) {
                    if(post.getOrganizationId() == organizationId) {
                        res.add(post);
                    }
                }
                return res;
            }
        });

        Mockito.when(mockJpa.deleteByVolunteeringId(Mockito.anyInt())).thenAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                Integer volunteeringId = invocation.getArgument(0);
                List<VolunteeringPost> allPosts = new ArrayList<>(posts.values());
                for(VolunteeringPost post : allPosts) {
                    if(post.getVolunteeringId() == volunteeringId) {
                        posts.remove(post.getId());
                    }
                }
                return null;
            }
        });

    }

}
