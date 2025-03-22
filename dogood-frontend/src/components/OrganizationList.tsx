import { getAllOrganizations } from '../api/organization_api'
import { useEffect, useState } from 'react'
import OrganizationModel from '../models/OrganizationModel';
import { useNavigate } from 'react-router-dom';
import './../css/OrganizationList.css'
import './../css/CommonElements.css'
import ListWithArrows, { ListItem } from './ListWithArrows';
import ListWithPlus from './ListWithPlus';

function OrganizationList() {
    const navigate = useNavigate();

    const [organizations, setOrganizations] = useState<ListItem[]>([]);
    const [allOrganizations, setAllOrganizations] = useState<ListItem[]>([]);
    const [search, setSearch] = useState("");
    
    const fetchOrganizations = async () => {
        try {
            const organizations = await getAllOrganizations();
            
            const listItems: ListItem[] = organizations.map((org) => ({
                id: org.id,
                image: 'https://cdn.thewirecutter.com/wp-content/media/2021/03/dogharnesses-2048px-6907-1024x682.webp', 
                title: org.name,  
                description: org.description, // assuming 'summary' is a short description
            }));

            setOrganizations(listItems);
            setAllOrganizations(listItems);
        } catch (e) {
            // send to error page
            alert(e);
        }
    }

    useEffect(() => {
        fetchOrganizations();
    }, [])

    const handleNewOrgOnClick = () => {
        navigate('/createOrganization/-1');
    };

    const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const newSearchValue = event.target.value; // Get the new value directly from the event
        setSearch(newSearchValue);
        let searchOrgs = allOrganizations.filter((org) => org.title.toLowerCase().includes(newSearchValue.toLowerCase()));
        console.log(newSearchValue);
        console.log(searchOrgs);
        setOrganizations(searchOrgs);
    };

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