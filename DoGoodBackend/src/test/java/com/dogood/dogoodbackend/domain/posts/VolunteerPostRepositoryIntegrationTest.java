package com.dogood.dogoodbackend.domain.posts;

import com.dogood.dogoodbackend.jparepos.VolunteerPostJPA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
public class VolunteerPostRepositoryIntegrationTest extends AbstractVolunteerPostRepositoryTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    protected VolunteerPostRepository createRepository() {
        DBVolunteerPostRepository repo = new DBVolunteerPostRepository();
        VolunteerPostJPA volunteerPostJPA = applicationContext.getBean(VolunteerPostJPA.class);
        repo.setJPA(volunteerPostJPA);
        return repo;
    }
}
