import APIResponse from "../models/APIResponse";
import OrganizationModel from "../models/OrganizationModel";
import axios from "axios";
import RequestModel from "../models/RequestModel";
import VolunteeringModel from "../models/VolunteeringModel";
import ReportModel from "../models/ReportModel";

const server: string = 'http://127.0.0.1:8080/api/reports';

export const createReport = async (reportedPostId: number, description: string): Promise<number> => {
    let username: string | null = sessionStorage.getItem("username");
    let token: string | null = sessionStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/createReport`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    const request = {
        actor: username,
        reportedPostId: reportedPostId,
        description: description,
    }

    let res = await axios.post(url, request, config);
    const response: APIResponse<number> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    let organizationId = response.data;
    return organizationId;
}

export const removeReport = async (reportId: number) => {
    let username: string | null = sessionStorage.getItem("username");
    let token: string | null = sessionStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }
    
    let url = `${server}/removeReport?actor=${username}&reportId=${reportId}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    let res = await axios.delete(url, config);
    const response: APIResponse<number> = await res.data;
    if(response.error){
        throw response.errorString;
    }
}

export const getAllReports = async (): Promise<ReportModel[]> => {
    let username: string | null = sessionStorage.getItem("username");
    let token: string | null = sessionStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }
    
    let url = `${server}/getAllReports?actor=${username}`;

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