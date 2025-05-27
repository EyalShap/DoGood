package com.dogood.dogoodbackend.domain.posts;

import com.dogood.dogoodbackend.jparepos.VolunteeringPostJPA;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
@Transactional
public class DBVolunteeringPostRepositoryIntegrationTest extends AbstractVolunteeringPostRepositoryTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    protected VolunteeringPostRepository createRepository() {
        DBVolunteeringPostRepository repo = new DBVolunteeringPostRepository();
        VolunteeringPostJPA volunteeringPostJPA = applicationContext.getBean(VolunteeringPostJPA.class);
        repo.setJPA(volunteeringPostJPA);
        return repo;
    }
}
