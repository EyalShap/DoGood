import { PostModel } from "./PostModel";

export interface VolunteerPostModel extends PostModel {
    relatedUsers: string[];
    images: string[];
    skills: string[];
    categories: string[];
}
