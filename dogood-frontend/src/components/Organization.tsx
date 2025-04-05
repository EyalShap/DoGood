import OrganizationModel from '../models/OrganizationModel';
import { useEffect, useRef, useState } from 'react'
import './../css/Organization.css'
import './../css/CommonElements.css'
import { getOrganization, getIsManager, getOrganizationVolunteerings, removeOrganization, removeManager, setFounder, sendAssignManagerRequest, resign, getUserRequests, getUserVolunteerings, removeImageFromOrganization, addImageToOrganization, uploadSignature, getSignature, removeSignature } from '../api/organization_api'
import { useParams } from "react-router-dom";
import { useNavigate } from 'react-router-dom';
import { getVolunteering } from '../api/volunteering_api';
import ListWithArrows, { ListItem } from './ListWithArrows';
import { getUserByUsername } from '../api/user_api';
import { getVolunteeringImages } from '../api/post_api';
import { supabase } from '../api/general';
import { createOrganizationReport } from '../api/report_api';
import SignatureCanvas from "react-signature-canvas";
import defaultOrgImage from "/src/assets/defaultOrganizationDog.jpg";
import defaultProfilePic from '/src/assets/defaultProfilePic.jpg';
import defaultVolunteeringPic from '/src/assets/defaultVolunteeringDog.webp';


