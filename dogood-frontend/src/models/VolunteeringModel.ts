type VolunteeringModel = {
    id: number,
    orgId: number,
    name: string,
    description: string,
    skills: string[],
    categories: string[],
    imagePaths?: string[]
}

export default VolunteeringModel;