import { useEffect, useState } from 'react'
import './../css/Volunteering.css'
import VolunteeringModel, { VolunteersToGroup } from '../models/VolunteeringModel'
import { getIsManager, getVolunteering, getVolunteeringVolunteers } from '../api/volunteering_api'
import { useNavigate, useParams } from "react-router-dom";

interface GroupToVolunteers {
    [key: number]: string[];
}

function GroupRow({ groupId, volunteers } : {groupId:number, volunteers: string[]}) {
    return (
        <div className='groupRow'>
            <h1 className='groupId'>Group {groupId}</h1>
            {volunteers.map(volunteer => <p className='volunteerUsername'>{volunteer}</p>)}
            <hr/>
        </div>
    )
}

function Volunteering() {
    const [model, setModel] = useState<VolunteeringModel>({id: -1, orgId: -1, name: "", description: "", skills: [], categories: []});
    const [groups, setGroups] = useState<GroupToVolunteers>({});
    let { id } = useParams();
    const [isManager, setIsManager] = useState(false);
    const [ready, setReady] = useState(false);
    let navigate = useNavigate();
    const fetchVolunteering = async () => {
        try{
            let found = await getVolunteering(parseInt(id!));
            await setModel(found);
            let volunteers: VolunteersToGroup = await getVolunteeringVolunteers(parseInt(id!));
            console.log(volunteers)
            let fetchedGroups: GroupToVolunteers = {};
            Object.entries(volunteers).forEach(([key, value]) => {
                if(!fetchedGroups.hasOwnProperty(value)){
                    fetchedGroups[value] = [];
                }
                fetchedGroups[value].push(key)
            })
            setGroups(fetchedGroups);
            setReady(true);
        }catch(e){
            //send to error page
            alert(e)
        }
    }

    const updatePermissions = async () => {
        try{
            setIsManager(await getIsManager(model.orgId))
        }catch(e){
            //send to error page
            alert(e)
        }
    }
    useEffect(() => {
        fetchVolunteering();
    }, [])
    useEffect(() =>{
        if(ready){
            updatePermissions();
        }
    }, [model, ready])
  return (
    <div>
        <div className="volInfo">
            <div className='volInfoText'>
                <h1>{model.name}</h1>
                <p>{model.description}</p>
            </div>
            {isManager ?
            <div className='volInfoButtons'>
                <button>Settings</button>
                <button>View Join Requests</button>
                <button>View Hour Approval Requests</button>
            </div> : <></>}
        </div>
        {isManager ? 
        <div className='scanButtons'>
                <button onClick={() => navigate("./code")}>Show Changing QR Code</button>
        </div> :
        <div className='scanButtons'>
            <button onClick={() => navigate("./scan")}>Scan QR Code</button>
        </div>}
        {isManager ? 
        <div className="volunteers">
            {Object.entries(groups).map(([key, value]) => <GroupRow groupId={parseInt(key)} volunteers={value}/>)}
        </div> : <></>}
    </div>
  )
}

export default Volunteering