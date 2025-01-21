import axios from "axios";
import VolunteeringModel, { VolunteersToGroup } from "../models/VolunteeringModel";
import APIResponse from "../models/APIResponse";
import ScheduleAppointment from "../models/ScheduleAppointment";
import HourApprovalRequest from "../models/HourApprovalRequest";
import JoinRequest from "../models/JoinRequest";
import { host } from "./general";
import ScheduleRange from "../models/ScheduleRange";
import Location from "../models/Location";

const server = host;

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

export const getVolunteerAvailableRanges = async (volunteeringId: number): Promise<ScheduleRange[]> => {
    const config = {
        headers: { Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    let res = await axios.get(`http://${server}/api/volunteering/getVolunteerAvailableRanges?volunteeringId=${volunteeringId}&userId=${sessionStorage.getItem('username')}`, config);
    const response: APIResponse<ScheduleRange[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getVolunteeringLocationGroupRanges = async (volunteeringId: number, groupId: number, locId: number): Promise<ScheduleRange[]> => {
    const config = {
        headers: { Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    let res = await axios.get(`http://${server}/api/volunteering/getVolunteeringLocationGroupRanges?volunteeringId=${volunteeringId}&userId=${sessionStorage.getItem('username')}&groupId=${groupId}&locId=${locId}`, config);
    const response: APIResponse<ScheduleRange[]> = await res.data;
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

export const userHasSettingsPermission = async (volunteeringId: number): Promise<boolean> => {
    const config = {
        headers: { Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    let res = await axios.get(`http://${server}/api/volunteering/userHasSettingsPermission?volunteeringId=${volunteeringId}&userId=${sessionStorage.getItem('username')}`, config);
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

export const getUserAssignedLocation = async (volunteeringId: number): Promise<number> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    let res = await axios.get(`http://${server}/api/volunteering/getUserAssignedLocation?userId=${sessionStorage.getItem('username')}&volunteeringId=${volunteeringId}`, config);
    const response: APIResponse<number> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getVolunteerGroup = async (volunteeringId: number): Promise<number> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    let res = await axios.get(`http://${server}/api/volunteering/getVolunteerGroup?userId=${sessionStorage.getItem('username')}&volunteeringId=${volunteeringId}`, config);
    const response: APIResponse<number> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getVolunteeringLocations = async (volunteeringId: number): Promise<Location[]> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    let res = await axios.get(`http://${server}/api/volunteering/getVolunteeringLocations?userId=${sessionStorage.getItem('username')}&volunteeringId=${volunteeringId}`, config);
    const response: APIResponse<Location[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getGroupLocations = async (volunteeringId: number, groupId: number): Promise<Location[]> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    let res = await axios.get(`http://${server}/api/volunteering/getGroupLocations?userId=${sessionStorage.getItem('username')}&volunteeringId=${volunteeringId}&groupId=${groupId}`, config);
    const response: APIResponse<Location[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getVolunteeringGroups = async (volunteeringId: number): Promise<number[]> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    let res = await axios.get(`http://${server}/api/volunteering/getVolunteeringGroups?userId=${sessionStorage.getItem('username')}&volunteeringId=${volunteeringId}`, config);
    const response: APIResponse<number[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getUserAssignedLocationData = async (volunteeringId: number): Promise<Location> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    let res = await axios.get(`http://${server}/api/volunteering/getUserAssignedLocationData?userId=${sessionStorage.getItem('username')}&volunteeringId=${volunteeringId}`, config);
    const response: APIResponse<Location> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const makeAppointment = async (volunteeringId: number, rangeId:number, startHour: number, startMinute: number, endHour: number, endMinute: number, oneTime: string | null, weekDays: boolean[] | null): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    const request = {
        volunteeringId: volunteeringId,
        groupId: await getVolunteerGroup(volunteeringId),
        locId: await getUserAssignedLocation(volunteeringId),
        rangeId: rangeId,
        startHour: startHour,
        startMinute: startMinute,
        endHour: endHour,
        endMinute: endMinute,
        weekdays: weekDays,
        oneTime: oneTime
    }
    let res = await axios.post(`http://${server}/api/volunteering/makeAppointment?userId=${sessionStorage.getItem('username')}`, request, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const addScheduleRangeToGroup = async (volunteeringId: number, groupId: number, locId: number, minimumAppointmentMinutes: number, maximumAppointmentMinutes: number, startHour: number, startMinute: number, endHour: number, endMinute: number, oneTime: string | null, weekDays: boolean[] | null): Promise<number> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    const request = {
        volunteeringId: volunteeringId,
        groupId: groupId,
        locId: locId,
        startHour: startHour,
        startMinute: startMinute,
        endHour: endHour,
        endMinute: endMinute,
        minimumMinutes: minimumAppointmentMinutes,
        maximumMinutes: maximumAppointmentMinutes,
        weekDays: weekDays,
        oneTime: oneTime
    }
    let res = await axios.post(`http://${server}/api/volunteering/addScheduleRangeToGroup?userId=${sessionStorage.getItem('username')}`, request, config);
    const response: APIResponse<number> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const addRestrictionToRange = async (volunteeringId: number, groupId: number, locId: number, rangeId: number, startHour: number, startMinute: number, endHour: number, endMinute: number, amount: number): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    const request = {
        volunteeringId: volunteeringId,
        groupId: groupId,
        locId: locId,
        rangeId: rangeId,
        startHour: startHour,
        startMinute: startMinute,
        endHour: endHour,
        endMinute: endMinute,
        amount: amount
    }
    let res = await axios.post(`http://${server}/api/volunteering/addRestrictionToRange?userId=${sessionStorage.getItem('username')}`, request, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const removeRestrictionFromRange = async (volunteeringId: number, groupId: number, locId: number, rangeId: number, startHour: number, startMinute: number): Promise<string> => {
    const params = {
        userId: sessionStorage.getItem('username'),
        volunteeringId: volunteeringId,
        groupId: groupId,
        locId: locId,
        rangeId: rangeId,
        startHour: startHour,
        startMinute: startMinute,
    }
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${sessionStorage.getItem('token')}` },
        params: params
    };
    let res = await axios.delete(`http://${server}/api/volunteering/removeRestrictionFromRange`, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const createNewGroup = async (volunteeringId: number): Promise<number> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    let res = await axios.post(`http://${server}/api/volunteering/createNewGroup?userId=${sessionStorage.getItem('username')}`, volunteeringId, config);
    const response: APIResponse<number> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const removeGroup = async (volunteeringId: number, groupId: number): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    let res = await axios.delete(`http://${server}/api/volunteering/removeGroup?userId=${sessionStorage.getItem('username')}&volunteeringId=${volunteeringId}&groupId=${groupId}`, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const removeLocation = async (volunteeringId: number, locId: number): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    let res = await axios.delete(`http://${server}/api/volunteering/removeLocation?userId=${sessionStorage.getItem('username')}&volunteeringId=${volunteeringId}&locId=${locId}`, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const removeRange = async (volunteeringId: number, rangeId: number): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    let res = await axios.delete(`http://${server}/api/volunteering/removeRange?userId=${sessionStorage.getItem('username')}&volunteeringId=${volunteeringId}&rangeId=${rangeId}`, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const moveVolunteerGroup = async (volunteeringId: number, groupId: number, volunteerId: string): Promise<string> => {
    const body = {
        volunteerId: volunteerId,
        volunteeringId: volunteeringId,
        toId: groupId,
    }
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${sessionStorage.getItem('token')}` },
    };
    let res = await axios.patch(`http://${server}/api/volunteering/moveVolunteerGroup?userId=${sessionStorage.getItem('username')}`, body, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const assignVolunteerToLocation = async (volunteeringId: number, locId: number): Promise<string> => {
    const body = {
        volunteerId: sessionStorage.getItem('username'),
        volunteeringId: volunteeringId,
        toId: locId,
    }
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${sessionStorage.getItem('token')}` },
    };
    let res = await axios.patch(`http://${server}/api/volunteering/assignVolunteerToLocation?userId=${sessionStorage.getItem('username')}`, body, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const addVolunteeringLocation = async (volunteeringId: number, name: string, city: string, street: string, address: string): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${sessionStorage.getItem('token')}` }
    };
    const request = {
        name: name,
        address: {
            city: city,
            street: street,
            address: address
        }
    }
    let res = await axios.post(`http://${server}/api/volunteering/addVolunteeringLocation?userId=${sessionStorage.getItem('username')}&volunteeringId=${volunteeringId}`, request, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}