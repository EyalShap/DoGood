import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { acceptUserJoinRequest, approveHourRequest, denyHourRequest, denyUserJoinRequest, getIsManager, getVolunteering, getVolunteeringGroups, getVolunteeringHourRequests, getVolunteeringJoinRequests } from "../api/volunteering_api";
import JoinRequest from "../models/JoinRequest";
import '../css/RequestList.css'

function Request({ groups, model, volunteeringId, fetchRequests } : {groups: number[], model: JoinRequest, volunteeringId: number, fetchRequests: () => void}){
    const [chosenGroup, setChosenGroup] = useState(-1);
    const navigate = useNavigate();

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
            await acceptUserJoinRequest(volunteeringId, model.userId,chosenGroup);
            await fetchRequests();
        }catch(e){
            alert(e)
        }
    }

    return (
        <div className="request">
            <div className="requestData">
                <h1 onClick={() => navigate(`/profile/${model.userId}`)} style={{textDecoration: "underline"}}>User: {model.userId}</h1>
                <p>Request Text: {model.text}</p>
            </div>
            <div className="requestButtons">
                <button disabled={chosenGroup < 0}  onClick = {() => accept()} className="accept">Accept</button>
                <select onChange={e => setChosenGroup(parseInt(e.target.value))}>
                    <option value={-1}></option>
                    {groups.map(group => <option value={group}>Group {group}</option>)}
                </select>
                <button onClick = {() => deny()} className="deny">Deny</button>
            </div>
        </div>
    )
}

function JoinRequestList() {
    const [model, setModel] = useState<JoinRequest[]>([]);
    const [groups, setGroups] = useState<number[]>([])
    let { id } = useParams();
    const fetchRequests = async () => {
        try{
            let found = await getVolunteeringJoinRequests(parseInt(id!));
            setGroups(await getVolunteeringGroups(parseInt(id!)))
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
        {model.map(request => <Request groups={groups} model={request} volunteeringId={parseInt(id!)} fetchRequests={fetchRequests}/>)}
    </div>
  )
}

export default JoinRequestList