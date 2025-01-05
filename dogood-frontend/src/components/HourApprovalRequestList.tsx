import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { getIsManager, getVolunteering, getVolunteeringHourRequests } from "../api/volunteering_api";
import HourApprovalRequest from "../models/HourApprovalRequest";

function Request({ model } : {model: HourApprovalRequest}){
    const getTimeBetween = () => {
        var seconds = ((new Date(model.endTime)).getTime() - (new Date(model.startTime)).getTime()) / 1000;
        var minutes = Math.floor(seconds/60);
        var hours = Math.floor(minutes/60)
        return `${hours}:${minutes%60}`
    }
    return (
        <div>
            <p>User: {model.userId}</p>
            <p>Start Time: {(new Date(model.startTime)).getHours()}:{(new Date(model.startTime)).getMinutes()}</p>
            <p>End Time: {(new Date(model.endTime)).getHours()}:{(new Date(model.endTime)).getMinutes()}</p>
            <h1>Total Time: {getTimeBetween()}</h1>
        </div>
    )
}

function HourApprovalRequestList() {
    const [model, setModel] = useState<HourApprovalRequest[]>([]);
    let { id } = useParams();
    const [isManager, setIsManager] = useState(false);
    const [ready, setReady] = useState(false);
    const navigate = useNavigate();
    const fetchRequests = async () => {
        try{
            let found = await getVolunteeringHourRequests(parseInt(id!));
            console.log(found)

            await setModel(found);
            setReady(true);
        }catch(e){
            //send to error page
            alert(e)
        }
    }
    useEffect(() => {
        fetchRequests();
    }, [])
  return (
    <div>
        {model.map(request => <Request model={request}/>)}
    </div>
  )
}

export default HourApprovalRequestList