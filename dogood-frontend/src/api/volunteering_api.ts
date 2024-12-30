import VolunteeringModel, { VolunteersToGroup } from "../models/VolunteeringModel";

const loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

export const getVolunteering = async (volunteeringId: number): Promise<VolunteeringModel> => {
    if(volunteeringId == 0){
        return {id: 0, orgId: 0, name: "Volnteering Name", description: loremIpsum, skills: [], categories: [], imagePaths: []}
    }
    else{
        throw "Volunteering not found";
    }
}

export const getVolunteeringVolunteers = async (volunteeringId: number): Promise<VolunteersToGroup> => {
    if(volunteeringId == 0){
        let dict: VolunteersToGroup = {};
        dict["EyalShapiro"] = 0;
        dict["DanaFriedman"] = 0;
        dict["BobsBurgers"] = 1;
        return dict;
    }
    else{
        throw "Volunteering not found";
    }
}

export const getIsManager = async (organizationId: number): Promise<boolean> => {
    console.log(organizationId)
    if(organizationId != 0){
        throw "Organization not found";
    }
    if(sessionStorage.getItem("username") === "TheDoctor"){
        return true;
    }else{
        return false;
    }
}