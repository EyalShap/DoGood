import OrganizationModel from '../models/OrganizationModel';
import { useEffect, useState } from 'react'
import './../css/Organization.css'
import './../css/CommonElements.css'
import { getOrganization, getIsManager, getOrganizationVolunteerings, removeOrganization, removeManager, setFounder, sendAssignManagerRequest, resign, getUserRequests, getUserVolunteerings } from '../api/organization_api'
import { useParams } from "react-router-dom";
import { useNavigate } from 'react-router-dom';
import Volunteering from './Volunteering';
import { getVolunteering } from '../api/volunteering_api';
import ListWithArrows, { ListItem } from './ListWithArrows';
import { getUserByUsername } from '../api/user_api';

function Organization() {
    const navigate = useNavigate();
    const [model, setModel] = useState<OrganizationModel>({id: -1, name: "", description: "", phoneNumber: "", email: "", volunteeringIds: [-1], managerUsernames: [], founderUsername: ""});
    let { id } = useParams();
    const [isManager, setIsManager] = useState(false);
    const [userVolunteerings, setUserVolunteerings] = useState<number[]>([]);
    const [volunteerings, setVolunteerings] = useState<ListItem[]>([]);
    const [managers, setManagers] = useState<ListItem[]>([]);
    const [ready, setReady] = useState(false);
    const [showAddManager, setShowAddManager] = useState(false);
    const [addManagerText, setAddManagerText] = useState('');
    const [dropdownOpen, setDropdownOpen] = useState(false);
    
    const fetchOrganization = async () => {
        try{
            let found = await getOrganization(parseInt(id!));
            setModel(found);

            let managerIds = model.managerUsernames;
            const userPromises = managerIds.map(username => getUserByUsername(username));
            const users = await Promise.all(userPromises);

            const managersItems: ListItem[] = users.map((user) => ({
                id: user.username,
                image: 'https://cdn.thewirecutter.com/wp-content/media/2021/03/dogharnesses-2048px-6907-1024x682.webp', 
                title: user.name,  
                description: "",
            }));
            setManagers(managersItems);
        }
        catch(e){
            //send to error page
            alert(e)
        }
    }

    const fetchVolunteerings = async () => {
        try {
            const volunteeringDetails = await getOrganizationVolunteerings(model.id);

            const listItems: ListItem[] = volunteeringDetails.map((volunteering) => ({
                id: volunteering.id,
                image: 'https://cdn.thewirecutter.com/wp-content/media/2021/03/dogharnesses-2048px-6907-1024x682.webp', 
                title: volunteering.name,  
                description: volunteering.description, // assuming 'summary' is a short description
            }));

            setVolunteerings(listItems);

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
            let username: string | null = localStorage.getItem("username");
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

    const handleCancelOnClick = async () => {
        setShowAddManager(false);
        setAddManagerText("");
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
                        managerUsernames: model.managerUsernames.filter((manager) => manager !== localStorage.getItem("username")),
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

    const toggleDropdown = () => {
        setDropdownOpen(!dropdownOpen);
    };

    const closeDropdown = () => {
        setDropdownOpen(false);
    };
    
    return (
        <div className='generalPageDiv'>
            
            <div className="actionsMenu">
                <img
                    src="https://icon-icons.com/icons2/2954/PNG/512/three_dots_vertical_menu_icon_184615.png"
                    alt="Profile"
                    className="dotsMenu"
                    onClick={toggleDropdown}
                />
                {dropdownOpen && (
                    <div className="actionDropdownMenu" onMouseLeave={closeDropdown}>
                        {isManager && <p className="actionDropdownItem" onClick = {handleRemoveOrganizationOnClick}>Remove Organization</p>}
                        {isManager && <p className="actionDropdownItem" onClick = {handleEditOrganizationOnClick}>Edit Organization</p>}
                        {isManager && <p className="actionDropdownItem" onClick = {handleEditOrganizationOnClick}>Report Organization</p>}
                    </div>
                )}
            </div>

            <div className = "orgHeaderContainer">
                <div className="orgImageContainer">
                    <img src="https://i.pinimg.com/564x/8c/f6/b0/8cf6b01e7f02e2befa711da2c9030f36.jpg" alt="Image" className="orgImage" />
                </div>

                <div className='orgInfoText'>
                    <h1 className='bigHeader'>{model.name}</h1>
                    <p className='smallHeader'>{model.description}</p>

                    <div className = "contact">
                        <h2 className='smallHeader'>Contact Us:</h2>
                        <div className='email'>
                            <img src = 'https://static.vecteezy.com/system/resources/previews/021/454/517/non_2x/email-confirmation-app-icon-email-icon-free-png.png'></img>
                            <a href={`mailto:${model.email}`}>{model.email}</a>
                        </div>
                        <div className='phone'>
                            <img src = 'https://static.vecteezy.com/system/resources/thumbnails/019/923/706/small_2x/telephone-and-mobile-phone-icon-calling-icon-transparent-background-free-png.png'></img>
                            <p>{model.phoneNumber}</p>
                        </div>
                    </div>
                </div>
            </div>

            <div className="listContainer">
                <h2 className='listHeader'>Our Volunteerings</h2>
                <div className='generalList'>
                    {volunteerings.length > 0 ? (
                        <ListWithArrows data = {volunteerings} limit = {4} navigateTo={'volunteering'}></ListWithArrows>
                    ) : (
                        <p>No volunteerings available.</p>
                    )}
                    {isManager && <button className = 'orangeCircularButton' onClick={handleCreateVolunteeringOnClick}>Create Volunteering</button>}
                </div>
            </div>

            <div className="listContainer">
                <h2 className='listHeader'>Our Managers</h2>
                <div className='generalList'>
                    {managers.length > 0 ? (
                        <ul className='managersList'>
                            {managers.map((manager) => (
                                <li className='managersListItem' key={manager.id}>
                                    <img className = 'managerProfilePic' src={manager.image}></img>
                                    <p className='managerName'>{manager.title}</p>
                                    {manager.id === model.founderUsername && <p className='isFounder'>(Founder)</p>}
                                    {(localStorage.getItem("username") === manager.id && manager.id !== model.founderUsername) && <button onClick={handleResignOnClick}>Resign</button>}
                                    {(localStorage.getItem("username") === model.founderUsername && manager.id !== model.founderUsername) && <button onClick={() => handleRemoveManagerOnClick(manager.id.toString())}>X</button>}
                                    {(localStorage.getItem("username") === model.founderUsername && manager.id !== model.founderUsername) && <button onClick={() => handleSetAsFounderOnClick(manager.id.toString())}>Set As Founder</button>}
                                    
                                </li>
                            ))}
                        </ul>
                        ) : 
                        (<p>No managers available.</p>)
                    }
                </div>
                {isManager && <button className = 'orangeCircularButton' onClick={() => handleAddNewManagerOnClick(addManagerText)}>Invite A New Manager To The Family</button>}
                {showAddManager && 
                    <div className="popup-window">
                        <div className="popup-header">
                            <span className="popup-title">Add Manager</span>
                            <button className="cancelButton" onClick={handleCancelOnClick}>
                                X
                            </button>
                            </div>
                            <div className="popup-body">
                                <textarea placeholder="New manager username"></textarea>
                                <button className="orangeCircularButton" onClick={() => handleAddNewManagerOnClick(addManagerText)}>
                                    Submit
                                </button>
                            </div>
                        </div>
                }
            </div>
        </div>

    )
}

export default Organization