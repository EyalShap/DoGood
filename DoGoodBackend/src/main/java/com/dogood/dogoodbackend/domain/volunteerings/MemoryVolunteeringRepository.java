package com.dogood.dogoodbackend.domain.volunteerings;

import java.util.*;

public class MemoryVolunteeringRepository implements VolunteeringRepository{
    private int latestId;
    private Map<Integer, Volunteering> volunteerings;
    private Map<Integer, Map<String, Date>> firstScans;

    public MemoryVolunteeringRepository() {
        volunteerings = new HashMap<>();
        firstScans = new HashMap<>();
        latestId = 0;
    }

    @Override
    public Volunteering getVolunteering(int volunteeringId) {
        return volunteerings.get(volunteeringId);
    }

    @Override
    public List<Volunteering> getAllVolunteerings() {
        return new LinkedList<>(volunteerings.values());
    }

    @Override
    public Volunteering addVolunteering(int organizationId, String name, String description) {
        Volunteering volunteering = new Volunteering(latestId++, organizationId, name, description, new BarcodeHandler());
        volunteerings.put(volunteering.getId(), volunteering);
        return volunteering;
    }

    @Override
    public void updateVolunteering(int volunteeringId, String name, String description) {
        Volunteering volunteering = getVolunteering(volunteeringId);
        volunteering.setName(name);
        volunteering.setDescription(description);
    }

    @Override
    public void updateVolunteeringSkills(int volunteeringId, Collection<String> skills) {
        Volunteering volunteering = getVolunteering(volunteeringId);
        volunteering.setSkills(new LinkedList<>(skills));
    }

    @Override
    public void updateVolunteeringCategories(int volunteeringId, Collection<String> categories) {
        Volunteering volunteering = getVolunteering(volunteeringId);
        volunteering.setCategories(new LinkedList<>(categories));
    }

    @Override
    public void disableVolunteering(int volunteeringId) {
        volunteerings.remove(volunteeringId);
    }

    @Override
    public void updateVolunteeringInDB(Volunteering volunteering) {
        return;
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
