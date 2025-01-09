import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { approveHourRequest, denyHourRequest, getIsManager, getVolunteering, getVolunteeringHourRequests } from "../api/volunteering_api";
import HourApprovalRequest from "../models/HourApprovalRequest";
import './../css/RequestList.css'

function Request({ model, volunteeringId, fetchRequests } : {model: HourApprovalRequest, volunteeringId: number, fetchRequests: () => void}){
    const getTimeBetween = () => {
        var seconds = ((new Date(model.endTime)).getTime() - (new Date(model.startTime)).getTime()) / 1000;
        var minutes = Math.floor(seconds/60);
        var hours = Math.floor(minutes/60)
        return `${hours}:${minutes%60}`
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

    return (
        <div className="request">
            <div className="requestData">
                <h1>User: {model.userId}</h1>
                <p>Date: {((date: Date) => `${date.getDate()}/${date.getMonth()+1}/${date.getFullYear()}`)(new Date(model.startTime))}</p>
                <p>Start Time: {(new Date(model.startTime)).getHours()}:{(new Date(model.startTime)).getMinutes()}</p>
                <p>End Time: {(new Date(model.endTime)).getHours()}:{(new Date(model.endTime)).getMinutes()}</p>
                <p>Total Time: {getTimeBetween()}</p>
            </div>
            <div className="requestButtons">
                <button onClick = {() => accept()} className="accept">Accept</button>
                <button onClick = {() => deny()} className="deny">Deny</button>
            </div>
        </div>
    )
}

function HourApprovalRequestList() {
    const [model, setModel] = useState<HourApprovalRequest[]>([]);
    let { id } = useParams();
    const fetchRequests = async () => {
        try{
            let found = await getVolunteeringHourRequests(parseInt(id!));
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

export default HourApprovalRequestList