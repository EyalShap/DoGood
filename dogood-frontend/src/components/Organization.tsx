import OrganizationModel from '../models/OrganizationModel';
import { useEffect, useState } from 'react'
import './../css/Organization.css'
import './../css/CommonElements.css'
import { getOrganization, getIsManager, getOrganizationVolunteerings, removeOrganization, removeManager, setFounder, sendAssignManagerRequest, resign, getUserRequests, getUserVolunteerings, removeImageFromOrganization, addImageToOrganization } from '../api/organization_api'
import { useParams } from "react-router-dom";
import { useNavigate } from 'react-router-dom';
import Volunteering from './Volunteering';
import { getVolunteering } from '../api/volunteering_api';
import ListWithArrows, { ListItem } from './ListWithArrows';
import { getUserByUsername } from '../api/user_api';
import { getVolunteeringImages } from '../api/post_api';
import { supabase } from '../api/general';
import { createOrganizationReport } from '../api/report_api';

function Organization() {
    const navigate = useNavigate();
    const [model, setModel] = useState<OrganizationModel>({id: -1, name: "", description: "", phoneNumber: "", email: "", volunteeringIds: [-1], managerUsernames: [], founderUsername: "", imagePaths: []});
    let { id } = useParams();
    const [isManager, setIsManager] = useState(false);
    const [userVolunteerings, setUserVolunteerings] = useState<number[]>([]);
    const [volunteerings, setVolunteerings] = useState<ListItem[]>([]);
    const [managers, setManagers] = useState<ListItem[]>([]);
    const [ready, setReady] = useState(false);
    const [showAddManager, setShowAddManager] = useState(false);
    const [addManagerText, setAddManagerText] = useState('');
    const [dropdownOpen, setDropdownOpen] = useState(false);
    const [selectedFile, setSelectedFile] = useState<File | null>(null)
    const [key, setKey] = useState(0)
    const [orgImages, setOrgImages] = useState<ListItem[]>([]);
    const [showReportDescription, setShowReportDescription] = useState(false);
    const [reportDescription, setReportDescription] = useState("");
    const isMobile = window.innerWidth <= 768;
    
    const fetchOrganization = async () => {
        try{
            let found = await getOrganization(parseInt(id!));
            console.log(found);
            setModel(found);

            let managerIds = found.managerUsernames;
            const userPromises = managerIds.map(username => getUserByUsername(username));
            const users = await Promise.all(userPromises);

            const managersItems: ListItem[] = users.map((user) => ({
                id: user.username,
                image: '/src/assets/defaultProfilePic.jpg', 
                title: user.name,  
                description: "",
            }));
            setManagers(managersItems);

            convertImagesToListItems(found.imagePaths);
        }
        catch(e){
            //send to error page
            alert(e)
        }
    }
    
    const convertImagesToListItems = (images: string[]) => {
        let imageListItems: ListItem[] = [];
        imageListItems = images.map((image) => ({
            id: "",
            image: image, 
            title: "",  
            description: "",
        }));
        if(imageListItems.length === 0) {
            imageListItems = [{id : "", image: "/src/assets/defaultOrganizationDog.jpg", title: "", description: ""}];
        }
        setOrgImages(imageListItems);
    } 

    const fetchVolunteerings = async () => {
        try {
            const volunteeringDetails = await getOrganizationVolunteerings(model.id);

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
                    console.log("hi");
                    console.log(newManager);
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
                        founderUsername: model.founderUsername,
                        imagePaths: model.imagePaths
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
                        founderUsername: newFounder,
                        imagePaths: model.imagePaths
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

    const handleAddManagerTextChange = (event: React.ChangeEvent<HTMLTextAreaElement>) => {
        setAddManagerText(event.target.value);  // Update the state with the new value
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
                        founderUsername: model.founderUsername,
                        imagePaths: model.imagePaths
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

    const isVolunteer = (volunteeringId: number | string) : boolean => {
        return userVolunteerings.includes(volunteeringId as number);
    }

    const toggleDropdown = () => {
        setDropdownOpen(!dropdownOpen);
    };

    const closeDropdown = () => {
        setDropdownOpen(false);
    };

    const onRemoveImage = async (image: string) => {
        try {
            await removeImageFromOrganization(model.id, `"${image}"`);
            let updatedModel: OrganizationModel = {
                id: model.id,
                name: model.name,
                description: model.description,
                phoneNumber: model.phoneNumber,
                email: model.email,
                volunteeringIds: model.volunteeringIds,
                managerUsernames: model.managerUsernames,
                founderUsername: model.founderUsername,
                imagePaths: model.imagePaths.filter(img => image !== img)
            }
            setModel(updatedModel);
            convertImagesToListItems(model.imagePaths.filter(img => image !== img));

        }
        catch (e) {
            //send to error page
            alert(e);
        }
    };
    
    const onAddImage = async () => {
        try {
            let {data,error} =
                await supabase.storage.from("organization-photos")
                    .upload(`${id}/${selectedFile!.name!}`, selectedFile!, {
                        cacheControl: '3600',
                        upsert: false,
                    })
                if(data == null || error !== null){
                    alert(error)
                    console.log(error)
                }else {
                    let filePath = data!.path;
                    let response = await supabase.storage.from("organization-photos").getPublicUrl(filePath);
                    let url = response.data.publicUrl;
                    await addImageToOrganization(model.id, url);
                    
                    let newImages = Array.isArray(model.imagePaths) ? [...model.imagePaths, url] : [url]
                    let updatedModel: OrganizationModel = {
                        ...model,  // Spread the existing properties of model
                        imagePaths: newImages,  // Safely update images
                    };
                    setModel(updatedModel);
                    convertImagesToListItems(newImages);
                    setSelectedFile(null)
                    setKey(prevState => 1-prevState)
                }
            }
            catch (e) {
                //send to error page
                alert(e);
            }
        };
    
        const onFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
            setSelectedFile(e.target.files![0])
        }

        const handleUserOnClick = async (username: string | number) => {
            navigate(`/profile/${username}`);
        }

        const handleReportOnClick = async () => {
            setShowReportDescription(true);
        }

        const handleSubmitReportOnClick = async () => {
                try {
                    await createOrganizationReport(model.id, reportDescription);
                    alert("Thank you for your report!");
                }
                catch(e) {
                    alert(e);
                }
                setShowReportDescription(false);
                setReportDescription("");
            }
        
            const handleCancelReportOnClick = () => {
                setShowReportDescription(false);
                setReportDescription("");
            }
    
    return (
        <div className='generalPageDiv'>
            
            <div className="orgActionsMenu">
                <img
                    src="https://icon-icons.com/icons2/2954/PNG/512/three_dots_vertical_menu_icon_184615.png"
                    alt="Profile"
                    className="dotsMenu"
                    onClick={toggleDropdown}
                />
                {dropdownOpen && (
                    <div className="actionDropdownMenu" onClick={toggleDropdown}>
                        {isManager && <p className="actionDropdownItem" onClick = {handleRemoveOrganizationOnClick}>Remove</p>}
                        {isManager && <p className="actionDropdownItem" onClick = {handleEditOrganizationOnClick}>Edit</p>}
                        <p className="actionDropdownItem" onClick={(e) => { e.stopPropagation(); handleReportOnClick();}}>Report</p>
                    
                        {showReportDescription && (
                                <div className="popup-window">
                                    <div className="popup-header">
                                    <span className="popup-title">Report</span>
                                    <button className="cancelButton" onClick={handleCancelReportOnClick}>
                                        X
                                    </button>
                                    </div>
                                    <div className="popup-body">
                                        <textarea placeholder="What went wrong?..." onClick={(e) => { e.stopPropagation()}} onChange={(e) => setReportDescription(e.target.value)}></textarea>
                                        <button className="orangeCircularButton" onClick={handleSubmitReportOnClick}>
                                            Submit
                                        </button>
                                    </div>
                                </div>
                            )}

                    </div>
                )}
            </div>

            <div className = "orgHeaderContainer">
                <div className="organizationImages">
                    <ListWithArrows data = {orgImages} limit = {1} navigateTo={''} onRemove={onRemoveImage} isOrgManager={isManager}></ListWithArrows>
                    {isManager && <div className='uplaodImage'>
                    <input type="file" onChange={onFileUpload} accept="image/*" key={key}/>
                    <button onClick={onAddImage} className="orangeCircularButton">Upload Image</button>
                    </div>}
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
                        <ListWithArrows data = {volunteerings} limit = {isMobile ? 1 : 3} navigateTo={'volunteering'} clickable={(id) => isVolunteer(id) || isManager}></ListWithArrows>
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
                                <li className='managersListItem' key={manager.id} onClick={() => handleUserOnClick(manager.id)}>
                                    <img className = 'managerProfilePic' src={manager.image}></img>
                                    <p className='managerName'>{manager.title}</p>
                                    {manager.id === model.founderUsername && <p className='isFounder'>(Founder)</p>}
                                    {(localStorage.getItem("username") === manager.id && manager.id !== model.founderUsername) && <button onClick={handleResignOnClick} className='orangeCircularButton'>Resign</button>}
                                    {(localStorage.getItem("username") === model.founderUsername && manager.id !== model.founderUsername) && <button className='orangeCircularButton' onClick={() => handleRemoveManagerOnClick(manager.id.toString())}>X</button>}
                                    {(localStorage.getItem("username") === model.founderUsername && manager.id !== model.founderUsername) && <button className='orangeCircularButton' onClick={() => handleSetAsFounderOnClick(manager.id.toString())}>Set As Founder</button>}
                                    
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
                                <textarea placeholder="New manager username" value={addManagerText} onChange={handleAddManagerTextChange}></textarea>
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