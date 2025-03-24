import APIResponse from "../models/APIResponse";
import OrganizationModel from "../models/OrganizationModel";
import axios from "axios";
import RequestModel from "../models/RequestModel";
import VolunteeringModel from "../models/VolunteeringModel";
import { host } from "./general";

const server: string = `${host}/api/organizations`;

export const createOrganization = async (name: string, description: string, email: string, phoneNumber: string): Promise<number> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if (username === null) {
        throw new Error("Error");
    }

    let url = `${server}/createOrganization`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    const request = {
        name: name,
        description: description,
        email: email,
        phoneNumber: phoneNumber,
        actor: username
    }
    console.log(request)

    let res = await axios.post(url, request, config);
    const response: APIResponse<number> = await res.data;
    if (response.error) {
        throw response.errorString;
    }
    let organizationId = response.data;
    return organizationId;
}

export const removeOrganization = async (organizationId: number) => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if (username === null) {
        throw new Error("Error");
    }

    let url = `${server}/removeOrganization?orgId=${organizationId}&actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    let res = await axios.delete(url, config);
    const response: APIResponse<number> = await res.data;
    if (response.error) {
        throw response.errorString;
    }
}

export const editOrganization = async (organizationId: number, newName: string, newDescription: string, newEmail: string, newPhoneNumber: string) => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if (username === null) {
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
    if (response.error) {
        throw response.errorString;
    }
}

export const createVolunteering = async (organizationId: number, volunteeringName: string, volunteeringDescription: string): Promise<number> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if (username === null) {
        throw new Error("Error");
    }

    let url = `${server}/createVolunteering`;
    console.log(url)
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
    console.log(res);
    const response: APIResponse<number> = await res.data;

    if (response.error) {
        throw response.errorString;
    }
    return response.data;
}

/*export const removeVolunteering = async (organizationId: number, volunteeringId: number) => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }
    
    let url = `${server}/removeVolunteering?organizationId=${organizationId}&volunteeringId=${volunteeringId}&actor=${username}`;
    console.log(url)
    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };

    let res = await axios.delete(url, config);
    console.log(res);
    const response: APIResponse<Boolean> = await res.data;

    if(response.error){
        throw response.errorString;
    }
}*/

export const sendAssignManagerRequest = async (organizationId: number, newManager: string) => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if (username === null) {
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

    if (response.error) {
        throw response.errorString;
    }
}

export const handleAssignManagerRequest = async (organizationId: number, approved: boolean) => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if (username === null) {
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

    if (response.error) {
        throw response.errorString;
    }
}

export const resign = async (organizationId: number) => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if (username === null) {
        throw new Error("Error");
    }

    let url = `${server}/resign?orgId=${organizationId}&actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };

    let res = await axios.delete(url, config);
    const response: APIResponse<number> = await res.data;

    if (response.error) {
        throw response.errorString;
    }
}

export const removeManager = async (organizationId: number, managerToRemove: string) => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if (username === null) {
        throw new Error("Error");
    }

    let url = `${server}/removeManager?orgId=${organizationId}&actor=${username}&managerToRemove=${managerToRemove}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };

    let res = await axios.delete(url, config);
    const response: APIResponse<number> = await res.data;

    if (response.error) {
        throw response.errorString;
    }
}

export const setFounder = async (organizationId: number, newFounder: string) => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if (username === null) {
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

    if (response.error) {
        throw response.errorString;
    }
}

export const getUserRequests = async (): Promise<RequestModel[]> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    let url = `${server}/getUserAssignManagerRequests?actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    let res = await axios.get(url, config);
    const response: APIResponse<RequestModel[]> = await res.data;
    if (response.error) {
        throw response.errorString;
    }
    return response.data;
}

export const getOrganization = async (organizationId: number): Promise<OrganizationModel> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    let url = `${server}/getOrganization?orgId=${organizationId}&actor=${username}`;

    console.log(url)

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    let res = await axios.get(url, config);
    const response: APIResponse<OrganizationModel> = await res.data;
    if (response.error) {
        throw response.errorString;
    }
    return response.data;
}

export const getAllOrganizations = async (): Promise<OrganizationModel[]> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    let url = `${server}/getAllOrganizations?actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    let res = await axios.get(url, config);
    const response: APIResponse<OrganizationModel[]> = await res.data;
    if (response.error) {
        throw response.errorString;
    }
    return response.data;
}

export const getIsManager = async (organizationId: number): Promise<boolean> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    let url = `${server}/isManager?orgId=${organizationId}&actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    let res = await axios.get(url, config);
    const response: APIResponse<boolean> = await res.data;
    if (response.error) {
        throw response.errorString;
    }
    return response.data;
}

export const getOrganizationVolunteerings = async (organizationId: number): Promise<VolunteeringModel[]> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    let url = `${server}/getOrganizationVolunteerings?orgId=${organizationId}&actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    let res = await axios.get(url, config);
    const response: APIResponse<VolunteeringModel[]> = await res.data;
    if (response.error) {
        throw response.errorString;
    }
    return response.data;
}

export const getOrganizationName = async (organizationId: number): Promise<string> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    let url = `${server}/getOrganizationName?orgId=${organizationId}&actor=${username}`;
    console.log("Request URL:", url); // Log the URL to check for issues

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    let res = await axios.get(url, config);
    const response: APIResponse<string> = await res.data;
    if (response.error) {
        throw response.errorString;
    }
    return response.data;
}

export const getUserVolunteerings = async (organizationId: number): Promise<number[]> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    let url = `${server}/getUserVolunteerings?organizationId=${organizationId}&actor=${username}`;
    console.log("Request URL:", url); // Log the URL to check for issues

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    let res = await axios.get(url, config);
    const response: APIResponse<number[]> = await res.data;
    if (response.error) {
        throw response.errorString;
    }
    return response.data;
}

export const addImageToOrganization = async (orgId: number, image: string) => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if (username === null) {
        throw new Error("Error");
    }

    let url = `${server}/addImageToOrganization?organizationId=${orgId}&actor=${username}`;

    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    console.log(config);

    let res = await axios.post(url, image.replace(/"/g, ""), config);
    const response: APIResponse<boolean> = await res.data;
    if (response.error) {
        throw response.errorString;
    }
}

export const removeImageFromOrganization = async (orgId: number, image: string) => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if (username === null) {
        throw new Error("Error");
    }

    let url = `${server}/removeImageFromOrganization?organizationId=${orgId}&image=${image.replace(/"/g, "")}&actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };

    let res = await axios.delete(url, config);
    const response: APIResponse<boolean> = await res.data;
    if (response.error) {
        throw response.errorString;
    }
}

