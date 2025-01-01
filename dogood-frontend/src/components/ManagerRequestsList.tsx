import { getOrganizationName, getUserRequests, handleAssignManagerRequest } from '../api/organization_api'
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom';
import RequestModel from '../models/RequestModel';

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
            let newRequests = requests.filter((request) => request.organizationId !== organizationId && request.assigneeUsername !== sessionStorage.getItem("username"));
            setRequests(newRequests);
        }
    };

    const handleDenyOnClick = (organizationId: number) => {
        if (window.confirm(`Are you sure you want to deny this request?`)) {
            handleAssignManagerRequest(organizationId, false);
            let newRequests = requests.filter((request) => request.organizationId !== organizationId && request.assigneeUsername !== sessionStorage.getItem("username"));
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
                            <h3>Organization name: {organizationNames[request.organizationId]}</h3>
                            <p>Sent by: {request.assignerUsername}</p>
                            <button onClick={() => handleShowOnClick(request.organizationId)}>Show Organization</button>
                            <button onClick={() => handleApproveOnClick(request.organizationId)}>Approve</button>
                            <button onClick={() => handleDenyOnClick(request.organizationId)}>Deny</button>
                        </div>
                    ))
                ) : (
                    <p>No requests available.</p>
                )}
            </div>
        </div>
    )
}

export default ManagerRequestsList