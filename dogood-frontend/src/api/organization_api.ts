import OrganizationModel from "../models/OrganizationModel";
import axios from "axios";

const server: string = 'http://127.0.0.1:8080';

export const getOrganization = async (organizationId: number): Promise<OrganizationModel> => {
    let username: string | null = sessionStorage.getItem("username");
    let request = {
        id: organizationId,
        actor: username
    }
    let url = `${server}/api/organizations/getOrganization`;
    let headers = {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
    }

    try {
        const response: Response<OrganizationModel> = (await axios.patch(url, request,
            {
                headers: headers
            }));

            if (response.error) {
                return []
            }

            const org = response.data;

            const organization: OrganizationModel = {
                id: number,
                name: string,
                description: string,
                phoneNumber: string,
                email: string,
                volunteeringIds: number[],
                managerUsernames: string[],
                founderUsername: string
                
            };


            return org;
        } catch (error) {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }

}