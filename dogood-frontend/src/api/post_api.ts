import APIResponse from "../models/APIResponse";
import axios from "axios";
import { VolunteeringPostModel } from "../models/VolunteeringPostModel";
import PastExperienceModel from "../models/PastExpreienceModel";
import { host } from "./general";
import { PostModel } from "../models/PostModel";
import { VolunteerPostModel } from "../models/VolunteerPostModel";
import RequestModel from "../models/RequestModel";

const server: string = `${host}/api/posts`;

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

export const createVolunteerPost = async (title: string, description: string): Promise<number> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/createVolunteerPost`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    const request = {
        title: title,
        description: description,
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

export const removeVolunteerPost = async (postId: number) => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }
    
    let url = `${server}/removeVolunteerPost?postId=${postId}&actor=${username}`;

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

export const editVolunteerPost = async (postId: number, title: string, description: string) => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }
    
    let url = `${server}/editVolunteerPost?postId=${postId}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    const request = {
        title: title, 
        description: description, 
        actor: username
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

export const getVolunteerPost = async (postId: number): Promise<VolunteerPostModel> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/getVolunteerPost?postId=${postId}&actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };

    let res = await axios.get(url, config);
    const response: APIResponse<VolunteerPostModel> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    let post: VolunteerPostModel = response.data;
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

export const sendAddRelatedUserRequest = async (postId: number, newUsername: string) => {
    console.log(newUsername);
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/sendAddRelatedUserRequest?username=${newUsername}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    const request = {
        id: postId,
        actor: username
    }

    let res = await axios.post(url, request, config);
    const response: APIResponse<Boolean> = await res.data;
    if(response.error){
        throw response.errorString;
    }
}

export const searchByKeywords = async (search: string, postsToSearch: PostModel[], volunteering: boolean): Promise<PostModel[]> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/searchByKeywords?search=${search}`;
    console.log(url)

    const config = {
        headers: { 
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json"
        }
    };
    const request = {
        actor: username,
        allPosts: postsToSearch,
        volunteering: volunteering
    }
    console.log(request)
    let res = await axios.post(url, request, config);
    const response: APIResponse<PostModel[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    let posts: PostModel[] = response.data;
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

export const sortByPostingTime = async (postsToSort: PostModel[]): Promise<PostModel[]> => {
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
    const response: APIResponse<PostModel[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    let posts: PostModel[] = response.data;
    return posts;
}

export const sortByLastEditTime = async (postsToSort: PostModel[]): Promise<PostModel[]> => {
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
    const response: APIResponse<PostModel[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    let posts: PostModel[] = response.data;
    return posts;
}

export const filterVolunteeringPosts = async (categories: string[], skills: string[], cities: string[], organizationNames: string[], voluntteringNames: string[], postsToFilter: VolunteeringPostModel[]): Promise<VolunteeringPostModel[]> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/filterVolunteeringPosts`;

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

export const filterVolunteerPosts = async (categories: string[], skills: string[], postsToFilter: VolunteerPostModel[]): Promise<VolunteerPostModel[]> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/filterVolunteerPosts`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    const request = {
        categories: categories,
        skills: skills,
        actor: username,
        allPosts: postsToFilter
    }

    let res = await axios.post(url, request, config);
    const response: APIResponse<VolunteerPostModel[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    let posts: VolunteerPostModel[] = response.data;
    return posts;
}

export const getAllVolunteeringPostsCategories = async (): Promise<string[]> => {
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

export const getAllVolunteeringPostsSkills = async (): Promise<string[]> => {
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

export const getAllVolunteerPostsCategories = async (): Promise<string[]> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/getAllVolunteerPostsCategories?actor=${username}`;

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

export const getAllVolunteerPostsSkills = async (): Promise<string[]> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/getAllVolunteerPostsSkills?actor=${username}`;

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

export const getAllVolunteerPosts = async (): Promise<VolunteerPostModel[]> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }

    let url = `${server}/getAllVolunteerPosts?actor=${username}`;
    console.log(url);
    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };

    let res = await axios.get(url, config);
    const response: APIResponse<VolunteerPostModel[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    let posts: VolunteerPostModel[] = response.data;
    return posts;
}

export const getVolunteerPostRequests = async (): Promise<RequestModel[]> => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");
    
    let url = `${server}/getVolunteerPostRequests?actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    let res = await axios.get(url, config);
    const response: APIResponse<RequestModel[]> = await res.data;
    if(response.error){
        throw response.errorString;
    }
    return response.data;
}

export const handleJoinVolunteerPostRequest = async (postId: number, approved: boolean) => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if(username === null) {
        throw new Error("Error");
    }
    
    let url = `${server}/handleAddRelatedUserRequest?approved=${approved}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };
    const request = {
        id: postId,
        actor: username
    }

    let res = await axios.post(url, request, config);
    const response: APIResponse<number> = await res.data;
    
    if(response.error){
        throw response.errorString;
    }
}

export const addImageToVolunteerPost = async (postId: number, image: string) => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if (username === null) {
        throw new Error("Error");
    }

    let url = `${server}/addImage?postId=${postId}&actor=${username}`;

    const config = {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` }
    };
    console.log(config);

    let res = await axios.post(url, image.replace(/"/g, ""), config);
    const response: APIResponse<boolean> = await res.data;
    if (response.error) {
        throw response.errorString;
    }
}

export const removeImageFromVolunteerPost = async (postId: number, image: string) => {
    let username: string | null = localStorage.getItem("username");
    let token: string | null = localStorage.getItem("token");

    if (username === null) {
        throw new Error("Error");
    }

    let url = `${server}/removeImage?postId=${postId}&path=${image.replace(/"/g, "")}&actor=${username}`;

    const config = {
        headers: { Authorization: `Bearer ${token}` }
    };

    let res = await axios.delete(url, config);
    const response: APIResponse<boolean> = await res.data;
    if (response.error) {
        throw response.errorString;
    }
}