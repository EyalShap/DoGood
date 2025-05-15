import { useEffect, useState } from 'react'
import './../css/Volunteering.css'
import { getVolunteering } from '../api/volunteering_api'
import { useParams } from "react-router-dom";
import { VolunteeringPostModel } from '../models/VolunteeringPostModel';
import { addImageToVolunteerPost, getPostPastExperiences, getVolunteeringImages, getVolunteeringName, getVolunteeringPost, getVolunteerPost, joinVolunteeringRequest, removeImageFromVolunteerPost, removeRelatedUser, removeVolunteeringPost, removeVolunteerPost, sendAddRelatedUserRequest, setPoster, setVolunteerPostCategories, setVolunteerPostSkills } from '../api/post_api';
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
import { getOpenPostChats } from '../api/chat_api';
import User from '../models/UserModel';
import defaultProfilePic from '/src/assets/defaultProfilePic.jpg';
import defaultVolunteerPostPic from '/src/assets/defaultVolunteerPostDog.jpg';

function VolunteerPost() {
    const navigate = useNavigate();
    const [model, setModel] = useState<VolunteerPostModel>({id: -1, title: "", description: "", postedTime: "", lastEditedTime: "", posterUsername: "", relevance: -1, relatedUsers: [], images: [], skills: [], categories: [], keywords: []});
    const [postImages, setPostImages] = useState<ListItem[]>([]);
    const [relatedUsers, setRelatedUsers] = useState<ListItem[]>([]);
    const [isPoster, setIsPoster] = useState<boolean>(false);
    const [isRelatedUser, setIsRelatedUser] = useState<boolean>(false);
    const [isActorInPost, setIsActorInPost] = useState<boolean>(false);
    const [ready, setReady] = useState(false);
    const [showJoinFreeText, setShowJoinFreeText] = useState(false);
    const [joinFreeText, setJoinFreeText] = useState("");
    const [showReportDescription, setShowReportDescription] = useState(false);
    const [reportDescription, setReportDescription] = useState("");
    const [showChatList, setShowChatList] = useState(false);
    const [openChatUsers, setOpenChatUsers] = useState<string[]>([]);
    const [openChatUsersProfilePics, setOpenChatUsersProfilePics] = useState<{ [key: string]: string }>({});
    const [isHovered, setIsHovered] = useState(false);
    const [dropdownOpen, setDropdownOpen] = useState(false);
    const [selectedFile, setSelectedFile] = useState<File | null>(null)
    const [key, setKey] = useState(0)
    const [skillsInput, setSkillsInput] = useState("");
    const [catsInput, setCatsInput] = useState("");

    const isMobile = window.innerWidth <= 768;

    let { id } = useParams();
    
    const fetchVolunteerPost = async () => {
        try {
            if(id !== undefined) {
                let post: VolunteerPostModel = await getVolunteerPost(parseInt(id));
                setModel(post);

                setSkillsInput(post.skills.join(", "));
                setCatsInput(post.categories.join(", "));

                await convertUsersToListItems(post.relatedUsers, post.posterUsername);
                convertImagesToListItems(post.images);
  
                setIsPoster(localStorage.getItem("username") === post.posterUsername);
                setIsRelatedUser(post.relatedUsers.includes(localStorage.getItem("username") ?? ""));

                const actorInPost = post.relatedUsers.includes(localStorage.getItem("username") ?? "");
                setIsActorInPost(actorInPost);
                
                if(actorInPost) {
                    let openChatUsers = await getOpenPostChats(post.id);
                    setOpenChatUsers(openChatUsers);
                    
                    for (let username of openChatUsers) {
                        try {
                            const user: User = await getUserByUsername(username);
                            const image: string = (user.profilePicUrl !== null && user.profilePicUrl !== "") ? user.profilePicUrl : defaultProfilePic;
                                                    
                            setOpenChatUsersProfilePics((prev) => ({
                                ...prev,
                                [username]: image,
                            }));
                        } catch (e) {
                            alert(e);
                        }
                    }
                }

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
            images = [defaultVolunteerPostPic];
        }
                 
        let imageListItems: ListItem[] = images.map((image) => ({
            id: "",
            image: image, 
            title: "",  
            description: "",
        }));

        setPostImages(imageListItems);
    } 

    const convertUsersToListItems = async (usernames : string[], posterUsername: string) => {
        const userPromises = usernames.map(username => getUserByUsername(username));
        const users = await Promise.all(userPromises);

        const usersItems: ListItem[] = users.map((user) => ({
            id: user.username,
            image: (user.profilePicUrl !== null && user.profilePicUrl !== "") ? user.profilePicUrl : defaultProfilePic, 
            title: user.name,  
            description: user.username === posterUsername ? "(Poster)" : "",
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

    const handleChatOnClick = () => {
        if(isActorInPost) {
            setShowChatList(true);
        }
        else {
            navigate("./chat");
        }
    }

    const handleOpenChatOnClick = (username: string) => {
        navigate(`./chat/${username}`);
    }

    const handleCancelChatOnClick = async () => {
        setShowChatList(false);
    }

    const onRemoveVolunteer = async (username: string) => {
        try {
            if(window.confirm("Are you sure you want to remove this user from the post?")) {
                await removeRelatedUser(model.id, username);
                let newRelatedUser = model.relatedUsers.filter(user => username !== user)
                let updatedModel: VolunteerPostModel = {
                    ...model, 
                    relatedUsers: newRelatedUser
                }
                setModel(updatedModel);

                await convertUsersToListItems(newRelatedUser, model.posterUsername);
            }
        }
        catch (e) {
            //send to error page
            alert(e);
        }
    };
        
    const onSetAsFounder = async (username: string) => {
        try {
            if(window.confirm(`Are you sure you want to set ${username} as the poster of this post?`)) {
                await setPoster(model.id, username);
                let updatedModel: VolunteerPostModel = {
                    ...model, 
                    posterUsername: username
                }
                setModel(updatedModel);
                await convertUsersToListItems(model.relatedUsers, username);
            }
        }
        catch (e) {
            //send to error page
            alert(e);
        }
    };

    const saveSkillsOnClick = async () => {
        try {
            const updated = skillsInput.trim() !== "" ? skillsInput.split(",").map(skill => skill.trim()) : [];
            await setVolunteerPostSkills(model.id, updated);
            
            let updatedModel: VolunteerPostModel = {
                    ...model, 
                    skills: updated
                }
                setModel(updatedModel);
                        
            alert("Skills updated successfully!");
        }
        catch (e) {
            //send to error page
            alert(e);
        }
    };

     const saveCatsOnClick = async () => {
        try {
            const updated = catsInput.trim() !== "" ? catsInput.split(",").map(skill => skill.trim()) : [];
            await setVolunteerPostCategories(model.id, updated);
            
            let updatedModel: VolunteerPostModel = {
                    ...model, 
                    categories: updated
            }
            setModel(updatedModel);
                        
            alert("Categories updated successfully!");
        }
        catch (e) {
            //send to error page
            alert(e);
        }
    };
        
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
                <h1 className='bigHeader' style={{overflowWrap: "break-word"}}>{model.title}</h1>
                <p className='smallHeader' style={{overflowWrap: "break-word"}}>{model.description}</p>

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

            <div className="relatedUsersContainer">
                <h2 className='relatedVolunteersHeader'>{isActorInPost ? "Contact Organization Managers" : "Contact Volunteers"}</h2>
                <button className="orangeCircularButton" onClick={handleChatOnClick} style={{marginTop:'20px'}}>{isActorInPost ? "Open Chat" : "Start Chat"}</button>
                {showChatList && (
                    <div className="popup-window">
                        <div className="popup-header">
                            <span className="popup-title">Open Chats</span>
                            <button className="cancelButton" onClick={handleCancelChatOnClick}>
                                X
                            </button>
                        </div>
                        <div className="popup-body chats-popup-body">
                            <ul className="openChatsList">
                                {openChatUsers.length > 0 && openChatUsers.map((username, index) => (
                                <li key={index} onClick={() => handleOpenChatOnClick(username)}>
                                    <div className='openChatListItem'>
                                    <img className='openChatProfilePic' src={openChatUsersProfilePics[username]}></img>
                                    <p className='chatUsername'>{username}</p>
                                    </div>
                                </li>
                                ))}
                                {openChatUsers.length === 0 && <p className='chatUsername'>No Open Chats.</p>}
                            </ul>
                            
                            
                        </div>
                    </div>
                )}
            </div>

            <div className="relatedUsersContainer">
                <h2 className='relatedVolunteersHeader'>Friends In This Post</h2>
                <div className='generalList'>
                    <ListWithArrows
                        data={relatedUsers}
                        limit = {isMobile ? 1 : 3}
                        navigateTo={`profile`}
                        clickable={() => true}
                        showResign = {(username) => localStorage.getItem("username") === username && username !== model.posterUsername}
                        showFire = {(username) => localStorage.getItem("username") === model.posterUsername && username !== model.posterUsername}
                        showSetAsFounder = {(username) => localStorage.getItem("username") === model.posterUsername && username !== model.posterUsername}
                        fireHandler={onRemoveVolunteer}
                        resignHandler={onRemoveVolunteer}
                        setFounderHandler={onSetAsFounder}
                        setFounderOrPoster = {false}
                    ></ListWithArrows>
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
                <div className='skills'>
                    <h2 className='volunteerPostheader'>Offered Skills</h2>
                    <h2 className='smallHeader'>Extracted Automatically Using AI</h2>
                    <div className="list-section">
                        <textarea
                            value={skillsInput}
                            onChange={(e) => setSkillsInput(e.target.value)}
                            placeholder="Enter skills separated by commas"
                        />
                        {isRelatedUser && <button onClick={saveSkillsOnClick} className="orangeCircularButton">Save Changes</button>}
                    </div>
                </div>
    
                <div className='cats'>
                    <h2 className='volunteerPostheader'>Offered Categories</h2>
                    <h2 className='smallHeader'>Extracted Automatically Using AI</h2>
                    {/*<ul className='skillsList'>
                    <div>
                    {model.skills.length > 0 ? model.skills.map((item, index) => (
                        <li key={index}>{item}</li> // Always add a unique 'key' prop when rendering lists
                    )) : <p className='notFound'>No Skills Found</p>}
                    </div>
                    </ul>*/}
                    <div className="list-section">
                        <textarea
                            value={catsInput}
                            onChange={(e) => setCatsInput(e.target.value)}
                            placeholder="Enter categories separated by commas"
                        />
                        {isRelatedUser && <button onClick={saveCatsOnClick} className="orangeCircularButton">Save Changes</button>}
                    </div>
                </div>
            </div>
        </div>
    )
}


export default VolunteerPost;