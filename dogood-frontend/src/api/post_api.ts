import APIResponse from "../models/APIResponse";
import axios from "axios";
import { VolunteeringPostModel } from "../models/VolunteeringPostModel";
import PastExperienceModel from "../models/PastExpreienceModel";
import { host } from "./general";

const server: string = `http://${host}/api/posts`;

export const createVolunteeringPost = async (title: string, description: string, volunteeringId: number): Promise<number> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/createVolunteeringPost`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    const request = {
        title: title,
        description: description,
        volunteeringId: volunteeringId,
        actor: username
    }
    console.log(request)

    let res = await axios.post(url, request, config);
    const response: APIResponse<number> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    let postId = response.data;
    return postId;
}

export const removeVolunteeringPost = async (postId: number) => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }
    
    let url = `${server}/removeVolunteeringPost?postId=${postId}&actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    let res = await axios.delete(url, config);
    const response: APIResponse<number> = await res.data;
    if(response.error){
        throw response.errorString;
    }
}

export const editVolunteeringPost = async (postId: number, title: string, description: string) => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }
    
    let url = `${server}/editVolunteeringPost?postId=${postId}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    const request = {
        title: title, 
        description: description, 
        actor: username,
        volunteeringId: -1
    }
    let res = await axios.put(url, request, config);
    const response: APIResponse<number> = await res.data;
    if(response.error){
        throw response.errorString;
    }
}

export const getVolunteeringPost = async (postId: number): Promise<VolunteeringPostModel> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/getVolunteeringPost?postId=${postId}&actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };

    let res = await axios.get(url, config);
    const response: APIResponse<VolunteeringPostModel> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    let post: VolunteeringPostModel = response.data;
    return post;
}

export const getAllVolunteeringPosts = async (): Promise<VolunteeringPostModel[]> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/getAllVolunteeringPosts?actor=${username}`;
    console.log(url);
    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };

    let res = await axios.get(url, config);
    const response: APIResponse<VolunteeringPostModel[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    let posts: VolunteeringPostModel[] = response.data;
    return posts;
}

export const getOrganizationVolunteeringPosts = async (organizationId: number): Promise<VolunteeringPostModel[]> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/getOrganizationVolunteeringPosts?orgId=${organizationId}&actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };

    let res = await axios.get(url, config);
    const response: APIResponse<VolunteeringPostModel[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    let posts: VolunteeringPostModel[] = response.data;
    return posts;
}

export const joinVolunteeringRequest = async (postId: number, freeText: string) => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/joinVolunteeringRequest?freeText=${freeText}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    const request = {
        id: postId,
        actor: username
    }

    let res = await axios.post(url, request, config);
    const response: APIResponse<VolunteeringPostModel[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
}

export const searchByKeywords = async (search: string, postsToSearch: VolunteeringPostModel[]): Promise<VolunteeringPostModel[]> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/searchByKeywords?search=${search}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    const request = {
        actor: username,
        allPosts: postsToSearch
    }
    let res = await axios.post(url, request, config);
    const response: APIResponse<VolunteeringPostModel[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    let posts: VolunteeringPostModel[] = response.data;
    return posts;
}

export const sortByRelevance = async (postsToSort: VolunteeringPostModel[]): Promise<VolunteeringPostModel[]> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/sortByRelevance`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    const request = {
        actor: username,
        allPosts: postsToSort
    }
    let res = await axios.post(url, request, config);
    const response: APIResponse<VolunteeringPostModel[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    let posts: VolunteeringPostModel[] = response.data;
    return posts;
}

export const sortByPopularity = async (postsToSort: VolunteeringPostModel[]): Promise<VolunteeringPostModel[]> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/sortByPopularity`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    const request = {
        actor: username,
        allPosts: postsToSort
    }
    let res = await axios.post(url, request, config);
    const response: APIResponse<VolunteeringPostModel[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    let posts: VolunteeringPostModel[] = response.data;
    return posts;
}

export const sortByPostingTime = async (postsToSort: VolunteeringPostModel[]): Promise<VolunteeringPostModel[]> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/sortByPostingTime`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    const request = {
        actor: username,
        allPosts: postsToSort
    }
    let res = await axios.post(url, request, config);
    const response: APIResponse<VolunteeringPostModel[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    let posts: VolunteeringPostModel[] = response.data;
    return posts;
}

export const sortByLastEditTime = async (postsToSort: VolunteeringPostModel[]): Promise<VolunteeringPostModel[]> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/sortByLastEditTime`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    const request = {
        actor: username,
        allPosts: postsToSort
    }
    let res = await axios.post(url, request, config);
    const response: APIResponse<VolunteeringPostModel[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    let posts: VolunteeringPostModel[] = response.data;
    return posts;
}

export const filterPosts = async (categories: string[], skills: string[], cities: string[], organizationNames: string[], voluntteringNames: string[], postsToFilter: VolunteeringPostModel[]): Promise<VolunteeringPostModel[]> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/filterPosts`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    const request = {
        categories: categories,
        skills: skills,
        cities: cities,
        organizationNames: organizationNames, 
        volunteeringNames: voluntteringNames,
        actor: username,
        allPosts: postsToFilter
    }

    let res = await axios.post(url, request, config);
    const response: APIResponse<VolunteeringPostModel[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    let posts: VolunteeringPostModel[] = response.data;
    return posts;
}

export const getAllPostsCategories = async (): Promise<string[]> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/getAllPostsCategories?actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };

    let res = await axios.get(url, config);
    const response: APIResponse<string[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    let categories: string[] = response.data;
    return categories;
}

export const getAllPostsSkills = async (): Promise<string[]> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/getAllPostsSkills?actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };

    let res = await axios.get(url, config);
    const response: APIResponse<string[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    let skills: string[] = response.data;
    return skills;
}

export const getAllPostsCities = async (): Promise<string[]> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/getAllPostsCities?actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };

    let res = await axios.get(url, config);
    const response: APIResponse<string[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    let cities: string[] = response.data;
    return cities;
}

export const getAllOrganizationNames = async (): Promise<string[]> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/getAllOrganizationNames?actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };

    let res = await axios.get(url, config);
    const response: APIResponse<string[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    let cities: string[] = response.data;
    return cities;
}

export const getAllVolunteeringNames = async (): Promise<string[]> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/getAllVolunteeringNames?actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };

    let res = await axios.get(url, config);
    const response: APIResponse<string[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    let cities: string[] = response.data;
    return cities;
}

export const getPostPastExperiences = async (postId : number): Promise<PastExperienceModel[]> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/getPostPastExperiences?postId=${postId}&actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };

    let res = await axios.get(url, config);
    const response: APIResponse<PastExperienceModel[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    let experiences: PastExperienceModel[] = response.data;
    return experiences;
}

export const getVolunteeringName = async (volunteeringId : number): Promise<string> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/getVolunteeringName?volunteeringId=${volunteeringId}&actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };

    let res = await axios.get(url, config);
    const response: APIResponse<string> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    let name: string = response.data;
    return name;
}

export const getVolunteeringImages = async (volunteeringId : number): Promise<string[]> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/getVolunteeringImages?volunteeringId=${volunteeringId}&actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };

    let res = await axios.get(url, config);
    const response: APIResponse<string[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    let images: string[] = response.data;
    return images;
}