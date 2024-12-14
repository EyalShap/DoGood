package com.dogood.dogoodbackend.domain.volunteerings;

import java.util.Collection;

public interface VolunteeringRepository {
    public Volunteering getVolunteering(int volunteeringId);
    public Volunteering addVolunteering(int organizationId, String name, String description);
    public Volunteering updateVolunteering(int volunteeringId, String name, String description);
    public Volunteering updateVolunteeringSkills(int volunteeringId, Collection<String> skills);
    public Volunteering updateVolunteeringCategories(int volunteeringId, Collection<String> categories);
    public Volunteering disableVolunteering(int volunteeringId);
}
