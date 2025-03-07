export interface PostModel {
    id: number;
    title: string;
    description: string;
    postedTime: string;
    lastEditedTime: string; 
    posterUsername: string;
    relevance: number;
    keywords: string[];
}

