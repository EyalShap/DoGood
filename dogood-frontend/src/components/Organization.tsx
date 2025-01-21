import OrganizationModel from '../models/OrganizationModel';
import { useEffect, useState } from 'react'
import './../css/Organization.css'
import { getOrganization, getIsManager, getOrganizationVolunteerings, removeOrganization, removeManager, setFounder, sendAssignManagerRequest, resign, getUserRequests, getUserVolunteerings } from '../api/organization_api'
import { useParams } from "react-router-dom";
import { useNavigate } from 'react-router-dom';
import Volunteering from './Volunteering';
import { getVolunteering } from '../api/volunteering_api';

function Organization() {
    const navigate = useNavigate();
    const [model, setModel] = useState<OrganizationModel>({id: -1, name: "", description: "", phoneNumber: "", email: "", volunteeringIds: [-1], managerUsernames: [], founderUsername: ""});
    let { id } = useParams();
    const [isManager, setIsManager] = useState(false);
    const [userVolunteerings, setUserVolunteerings] = useState<number[]>([]);
    const [volunteerings, setVolunteerings] = useState<{ name: string; description: string; id: number }[]>([]);
    const [ready, setReady] = useState(false);
    const [showAddManager, setShowAddManager] = useState(false);
    const [addManagerText, setAddManagerText] = useState('');
    
    const fetchOrganization = async () => {
        try{
            let found = await getOrganization(parseInt(id!));
            setModel(found);
        }
        catch(e){
            //send to error page
            alert(e)
        }
    }

    const fetchVolunteerings = async () => {
        try {
            const volunteeringDetails = await getOrganizationVolunteerings(model.id);
            setVolunteerings(volunteeringDetails);

            const userVolunteerings = await getUserVolunteerings(model.id);
            setUserVolunteerings(userVolunteerings);

            setReady(true);
        } catch (e) {
            // send to error page
            alert(e);
        }
    }

    const updatePermissions = async () => {
        try {
            let username: string | null = sessionStorage.getItem("username");
            if(username === null) {
                alert("Username is null.");
            }
            else {
                let isManager = await getIsManager(model.id);
                setIsManager(isManager);
            }
        }
        catch(e){
            //send to error page
            alert(e)
        }
    }
    
    useEffect(() => {
        fetchOrganization();
    }, [])

    useEffect(() => {
        if(JSON.stringify(model.volunteeringIds) !== JSON.stringify([-1])) {
            if (model.volunteeringIds.length > 0) {
                fetchVolunteerings();
            }
            else {
                setReady(true);
            }
        }
    }, [model]);
    
    useEffect(() =>{
        if(ready){    
            updatePermissions();
        }
    }, [model, ready])

    const handleCreateVolunteeringOnClick = () => {
        navigate('./createvolunteering');
    };

    const handleRemoveOrganizationOnClick = async () => {
        if (window.confirm(`Are you sure you want to remove this organization?`)) {
            try {
                await removeOrganization(model.id);
                navigate('/organizationList');
            }
            catch(e) {
                alert("Problem with removing organization!");
            }
        }
    };

    const handleEditOrganizationOnClick = async () => {
        navigate(`/createOrganization/${id}`);
    };

    
    const handleAddNewManagerOnClick = async (newManager : string) => {
        if(showAddManager) {
            if(id !== undefined) {
                try {
                    await sendAssignManagerRequest(parseInt(id), newManager);
                    alert("Your request was sent successfully!");
                    setShowAddManager(false);
                    setAddManagerText("");
                }
                catch(e) {
                    alert(e);
                }
            }
            else {
                alert("Error!");
            }
        }
        else {
            setShowAddManager(true);
        }
    };


    const handleRemoveManagerOnClick = async (managerToRemove: string) => {
        if (window.confirm(`Are you sure you want to remove ${managerToRemove} as a manager?`)) {
            if(id !== undefined) {
                try {
                    await removeManager(parseInt(id), managerToRemove);
                    let updatedModel: OrganizationModel = {
                        id: model.id,
                        name: model.name,
                        description: model.description,
                        phoneNumber: model.phoneNumber,
                        email: model.email,
                        volunteeringIds: model.volunteeringIds,
                        managerUsernames: model.managerUsernames.filter((manager) => manager !== managerToRemove),
                        founderUsername: model.founderUsername
                    }
                    setModel(updatedModel);
                }
                catch(e) {
                    alert(e);
                }
            }
            else {
                alert("Error");
            }
        }
    };

    const handleSetAsFounderOnClick = async (newFounder: string) => {
        if (window.confirm(`Are you sure you want to set ${newFounder} as a founder?`)) {
            if(id !== undefined) {
                try {
                    await setFounder(parseInt(id), newFounder);
                    let updatedModel: OrganizationModel = {
                        id: model.id,
                        name: model.name,
                        description: model.description,
                        phoneNumber: model.phoneNumber,
                        email: model.email,
                        volunteeringIds: model.volunteeringIds,
                        managerUsernames: model.managerUsernames,
                        founderUsername: newFounder
                    }
                    setModel(updatedModel);
                }
                catch(e) {
                    alert(e);
                }
            }
            else {
                alert("Error");
            }
        }
    };

    const handleAddManagerTextChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setAddManagerText(event.target.value);
    };

    const handleResignOnClick = async () => {
        if (window.confirm(`Are you sure you want to resign?`)) {
            if(id !== undefined) {
                try {
                    await resign(parseInt(id));
                    let updatedModel: OrganizationModel = {
                        id: model.id,
                        name: model.name,
                        description: model.description,
                        phoneNumber: model.phoneNumber,
                        email: model.email,
                        volunteeringIds: model.volunteeringIds,
                        managerUsernames: model.managerUsernames.filter((manager) => manager !== sessionStorage.getItem("username")),
                        founderUsername: model.founderUsername
                    }
                    setModel(updatedModel);
                }
                catch(e) {
                    alert(e);
                }
            }
            else {
                alert("Error");
            }
        }
    };

    const handleShowVolunteeringOnClick = (volunteeringId: number) => {
        navigate(`/volunteering/${volunteeringId}`);
    }

    /*const handleRemoveVolunteeringOnClick = async (volunteeringId: number) => {
        if (window.confirm(`Are you sure you want to remove this volunteering?`)) {
            if(id !== undefined) {
                try {
                    await removeVolunteering(parseInt(id), volunteeringId);
                    let new_volunteerings = volunteerings.filter((volunteering) => volunteering.id !== volunteeringId)
                    setVolunteerings(new_volunteerings);
                }
                catch(e) {
                    alert(e);
                }
            }
            else {
                alert("Error");
            }
        }
    }*/

    const isVolunteer = (volunteeringId: number) : boolean => {
        return userVolunteerings.includes(volunteeringId);
    }
    
    return (
        <div>
        <div className = "orgInfo">
            <div className='orgInfoText'>
                <h1>{model.name}</h1>
                <p>{model.description}</p>
            </div>

            <div className = "orgInfoContact">
                <h2>Contact Us:</h2>
                <p>Email: <a href={`mailto:${model.email}`}>{model.email}</a></p>
                <p>Phone: {model.phoneNumber}</p>
            </div>

            {isManager && 
            (<div className = 'orgInfoButtons'>
                <button onClick={handleRemoveOrganizationOnClick}>Remove Organization</button>
                <button onClick={handleEditOrganizationOnClick}>Edit Organization</button>
            </div>)}

            <div className="volunteerings">
                <h2>Volunteerings</h2>
                {volunteerings.length > 0 ? (
                    volunteerings.map((volunteering, index) => (
                        <div
                            key={index}
                            className="volunteeringItem"
                        >
                            <h3>{volunteering.name}</h3>
                            <p>{volunteering.description}</p>
                            {(isVolunteer(volunteering.id) || isManager) && <button onClick={() => handleShowVolunteeringOnClick(volunteering.id)}>Show</button>}
                        </div>
                    ))
                ) : (
                    <p>No volunteerings available.</p>
                )}
                {isManager && <button onClick={handleCreateVolunteeringOnClick}>Create Volunteering</button>}
            </div>

            {isManager &&
            (<div className="managers">
                <h2>Managers</h2>
                {model.managerUsernames.length > 0 ? (
                    <ul>
                        {model.managerUsernames.map((manager, index) => (
                            <li key={index}>
                                {manager}
                                {manager === model.founderUsername && (
                                    <span style={{ fontWeight: 'bold' }}> (Founder)</span>
                                )}
                                {(sessionStorage.getItem("username") === manager && manager !== model.founderUsername) && <button onClick={handleResignOnClick}>Resign</button>}
                                {(sessionStorage.getItem("username") === model.founderUsername && manager !== model.founderUsername) && <button onClick={() => handleRemoveManagerOnClick(manager)}>X</button>}
                                {(sessionStorage.getItem("username") === model.founderUsername && manager !== model.founderUsername) && <button onClick={() => handleSetAsFounderOnClick(manager)}>Set As Founder</button>}
                                
                            </li>
                        ))}
                    </ul>
                    ) : 
                    (<p>No managers available.</p>)
                }
                <button onClick={() => handleAddNewManagerOnClick(addManagerText)}>Add New Manager</button>
                {showAddManager && <input type="text" value={addManagerText} onChange={handleAddManagerTextChange} placeholder="Manager Name"/>}
            </div>)}
        </div>
    </div>
    )
}

export default Organization