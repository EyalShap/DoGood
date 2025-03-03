
export type VolunteeringInHistory = {
    id: number,
    orgId: number,
    name: string,
    description: string,
    skills: string[],
    categories: string[],
    imagePaths: string[] // optional for now
};

type User = {
    username: string;
    emails: string[];
    name: string;
    phone: string;
    birthDate: string;
    preferredCategories: string[];
    skills: string[];
    isStudent: boolean;
    isAdmin: boolean;
    volunteeringIds: number[];
    volunteeringsInHistory: VolunteeringInHistory[];
    myOrganizationIds: number[];
};

export default User;