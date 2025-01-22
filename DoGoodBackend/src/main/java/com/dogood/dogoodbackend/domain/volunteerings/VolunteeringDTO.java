package com.dogood.dogoodbackend.domain.volunteerings;

import com.dogood.dogoodbackend.domain.users.StringListConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;

import java.util.List;

@Embeddable
public class VolunteeringDTO {
    private int id;
    private int orgId;
    private String name;
    private String description;
    @Convert(converter = StringListConverter.class)
    private List<String> skills;
    @Convert(converter = StringListConverter.class)
    private List<String> categories;
    @Convert(converter = StringListConverter.class)
    private List<String> imagePaths;

    public VolunteeringDTO() {
    }

    public VolunteeringDTO(int id, int orgId, String name, String description, List<String> skills,
            List<String> categories, List<String> imagePaths) {
        this.id = id;
        this.orgId = orgId;
        this.name = name;
        this.description = description;
        this.skills = skills;
        this.categories = categories;
        this.imagePaths = imagePaths;
    }

    public int getId() {
        return id;
    }

    public int getOrgId() {
        return orgId;
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

    public List<String> getImagePaths() {
        return imagePaths;
    }

    public boolean equals(Object other){
        if(!(other instanceof VolunteeringDTO)){
            return false;
        }
        return id == ((VolunteeringDTO)other).id && orgId == ((VolunteeringDTO)other).orgId && name.equals(((VolunteeringDTO)other).name) && 
        description.equals(((VolunteeringDTO)other).description) && name.equals(((VolunteeringDTO)other).name) && ((skills == null && ((VolunteeringDTO)other).skills == null) || skills.equals(((VolunteeringDTO)other).skills)) &&
        ((categories == null && ((VolunteeringDTO)other).categories == null) || categories.equals(((VolunteeringDTO)other).categories)) && ((imagePaths == null && ((VolunteeringDTO)other).imagePaths == null) || imagePaths.equals(((VolunteeringDTO)other).imagePaths));
        
    }

}
