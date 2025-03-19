import {host} from "./general.ts";
import axios from "axios";
import APIResponse from "../models/APIResponse.ts";
import ChatMessage from "../models/ChatMessage.ts";

const server: string = `${host}/api/chat`;

export const sendVolunteeringMessage = async (volunteeringId: number, content: string): Promise<number> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    const body = {
        username: localStorage.getItem("username"),
        content: content,
        volunteeringId: volunteeringId
    }
    let res = await axios.post(`${server}/sendVolunteeringMessage`, body, config);
    const response: APIResponse<number> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const getVolunteeringChatMessages = async (volunteeringId: number): Promise<ChatMessage[]> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };

    let res = await axios.get(`${server}/getVolunteeringChatMessages?username=${localStorage.getItem("username")}&volunteeringId=${volunteeringId}`, config);
    const response: APIResponse<ChatMessage[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const deleteMessage = async (messageId: number): Promise<string> => {
    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    let res = await axios.delete(`${server}/deleteMessage?username=${localStorage.getItem("username")}&messageId=${messageId}`, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}