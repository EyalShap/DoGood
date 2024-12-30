type VolunteeringModel = {
    id: number,
    orgId: number,
    name: string,
    description: string,
    skills: string[],
    categories: string[],
    imagePaths?: string[]
}

export interface VolunteersToGroup {
    [Key: string]: number;
}

export default VolunteeringModel;