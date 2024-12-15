package com.dogood.dogoodbackend.domain.volunteerings;

import java.util.Collection;
import java.util.Date;

public interface VolunteeringRepository {
    public Volunteering getVolunteering(int volunteeringId);
    public Volunteering addVolunteering(int organizationId, String name, String description);
    public void updateVolunteering(int volunteeringId, String name, String description);
    public void updateVolunteeringSkills(int volunteeringId, Collection<String> skills);
    public void updateVolunteeringCategories(int volunteeringId, Collection<String> categories);
    public void disableVolunteering(int volunteeringId);
    public void updateVolunteeringInDB(int volunteeringId);
    public void recordFirstVolunteerScan(int volunteeringId, String userId);
    public Date getFirstVolunteerScan(int volunteeringId, String userId);
}
