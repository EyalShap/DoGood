import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { approveHourRequest, denyHourRequest, getIsManager, getVolunteering, getVolunteeringHourRequests } from "../api/volunteering_api";
import HourApprovalRequest from "../models/HourApprovalRequest";
import './../css/RequestList.css'
import { getVolunteeringName } from "../api/post_api";

function Request({ model, volunteeringId, fetchRequests } : {model: HourApprovalRequest, volunteeringId: number, fetchRequests: () => void}){
    const getTimeBetween = () => {
        var seconds = ((new Date(model.endTime)).getTime() - (new Date(model.startTime)).getTime()) / 1000;
        var minutes = Math.floor(seconds/60);
        var hours = Math.floor(minutes/60)
        return `${formatNumber(hours)}:${formatNumber(minutes%60)}`
    }

    const deny = async () => {
        try{
            await denyHourRequest(volunteeringId, model.userId, model.startTime, model.endTime);
            await fetchRequests();
        }catch(e){
            alert(e)
        }
    }

    const accept = async () => {
        try{
            await approveHourRequest(volunteeringId, model.userId, model.startTime, model.endTime);
            await fetchRequests();
        }catch(e){
            alert(e)
        }
    }

    const formatNumber = (digit: number) =>{
        return `${digit > 9 ? "" : "0"}${digit}`
    }

    return (
        <div className="request">
            <div className="requestData">
                <h1>{model.userId}</h1>
                <p><b>Date:</b> {((date: Date) => `${date.getDate()}/${date.getMonth()+1}/${date.getFullYear()}`)(new Date(model.startTime))}</p>
                <p><b>Start Time:</b> {formatNumber((new Date(model.startTime)).getHours())}:{formatNumber((new Date(model.startTime)).getMinutes())}</p>
                <p><b>End Time:</b> {formatNumber((new Date(model.endTime)).getHours())}:{formatNumber((new Date(model.endTime)).getMinutes())}</p>
                <p><b>Total Time:</b> {getTimeBetween()}</p>
            </div>
            <div className="requestButtons">
                <button onClick = {() => accept()} className="orangeCircularButton accept">Accept</button>
                <button onClick = {() => deny()} className="orangeCircularButton deny">Deny</button>
            </div>
        </div>
    )
}

function HourApprovalRequestList() {
    const [model, setModel] = useState<HourApprovalRequest[]>([]);
    const [voluneeringName, setVolunteeringName] = useState<string>("");

    let { id } = useParams();

    const fetchRequests = async () => {
        try{
            let found = await getVolunteeringHourRequests(parseInt(id!));
            console.log(found)

            let name = await getVolunteeringName(parseInt(id!));
            
            await setModel(found);
            await setVolunteeringName(name);
        }catch(e){
            //send to error page
            alert(e)
        }
    }
    useEffect(() => {
        fetchRequests();
    }, [])
  return (
    <div className="requestList" style={{display:'flex', flexDirection:'column', alignItems:'center'}}>
        <h2 className="bigHeader" style={{marginTop: '50px', textAlign:'center'}}>{voluneeringName} Volunteers Hour Approval Requests</h2>
        {model.length === 0 && <p className="smallHeader" style={{marginTop: '50px', marginBottom: '50px'}}>No Requests Found</p>}
        {model.map(request => <Request model={request} volunteeringId={parseInt(id!)} fetchRequests={fetchRequests}/>)}
    </div>
  )
}

export default HourApprovalRequestList