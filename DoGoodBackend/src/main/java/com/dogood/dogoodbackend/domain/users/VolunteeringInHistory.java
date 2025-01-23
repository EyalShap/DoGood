package com.dogood.dogoodbackend.domain.users;

import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringDTO;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Embeddable
public class VolunteeringInHistory {
    private int id;
    private int orgId;
    private String name;
    private String description;
    private String skills;
    private String categories;
    private String imagePaths;

    public VolunteeringInHistory() {}

    public VolunteeringInHistory(VolunteeringDTO dto){
        this.id = dto.getId();
        this.orgId = dto.getOrgId();
        this.name = dto.getName();
        this.description = dto.getDescription();
        this.skills = String.join(",", dto.getSkills());
        this.categories = String.join(",", dto.getCategories());
        this.imagePaths = String.join(",", dto.getImagePaths());
    }

    public VolunteeringDTO toDTO(){
        return new VolunteeringDTO(id, orgId, name, description,
                Arrays.stream(skills.split(",")).toList(),
                        Arrays.stream(categories.split(",")).toList(),
                                Arrays.stream(imagePaths.split(",")).toList());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VolunteeringInHistory that)) return false;
        return id == that.id && orgId == that.orgId && Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(skills, that.skills) && Objects.equals(categories, that.categories) && Objects.equals(imagePaths, that.imagePaths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orgId, name, description, skills, categories, imagePaths);
    }
}
