import APIResponse from "../models/APIResponse";
import axios from "axios";
import ReportModel from "../models/ReportModel";
import { host } from "./general";

const server: string = `${host}/api/reports`;

export const createReport = async (reportedId: number, description: string, method: string): Promise<ReportModel> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/${method}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    const request = {
        actor: username,
        reportedId: reportedId,
        description: description,
    }

    let res = await axios.post(url, request, config);
    const response: APIResponse<ReportModel> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const createVolunteeringPostReport = async (reportedId: number, description: string): Promise<ReportModel> => {
    return createReport(reportedId, description, "createVolunteeringPostReport");
}

export const createVolunteerPostReport = async (reportedId: number, description: string): Promise<ReportModel> => {
    return createReport(reportedId, description, "createVolunteerPostReport");
}

export const createVolunteeringReport = async (reportedId: number, description: string): Promise<ReportModel> => {
    return createReport(reportedId, description, "createVolunteeringReport");
}

export const createOrganizationReport = async (reportedId: number, description: string): Promise<ReportModel> => {
    return createReport(reportedId, description, "createOrganizationReport");
}

export const createUserReport = async (reportedId: string, description: string): Promise<ReportModel> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/createUserReport?actor=${username}&reportedId=${reportedId}&description=${description}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };


    let res = await axios.post(url, {}, config);
    const response: APIResponse<ReportModel> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const removeVolunteeringPostReport = async (reportingUser : string, date: string, reportedId : number) => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }
    
    let url = `${server}/removeVolunteeringPostReport?reportingUser=${reportingUser}&date=${date}&reportedId=${reportedId}&actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    let res = await axios.delete(url, config);
    const response: APIResponse<number> = await res.data;
    if(response.error){
        throw response.errorString;
    }
}

export const removeVolunteerPostReport = async (reportingUser : string, date: string, reportedId : number) => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }
    
    let url = `${server}/removeVolunteerPostReport?reportingUser=${reportingUser}&date=${date}&reportedId=${reportedId}&actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    let res = await axios.delete(url, config);
    const response: APIResponse<number> = await res.data;
    if(response.error){
        throw response.errorString;
    }
}

export const removeVolunteeringReport = async (reportingUser : string, date: string, reportedId : number) => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }
    
    let url = `${server}/removeVolunteeringReport?reportingUser=${reportingUser}&date=${date}&reportedId=${reportedId}&actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    let res = await axios.delete(url, config);
    const response: APIResponse<number> = await res.data;
    if(response.error){
        throw response.errorString;
    }
}

export const removeOrganizationReport = async (reportingUser : string, date: string, reportedId : number) => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }
    
    let url = `${server}/removeOrganizationReport?reportingUser=${reportingUser}&date=${date}&reportedId=${reportedId}&actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    let res = await axios.delete(url, config);
    const response: APIResponse<number> = await res.data;
    if(response.error){
        throw response.errorString;
    }
}

export const removeUserReport = async (reportingUser : string, date: string, reportedId : string) => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }
    
    let url = `${server}/removeUserReport?reportingUser=${reportingUser}&date=${date}&reportedId=${reportedId}&actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    let res = await axios.delete(url, config);
    const response: APIResponse<number> = await res.data;
    if(response.error){
        throw response.errorString;
    }
}

export const getAllReports = async (method: string): Promise<ReportModel[]> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }
    
    let url = `${server}/${method}?actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    let res = await axios.get(url, config);
    const response: APIResponse<ReportModel[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    let reports: ReportModel[] = response.data;
    return reports;
}

export const getAllVolunteeringPostReports = async (): Promise<ReportModel[]> => {
    return getAllReports("getAllVolunteeringPostReports");
}

export const getAllVolunteerPostReports = async (): Promise<ReportModel[]> => {
    return getAllReports("getAllVolunteerPostReports");
}

export const getAllVolunteeringReports = async (): Promise<ReportModel[]> => {
    return getAllReports("getAllVolunteeringReports");
}

export const getAllUserReports = async (): Promise<ReportModel[]> => {
    return getAllReports("getAllUserReports");
}

export const getAllOrganizationReports = async (): Promise<ReportModel[]> => {
    return getAllReports("getAllOrganizationReports");
}

export const banEmail = async (email: string) => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/banEmail?actor=${username}`;

    const config = {
        headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json"
        }
    };
    
    let res = await axios.post(url, JSON.stringify(email), config);
    const response: APIResponse<boolean> = await res.data;
    if(response.error){
        throw response.errorString;
    }
}

export const unbanEmail = async (email: string) => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/unbanEmail?actor=${username}&email=${email}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };

    let res = await axios.delete(url, config);
    const response: APIResponse<boolean> = await res.data;
    if(response.error){
        throw response.errorString;
    }
}

export const getBannedEmails = async (): Promise<string[]> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }
    
    let url = `${server}/getBannedEmails?actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    let res = await axios.get(url, config);
    const response: APIResponse<string[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    let emails: string[] = response.data.map(email => email.replace(/^"|"$/g, ""));
    return emails;
}