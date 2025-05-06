package com.dogood.dogoodbackend.domain.organizations;

import com.dogood.dogoodbackend.domain.posts.DBVolunteeringPostRepository;
import com.dogood.dogoodbackend.domain.posts.VolunteeringPostRepository;
import com.dogood.dogoodbackend.jparepos.OrganizationJPA;
import com.dogood.dogoodbackend.jparepos.VolunteeringPostJPA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
public class OrganizationRepositoryIntegrationTest extends AbstractOrganizationRepositoryTest{
    @Autowired
    private ApplicationContext applicationContext;

    @Override
    protected OrganizationRepository createRepository() {
        DBOrganizationRepository repo = new DBOrganizationRepository();
        OrganizationJPA jpa = applicationContext.getBean(OrganizationJPA.class);
        repo.setJPA(jpa);
        return repo;
    }
}
