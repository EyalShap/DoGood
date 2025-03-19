import { useEffect, useState } from 'react'
import './../css/Volunteering.css'
import { getVolunteering } from '../api/volunteering_api'
import { useParams } from "react-router-dom";
import { VolunteeringPostModel } from '../models/VolunteeringPostModel';
import { getPostPastExperiences, getVolunteeringImages, getVolunteeringName, getVolunteeringPost, joinVolunteeringRequest, removeVolunteeringPost } from '../api/post_api';
import { getIsManager, getOrganizationName } from '../api/organization_api';
import { useNavigate } from 'react-router-dom';
import './../css/VolunteeringPost.css'
import { createReport } from '../api/report_api';
import PastExperienceModel from '../models/PastExpreienceModel';


function VolunteeringPost() {
    const navigate = useNavigate();
    const [model, setModel] = useState<VolunteeringPostModel>({id: -1, title: "", description: "", postedTime: "", lastEditedTime: "", posterUsername: "", numOfPeopleRequestedToJoin: -1, relevance: -1, volunteeringId: -1, organizationId: -1});
    const [volunteeringName, setVolunteeringName] = useState<string>('');
    const [volunteeringImages, setVolunteeringImages] = useState<string[]>([]);
    const [isVolunteer, setIsVolunteer] = useState(true);
    const [organizationName, setOrganizationName] = useState<string>('');
    const [isManager, setIsManager] = useState(false);
    const [ready, setReady] = useState(false);
    const [showJoinFreeText, setShowJoinFreeText] = useState(false);
    const [joinFreeText, setJoinFreeText] = useState("");
    const [showReportDescription, setShowReportDescription] = useState(false);
    const [reportDescription, setReportDescription] = useState("");
    const [pastExperiences, setPastExperiences] = useState<PastExperienceModel[]>([]);
    const [currentIndex, setCurrentIndex] = useState(0);
    let { id } = useParams();
    
    const fetchVolunteeringPost = async () => {
        try {
            if(id !== undefined) {
                let post: VolunteeringPostModel = await getVolunteeringPost(parseInt(id));
                setModel(post);
                console.log(post);
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

    const fetchNames = async () => {
        try {
            if(ready) {
                let volunteeringName: string = await getVolunteeringName(model.volunteeringId);
                let organizationName: string = await getOrganizationName(model.organizationId);
                setVolunteeringName(volunteeringName);
                setOrganizationName(organizationName);
                
                try {
                    await getVolunteering(model.volunteeringId);
                    setIsVolunteer(true);
                }
                catch {
                    setIsVolunteer(false);                
                }
            }
        }
        catch(e) {
            alert(e);
        }
    }

    const fetchImages = async () => {
        try {
            if(ready) {
                setVolunteeringImages(await getVolunteeringImages(parseInt(id!)))
            }
        }
        catch(e) {
            alert(e);
        }
    }

    const fetchPastExperiences = async () => {
        try {
            //if(ready) {
                if(id !== undefined) {
                    let pastExperiences: PastExperienceModel[] = await getPostPastExperiences(parseInt(id));                    
                    setPastExperiences(pastExperiences);
                    setReady(true);
                }
                else {
                    alert("Error");
                }
            //}
        }
        catch(e) {
            alert(e);
        }
    }

    const updatePermissions = async () => {
        try{
            setIsManager(await getIsManager(model.organizationId));
        }
        catch(e){
            //send to error page
            alert(e)
        }
    }

    useEffect(() => {
        fetchVolunteeringPost();
        fetchPastExperiences();
    }, [id])

    useEffect(() => {
        fetchNames();
        fetchImages();
    }, [model, ready])

    useEffect(() =>{
        if(ready){
            updatePermissions();
        }
    }, [model, ready])

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

    const handleJoinFreeTextChange = (event: React.ChangeEvent<HTMLTextAreaElement>) => {
        setJoinFreeText(event.target.value);
    };

    const handleReportDescriptionChange = (event: React.ChangeEvent<HTMLTextAreaElement>) => {
        setReportDescription(event.target.value);
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
            await createReport(model.id, reportDescription);
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
        navigate(`/volunteering/${model.volunteeringId}/createVolunteeringPost/${model.id}`);
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
            <div id = "postHeader">
                <h1 id="postTitle">{model.title}</h1>
                <p id="postDescription">{model.description}</p>
            </div>

            <div id="postInfo" className="postInfo">
                <div id = "postImage" className="postImage" >
                    {volunteeringImages.map(image =>
                        <img style={{margin: "5px"}}
                        src={image.replace(/"/g, "")} 
                        />
                    )}
                </div>

                <div id="postInfoText" className="postInfoText">
                    <p id="postPosterUsername">Posted by: {model.posterUsername}</p>
                    <p id="postPostedTime">Posted on: {fixDate(model.postedTime, true)}</p>
                    <p id="postLastEditedTime">Last edited on: {fixDate(model.lastEditedTime, true)}</p>
                    <p id="postNumOfPeopleRequested">Number of people requested to join so far: {model.numOfPeopleRequestedToJoin}</p>
                </div>
            </div>
            <div id="volunteeringAndOrganization">
                    <div 
                        id="volunteeringBox" 
                        onClick={handleShowVolunteeringOnClick}
                        style={{ pointerEvents: isVolunteer ? 'auto' : 'none' }}
                        >
                        <p id="postVolunteering">Volunteering: {volunteeringName}</p>
                    </div>
                    <div id="organizationBox" onClick={handleShowOrganizationOnClick}>
                        <p id="postOrganization">Organization: {organizationName}</p>
                    </div>
                </div>
                
                <div id="actions" className="actions">
                    <button id="joinVolunteeringButton" onClick={handleJoinVolunteeringOnClick}>Join Volunteering</button>
    
                    {showJoinFreeText && (
                        <div id="joinFreeTextContainer">
                            <textarea
                                id="joinFreeText"
                                value={joinFreeText}
                                onChange={handleJoinFreeTextChange}
                                placeholder="Enter your message here..."
                            />
                        </div>
                    )}
                    {showJoinFreeText && <button id="submitJoinRequestButton" onClick={handleSubmitOnClick}>Submit Request</button>}
                    {showJoinFreeText && <button id="cancelJoinRequestButton" onClick={handleCancelOnClick}>Cancel</button>}
    
                    <button id="reportButton" onClick={handleReportOnClick}>Report</button>
    
                    {showReportDescription && (
                        <div id="reportDescriptionContainer">
                            <textarea
                                id="reportDescription"
                                value={reportDescription}
                                onChange={handleReportDescriptionChange}
                                placeholder="Enter your report here..."
                            />
                        </div>
                    )}
                    {showReportDescription && <button id="submitReportButton" onClick={handleSubmitReportOnClick}>Submit Report</button>}
                    {showReportDescription && <button id="cancelReportButton" onClick={handleCancelReportOnClick}>Cancel</button>}
    
                    {isManager && <button id="editPostButton" onClick={handleEditPostOnClick}>Edit Post</button>}
                    {isManager && <button id="removePostButton" onClick={handleRemovePostOnClick}>Remove Post</button>}
                </div>
    
                <div id="pastExperiences" className="pastExperiences">
                    <h1 id="pastExperiencesHeader">Volunteers Past Experiences:</h1>
                    {pastExperiences.length > 0 ? (
                        <div id="pastExperienceItem" className="pastExperienceItem">
                            <button id="prevExperienceButton" onClick={handlePrev}>&lt;</button>
                            <div id="currentExperience" className="currentExperience">
                                <h3 id="experienceUserId">{pastExperiences[currentIndex].userId}</h3>
                                <p id="experienceText">{pastExperiences[currentIndex].text}</p>
                                <p id="experienceDate">{fixDate(pastExperiences[currentIndex].when, false)}</p>
                            </div>
                            <button id="nextExperienceButton" onClick={handleNext}>&gt;</button>
                        </div>
                    ) : (
                        <p id="noPastExperiences">No past experiences available.</p>
                    )}
                </div>
        </div>
    )
    
}

export default VolunteeringPost;