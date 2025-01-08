import { useEffect, useState } from 'react'
import './../css/Volunteering.css'
import VolunteeringModel, { VolunteersToGroup } from '../models/VolunteeringModel'
import { getIsManager, getVolunteerAppointments, getVolunteering, getVolunteeringVolunteers } from '../api/volunteering_api'
import { useNavigate, useParams } from "react-router-dom";
import ScheduleAppointment from '../models/ScheduleAppointment';
import { DayPilotCalendar } from '@daypilot/daypilot-lite-react';
import { DayPilot } from '@daypilot/daypilot-lite-react';

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

function AppointmentCalender({ volunteeringId } : {volunteeringId:number}) {
    const getLastSunday = (d: Date) => {
        var t = new Date(d);
        t.setDate(t.getDate() - t.getDay());
        return t;
    }
    const [startDate, setStartDate] = useState(getLastSunday(new Date))
    const [appointments, setAppointments] = useState<ScheduleAppointment[]>([])
    const [events, setEvents] = useState<DayPilot.EventData[]>([])
    const [ready, setReady] = useState(false)
    const [width, setWidth] = useState<number>(window.innerWidth);

    const handleWindowSizeChange = () => {
        setWidth(window.innerWidth);
    }


    const addWeeks = (d: number) => {
        let date = new Date(startDate)
        date.setDate(startDate.getDate() + d*7)
        setStartDate(date)
    }

    const appointmentToEvents = (appoint: ScheduleAppointment): DayPilot.EventData[] => {
        if(appoint.oneTime !== null){
            return [{
                text: "One Time Appointment",
                id: 0,
                start: `${appoint.oneTime}T${appoint.startTime}`,
                end: `${appoint.oneTime}T${appoint.endTime}`,
            }]
        }
        let eventlist: DayPilot.EventData[] = []
        let days = []
        for(let i = 0; i < 7; i++){
            if(appoint.weekDays[i]){
                days.push(i)
            }
        }
        days.forEach(d => {
            let appointmentDay = new DayPilot.Date(startDate, true)
            appointmentDay = appointmentDay.addDays(d)
            let dayString = appointmentDay.toString().split("T")[0]
            let ev: DayPilot.EventData = {
                text: "Weekly Appointment",
                id: 0,
                start: `${dayString}T${appoint.startTime}`,
                end: `${dayString}T${appoint.endTime}`,
            }
            eventlist.push(ev)
        })
        return eventlist
    }

    const updateEvents = () => {
        let eventList: DayPilot.EventData[] = [];
        appointments.forEach(appoint => {
            eventList = eventList.concat(appointmentToEvents(appoint))
        })
        setEvents(eventList)
    }

    const fetchAppointments = async() => {
        try{
            setAppointments(await getVolunteerAppointments(volunteeringId))
            setReady(true)
        }catch(e){
            alert(e)
        }
    }

    useEffect(() => {
        fetchAppointments()
        window.addEventListener('resize', handleWindowSizeChange);
        return () => {
            window.removeEventListener('resize', handleWindowSizeChange);
        }
    },[])

    useEffect(() =>{
        if(ready){
            updateEvents()
        }
    }, [appointments, startDate, ready])

    const isMobile = width <= 768;

    return (
        <div>
            <div>
                <button onClick={() => addWeeks(-1)}>Last Week</button>
                <button onClick={() => addWeeks(1)}>Next Week</button>
            </div>
            <DayPilotCalendar 
            startDate={new DayPilot.Date(startDate, true)} 
            viewType='Week' 
            headerDateFormat='dddd dd/MM/yyyy'
            events={events}
            height={isMobile ? 100 : 300}
            cellHeight={isMobile ? 15 : 30}
            headerTextWrappingEnabled={true}/>
        </div>
    )
}

function Volunteering() {
    const [model, setModel] = useState<VolunteeringModel>({id: -1, orgId: -1, name: "", description: "", skills: [], categories: []});
    const [groups, setGroups] = useState<GroupToVolunteers>({});
    let { id } = useParams();
    const [isManager, setIsManager] = useState(false);
    const [ready, setReady] = useState(false);
    const [permissionsLoaded, setPemissionsLoaded] = useState(false)
    let navigate = useNavigate();
    const fetchVolunteering = async () => {
        try{
            let found = await getVolunteering(parseInt(id!));
            await setModel(found);
            let volunteers: VolunteersToGroup = await getVolunteeringVolunteers(parseInt(id!));
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
            setPemissionsLoaded(true)
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
                <button onClick={() => navigate("./jrequests")}>View Join Requests</button>
                <button onClick={() => navigate("./hrrequests")}>View Hour Approval Requests</button>
            </div> : <></>}
        </div>
        {isManager ? 
        <div className='scanButtons'>
                <button onClick={() => navigate("./code")}>Show Changing QR Code</button>
        </div> :
        <div className='scanButtons'>
            <button onClick={() => navigate("/scan")}>Scan QR Code</button>
        </div>}
        {isManager ? 
        <div className="volunteers">
            {Object.entries(groups).map(([key, value]) => <GroupRow groupId={parseInt(key)} volunteers={value}/>)}
        </div> : <></>}
        {permissionsLoaded && !isManager ? <AppointmentCalender volunteeringId={parseInt(id!)}/> : <></>}
    </div>
  )
}

export default Volunteering