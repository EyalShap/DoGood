import axios from "axios";
import { host } from "./general";
import APIResponse from "../models/APIResponse";
import User from "../models/UserModel";
import ApprovedHours from "../models/ApprovedHoursModel";

const server = `http://${host}/api/users`;

export const login = async (username: string, password: string): Promise<string> => {
    const body = {
        username: username,
        password: password
    };
    console.log(body);
    let res = await axios.post(`${server}/login`, body);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const logout = async (): Promise<string> => {
    const config = {
        headers: { Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    let res = await axios.post(`${server}/logout`, {}, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const register = async (username: string, password: string, name : string, email:string, phone:string, birthDate:string ): Promise<string> => {
    const body = {
        username: username,
        password: password,
        name: name,
        email: email,
        phone: phone,
        birthDate: birthDate
    };
    let res = await axios.post(`${server}/register`, body);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getIsAdmin = async (username: string): Promise<boolean> => {
    let res = await axios.get(`${server}/isAdmin?username=${username}`);
    const response: APIResponse<boolean> = await res.data;
    if (response.error) {
        throw response.errorString;
    }
    return response.data;
}

export const getUserApprovedHours = async (username: string): Promise<ApprovedHours[]> => {
    const config = {
        headers: { Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    }
    let res = await axios.get(`${server}/getUserApprovedHours?username=${username}`, config);
    const response: APIResponse<ApprovedHours[]> = await res.data;
    return response.data;
}


export const getUserByUsername = async(username: string): Promise<User> => {
    console.log(username);
    let res = await axios.get(`${server}/getUserByUsername?username=${username}`);
    const response: APIResponse<User> = await res.data;
    if (response.error) {
        throw response.errorString;
    }
    return response.data;
}

export const getUserByToken = async (): Promise<User> => {
    const config = {
        headers: { Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    }
    let res = await axios.get(`${server}/getUserByToken`, config);
    const response: APIResponse<User> = await res.data;
    if (response.error) {
        throw response.errorString;
    }
     console.log(response.data);
    return response.data;
}

export const updateUserFields = async (
    username: string,
    password: string|null,
    emails: string[],
    name: string,
    phone: string
): Promise<string> => {
    const config = {
        headers: { Authorization: `Bearer ${sessionStorage.getItem('token')}` },
    };
    const body = { password, name, emails, phone };
    const res = await axios.patch(`${server}/updateUserFields`, body, { ...config, params: { username } });
    const response: APIResponse<string> = res.data;
    if (response.error) {
        throw response.errorString;
    }
    return response.data;
};


export const updateUserSkills = async (username:string, skills: string[]): Promise<string> => {
    const config = {
        headers: { Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    let res = await axios.patch(`${server}/updateUserSkills`, skills, { ...config, params: { username } });
    const response: APIResponse<string> = await res.data;
    if (response.error) {
        throw response.errorString;
    }
    return response.data;
}

export const updateUserPreferences = async (username: string, preferences: string[]): Promise<string> => {
    const config = {
        headers: { Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    let res = await axios.patch(`${server}/updateUserPreferences`, preferences, { ...config, params: { username }});
    const response: APIResponse<string> = await res.data;
    if (response.error) {
        throw response.errorString;
    }
    return response.data;
}

