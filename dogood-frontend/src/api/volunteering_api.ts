import axios from "axios";
import VolunteeringModel, { VolunteersToGroup } from "../models/VolunteeringModel";
import APIResponse from "../models/APIResponse";

const loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

const server = '127.0.0.1:8080';

export const getVolunteering = async (volunteeringId: number): Promise<VolunteeringModel> => {
    const config = {
        headers: { Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    let res = await axios.get(`http://${server}/api/volunteering/getVolunteering?volunteeringId=${volunteeringId}&userId=${sessionStorage.getItem('username')}`, config);
    const response: APIResponse<VolunteeringModel> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getVolunteeringVolunteers = async (volunteeringId: number): Promise<VolunteersToGroup> => {
    const config = {
        headers: { Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    let res = await axios.get(`http://${server}/api/volunteering/getVolunteeringVolunteers?volunteeringId=${volunteeringId}&userId=${sessionStorage.getItem('username')}`, config);
    const response: APIResponse<VolunteersToGroup> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getIsManager = async (organizationId: number): Promise<boolean> => {
    const config = {
        headers: { Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    let res = await axios.get(`http://${server}/api/organizations/isManager?orgId=${organizationId}&actor=${sessionStorage.getItem('username')}`, config);
    const response: APIResponse<boolean> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    console.log(res);
    return response.data;
}