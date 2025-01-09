import axios from "axios";
import VolunteeringModel, { VolunteersToGroup } from "../models/VolunteeringModel";
import APIResponse from "../models/APIResponse";
import ScheduleAppointment from "../models/ScheduleAppointment";
import HourApprovalRequest from "../models/HourApprovalRequest";
import JoinRequest from "../models/JoinRequest";

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

export const getVolunteeringJoinRequests = async (volunteeringId: number): Promise<JoinRequest[]> => {
    const config = {
        headers: { Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    let res = await axios.get(`http://${server}/api/volunteering/getVolunteeringJoinRequests?volunteeringId=${volunteeringId}&userId=${sessionStorage.getItem('username')}`, config);
    const response: APIResponse<JoinRequest[]> = await res.data;
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

export const approveHourRequest = async (volunteeringId: number, volunteerId: string, startDate: string, endDate: string): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    const request = {
        volunteerId: volunteerId,
        startDate: startDate,
        endDate: endDate
    }
    let res = await axios.post(`http://${server}/api/volunteering/approveUserHours?userId=${sessionStorage.getItem('username')}&volunteeringId=${volunteeringId}`, request, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const denyHourRequest = async (volunteeringId: number, volunteerId: string, startDate: string, endDate: string): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    const request = {
        volunteerId: volunteerId,
        startDate: startDate,
        endDate: endDate
    }
    let res = await axios.post(`http://${server}/api/volunteering/denyUserHours?userId=${sessionStorage.getItem('username')}&volunteeringId=${volunteeringId}`, request, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const acceptUserJoinRequest = async (volunteeringId: number, volunteerId: string, groupId: number): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    const request = {
        volunteeringId: volunteeringId,
        requesterId: volunteerId,
        groupId: groupId
    }
    let res = await axios.put(`http://${server}/api/volunteering/acceptUserJoinRequest?userId=${sessionStorage.getItem('username')}`, request, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const denyUserJoinRequest = async (volunteeringId: number, volunteerId: string): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    const request = {
        volunteeringId: volunteeringId,
        requesterId: volunteerId
    }
    let res = await axios.put(`http://${server}/api/volunteering/denyUserJoinRequest?userId=${sessionStorage.getItem('username')}`, request, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}