import { useEffect, useState } from 'react'
import './../css/Volunteering.css'
import { getVolunteering } from '../api/volunteering_api'
import { useParams } from "react-router-dom";
import { VolunteeringPostModel } from '../models/VolunteeringPostModel';
import { addImageToVolunteerPost, getPostPastExperiences, getVolunteeringImages, getVolunteeringName, getVolunteeringPost, getVolunteerPost, joinVolunteeringRequest, removeImageFromVolunteerPost, removeVolunteeringPost, removeVolunteerPost, sendAddRelatedUserRequest } from '../api/post_api';
import { getIsManager, getOrganizationName } from '../api/organization_api';
import { useNavigate } from 'react-router-dom';
import './../css/VolunteerPost.css'
import './../css/CommonElements.css'
import { createVolunteeringPostReport, createVolunteerPostReport } from '../api/report_api';
import PastExperienceModel from '../models/PastExpreienceModel';
import ListWithArrows, { ListItem } from './ListWithArrows';
import { VolunteerPostModel } from '../models/VolunteerPostModel';
import { supabase } from '../api/general';
import { getUserByUsername } from '../api/user_api';

function VolunteerPost() {
    const navigate = useNavigate();
    const [model, setModel] = useState<VolunteerPostModel>({id: -1, title: "", description: "", postedTime: "", lastEditedTime: "", posterUsername: "", relevance: -1, relatedUsers: [], images: [], skills: [], categories: [], keywords: []});
    const [postImages, setPostImages] = useState<ListItem[]>([]);
    const [relatedUsers, setRelatedUsers] = useState<ListItem[]>([]);
    const [isPoster, setIsPoster] = useState<boolean>(false);
    const [ready, setReady] = useState(false);
    const [showJoinFreeText, setShowJoinFreeText] = useState(false);
    const [joinFreeText, setJoinFreeText] = useState("");
    const [showReportDescription, setShowReportDescription] = useState(false);
    const [reportDescription, setReportDescription] = useState("");
    const [isHovered, setIsHovered] = useState(false);
    const [dropdownOpen, setDropdownOpen] = useState(false);
    const [selectedFile, setSelectedFile] = useState<File | null>(null)
    const [key, setKey] = useState(0)
    let { id } = useParams();
    
    const fetchVolunteerPost = async () => {
        try {
            if(id !== undefined) {
                let post: VolunteerPostModel = await getVolunteerPost(parseInt(id));
                setModel(post);

                await convertUsersToListItems(post.relatedUsers);
                convertImagesToListItems(post.images);
  
                setIsPoster(localStorage.getItem("username") === post.posterUsername);
                setReady(true);
            }
            else {
                alert("Error");
            }
        }
        catch(e) {
            alert(e);
        }
    }

    const convertImagesToListItems = (images: string[]) => {
        if(!Array.isArray(images) || images.length === 0) {
            images = ['/src/assets/defaultVolunteerPostDog.jpg'];
        }
                 
        let imageListItems: ListItem[] = images.map((image) => ({
            id: "",
            image: image, 
            title: "",  
            description: "",
        }));

        setPostImages(imageListItems);
    } 

    const convertUsersToListItems = async (usernames : string[]) => {
        const userPromises = usernames.map(username => getUserByUsername(username));
        const users = await Promise.all(userPromises);

        const usersItems: ListItem[] = users.map((user) => ({
            id: user.username,
            image: '/src/assets/defaultProfilePic.jpg', 
            title: user.name,  
            description: "",
        }));
        setRelatedUsers(usersItems);
    }

    useEffect(() => {
        fetchVolunteerPost();
    }, [id])

    const fixDate = (dateJson: string, showHour: boolean) : string => {
        let date: Date = new Date(dateJson);
        let dateStr = `${date.getDate()}/${date.getMonth() + 1}/${date.getFullYear()}`;
        let houtStr = showHour ? ` at ${date.getHours()}:${date.getMinutes()}` : "";
        return dateStr + houtStr;
    }

    const handleReportOnClick = async () => {
        setShowReportDescription(true);
    }

    const toggleDropdown = () => {
        setDropdownOpen(!dropdownOpen);
    };

    const closeDropdown = () => {
        setDropdownOpen(false);
    };

    const handleAddVolunteerOnClick = async () => {
        setShowJoinFreeText(true);
    }

    const handleSubmitOnClick = async () => {
        try {
            await sendAddRelatedUserRequest(model.id, joinFreeText);
            alert("Join request was sent successfully!");
        }
        catch(e) {
            alert(e);
        }
        setShowJoinFreeText(false);
        setJoinFreeText("");
    }

    const handleCancelOnClick = async () => {
        setShowJoinFreeText(false);
        setJoinFreeText("");
    }

    const handleSubmitReportOnClick = async () => {
        try {
            await createVolunteerPostReport(model.id, reportDescription);
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

    const handleEditPostOnClick = () => {
        navigate(`/createVolunteerPost/${model.id}`);
    }

    const handleUserOnClick = async (username: string | number) => {
        navigate(`/profile/${username}`);
    }

    const handleRemovePostOnClick = async () => {
        if(window.confirm("Are you sure you want to remove this post?")) {
            try {
                if(id !== undefined) {
                    await removeVolunteerPost(parseInt(id));
                    alert("Post removed successfully!");
                    navigate("/volunteeringPostList");
                }
                else {
                    alert("error");
                }
            }
            catch(e) {
                alert(e);
            }
        }
    }

    const onRemoveImage = async (image: string) => {
        try {
            await removeImageFromVolunteerPost(model.id, `"${image}"`);
            let newImages = model.images.filter(img => image !== img)
            let updatedModel: VolunteerPostModel = {
                ...model, 
                images: newImages
            }
            setModel(updatedModel);
            convertImagesToListItems(newImages);

        }
        catch (e) {
            //send to error page
            alert(e);
        }
    };
    
    const onAddImage = async () => {
        try {
            let {data,error} =
                await supabase.storage.from("volunteer-post-photos")
                    .upload(`${id}/${selectedFile!.name!}`, selectedFile!, {
                        cacheControl: '3600',
                        upsert: false,
                    })
                if(data == null || error !== null){
                    alert(error)
                    console.log(error)
                }else {
                    let filePath = data!.path;
                    let response = await supabase.storage.from("volunteer-post-photos").getPublicUrl(filePath);
                    let url = response.data.publicUrl;
                    await addImageToVolunteerPost(model.id, url);
                    
                    let newImages : string[] = Array.isArray(model.images) ? [...model.images, url] : [url];
                    let updatedModel: VolunteerPostModel = {
                        ...model,  // Spread the existing properties of model
                        images: newImages,  // Safely update images
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
        
    return (
        <div id="postPage" className="postPage">

            <div className="actionsMenu">
                    <img
                        src="https://icon-icons.com/icons2/2954/PNG/512/three_dots_vertical_menu_icon_184615.png"
                        alt="Profile"
                        className="dotsMenu"
                        onClick={toggleDropdown}
                    />
                    {dropdownOpen && (
                        <div className="actionDropdownMenu" onClick={toggleDropdown}>
                            {localStorage.getItem("username") === model.posterUsername && <p className="actionDropdownItem" onClick = {handleEditPostOnClick}>Edit</p>}
                            {localStorage.getItem("username") === model.posterUsername && <p className="actionDropdownItem" onClick = {handleRemovePostOnClick}>Remove</p>}
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

            <div className = "volunteerPostHeaderContainer">

            <div className='headers'>
                <h1 className='bigHeader'>{model.title}</h1>
                <p className='smallHeader'>{model.description}</p>

                <div className="info-container">
                    <button
                        className="info-button"
                        onMouseEnter={() => setIsHovered(true)} // Show on hover
                        onMouseLeave={() => setIsHovered(false)} // Hide when hover ends
                    >
                        i
                    </button>
                    {isHovered && (
                        <div className="info-tooltip">
                        <p id="postPosterUsername">Posted by: {model.posterUsername}</p>
                        <p id="postPostedTime">Posted on: {fixDate(model.postedTime, true)}</p>
                        <p id="postLastEditedTime">Last edited on: {fixDate(model.lastEditedTime, true)}</p>
                        </div>
                    )}
                </div>
            </div>

                <div className="postImages">
                    <ListWithArrows data = {postImages} limit = {1} navigateTo={''} onRemove={onRemoveImage} isOrgManager={isPoster}></ListWithArrows>
                    {isPoster && <div className='uplaodImage'>
                    <input type="file" onChange={onFileUpload} accept="image/*" key={key}/>
                    <button onClick={onAddImage} className="orangeCircularButton">Upload Image</button>
                    </div>}
                </div>
            </div>

            
            <div className="listContainer">
                <h2 className='volunteerPostheader'>Friends In This Post</h2>

                <div className='generalUsersList'>
                        {relatedUsers.length > 0 ? (
                            <ul className='relatedUsersList'>
                                {relatedUsers.map((user) => (
                                    <li className='managersListItem' key={user.id} onClick={() => handleUserOnClick(user.id)}>
                                        <img className = 'managerProfilePic' src={user.image}></img>
                                        <p className='managerName'>{user.title}</p>
                                        {user.id === model.posterUsername && <p className='isPoster'>(Poster)</p>}
                                        
                                    </li>
                                ))}
                            </ul>
                            ) : 
                            (<p>No related users available.</p>)
                        }
                </div>

                <div id="addVolunteerButton" className="addVolunteerButton">
            
                {localStorage.getItem("username") === model.posterUsername && <button className='orangeCircularButton' onClick={handleAddVolunteerOnClick}>Add Volunteer</button>}
    
                    {showJoinFreeText && (
                        <div className="popup-window">
                            <div className="popup-header">
                            <span className="popup-title">Add Volunteer</span>
                            <button className="cancelButton" onClick={handleCancelOnClick}>
                                X
                            </button>
                            </div>
                            <div className="popup-body">
                                <textarea placeholder="Volunteer Username" value={joinFreeText} onChange={(e) => setJoinFreeText(e.target.value)}></textarea>
                                <button className="orangeCircularButton" onClick={handleSubmitOnClick}>
                                    Submit
                                </button>
                            </div>
                        </div>
                    )}
            </div>
            
            </div>

            <div className='catsAndSkills'>
                <div className='cats'>
                    <h2 className='volunteerPostheader'>Offered Categories</h2>
                    <h2 className='smallHeader'>Extracted Automatically Using AI</h2>
                    <ul className='catsList'>
                        <div>
                    {model.categories.length > 0 ? model.categories.map((item, index) => (
                        <li key={index}>{item}</li> // Always add a unique 'key' prop when rendering lists
                    )) : <p className='notFound'>No Categories Found</p>}
                    </div>
                    </ul>
                </div>
    
                <div className='skills'>
                    <h2 className='volunteerPostheader'>Offered Skills</h2>
                    <h2 className='smallHeader'>Extracted Automatically Using AI</h2>
                    <ul className='skillsList'>
                    <div>
                    {model.skills.length > 0 ? model.skills.map((item, index) => (
                        <li key={index}>{item}</li> // Always add a unique 'key' prop when rendering lists
                    )) : <p className='notFound'>No Skills Found</p>}
                    </div>
                    </ul>
                </div>
            </div>
        </div>
    )
}


export default VolunteerPost;