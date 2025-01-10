package com.dogood.dogoodbackend.domain.volunteerings;

import com.dogood.dogoodbackend.jparepos.VolunteeringJPA;

import java.util.*;

public class DatabaseVolunteeringRepository implements VolunteeringRepository{
    private VolunteeringJPA jpa;
    private Map<Integer, Map<String, Date>> firstScans;

    public DatabaseVolunteeringRepository(VolunteeringJPA jpa) {
        this.jpa = jpa;
        firstScans = new HashMap<>();
    }

    @Override
    public Volunteering getVolunteering(int volunteeringId) {
        return jpa.findById(volunteeringId).orElse(null);
    }

    @Override
    public Volunteering addVolunteering(int organizationId, String name, String description) {
        Volunteering volunteering = new Volunteering(organizationId, name, description, new BarcodeHandler());
        jpa.save(volunteering);
        return volunteering;
    }

    @Override
    public void updateVolunteering(int volunteeringId, String name, String description) {
        Volunteering volunteering = getVolunteering(volunteeringId);
        volunteering.setName(name);
        volunteering.setDescription(description);
        jpa.save(volunteering);
    }

    @Override
    public void updateVolunteeringSkills(int volunteeringId, Collection<String> skills) {
        Volunteering volunteering = getVolunteering(volunteeringId);
        volunteering.setSkills(new LinkedList<>(skills));
        jpa.save(volunteering);
    }

    @Override
    public void updateVolunteeringCategories(int volunteeringId, Collection<String> categories) {
        Volunteering volunteering = getVolunteering(volunteeringId);
        volunteering.setCategories(new LinkedList<>(categories));
        jpa.save(volunteering);
    }

    @Override
    public void disableVolunteering(int volunteeringId) {
        jpa.deleteById(volunteeringId);
    }

    @Override
    public void updateVolunteeringInDB(Volunteering volunteering) {
        jpa.save(volunteering);
    }

    @Override
    public void recordFirstVolunteerScan(int volunteeringId, String userId) {
        if(!firstScans.containsKey(volunteeringId)) {
            firstScans.put(volunteeringId, new HashMap<>());
        }
        firstScans.get(volunteeringId).put(userId, new Date());
    }

    @Override
    public void removeFirstVolunteerScan(int volunteeringId, String userId) {
        firstScans.get(volunteeringId).remove(userId);
    }

    @Override
    public Date getFirstVolunteerScan(int volunteeringId, String userId) {
        if(!firstScans.containsKey(volunteeringId)){
            return null;
        }
        return firstScans.get(volunteeringId).get(userId);
    }
}
