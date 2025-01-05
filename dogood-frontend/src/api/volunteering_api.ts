import axios from "axios";
import VolunteeringModel, { VolunteersToGroup } from "../models/VolunteeringModel";
import APIResponse from "../models/APIResponse";
import ScheduleAppointment from "../models/ScheduleAppointment";
import HourApprovalRequest from "../models/HourApprovalRequest";

const loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

const server = '192.168.1.68:8080';

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

export const getVolunteerAppointments = async (volunteeringId: number): Promise<ScheduleAppointment[]> => {
    const config = {
        headers: { Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    let res = await axios.get(`http://${server}/api/volunteering/getVolunteerAppointments?volunteeringId=${volunteeringId}&userId=${sessionStorage.getItem('username')}`, config);
    const response: APIResponse<ScheduleAppointment[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getVolunteeringHourRequests = async (volunteeringId: number): Promise<HourApprovalRequest[]> => {
    const config = {
        headers: { Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    let res = await axios.get(`http://${server}/api/volunteering/getVolunteeringHourRequests?volunteeringId=${volunteeringId}&userId=${sessionStorage.getItem('username')}`, config);
    const response: APIResponse<HourApprovalRequest[]> = await res.data;
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
    return response.data;
}

export const getCode = async (volunteeringId: number, constant: boolean): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    let res = await axios.post(`http://${server}/api/volunteering/makeVolunteeringCode?userId=${sessionStorage.getItem('username')}`, {
        volunteeringId: volunteeringId,
        constant: constant
    }, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const scanCode = async (code: string): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    let res = await axios.post(`http://${server}/api/volunteering/scanCode?userId=${sessionStorage.getItem('username')}`, code, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}