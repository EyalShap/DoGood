import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { acceptUserJoinRequest, approveHourRequest, denyHourRequest, denyUserJoinRequest, getIsManager, getVolunteering, getVolunteeringHourRequests, getVolunteeringJoinRequests } from "../api/volunteering_api";
import JoinRequest from "../models/JoinRequest";
import '../css/RequestList.css'

function Request({ model, volunteeringId, fetchRequests } : {model: JoinRequest, volunteeringId: number, fetchRequests: () => void}){

    const deny = async () => {
        try{
            await denyUserJoinRequest(volunteeringId, model.userId);
            await fetchRequests();
        }catch(e){
            alert(e)
        }
    }

    const accept = async () => {
        try{
            await acceptUserJoinRequest(volunteeringId, model.userId,0);
            await fetchRequests();
        }catch(e){
            alert(e)
        }
    }

    return (
        <div className="request">
            <div className="requestData">
                <h1>User: {model.userId}</h1>
                <p>Request Text: {model.text}</p>
            </div>
            <div className="requestButtons">
                <button onClick = {() => accept()} className="accept">Accept</button>
                <button onClick = {() => deny()} className="deny">Deny</button>
            </div>
        </div>
    )
}

function JoinRequestList() {
    const [model, setModel] = useState<JoinRequest[]>([]);
    let { id } = useParams();
    const fetchRequests = async () => {
        try{
            let found = await getVolunteeringJoinRequests(parseInt(id!));
            console.log(found)

            await setModel(found);
        }catch(e){
            //send to error page
            alert(e)
        }
    }
    useEffect(() => {
        fetchRequests();
    }, [])
  return (
    <div className="requestList">
        {model.map(request => <Request model={request} volunteeringId={parseInt(id!)} fetchRequests={fetchRequests}/>)}
    </div>
  )
}

export default JoinRequestList