function Organization() {
    const navigate = useNavigate();
    const [model, setModel] = useState<OrganizationModel>({id: -1, name: "", description: "", phoneNumber: "", email: "", volunteeringIds: [-1], managerUsernames: [], founderUsername: "", imagePaths: [], signature: null});
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
    const [signature, setSignature] = useState<string>("");
    const [selectedSignature, setSelectedSignature] = useState<File | null>(null)
    const [keySignature, setKeySignature] = useState(0)
    const sigCanvas = useRef<SignatureCanvas | null>(null);
    const [imageURL, setImageURL] = useState(null);
    const isMobile = window.innerWidth <= 768;
    
    const fetchOrganization = async () => {
        try{
            let found = await getOrganization(parseInt(id!));
            setModel(found);

            await fecthManagers(found.managerUsernames, found.founderUsername);
            convertImagesToListItems(found.imagePaths);

            try {
                const signatureBlob : Blob = await getSignature(found.id);
                setSignature(signatureBlob.size > 0 ? URL.createObjectURL(signatureBlob) : "");
            }
            catch(e) {
                setSignature("");
                console.log("here");
            }    
            
            
        }
        catch(e){
            //send to error page
            alert(e)
        }
    }

    const fecthManagers = async (managerIds: string[], founder: string) => {
        const userPromises = managerIds.map(username => getUserByUsername(username));
        const users = await Promise.all(userPromises);

        const managersItems: ListItem[] = users.map((user) => ({
            id: user.username,
            image: (user.profilePicUrl !== null && user.profilePicUrl !== "") ? user.profilePicUrl : defaultProfilePic, 
            title: user.name,  
            description: user.username === founder ? "(Founder)" : "",
        }));
        setManagers(managersItems);
        console.log(managersItems);
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
            imageListItems = [{id : "", image: defaultOrgImage, title: "", description: ""}];
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
                image: imagesArray[index].length > 0 ? imagesArray[index][0] : defaultVolunteeringPic, 
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
        navigate('./createvolunteering/0');
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
        navigate(`/createOrganization/${id}/0`);
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
                    let newManagers = model.managerUsernames.filter((manager) => manager !== managerToRemove);

                    let updatedModel: OrganizationModel = {
                        ...model,
                        managerUsernames: newManagers,
                    };
                    
                    setModel(updatedModel);
                    fecthManagers(newManagers, model.founderUsername);
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
                        ...model,
                        founderUsername: newFounder,
                    };
                    setModel(updatedModel);
                    fecthManagers(model.managerUsernames, newFounder);
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
                    let newManagers = model.managerUsernames.filter((manager) => manager !== localStorage.getItem("username"));

                    let updatedModel: OrganizationModel = {
                        ...model,
                        managerUsernames: newManagers,
                    };
                    setModel(updatedModel);
                    await fecthManagers(newManagers, model.founderUsername);
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
                imagePaths: model.imagePaths.filter(img => image !== img),
                signature: model.signature
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

        const onSelectedSignature = async (e: React.ChangeEvent<HTMLInputElement>) => {
            setSelectedSignature(e.target.files![0])
        }

        const onUploadSignatureOnClick = async () => {
            try {
                if(selectedSignature === null) {
                    alert("Did not upload signature.");
                }
                else {
                    await uploadSignature(model.id, selectedSignature);
                    alert("Signature uploaded successfully!");
                    setSelectedSignature(null);
                    setSignature(URL.createObjectURL(selectedSignature));
                    setKeySignature(prevState => 1-prevState);
                }
            }
            catch (e) {
                alert(e);
            }
        }

        const handleRemoveSignatureOnClick = async () => {
            try {
                if(window.confirm("Are you sure you want to remove the signature?")) {
                    await removeSignature(model.id);
                    setSelectedSignature(null);
                    setSignature("");
                }
            }
            catch (e) {
                alert(e);
            }
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

            const clearSignature = () => {
                sigCanvas.current?.clear(); // 👈 Safe optional chaining
                setImageURL(null);
              };
            
              const saveSignature = async () => {
                if (!sigCanvas.current || sigCanvas.current.isEmpty()) return;
                
                try{
                const canvas = sigCanvas.current.getCanvas();  // Directly get the canvas
                const dataURL = canvas.toDataURL("image/png"); // Get data URL from canvas

                // Convert dataURL (base64) to Blob
                const blob = await fetch(dataURL).then(res => res.blob());
                const file = new File([blob], "signature.png", { type: "image/png" });

                // Upload signature and wait for completion
                await uploadSignature(model.id, file);

                // Update state with the object URL for preview
                setSignature(URL.createObjectURL(file));
                }
                catch(e) {
                    alert(e);
                }
              };
            
    
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
            
            <div className='orgList' style = {{width: '100%'}}>
            <div className="listContainer">
                <h2 className='listHeader'>Our Volunteerings</h2>
                <div className='generalList'>
                    {volunteerings.length > 0 ? (
                        <ListWithArrows data = {volunteerings} limit = {isMobile ? 1 : Math.min(3,volunteerings.length)} navigateTo={'volunteering'} clickable={(id) => isVolunteer(id) || isManager}></ListWithArrows>                    ) : (
                        <p>No volunteerings available.</p>
                    )}
                    {isManager && <button className = 'orangeCircularButton' onClick={handleCreateVolunteeringOnClick}>Create Volunteering</button>}
                </div>
            </div>
            </div>

            <div className='orgList' style = {{width: '100%'}}>
            <div className="listContainer">
                <h2 className='listHeader'>Our Managers</h2>
                <div className='generalList'>
                <ListWithArrows 
                    data={managers} 
                    limit = {isMobile ? 1 : Math.min(3,volunteerings.length)}
                    navigateTo={`profile`} 
                    onRemove={(username) => handleRemoveManagerOnClick(username)} 
                    showResign = {(username) => localStorage.getItem("username") === username && username !== model.founderUsername}
                    showFire = {(username) => localStorage.getItem("username") === model.founderUsername && username !== model.founderUsername}
                    showSetAsFounder = {(username) => localStorage.getItem("username") === model.founderUsername && username !== model.founderUsername}
                    resignHandler={handleResignOnClick}
                    fireHandler={handleRemoveManagerOnClick}
                    setFounderHandler={handleSetAsFounderOnClick}
                    clickable={() => true}
                    >
                </ListWithArrows>
                
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
            </div>

            <div className='orgList' style = {{width: '100%'}}>
            {isManager && <div className='signature' style={{marginTop:'40px'}}>
                <h2 className='listHeader'>Organization Signature</h2>
                <h2 className='sigDesc'>Upload the organization signature to automatically sign forms for volunteers!</h2>
                {signature !== "" && <img src={signature}></img>}
                {signature === "" && <p style={{marginBottom:'-10px'}}>No signature available.</p>}
                {signature !== "" && localStorage.getItem("username") === model.founderUsername && <button className="removeButton" onClick = {handleRemoveSignatureOnClick}>X</button>}

                {localStorage.getItem("username") === model.founderUsername && <div className='uploads'>
                    <div className='upload uploadSig'>
                        <h2 className='sigDesc uploadHeader'>Upload your signature as a picture</h2>
                        <div className='uploadInput'>
                        <input type="file" accept="image/*" onChange={onSelectedSignature} key={keySignature}/>
                        <button onClick={onUploadSignatureOnClick} className={`orangeCircularButton ${selectedSignature === null ? 'disabledButton' : ''}`}>Upload Signature</button>
                        </div>
                    </div>

                    <h2 className='sigDesc or' style={{fontSize:'1.5rem'}}>OR</h2>

                    <div className='upload drawSig'>
                        <h2 className='sigDesc'>Sign here</h2>
                        <div>
                        <SignatureCanvas
                            ref={sigCanvas} // Attach ref here
                            penColor="black"
                            canvasProps={{ 
                                className: "border", 
                                style: { border:"1px solid black", marginBottom:'20px', width:'90%', height:'150px' }
                             }}
                        />
                        </div>
                        <div className="mt-2">
                            <button onClick={saveSignature} className="orangeCircularButton">Save</button>
                            <button onClick={clearSignature} className="orangeCircularButton">Clear</button>
                        </div>
                    </div>
                </div>}
            </div>}
            </div>

        </div>

    )
}

export default Organization