import { getOrganizationName, getUserRequests, handleAssignManagerRequest } from '../api/organization_api'
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom';
import RequestModel from '../models/RequestModel';
import './../css/ManagerRequestsList.css'

function ManagerRequestsList() {
    const navigate = useNavigate();

    const [requests, setRequests] = useState<RequestModel[]>([]);
    const [organizationNames, setOrganizationNames] = useState<{ [key: number]: string }>({});
    
    const fetchRequests = async () => {
        try {
            const requests = await getUserRequests();
            setRequests(requests);

            for (let request of requests) {
                try {
                    const name = await getOrganizationName(request.organizationId);
                    
                    setOrganizationNames((prevNames) => ({
                        ...prevNames,
                        [request.organizationId]: name,
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

    const handleShowOnClick = (organizationId: number) => {
        navigate(`/organization/${organizationId}`);
    };

    const handleApproveOnClick = (organizationId: number) => {
        if (window.confirm(`Are you sure you want to approve this request?`)) {
            handleAssignManagerRequest(organizationId, true);
            let newRequests = requests.filter((request) => request.organizationId !== organizationId && request.assigneeUsername !== localStorage.getItem("username"));
            setRequests(newRequests);
        }
    };

    const handleDenyOnClick = (organizationId: number) => {
        if (window.confirm(`Are you sure you want to deny this request?`)) {
            handleAssignManagerRequest(organizationId, false);
            let newRequests = requests.filter((request) => request.organizationId !== organizationId && request.assigneeUsername !== localStorage.getItem("username"));
            setRequests(newRequests);
        }
    };


    return (
        <div>
            <div className="Requests">
                <h2>Manager Requests</h2>
                {requests.length > 0 ? (
                    requests.map((request, index) => (
                        <div key={index} className="requestItem">
                            <div id = "info">
                                <h3>Organization name: {organizationNames[request.organizationId]}</h3>
                                <p>Sent by: {request.assignerUsername}</p>
                            </div>

                            <div id = "buttons"> 
                                <button onClick={() => handleShowOnClick(request.organizationId)}>Show Organization</button>
                                <button onClick={() => handleApproveOnClick(request.organizationId)}>V</button>
                                <button onClick={() => handleDenyOnClick(request.organizationId)}>X</button>
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