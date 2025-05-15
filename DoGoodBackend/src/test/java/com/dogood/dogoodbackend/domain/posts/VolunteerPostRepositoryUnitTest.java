package com.dogood.dogoodbackend.domain.posts;

public class VolunteerPostRepositoryUnitTest extends AbstractVolunteerPostRepositoryTest {

    @Override
    protected VolunteerPostRepository createRepository() {
        return new MemoryVolunteerPostRepository();
    }
}
