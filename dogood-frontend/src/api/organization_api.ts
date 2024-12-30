import OrganizationModel from "../models/OrganizationModel";
import axios, { AxiosResponse } from "axios";

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
        const response: AxiosResponse<{ data: OrganizationModel; error?: string }> = (await axios.patch(url, request,
            {
                headers: headers
            }));

            if (response.data) {
                console.error("Error from server:", response.data.error);
            }
            const organization: OrganizationModel = response.data.data;
            return organization;
    } 
    catch (error) {
            connectionError.dispatch(errorSlice.actions.setTrue())
    }

}