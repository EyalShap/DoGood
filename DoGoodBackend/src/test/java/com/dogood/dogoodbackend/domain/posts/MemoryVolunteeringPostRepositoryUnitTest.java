package com.dogood.dogoodbackend.domain.posts;

import com.dogood.dogoodbackend.jparepos.VolunteeringPostJPA;
import jakarta.transaction.Transactional;

@Transactional
public class MemoryVolunteeringPostRepositoryUnitTest extends AbstractVolunteeringPostRepositoryTest {

    @Override
    protected VolunteeringPostRepository createRepository() {
        return new MemoryVolunteeringPostRepository();
    }
}
