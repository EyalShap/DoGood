import { getAllOrganizations } from '../api/organization_api'
import { useEffect, useState } from 'react'
import OrganizationModel from '../models/OrganizationModel';
import { useNavigate } from 'react-router-dom';
import './../css/OrganizationList.css'
import './../css/CommonElements.css'
import ListWithArrows, { ListItem } from './ListWithArrows';
import ListWithPlus from './ListWithPlus';
import { Switch } from '@mui/material';
import { getVolunteeringsOfUser, getVolunteeringVolunteers } from '../api/volunteering_api';

function OrganizationList() {
    const navigate = useNavigate();

    const [organizations, setOrganizations] = useState<ListItem[]>([]);
    const [allOrganizations, setAllOrganizations] = useState<ListItem[]>([]);
    const [myOrganizations, setMyOrganizations] = useState<ListItem[]>([]);
    const [searchFrom, setSearchFrom] = useState<ListItem[]>([]);
    const [isMyOrgs, setIsMyOrgs] = useState(false);
    const [search, setSearch] = useState("");
    
    const fetchOrganizations = async () => {
        try {
            const organizations = await getAllOrganizations();
            
            const listItems: ListItem[] = convertToListItems(organizations);

            setOrganizations(listItems);
            setAllOrganizations(listItems);

            const myOrganizations: OrganizationModel[] = await myOrgs(organizations);
            const myOrgsListItems: ListItem[] = convertToListItems(myOrganizations);
            setMyOrganizations(myOrgsListItems);

        } catch (e) {
            // send to error page
            alert(e);
        }
    }

    const myOrgs = async (orgs: OrganizationModel[]) => {
        const results = await Promise.all(
            orgs.map(async (org) => ({
                org,
                isMy: await isMyOrg(org) 
            }))
        );
    
        return results.filter((item) => item.isMy).map((item) => item.org);
    };

    const isMyOrg = async (org: OrganizationModel) => {
        if(org.founderUsername === localStorage.getItem("username")) {
            return true;
        }
        let actorVolunteerings = await getVolunteeringsOfUser();
        let orgVolunteerings = org.volunteeringIds;

        for(let volunteering of actorVolunteerings) {
            if(orgVolunteerings.includes(volunteering.id)) {
                return true;
            }
        }
        return false;
    }

    const convertToListItems = (orgs: OrganizationModel[]) => {
        const listItems: ListItem[] = orgs.map((org) => ({
            id: org.id,
            image: org.imagePaths.length > 0 ? org.imagePaths[0] : "/src/assets/defaultOrganizationDog.jpg", 
            title: org.name.concat(org.founderUsername === localStorage.getItem("username") ? " (Founder)" : ""),  
            description: org.description, 
        }));
        return listItems;
    }

    useEffect(() => {
        fetchOrganizations();
    }, []);

    useEffect(() => {
        const searchFrom = isMyOrgs ? myOrganizations : allOrganizations;
        setSearchFrom(searchFrom);
        setOrganizations(searchFrom);
    }, [isMyOrgs]);

    const handleNewOrgOnClick = () => {
        navigate('/createOrganization/-1');
    };

    const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const newSearchValue = event.target.value; // Get the new value directly from the event
        setSearch(newSearchValue);
        console.log(isMyOrgs);
        //const searchFrom = isMyOrgs ? myOrganizations : allOrganizations;
        let searchOrgs = searchFrom.filter((org) => org.title.toLowerCase().includes(newSearchValue.toLowerCase()));
        setOrganizations(searchOrgs);
    };

    const toggleSwitch = () => {
        const newSwitchState = !isMyOrgs;
        setIsMyOrgs(newSwitchState);
    }

    return (
        <div className='generalPageDiv'>
            <link
            href="https://fonts.googleapis.com/css2?family=Lobster&display=swap"
            rel="stylesheet"
            />
            <link
            href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap"
            rel="stylesheet"
            />
            
            <div className='headers'>
                <h2 className='bigHeader'>Our Organizations</h2>
                <h2 className='smallHeader'>Join our growing list of impactful organizations and let others discover the great work you do.</h2>
                <button className='orangeCircularButton' onClick={handleNewOrgOnClick}>Create Your Organization</button>
                <input id = "searchTextbox"
                    type="text"
                    value={search}
                    onChange={handleSearchChange}
                    placeholder = "Search..."
                />
            
                <div className="switch-container">
                    <label className="switch-label">My Organizations</label>
                    <Switch className='switch' defaultChecked onChange={toggleSwitch}/>
                    <label className="switch-label">All Organizations</label>
                </div>
            </div>
                
            <div className='orgList'>
                {organizations.length > 0 ? (
                    <ListWithPlus data={organizations} limit = {6} navigateTo = 'organization'></ListWithPlus>
                ) : (
                <p>No organizations available.</p>
            )}</div>
        </div>
    )
}

export default OrganizationList