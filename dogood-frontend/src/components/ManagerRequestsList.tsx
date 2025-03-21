import { getOrganizationName, getUserRequests, handleAssignManagerRequest } from '../api/organization_api'
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom';
import RequestModel from '../models/RequestModel';
import './../css/ManagerRequestsList.css'
import { getVolunteerPost, getVolunteerPostRequests, handleJoinVolunteerPostRequest } from '../api/post_api';

function ManagerRequestsList() {
    const navigate = useNavigate();

    const [managerRequests, setManagerRequests] = useState<RequestModel[]>([]);
    const [volunteerPostsRequests, setVolunteerPostsRequests] = useState<RequestModel[]>([]);

    const [organizationNames, setOrganizationNames] = useState<{ [key: number]: string }>({});
    const [postTitles, setPostTitles] = useState<{ [key: number]: string }>({});
    
    const fetchRequests = async () => {
        try {
            const managerRequests = await getUserRequests();
            setManagerRequests(managerRequests);

            for (let request of managerRequests) {
                try {
                    const name = await getOrganizationName(request.objectId);
                    
                    setOrganizationNames((prevNames) => ({
                        ...prevNames,
                        [request.objectId]: name,
                    }));
                } catch (e) {
                    alert(e);
                }
            }

            const volunteerPostRequests = await getVolunteerPostRequests();
            console.log(volunteerPostRequests);
            setVolunteerPostsRequests(volunteerPostRequests);

            for (let request of volunteerPostRequests) {
                try {
                    const title = (await getVolunteerPost(request.objectId)).title;
                    
                    setPostTitles((prevTitles) => ({
                        ...prevTitles,
                        [request.objectId]: title,
                    }));
                } catch (e) {
                    alert(e);
                }
            }
        } 
        catch (e) {
            // send to error page
            alert(e);
        }
    }

    useEffect(() => {
        fetchRequests();
    }, [])

    const handleShowOrgOnClick = (organizationId: number) => {
        navigate(`/organization/${organizationId}`);
    };

    const handleShowPostOnClick = (postId: number) => {
        navigate(`/volunteerPost/${postId}`);
    };

    const handleApproveManagerRequestOnClick = (organizationId: number) => {
        if (window.confirm(`Are you sure you want to approve this request?`)) {
            handleAssignManagerRequest(organizationId, true);
            let newRequests = managerRequests.filter((request) => request.objectId !== organizationId && request.assigneeUsername !== localStorage.getItem("username"));
            setManagerRequests(newRequests);
        }
    };

    const handleDenyManagerRequestOnClick = (organizationId: number) => {
        if (window.confirm(`Are you sure you want to deny this request?`)) {
            handleAssignManagerRequest(organizationId, false);
            let newRequests = managerRequests.filter((request) => request.objectId !== organizationId && request.assigneeUsername !== localStorage.getItem("username"));
            setManagerRequests(newRequests);
        }
    };

    const handleApproveJoinVolunteerPostRequestOnClick = (postId: number) => {
        if (window.confirm(`Are you sure you want to approve this request?`)) {
            handleJoinVolunteerPostRequest(postId, true);
            let newRequests = managerRequests.filter((request) => request.objectId !== postId && request.assigneeUsername !== localStorage.getItem("username"));
            setVolunteerPostsRequests(newRequests);
        }
    };

    const handleDenyVolunteerPostRequestOnClick = (postId: number) => {
        if (window.confirm(`Are you sure you want to deny this request?`)) {
            handleJoinVolunteerPostRequest(postId, false);
            let newRequests = managerRequests.filter((request) => request.objectId !== postId && request.assigneeUsername !== localStorage.getItem("username"));
            setVolunteerPostsRequests(newRequests);
        }
    };


    return (
        <div className = 'requests'>
            <div className="requestsList">
                <div className='headers'>
                    <h2 className='bigHeader listHeader'>Manager Requests</h2>
                    <h2 className='smallHeader'>These are organizations that have requested you to become a manager</h2>
                </div>
                {managerRequests.length > 0 ? (
                    managerRequests.map((request, index) => (
                        <div key={index} className="requestItem">
                            <div className = "info">
                                <h3 className = "requestHeader" onClick={() => handleShowOrgOnClick(request.objectId)}>Organization name: {organizationNames[request.objectId]}</h3>
                                <p className = "requestSender">Sent by: {request.assignerUsername}</p>
                            </div>

                            <div className = "buttons"> 
                                <button className = 'orangeCircularButton requestButton' onClick={() => handleApproveManagerRequestOnClick(request.objectId)}>Accept</button>
                                <button className = 'orangeCircularButton requestButton' onClick={() => handleDenyManagerRequestOnClick(request.objectId)}>Deny</button>
                            </div>
                        </div>
                    ))
                ) : (
                    <p id = "noRequests">No requests available.</p>
                )}
            </div>

            <div className="requestsList">
                <div className='headers'>
                    <h2 className='bigHeader listHeader'>Join Volunteer Post Requests</h2>
                    <h2 className='smallHeader'>These are volunteering opportunities that have requested you to join</h2>
                </div>
                {volunteerPostsRequests.length > 0 ? (
                    volunteerPostsRequests.map((request, index) => (
                        <div key={index} className="requestItem">
                            <div className = "info">
                                <h3 className = "requestHeader" onClick={() => handleShowPostOnClick(request.objectId)}>Post title: {postTitles[request.objectId]}</h3>
                                <p className = "requestSender">Sent by: {request.assignerUsername}</p>
                            </div>

                            <div className = "buttons"> 
                                <button className = 'orangeCircularButton requestButton' onClick={() => handleApproveJoinVolunteerPostRequestOnClick(request.objectId)}>Accept</button>
                                <button className = 'orangeCircularButton requestButton' onClick={() => handleDenyVolunteerPostRequestOnClick(request.objectId)}>Deny</button>
                            </div>
                        </div>
                    ))
                ) : (
                    <p id = "noRequests">No requests available.</p>
                )}
            </div>
        </div>
    )
}

export default ManagerRequestsList