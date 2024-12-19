package com.dogood.dogoodbackend.domain.volunteerings;

import java.util.List;

public class VolunteeringDTO {
    private final int id;
    private final String name;
    private final String description;
    private List<String> skills;
    private List<String> categories;

    public VolunteeringDTO(int id, String name, String description, List<String> skills, List<String> categories) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.skills = skills;
        this.categories = categories;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getSkills() {
        return skills;
    }

    public List<String> getCategories() {
        return categories;
    }
}
