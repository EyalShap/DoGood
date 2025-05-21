import { useEffect, useState } from 'react'
import './../css/Volunteering.css'
import { getVolunteering, getVolunteeringCategories, getVolunteeringSkills } from '../api/volunteering_api'
import { useParams } from "react-router-dom";
import { VolunteeringPostModel } from '../models/VolunteeringPostModel';
import { getPostPastExperiences, getVolunteeringImages, getVolunteeringName, getVolunteeringPost, joinVolunteeringRequest, removeVolunteeringPost } from '../api/post_api';
import { getIsManager, getOrganizationName } from '../api/organization_api';
import { useNavigate } from 'react-router-dom';
import './../css/VolunteeringPost.css'
import './../css/CommonElements.css'
import { createVolunteeringPostReport } from '../api/report_api';
import PastExperienceModel from '../models/PastExpreienceModel';
import ListWithArrows, { ListItem } from './ListWithArrows';
import defaultVolunteeringPic from '/src/assets/defaultVolunteeringDog.webp';
import defaultProfilePic from '/src/assets/defaultProfilePic.jpg';
import { getUserByUsername } from '../api/user_api';

function VolunteeringPost() {
    const navigate = useNavigate();
    const [model, setModel] = useState<VolunteeringPostModel>({id: -1, title: "", description: "", postedTime: "", lastEditedTime: "", posterUsername: "", numOfPeopleRequestedToJoin: -1, relevance: -1, volunteeringId: -1, organizationId: -1, keywords: []});
    const [volunteeringName, setVolunteeringName] = useState<string>('');
    const [volunteeringImages, setVolunteeringImages] = useState<ListItem[]>([]);
    const [isVolunteer, setIsVolunteer] = useState(true);
    const [organizationName, setOrganizationName] = useState<string>('');
    const [isManager, setIsManager] = useState(false);
    const [ready, setReady] = useState(false);
    const [showJoinFreeText, setShowJoinFreeText] = useState(false);
    const [joinFreeText, setJoinFreeText] = useState("");
    const [showReportDescription, setShowReportDescription] = useState(false);
    const [reportDescription, setReportDescription] = useState("");
    const [pastExperiences, setPastExperiences] = useState<ListItem[]>([]);
    const [currentIndex, setCurrentIndex] = useState(0);
    const [isHovered, setIsHovered] = useState(false);
    const [dropdownOpen, setDropdownOpen] = useState(false);
    const [skills, setSkills] = useState<string[]>([]);
    const [categories, setCategories] = useState<string[]>([]);
    let { id } = useParams();
    
    const fetchVolunteeringPost = async () => {
        try {
            if(id !== undefined) {
                let post: VolunteeringPostModel = await getVolunteeringPost(parseInt(id));
                let volunteeringName: string = await getVolunteeringName(post.volunteeringId);
                let organizationName: string = await getOrganizationName(post.organizationId);
                setModel(post);
                setReady(true);

                setVolunteeringName(volunteeringName);
                setOrganizationName(organizationName);
                
                try {
                    let vol = await getVolunteering(post.volunteeringId);
                    
                    setIsVolunteer(true);
                }
                catch {
                    setIsVolunteer(false);                
                }
                setIsManager(await getIsManager(post.organizationId));

                setSkills(await getVolunteeringSkills(post.volunteeringId));
                setCategories(await getVolunteeringCategories(post.volunteeringId));

            }
            else {
                alert("Error");
            }
        }
        catch(e) {
            alert(e);
        }
    }


    const fetchImages = async () => {
        try {
            if(ready) {
                let images = await getVolunteeringImages(model.volunteeringId);
                if(images.length === 0) {
                    images = [defaultVolunteeringPic];
                }
                const listItems: ListItem[] = images.map((image) => ({
                    id: "",
                    image: image, 
                    title: "",  
                    description: "", // assuming 'summary' is a short description
                }));
                setVolunteeringImages(listItems);
            }
        }
        catch(e) {
            alert(e);
        }
    }

    const fetchPastExperiences = async () => {
        try {
            if(id !== undefined) {
                let pastExperiences: PastExperienceModel[] = await getPostPastExperiences(parseInt(id));                    
                console.log(pastExperiences);
                let listItems: ListItem[] = [];

                for(let expr of pastExperiences) {
                    let user = await getUserByUsername(expr.userId);

                    listItems = listItems.concat([{
                        id: expr.userId,
                        image: user.profilePicUrl ? user.profilePicUrl : defaultProfilePic,
                        title: expr.text,  
                        description: `By ${expr.userId} on ${fixDate(expr.when, true)}.`,
                    }]);
                }
                setPastExperiences(listItems);
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

    useEffect(() => {
        fetchVolunteeringPost();
        fetchPastExperiences();
    }, [id])

    useEffect(() => {
        fetchImages();
    }, [model])


    const fixDate = (dateJson: string, showHour: boolean) : string => {
        let date: Date = new Date(dateJson);
        let dateStr = `${date.getDate()}/${date.getMonth() + 1}/${date.getFullYear()}`;
        let houtStr = showHour ? ` at ${date.getHours()}:${date.getMinutes()}` : "";
        return dateStr + houtStr;
    }

    const handleShowVolunteeringOnClick = () => {
        navigate(`/volunteering/${model.volunteeringId}`);
    }

    const handleShowOrganizationOnClick = () => {
        navigate(`/organization/${model.organizationId}`);
    }

    const handleJoinVolunteeringOnClick = async () => {
        setShowJoinFreeText(true);
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

    const handleSubmitOnClick = async () => {
        try {
            await joinVolunteeringRequest(model.id, joinFreeText);
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
            await createVolunteeringPostReport(model.id, reportDescription);
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
        navigate(`/volunteering/${model.volunteeringId}/createVolunteeringPost/${model.id}/0`);
    }

    const handleRemovePostOnClick = async () => {
        if(window.confirm("Are you sure you want to remove this post?")) {
            try {
                if(id !== undefined) {
                    await removeVolunteeringPost(parseInt(id));
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

    const handlePrev = async () => {
        if(currentIndex == 0) {
            setCurrentIndex(pastExperiences.length - 1);
        }
        else {
            setCurrentIndex(currentIndex - 1);
        }
    }

    const handleNext = async () => {
        if(currentIndex == pastExperiences.length - 1) {
            setCurrentIndex(0);
        }
        else {
            setCurrentIndex(currentIndex + 1);
        }
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
                            {isManager && <p className="actionDropdownItem" onClick = {handleEditPostOnClick}>Edit</p>}
                            {isManager && <p className="actionDropdownItem" onClick = {handleRemovePostOnClick}>Remove</p>}
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
            <div className='headers volunteeringPostHeaders'>
                <h1 className='bigHeader volunteeringPostHeader'>{model.title}</h1>
                <p className='smallHeader volunteeringPostHeader'>{model.description}</p>

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
                        <p id="postNumOfPeopleRequested">Number of people requested to join so far: {model.numOfPeopleRequestedToJoin}</p>

                        </div>
                    )}
                </div>
            </div>
            <div className='postImages'>
            <ListWithArrows data={volunteeringImages} limit={1} navigateTo={""}></ListWithArrows>
            </div>
            </div>
            

            <div id="postInfo" className="postInfo">
                

                {!isManager && !isVolunteer && <button className='joinButton' onClick={handleJoinVolunteeringOnClick}>I Want To Join</button>}
    
                    {showJoinFreeText && (
                        <div className="popup-window">
                            <div className="popup-header">
                            <span className="popup-title">Join Volunteering</span>
                            <button className="cancelButton" onClick={handleCancelOnClick}>
                                X
                            </button>
                            </div>
                            <div className="popup-body">
                                <textarea value={joinFreeText} onChange={e => setJoinFreeText(e.target.value)} placeholder="Why do you want to join the volunteering?..."></textarea>
                                <button className="orangeCircularButton" onClick={handleSubmitOnClick}>
                                    Submit
                                </button>
                            </div>
                        </div>
                    )}
            </div>

            <div className='catsAndSkills'>
                <div className='cats'>
                    <h2 className='relatedVolunteersHeader' style={{fontSize:"1.6rem"}}>Volunteering Categories</h2>
                    <ul className='catsList'>
                        <div>
                    {categories.length > 0 ? categories.map((item, index) => (
                        <li key={index} style={{fontSize:"1.2rem"}}>{item}</li> // Always add a unique 'key' prop when rendering lists
                    )) : <p className='notFound' style={{fontSize:"1.2rem"}}>No Categories Found</p>}
                    </div>
                    </ul>
                </div>
    
                <div className='skills'>
                    <h2 className='relatedVolunteersHeader' style={{fontSize:"1.6rem"}}>Required Skills</h2>
                    <ul className='skillsList'>
                    <div>
                    {skills.length > 0 ? skills.map((item, index) => (
                        <li key={index} style={{fontSize:"1.2rem"}}>{item}</li> // Always add a unique 'key' prop when rendering lists
                    )) : <p className='notFound' style={{fontSize:"1.2rem"}}>No Skills Found</p>}
                    </div>
                    </ul>
                </div>
            </div>
            
            <div className="learnMore">
                <h2 className='relatedVolunteersHeader' style={{textAlign:'center'}}>Learn More About The Volunteering</h2>
            <div id="volunteeringAndOrganization">
                

                    <div 
                        className="volunteeringOrganizationBox" 
                        onClick={handleShowVolunteeringOnClick}
                        style={{ pointerEvents: isVolunteer ? 'auto' : 'none' }}
                        >
                        <p className='volunteeringOrganizationBoxHeader'>Volunteering</p>
                        <p id="postVolunteering" >{volunteeringName}</p>
                    </div>
                    <div className="volunteeringOrganizationBox" onClick={handleShowOrganizationOnClick}>
                        <p className='volunteeringOrganizationBoxHeader'>Organization</p>
                        <p id="postOrganization">{organizationName}</p>
                    </div>
                </div>
    
                
        </div>

        <div className="learnMore">
                    <h1 className="relatedVolunteersHeader">Volunteers Past Experiences</h1>
                    {pastExperiences.length > 0 ? (
                        <ListWithArrows data={pastExperiences} limit={3} navigateTo={"profile"} clickable={(id: number | string) => true}></ListWithArrows>
                    ) : (
                        <p id="noPastExperiences">No past experiences available.</p>
                    )}
                </div>
        </div>
    )
}


export default VolunteeringPost;