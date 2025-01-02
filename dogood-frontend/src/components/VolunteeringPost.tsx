import { useEffect, useState } from 'react'
import './../css/Volunteering.css'
import VolunteeringModel from '../models/VolunteeringModel'
import { getVolunteering } from '../api/volunteering_api'
import { useParams } from "react-router-dom";
import { VolunteeringPostModel } from '../models/VolunteeringPostModel';
import { getVolunteeringPost, joinVolunteeringRequest, removeVolunteeringPost } from '../api/post_api';
import OrganizationModel from '../models/OrganizationModel';
import { getIsManager, getOrganization } from '../api/organization_api';
import { useNavigate } from 'react-router-dom';
import './../css/VolunteeringPost.css'
import { createReport } from '../api/report_api';


function VolunteeringPost() {
    const navigate = useNavigate();
    const [model, setModel] = useState<VolunteeringPostModel>({id: -1, title: "", description: "", postedTime: "", lastEditedTime: "", posterUsername: "", numOfPeopleRequestedToJoin: -1, relevance: -1, volunteeringId: -1, organizationId: -1});
    const [volunteeringName, setVolunteeringName] = useState<string>('');
    const [organizationName, setOrganizationName] = useState<string>('');
    const [isManager, setIsManager] = useState(false);
    const [ready, setReady] = useState(false);
    const [showJoinFreeText, setShowJoinFreeText] = useState(false);
    const [joinFreeText, setJoinFreeText] = useState("");
    const [showReportDescription, setShowReportDescription] = useState(false);
    const [reportDescription, setReportDescription] = useState("");
    let { id } = useParams();
    
    const fetchVolunteeringPost = async () => {
        try {
            if(id !== undefined) {
                let post: VolunteeringPostModel = await getVolunteeringPost(parseInt(id));
                setModel(post);
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
                let volunteering: VolunteeringModel = await getVolunteering(model.volunteeringId);
                let organization: OrganizationModel = await getOrganization(model.organizationId);
                
                setVolunteeringName(volunteering.name);
                setOrganizationName(organization.name);
            }
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
    }, [id])

    useEffect(() => {
        fetchNames();
    }, [model, ready])

    useEffect(() =>{
        if(ready){
            updatePermissions();
        }
    }, [model, ready])

    const fixDate = (dateJson: string) : string => {
        let date: Date = new Date(dateJson);
        let dateStr = `${date.getDate()}/${date.getMonth() + 1}/${date.getFullYear()}`;
        let houtStr = `${date.getHours()}:${date.getMinutes()}`;
        return dateStr + " at " + houtStr;
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

    return (
        <div>
            <div className="postInfo">
                <div className='postInfoText'>
                    <h1>{model.title}</h1>
                    <p>{model.description}</p>
                    <img src="https://i.natgeofe.com/n/4f5aaece-3300-41a4-b2a8-ed2708a0a27c/domestic-dog_thumb_square.jpg" alt="Description of image" className='image' />
                    <p>Posted by: {model.posterUsername}</p>
                    <p>Posted on: {fixDate(model.postedTime)}</p>
                    <p>Last edited on: {fixDate(model.lastEditedTime)}</p>
                    <p>Number of people requested to join so far: {model.numOfPeopleRequestedToJoin}</p>
                    <p>Volunteering: {volunteeringName}</p>
                    <button onClick={handleShowVolunteeringOnClick}>show</button>
                    <p>Organization: {organizationName}</p>
                    <button onClick={handleShowOrganizationOnClick}>show</button>
                </div>
            <div>
                <button onClick={handleJoinVolunteeringOnClick}>Join Volunteering</button>
                
                {showJoinFreeText && (
                    <div>
                        <textarea
                            value={joinFreeText}
                            onChange = {handleJoinFreeTextChange}
                            placeholder="Enter your message here..."
                        />
                    </div>
                )}
                {showJoinFreeText && <button onClick={handleSubmitOnClick}>Submit Request</button>}
                {showJoinFreeText && <button onClick={handleCancelOnClick}>Cancel</button>}

                <button onClick={handleReportOnClick}>Report</button>
                {showReportDescription && (
                    <div>
                        <textarea
                            value={reportDescription}
                            onChange = {handleReportDescriptionChange}
                            placeholder="Enter your report here..."
                        />
                    </div>
                )}
                {showReportDescription && <button onClick={handleSubmitReportOnClick}>Submit Report</button>}
                {showReportDescription && <button onClick={handleCancelReportOnClick}>Cancel</button>}

                {isManager && <button onClick={handleEditPostOnClick}>Edit Post</button>}
                {isManager && <button onClick={handleRemovePostOnClick}>Remove Post</button>}
            </div>
            </div>
        </div>
    )
}

export default VolunteeringPost;