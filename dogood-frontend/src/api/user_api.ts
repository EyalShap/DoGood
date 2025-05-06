import axios from "axios";
import { host } from "./general";
import APIResponse from "../models/APIResponse";
import User from "../models/UserModel";
import Notification from "../models/Notification";
import ApprovedHours from "../models/ApprovedHoursModel";
import {Leaderboard} from "../models/Leaderboard";
import {string} from "yup";

const server = `${host}/api/users`;

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
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.post(`${server}/logout`, {}, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const register = async (
    username: string,
    password: string,
    name: string,
    email: string,
    phone: string,
    birthDate: string,
    profilePicUrl: string
): Promise<string> => {
    const body = {
        username: username,
        password: password,
        name: name,
        email: email,
        phone: phone,
        birthDate: birthDate,
        profilePicUrl: profilePicUrl
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
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
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
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
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
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
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
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
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
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.patch(`${server}/updateUserPreferences`, preferences, { ...config, params: { username }});
    const response: APIResponse<string> = await res.data;
    if (response.error) {
        throw response.errorString;
    }
    return response.data;
}

export const leaderboard = async (): Promise<Leaderboard> => {
    const config = {
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    }
    let res = await axios.get(`${server}/leaderboard?username=${localStorage.getItem('username')}`, config);
    const response: APIResponse<Leaderboard> = await res.data;
    return response.data;
}

export const setLeaderboard = async (isLeaderboard: boolean) => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if (username === null) {
        throw new Error("Error");
    }

    let url = `${server}/setLeaderboard?username=${username}&leaderboard=${isLeaderboard}`;
    console.log(url);
    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    let res = await axios.put(url, {}, config);
    const response: APIResponse<boolean> = await res.data;
    if (response.error) {
        throw response.errorString;
    }
}

export const banUser = async (toBan: string) => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/banUser?actor=${username}&username=${toBan}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };

    const res = await axios.patch(url, {}, config);
    const response: APIResponse<boolean> = await res.data;
    if(response.error){
        throw response.errorString;
    }
}

export const getAllUserEmails = async (): Promise<string[]> => {
    const config = {
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    }
    let res = await axios.get(`${server}/getAllUserEmails?actor=${localStorage.getItem('username')}`, config);
    const response: APIResponse<string[]> = await res.data;
    return response.data;
}

export const getUserNotifications = async (): Promise<Notification[]> => {
    const config = {
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    }
    let res = await axios.get(`${server}/getUserNotifications?actor=${localStorage.getItem('username')}`, config);
    const response: APIResponse<Notification[]> = await res.data;
    return response.data;
}

export const readNewUserNotifications = async (): Promise<Notification[]> => {
    const config = {
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    }
    let res = await axios.patch(`${server}/readNewUserNotifications?actor=${localStorage.getItem('username')}`, {}, config);
    const response: APIResponse<Notification[]> = await res.data;
    return response.data;
}

export const getNewUserNotificationsAmount = async (): Promise<number> => {
    const config = {
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    }
    let res = await axios.get(`${server}/getNewUserNotificationsAmount?actor=${localStorage.getItem('username')}`, config);
    const response: APIResponse<number> = await res.data;
    return response.data;
}

export const uploadCV = async (cvPdf: File) => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if (username === null) {
        throw new Error("Error");
    }

    let url = `${server}/uploadCV?username=${username}`;
    
    const config = {
        headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "multipart/form-data", // Required for file uploads
        },
    };
    
    const formData = new FormData();
    formData.append("cvPdf", cvPdf);

    let res = await axios.put(url, formData, config);
    const response: APIResponse<boolean> = await res.data;
    if (response.error) {
        throw response.errorString;
    }
}

export const downloadCV = async () : Promise<Blob> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if (username === null) {
        throw new Error("Error");
    }

    let url = `${server}/getCV?actor=${username}`;
    
    const config = {
        headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/pdf",
        },
        responseType: "blob" as const,
    };
    
    let res = await axios.get(url, config);
    /*console.log(res.data); // Log the response data
    const response: APIResponse<Blob> = res.data;
    if (response.error) {
        throw response.errorString;
    }*/
    return res.data;
}

export const removeCV = async () => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if (username === null) {
        throw new Error("Error");
    }

    let url = `${server}/removeCV?username=${username}`;
    console.log(url);
    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    let res = await axios.put(url, {}, config);
    const response: APIResponse<boolean> = await res.data;
    if (response.error) {
        throw response.errorString;
    }
}

export const generateSkillsAndPreferences = async () => {
    const config = {
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    }
    let res = await axios.patch(`${server}/generateSkillsAndPreferences?username=${localStorage.getItem('username')}`, {}, config);
    const response: APIResponse<boolean> = await res.data;
    if (response.error) {
        throw response.errorString;
    }
}

export const updateProfilePicture = async (username: string, profilePicUrl: string): Promise<string> => {
    const config = {
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
    };
    const body = { profilePicUrl }; // Send the new URL in the body
    // Assuming a new PATCH endpoint /updateProfilePicture on the backend
    // It might take the username from the token or as a param, adjust as needed.
    // Here, we pass username as a query param for consistency with other update methods.
    const res = await axios.patch(`${server}/updateProfilePicture`, body, { ...config, params: { username } });
    const response: APIResponse<string> = res.data;
    if (response.error) {
        throw response.errorString;
    }
    return response.data; // Assuming backend sends back a success message or the URL itself
};

export const registerFcmToken = async (username: string, fcmToken: string): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.post(`${server}/registerFcmToken?username=${localStorage.getItem('username')}`, fcmToken,config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
};