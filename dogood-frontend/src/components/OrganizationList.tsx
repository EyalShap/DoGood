import { getAllOrganizations } from '../api/organization_api'
import { useEffect, useState } from 'react'
import OrganizationModel from '../models/OrganizationModel';
import { useNavigate } from 'react-router-dom';
import './../css/OrganizationList.css'

function OrganizationList() {
    const navigate = useNavigate();

    const [organizations, setOrganizations] = useState<OrganizationModel[]>([]);
    
    const fetchOrganizations = async () => {
        try {
            const organizations = await getAllOrganizations();
            setOrganizations(organizations);
        } catch (e) {
            // send to error page
            alert(e);
        }
    }

    useEffect(() => {
        fetchOrganizations();
    }, [])

    const handleShowOnClick = (organizationId: number) => {
        navigate(`/organization/${organizationId}`);
    };

    const handleNewOrgOnClick = () => {
        navigate('/createOrganization/-1');
    };

    return (
        <div>
            <div className="Organizations">
                <h2>Organizations</h2>
                {organizations.length > 0 ? (
                    organizations.map((organization, index) => (
                        <div key={index} className="organizationItem">
                            <h3>{organization.name}</h3>
                            <p>{organization.description}</p>
                            <button onClick={() => handleShowOnClick(organization.id)}>Show</button>
                        </div>
                    ))
                ) : (
                    <p>No organizations available.</p>
                )}
            </div>
            <div className='newOrganization'>
                <button onClick={handleNewOrgOnClick}>Add New Organization</button>
            </div>
        </div>
    )
}

export default OrganizationList