import axios from "axios";
import VolunteeringModel, { VolunteersToGroup } from "../models/VolunteeringModel";
import APIResponse from "../models/APIResponse";
import ScheduleAppointment from "../models/ScheduleAppointment";
import HourApprovalRequest from "../models/HourApprovalRequest";
import JoinRequest from "../models/JoinRequest";
import { host } from "./general";
import ScheduleRange from "../models/ScheduleRange";
import Location from "../models/Location";
import { ApprovalType, ScanType } from "../models/ScanTypes";

const server = host;

export const getVolunteering = async (volunteeringId: number): Promise<VolunteeringModel> => {
    const config = {
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.get(`${server}/api/volunteering/getVolunteering?volunteeringId=${volunteeringId}&userId=${localStorage.getItem('username')}`, config);
    const response: APIResponse<VolunteeringModel> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getVolunteeringsOfUser = async (): Promise<VolunteeringModel[]> => {
    const config = {
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.get(`${server}/api/volunteering/getVolunteeringsOfUser?userId=${localStorage.getItem('username')}`, config);
    const response: APIResponse<VolunteeringModel[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getVolunteeringVolunteers = async (volunteeringId: number): Promise<VolunteersToGroup> => {
    const config = {
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.get(`${server}/api/volunteering/getVolunteeringVolunteers?volunteeringId=${volunteeringId}&userId=${localStorage.getItem('username')}`, config);
    const response: APIResponse<VolunteersToGroup> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getVolunteerAppointments = async (volunteeringId: number): Promise<ScheduleAppointment[]> => {
    const config = {
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.get(`${server}/api/volunteering/getVolunteerAppointments?volunteeringId=${volunteeringId}&userId=${localStorage.getItem('username')}`, config);
    const response: APIResponse<ScheduleAppointment[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getVolunteerAvailableRanges = async (volunteeringId: number): Promise<ScheduleRange[]> => {
    const config = {
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.get(`${server}/api/volunteering/getVolunteerAvailableRanges?volunteeringId=${volunteeringId}&userId=${localStorage.getItem('username')}`, config);
    const response: APIResponse<ScheduleRange[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getVolunteeringLocationGroupRanges = async (volunteeringId: number, groupId: number, locId: number): Promise<ScheduleRange[]> => {
    const config = {
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.get(`${server}/api/volunteering/getVolunteeringLocationGroupRanges?volunteeringId=${volunteeringId}&userId=${localStorage.getItem('username')}&groupId=${groupId}&locId=${locId}`, config);
    const response: APIResponse<ScheduleRange[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getVolunteeringHourRequests = async (volunteeringId: number): Promise<HourApprovalRequest[]> => {
    const config = {
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.get(`${server}/api/volunteering/getVolunteeringHourRequests?volunteeringId=${volunteeringId}&userId=${localStorage.getItem('username')}`, config);
    const response: APIResponse<HourApprovalRequest[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getVolunteeringJoinRequests = async (volunteeringId: number): Promise<JoinRequest[]> => {
    const config = {
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.get(`${server}/api/volunteering/getVolunteeringJoinRequests?volunteeringId=${volunteeringId}&userId=${localStorage.getItem('username')}`, config);
    const response: APIResponse<JoinRequest[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getIsManager = async (organizationId: number): Promise<boolean> => {
    const config = {
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.get(`${server}/api/organizations/isManager?orgId=${organizationId}&actor=${localStorage.getItem('username')}`, config);
    const response: APIResponse<boolean> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const userHasSettingsPermission = async (volunteeringId: number): Promise<boolean> => {
    const config = {
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.get(`${server}/api/volunteering/userHasSettingsPermission?volunteeringId=${volunteeringId}&userId=${localStorage.getItem('username')}`, config);
    const response: APIResponse<boolean> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getCode = async (volunteeringId: number, constant: boolean): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.post(`${server}/api/volunteering/makeVolunteeringCode?userId=${localStorage.getItem('username')}`, {
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
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.post(`${server}/api/volunteering/scanCode?userId=${localStorage.getItem('username')}`, code, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const approveHourRequest = async (volunteeringId: number, volunteerId: string, startDate: string, endDate: string): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    const request = {
        volunteerId: volunteerId,
        startDate: startDate,
        endDate: endDate
    }
    let res = await axios.post(`${server}/api/volunteering/approveUserHours?userId=${localStorage.getItem('username')}&volunteeringId=${volunteeringId}`, request, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const denyHourRequest = async (volunteeringId: number, volunteerId: string, startDate: string, endDate: string): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    const request = {
        volunteerId: volunteerId,
        startDate: startDate,
        endDate: endDate
    }
    let res = await axios.post(`${server}/api/volunteering/denyUserHours?userId=${localStorage.getItem('username')}&volunteeringId=${volunteeringId}`, request, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const acceptUserJoinRequest = async (volunteeringId: number, volunteerId: string, groupId: number): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    const request = {
        volunteeringId: volunteeringId,
        requesterId: volunteerId,
        groupId: groupId
    }
    let res = await axios.put(`${server}/api/volunteering/acceptUserJoinRequest?userId=${localStorage.getItem('username')}`, request, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const denyUserJoinRequest = async (volunteeringId: number, volunteerId: string): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    const request = {
        volunteeringId: volunteeringId,
        requesterId: volunteerId
    }
    let res = await axios.put(`${server}/api/volunteering/denyUserJoinRequest?userId=${localStorage.getItem('username')}`, request, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getUserAssignedLocation = async (volunteeringId: number): Promise<number> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.get(`${server}/api/volunteering/getUserAssignedLocation?userId=${localStorage.getItem('username')}&volunteeringId=${volunteeringId}`, config);
    const response: APIResponse<number> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getVolunteerGroup = async (volunteeringId: number): Promise<number> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.get(`${server}/api/volunteering/getVolunteerGroup?userId=${localStorage.getItem('username')}&volunteeringId=${volunteeringId}`, config);
    const response: APIResponse<number> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getVolunteeringLocations = async (volunteeringId: number): Promise<Location[]> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.get(`${server}/api/volunteering/getVolunteeringLocations?userId=${localStorage.getItem('username')}&volunteeringId=${volunteeringId}`, config);
    const response: APIResponse<Location[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getGroupLocations = async (volunteeringId: number, groupId: number): Promise<Location[]> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.get(`${server}/api/volunteering/getGroupLocations?userId=${localStorage.getItem('username')}&volunteeringId=${volunteeringId}&groupId=${groupId}`, config);
    const response: APIResponse<Location[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getVolunteeringGroups = async (volunteeringId: number): Promise<number[]> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.get(`${server}/api/volunteering/getVolunteeringGroups?userId=${localStorage.getItem('username')}&volunteeringId=${volunteeringId}`, config);
    const response: APIResponse<number[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getUserAssignedLocationData = async (volunteeringId: number): Promise<Location> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.get(`${server}/api/volunteering/getUserAssignedLocationData?userId=${localStorage.getItem('username')}&volunteeringId=${volunteeringId}`, config);
    const response: APIResponse<Location> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const makeAppointment = async (volunteeringId: number, rangeId:number, startHour: number, startMinute: number, endHour: number, endMinute: number, oneTime: string | null, weekDays: boolean[] | null): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
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
    let res = await axios.post(`${server}/api/volunteering/makeAppointment?userId=${localStorage.getItem('username')}`, request, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const cancelAppointment = async (volunteeringId: number, startHour: number, startMinute: number): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.delete(`${server}/api/volunteering/cancelAppointment?userId=${localStorage.getItem('username')}&volunteeringId=${volunteeringId}&startHour=${startHour}&startMinute=${startMinute}`, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const addScheduleRangeToGroup = async (volunteeringId: number, groupId: number, locId: number, minimumAppointmentMinutes: number, maximumAppointmentMinutes: number, startHour: number, startMinute: number, endHour: number, endMinute: number, oneTime: string | null, weekDays: boolean[] | null): Promise<number> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
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
    let res = await axios.post(`${server}/api/volunteering/addScheduleRangeToGroup?userId=${localStorage.getItem('username')}`, request, config);
    const response: APIResponse<number> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const addRestrictionToRange = async (volunteeringId: number, groupId: number, locId: number, rangeId: number, startHour: number, startMinute: number, endHour: number, endMinute: number, amount: number): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
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
    let res = await axios.post(`${server}/api/volunteering/addRestrictionToRange?userId=${localStorage.getItem('username')}`, request, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const removeRestrictionFromRange = async (volunteeringId: number, groupId: number, locId: number, rangeId: number, startHour: number, startMinute: number): Promise<string> => {
    const params = {
        userId: localStorage.getItem('username'),
        volunteeringId: volunteeringId,
        groupId: groupId,
        locId: locId,
        rangeId: rangeId,
        startHour: startHour,
        startMinute: startMinute,
    }
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` },
        params: params
    };
    let res = await axios.delete(`${server}/api/volunteering/removeRestrictionFromRange`, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const createNewGroup = async (volunteeringId: number): Promise<number> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.post(`${server}/api/volunteering/createNewGroup?userId=${localStorage.getItem('username')}`, volunteeringId, config);
    const response: APIResponse<number> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const removeGroup = async (volunteeringId: number, groupId: number): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.delete(`${server}/api/volunteering/removeGroup?userId=${localStorage.getItem('username')}&volunteeringId=${volunteeringId}&groupId=${groupId}`, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const removeLocation = async (volunteeringId: number, locId: number): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.delete(`${server}/api/volunteering/removeLocation?userId=${localStorage.getItem('username')}&volunteeringId=${volunteeringId}&locId=${locId}`, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const removeRange = async (volunteeringId: number, rangeId: number): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.delete(`${server}/api/volunteering/removeRange?userId=${localStorage.getItem('username')}&volunteeringId=${volunteeringId}&rangeId=${rangeId}`, config);
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
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` },
    };
    let res = await axios.patch(`${server}/api/volunteering/moveVolunteerGroup?userId=${localStorage.getItem('username')}`, body, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const assignVolunteerToLocation = async (volunteeringId: number, locId: number): Promise<string> => {
    const body = {
        volunteerId: localStorage.getItem('username'),
        volunteeringId: volunteeringId,
        toId: locId,
    }
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` },
    };
    let res = await axios.patch(`${server}/api/volunteering/assignVolunteerToLocation?userId=${localStorage.getItem('username')}`, body, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const addVolunteeringLocation = async (volunteeringId: number, name: string, city: string, street: string, address: string): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    const request = {
        name: name,
        address: {
            city: city,
            street: street,
            address: address
        }
    }
    let res = await axios.post(`${server}/api/volunteering/addVolunteeringLocation?userId=${localStorage.getItem('username')}&volunteeringId=${volunteeringId}`, request, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const requestHoursApproval = async (volunteeringId: number, startDate: string, endDate: string): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    const request = {
        startDate: startDate,
        endDate: endDate
    }
    let res = await axios.post(`${server}/api/volunteering/requestHoursApproval?userId=${localStorage.getItem('username')}&volunteeringId=${volunteeringId}`, request, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getVolunteeringScanType = async (volunteeringId: number): Promise<ScanType> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.get(`${server}/api/volunteering/getVolunteeringScanType?userId=${localStorage.getItem('username')}&volunteeringId=${volunteeringId}`, config);
    const response: APIResponse<ScanType> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getVolunteeringApprovalType = async (volunteeringId: number): Promise<ApprovalType> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.get(`${server}/api/volunteering/getVolunteeringApprovalType?userId=${localStorage.getItem('username')}&volunteeringId=${volunteeringId}`, config);
    const response: APIResponse<ApprovalType> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const updateVolunteeringScanDetails = async (volunteeringId: number, scanType: ScanType, approvalType: ApprovalType): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    const request = {
        scanTypes: scanType,
        approvalType: approvalType
    }
    let res = await axios.patch(`${server}/api/volunteering/updateVolunteeringScanDetails?userId=${localStorage.getItem('username')}&volunteeringId=${volunteeringId}`, request, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const removeImageFromVolunteering = async (volunteeringId: number, image: string): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` },
        params: {
            userId: localStorage.getItem('username'),
            volunteeringId: volunteeringId,
            imagePath: image.replace(/"/g, "")
        }
    };
    let res = await axios.delete(`${server}/api/volunteering/removeImageFromVolunteering`, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const addImageToVolunteering = async (volunteeringId: number, image: string): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.post(`${server}/api/volunteering/addImageToVolunteering?userId=${localStorage.getItem('username')}&volunteeringId=${volunteeringId}`, image.replace(/"/g, ""), config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const generateSkillsAndCategories = async (volunteeringId: number): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.patch(`${server}/api/volunteering/generateSkillsAndCategories?userId=${localStorage.getItem('username')}&volunteeringId=${volunteeringId}`, {},config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const updateVolunteeringSkills = async (volunteeringId: number, skills: string[]): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.patch(`${server}/api/volunteering/updateVolunteeringSkills?userId=${localStorage.getItem('username')}&volunteeringId=${volunteeringId}`, skills, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const updateVolunteeringCategories = async (volunteeringId: number, categories: string[]): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.patch(`${server}/api/volunteering/updateVolunteeringCategories?userId=${localStorage.getItem('username')}&volunteeringId=${volunteeringId}`, categories, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getConstantCodes = async (volunteeringId: number): Promise<string[]> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.get(`${server}/api/volunteering/getConstantCodes?userId=${localStorage.getItem('username')}&volunteeringId=${volunteeringId}`, config);
    const response: APIResponse<string[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getVolunteeringWarnings = async (volunteeringId: number): Promise<string[]> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.get(`${server}/api/volunteering/getVolunteeringWarnings?userId=${localStorage.getItem('username')}&volunteeringId=${volunteeringId}`, config);
    const response: APIResponse<string[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const clearConstantCodes = async (volunteeringId: number): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.delete(`${server}/api/volunteering/clearConstantCodes?userId=${localStorage.getItem('username')}&volunteeringId=${volunteeringId}`, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const finishVolunteering = async (volunteeringId: number, experience: string): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    const request = {
        id: volunteeringId,
        text: experience
    }
    let res = await axios.post(`${server}/api/volunteering/finishVolunteering?userId=${localStorage.getItem('username')}`, request, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getUserApprovedHoursFormatted = async (volunteeringId: number, israeliId: string): Promise<void> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` },
        responseType: 'arraybuffer',
        validateStatus: () => true
    };
    //@ts-ignore
    let response = await axios.get(`${server}/api/volunteering/getUserApprovedHoursFormatted?userId=${localStorage.getItem('username')}&volunteeringId=${volunteeringId}&israeliId=${israeliId}`, config);
    
    if(response.status === 400){
        throw(new TextDecoder().decode(response.data))
    }
    
    var blob = new Blob([response.data], { type: "application/pdf" });
        
    const href = URL.createObjectURL(blob);

    const link = document.createElement('a');
    link.href = href;
    link.setAttribute('download', 'export'+(new Date()).toLocaleTimeString() + ".pdf"); //or any other extension
    document.body.appendChild(link);
    link.click();

    document.body.removeChild(link);
    URL.revokeObjectURL(href);
}