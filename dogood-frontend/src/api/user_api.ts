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

// VERIFICATION START
export const verifyEmailCode = async (username: string, code: string): Promise<string> => {
    const body = {
        username: username,
        code: code
    };
    // No token needed for verification
    let res = await axios.post(`${server}/verify-email`, body);
    const response: APIResponse<string> = await res.data; 
    if(response.error){
        // The backend now returns the error message directly in response.errorString if response.error is true
        // For "Invalid or expired verification code." it will be in response.errorString
        // For "Email verified successfully." or "Email already verified." response.error will be false.
        throw response.errorString;
    }
    // If no error, data should contain the success message like "Email verified successfully."
    return response.data; 
}
// VERIFICATION END

// FORGOT_PASSWORD START
export const forgotPassword = async (email: string): Promise<string> => {
    const body = { email };
    // Backend endpoint: /api/users/forgot-password
    let res = await axios.post(`${server}/forgot-password`, body);
    const response: APIResponse<string> = await res.data;
    // Backend always returns a generic success message in response.data if no internal server error
    if(response.error){ // This would be for unexpected server errors, not "email not found"
        throw response.errorString;
    }
    return response.data; // This will be "If your email address is in our system..."
}

// This function might be optional for the frontend if ResetPasswordRequest includes the code
// and the /reset-password endpoint re-validates it.
// However, the backend provides it, so we include it.
export const verifyPasswordResetCode = async (username: string, code: string): Promise<string> => {
    const body = {
        username: username,
        code: code
    };
    // Backend endpoint: /api/users/verify-reset-password
    let res = await axios.post(`${server}/verify-reset-password`, body);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString; // e.g., "Invalid or expired verification code."
    }
    return response.data; // e.g., "Verification code is valid."
}

export const resetPassword = async (username: string, code: string, newPassword: string): Promise<string> => {
    const body = {
        username: username,
        newPassword: newPassword,
        code: code // Code is included for re-validation
    };
    // Backend endpoint: /api/users/reset-password
    let res = await axios.post(`${server}/reset-password`, body);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString; // e.g., "Invalid code", "User not found"
    }
    return response.data; // e.g., "Password reset successfully."
}
// FORGOT_PASSWORD END

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