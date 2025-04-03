type OrganizationModel = {
    id: number,
    name: string,
    description: string,
    phoneNumber: string,
    email: string,
    volunteeringIds: number[],
    managerUsernames: string[],
    founderUsername: string,
    imagePaths: string[],
    signature: Blob | null
}

export default OrganizationModel;