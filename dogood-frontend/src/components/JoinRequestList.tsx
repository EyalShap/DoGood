import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { acceptUserJoinRequest, approveHourRequest, denyHourRequest, denyUserJoinRequest, getIsManager, getVolunteering, getVolunteeringGroups, getVolunteeringHourRequests, getVolunteeringJoinRequests } from "../api/volunteering_api";
import JoinRequest from "../models/JoinRequest";
import '../css/RequestList.css'

function Request({ groups, model, volunteeringId, fetchRequests } : {groups: number[], model: JoinRequest, volunteeringId: number, fetchRequests: () => void}){
    const navigate = useNavigate();
    const [open, setOpen] = useState(false)

    const deny = async () => {
        try{
            await denyUserJoinRequest(volunteeringId, model.userId);
            await fetchRequests();
            setOpen(false);
        }catch(e){
            alert(e)
        }
    }

    const accept = async (chosenGroup: number) => {
        try{
            await acceptUserJoinRequest(volunteeringId, model.userId,chosenGroup);
            await fetchRequests();
            setOpen(false);
        }catch(e){
            alert(e)
        }
    }

    return (
        <div className="request">
            <div className="requestData">
                <h1 onClick={() => navigate(`/profile/${model.userId}`)}>{model.userId}</h1>
                <p>{model.text}</p>
            </div>
            <div className="requestButtons">
                <button onClick={() => setOpen(prevState => !prevState)} className="orangeCircularButton accept">Add to</button>
                <button onClick={() => deny()} className="orangeCircularButton deny">Deny</button>
            </div>
            <div className={`groupButtons${!open ? "" : " showing"}`}>
                <h2 className="smallHeader">Choose Group To Accept Into</h2>
                {groups.map(group => <button className="orangeCircularButton" onClick={() => accept(group)}>Group {group}</button>)}
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