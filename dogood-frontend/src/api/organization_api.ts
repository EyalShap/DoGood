import APIResponse from "../models/APIResponse";
import OrganizationModel from "../models/OrganizationModel";
import axios from "axios";
import RequestModel from "../models/RequestModel";
import CreateOrganizationModel from "../models/CreateOrganizationModel";

const server: string = 'http://127.0.0.1:8080/api/organizations';

export const createOrganization = async (newOrganization: CreateOrganizationModel): Promise<number> => {
    let username: string | null = sessionStorage.getItem("username");
    let token: string | null = sessionStorage.getItem("token");

    if(username === null || token === null) {
        throw new Error("Error");
    }
    
    newOrganization.actor = username

    let url = `${server}/createOrganization`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    let res = await axios.post(url, newOrganization, config);
    const response: APIResponse<number> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    let organizationId = response.data;
    return organizationId;
}

export const removeOrganization = async (organizationId: number) => {
    let username: string | null = sessionStorage.getItem("username");
    let token: string | null = sessionStorage.getItem("token");

    if(username === null || token === null) {
        throw new Error("Error");
    }
    
    let url = `${server}/removeOrganization?orgId=${organizationId}?actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    let res = await axios.delete(url, config);
    const response: APIResponse<number> = await res.data;
    if(response.error){
        throw response.errorString;
    }
}

export const editOrganization = async (organizationId: number, newName: string, newDescription: string, newEmail: string, newPhoneNumber: string) => {
    let username: string | null = sessionStorage.getItem("username");
    let token: string | null = sessionStorage.getItem("token");

    if(username === null || token === null) {
        throw new Error("Error");
    }
    
    let url = `${server}/editOrganization?orgId=${organizationId}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    const request = {
        name: newName, 
        description: newDescription, 
        email: newEmail,
        phoneNumber: newPhoneNumber,
        actor: username
    }
    let res = await axios.put(url, request, config);
    const response: APIResponse<number> = await res.data;
    if(response.error){
        throw response.errorString;
    }
}

export const createVolunteering = async (organizationId: number, volunteeringName: string, volunteeringDescription: string): Promise<number> => {
    let username: string | null = sessionStorage.getItem("username");
    let token: string | null = sessionStorage.getItem("token");

    if(username === null || token === null) {
        throw new Error("Error");
    }
    
    let url = `${server}/createVolunteering`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    const request = {
        organizationId: organizationId,
        volunteeringName: volunteeringName,
        volunteeringDescription: volunteeringDescription,
        actor: username
    }

    let res = await axios.post(url, request, config);
    const response: APIResponse<number> = await res.data;

    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const sendAssignManagerRequest = async (organizationId: number, newManager: string) => {
    let username: string | null = sessionStorage.getItem("username");
    let token: string | null = sessionStorage.getItem("token");

    if(username === null || token === null) {
        throw new Error("Error");
    }
    
    let url = `${server}/sendAssignManagerRequest?newManager=${newManager}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    const request = {
        id: organizationId,
        actor: username
    }

    let res = await axios.post(url, request, config);
    const response: APIResponse<number> = await res.data;
    
    if(response.error){
        throw response.errorString;
    }
}

export const handleAssignManagerRequest = async (organizationId: number, approved: boolean) => {
    let username: string | null = sessionStorage.getItem("username");
    let token: string | null = sessionStorage.getItem("token");

    if(username === null || token === null) {
        throw new Error("Error");
    }
    
    let url = `${server}/handleAssignManagerRequest?approved=${approved}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    const request = {
        id: organizationId,
        actor: username
    }

    let res = await axios.post(url, request, config);
    const response: APIResponse<number> = await res.data;
    
    if(response.error){
        throw response.errorString;
    }
}

export const resign = async (organizationId: number) => {
    let username: string | null = sessionStorage.getItem("username");
    let token: string | null = sessionStorage.getItem("token");

    if(username === null || token === null) {
        throw new Error("Error");
    }
    
    let url = `${server}/resign?orgId=${organizationId}?actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };

    let res = await axios.delete(url, config);
    const response: APIResponse<number> = await res.data;
    
    if(response.error){
        throw response.errorString;
    }
}

export const removeManager = async (organizationId: number, managerToRemove: string) => {
    let username: string | null = sessionStorage.getItem("username");
    let token: string | null = sessionStorage.getItem("token");

    if(username === null || token === null) {
        throw new Error("Error");
    }
    
    let url = `${server}/resign?orgId=${organizationId}?actor=${username}?managerToRemove=${managerToRemove}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };

    let res = await axios.delete(url, config);
    const response: APIResponse<number> = await res.data;
    
    if(response.error){
        throw response.errorString;
    }
}

export const setFounder = async (organizationId: number, newFounder: string) => {
    let username: string | null = sessionStorage.getItem("username");
    let token: string | null = sessionStorage.getItem("token");

    if(username === null || token === null) {
        throw new Error("Error");
    }
    
    let url = `${server}/setFounder?newFounder=${newFounder}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };

    const request = {
        id: organizationId, 
        actor: username,
    }
    let res = await axios.put(url, request, config);
    const response: APIResponse<number> = await res.data;
    
    if(response.error){
        throw response.errorString;
    }
}

export const getUserRequests = async (): Promise<RequestModel[]> => {
    let username: string | null = sessionStorage.getItem("username");
    let token: string | null = sessionStorage.getItem("token");
    
    let url = `${server}/getUserRequests?actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    let res = await axios.get(url, config);
    const response: APIResponse<RequestModel[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getOrganization = async (organizationId: number): Promise<OrganizationModel> => {
    let username: string | null = sessionStorage.getItem("username");
    let token: string | null = sessionStorage.getItem("token");
    
    let url = `${server}/getOrganization?orgId=${organizationId}?actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    let res = await axios.get(url, config);
    const response: APIResponse<OrganizationModel> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getAllOrganizations = async (): Promise<OrganizationModel[]> => {
    let username: string | null = sessionStorage.getItem("username");
    let token: string | null = sessionStorage.getItem("token");
    
    let url = `${server}/getAllOrganizations?actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    let res = await axios.get(url, config);
    const response: APIResponse<OrganizationModel[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const isManager = async (organizationId: number): Promise<Boolean> => {
    let username: string | null = sessionStorage.getItem("username");
    let token: string | null = sessionStorage.getItem("token");
    
    let url = `${server}/isManager?orgId=${organizationId}?actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    let res = await axios.get(url, config);
    const response: APIResponse<Boolean> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

