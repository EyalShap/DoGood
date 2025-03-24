import OrganizationModel from '../models/OrganizationModel';
import { useEffect, useState } from 'react'
import './../css/Organization.css'
import './../css/CommonElements.css'
import { getOrganization, getIsManager, getOrganizationVolunteerings, removeOrganization, removeManager, setFounder, sendAssignManagerRequest, resign, getUserRequests, getUserVolunteerings, removeImageFromOrganization, addImageToOrganization } from '../api/organization_api'
import { useParams } from "react-router-dom";
import { useNavigate } from 'react-router-dom';
import Volunteering from './Volunteering';
import {getVolunteering, getVolunteeringsOfUser} from '../api/volunteering_api';
import ListWithArrows, { ListItem } from './ListWithArrows';
import { getUserByUsername } from '../api/user_api';
import { getVolunteeringImages } from '../api/post_api';
import { supabase } from '../api/general';
import ListWithPlus from "./ListWithPlus.tsx";

function MyVolunteerings() {
    const navigate = useNavigate();
    const [volunteerings, setVolunteerings] = useState<ListItem[]>([]);
    const [ready, setReady] = useState(false);

    const fetchVolunteerings = async () => {
        try {
            const volunteeringDetails = await getVolunteeringsOfUser();

            const imagesArray = await Promise.all(
                volunteeringDetails.map(volunteering => getVolunteeringImages(volunteering.id))
            );

            const listItems: ListItem[] = volunteeringDetails.map((volunteering, index) => ({
                id: volunteering.id,
                image: imagesArray[index].length > 0 ? imagesArray[index][0] : '/src/assets/defaultVolunteeringDog.webp',
                title: volunteering.name,
                description: volunteering.description, // assuming 'summary' is a short description
            }));

            setVolunteerings(listItems);
            setReady(true);
        } catch (e) {
            // send to error page
            alert(e);
        }
    }

    useEffect(() => {
        fetchVolunteerings();
    }, []);

    return (
        <div className='generalPageDiv'>

            <div className="listContainer">
                <h2 className='listHeader'>My Volunteerings</h2>
                <div className='generalList'>
                    {volunteerings.length > 0 ? (
                        <ListWithPlus limit = {9} data = {volunteerings} navigateTo={'volunteering'}></ListWithPlus>
                    ) : (
                        <p>No volunteerings available.</p>
                    )}
                </div>
            </div>
        </div>

    )
}

export default MyVolunteerings