import axios from "axios";
import { host } from "./general";
import APIResponse from "../models/APIResponse";

const server = `http://${host}/api/users`;

type User = {
    username: string,
    emails: string[],
    name: string,
    phone: string,
    birthDate: string,
    preferredCategories: string[],
    skills: string[],
    isStudent: boolean,
    isAdmin: boolean
}

export const login = async (username: string, password: string): Promise<string> => {
    const body = {
        username: username,
        password: password
    };
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

export const isAdmin = async (username: string): Promise<string> => {
    const body = {
        username: username
    };
    let res = await axios.post(`${server}/isAdmin`, body);
    const response: APIResponse<string> = await res.data;
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
    // console.log(response.data);
    return response.data;
}

export const updateUserFields = async (
    username: string,
    password: string,
    emails: string[],
    name: string,
    phone: string
): Promise<string> => {
    const config = {
        headers: { Authorization: `Bearer ${sessionStorage.getItem('token')}` },
    };
    const body = { password, emails, name, phone };
    const res = await axios.patch(`${server}/updateUserFields`, body, { ...config, params: { username } });
    const response: APIResponse<string> = res.data;
    if (response.error) {
        throw response.errorString;
    }
    return response.data;
};

export const updateUserSkills = async (skills: string[]): Promise<string> => {
    const config = {
        headers: { Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    const body = {
        skills: skills
    };
    let res = await axios.post(`${server}/updateUserSkils`, body, config);
    const response: APIResponse<string> = await res.data;
    if (response.error) {
        throw response.errorString;
    }
    return response.data;
}

export const updateUserPreferences = async (preferences: string[]): Promise<string> => {
    const config = {
        headers: { Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    const body = {
        preferences: preferences
    };
    let res = await axios.post(`${server}/updateUserPreferences`, body, config);
    const response: APIResponse<string> = await res.data;
    if (response.error) {
        throw response.errorString;
    }
    return response.data;
}

