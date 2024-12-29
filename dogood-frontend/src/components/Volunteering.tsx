import { useEffect, useState } from 'react'
import './../css/Volunteering.css'
import VolunteeringModel from '../models/VolunteeringModel'
import { getVolunteering } from '../api/volunteering_api'
import { useParams } from "react-router-dom";

function Volunteering() {
    const [model, setModel] = useState<VolunteeringModel>({id: -1, orgId: -1, name: "", description: "", skills: [], categories: []})
    let { id } = useParams();
    const fetchVolunteering = async () => {
        try{
            let found = await getVolunteering(parseInt(id!))
            setModel(found)
        }catch(e){
            //send to error page
            alert(e)
        }
    }
    useEffect(() => {
        fetchVolunteering();
    })
  return (
    <div>
        <div className="volInfo">
            <div className='volInfoText'>
                <h1>{model.name}</h1>
                <p>{model.description}</p>
            </div>
            <div className='volInfoButtons'>
                <button>Settings</button>
                <button>View Join Requests</button>
                <button>View Hour Approval Requests</button>
            </div>
        </div>
    </div>
  )
}

export default Volunteering