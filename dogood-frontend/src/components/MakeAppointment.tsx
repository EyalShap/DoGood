import { DayPilotCalendar, DayPilot } from '@daypilot/daypilot-lite-react';
import './../css/Volunteering.css'
import "./../css/MakeAppointment.css"
import React, { useEffect, useState } from 'react';
import ScheduleRange from '../models/ScheduleRange';
import { getUserAssignedLocationData, getVolunteerAvailableRanges, getVolunteerGroup, makeAppointment } from '../api/volunteering_api';
import {useNavigate, useParams} from 'react-router-dom';
import Location from '../models/Location';
import { DatePicker, LocalizationProvider, TimePicker } from '@mui/x-date-pickers';
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";
import dayjs from 'dayjs';
import { Checkbox, FormControl, FormControlLabel, FormGroup, FormLabel, Radio, RadioGroup } from '@mui/material';
import {
    TbSquareLetterF,
    TbSquareLetterM,
    TbSquareLetterT,
    TbSquareLetterW, TbSquareLetterFFilled,
    TbSquareLetterMFilled, TbSquareLetterS,
    TbSquareLetterSFilled, TbSquareLetterTFilled, TbSquareLetterWFilled
} from "react-icons/tb";
import {FaChevronDown} from "react-icons/fa";
import {format} from "date-fns";

const daysEmpty = [<TbSquareLetterS className="empty"/>,<TbSquareLetterM className="empty"/>,<TbSquareLetterT className="empty"/>,<TbSquareLetterW className="empty"/>,<TbSquareLetterT className="empty"/>,<TbSquareLetterF className="empty"/>,<TbSquareLetterS className="empty"/>];
const daysFilled = [<TbSquareLetterSFilled className="filled"/>,<TbSquareLetterMFilled className="filled"/>,<TbSquareLetterTFilled className="filled"/>,<TbSquareLetterWFilled className="filled"/>,<TbSquareLetterTFilled className="filled"/>,<TbSquareLetterFFilled className="filled"/>,<TbSquareLetterSFilled className="filled"/>];

function ActualAppointmentMaker({ volunteeringId, range }: { volunteeringId: number, range: ScheduleRange }) {
    const [startTime, setStartTime] = useState(dayjs('2024-01-01T'+range.startTime));
    const [endTime, setEndTime] = useState(dayjs('2024-01-01T'+range.endTime));
    const [weekOrOne, setWeekOrOne] = useState("one");

    const [oneTime, setOneTime] = useState(dayjs());

    const [sunday, setSunday] = useState(false);
    const [monday, setMonday] = useState(false);
    const [tuesday, setTuesday] = useState(false);
    const [wednesday, setWednesday] = useState(false);
    const [thursday, setThursday] = useState(false);
    const [friday, setFriday] = useState(false);
    const [saturday, setSaturday] = useState(false);

    const navigate = useNavigate();

    const hasDay = (day: number) => {
        if(range.weekDays !== null){
            return range.weekDays[day];
        }
        return day === dayjs(range.oneTime+'T00:00:00').day();
    }

    const getOneTime = () => {
        if(range.oneTime !== null){
            return range.oneTime;
        }
        if(weekOrOne === "week"){
            return null;
        }
        return oneTime.format('YYYY-MM-DD');
    }

    const getWeekDays = () => {
        if(weekOrOne === "week"){
            return [sunday,monday,tuesday,wednesday,thursday,friday,saturday];
        }
        return null;
    }

    const send = async () => {
        try{
            await makeAppointment(volunteeringId, range.id, startTime.hour(), startTime.minute(),
    endTime.hour(), endTime.minute(), getOneTime(), getWeekDays());
            alert("Appointment made!");
            navigate(`/volunteering/${volunteeringId}`);
        }catch(e){
            alert(e)
        }
    }


    return (
        <div className='maker'>
            <LocalizationProvider dateAdapter={AdapterDayjs}>
                <TimePicker className='timePicker' ampm={false} label="Start Time" value={startTime} onChange={newValue => newValue != null && setStartTime(newValue)} minTime={dayjs('2024-01-01T'+range.startTime)} maxTime={dayjs('2024-01-01T'+range.endTime)} />
                <TimePicker className='timePicker' ampm={false} label="End Time" value={endTime} onChange={newValue => newValue != null && setEndTime(newValue)} minTime={dayjs('2024-01-01T'+range.startTime)} maxTime={dayjs('2024-01-01T'+range.endTime)} />
            </LocalizationProvider>
            {range.weekDays !== null &&
            <div className='selector'> 
            <FormControl>
                <FormLabel>Weekly or One Time?</FormLabel>
                <RadioGroup
                    value={weekOrOne}
                    onChange={e => setWeekOrOne(e.target.value)}
                    name="radio-buttons-group"
                    row
                >
                    <FormControlLabel value="week" control={<Radio />} label="Weekly" />
                    <FormControlLabel value="one" control={<Radio />} label="One Time" />
                </RadioGroup>
            </FormControl>
            {weekOrOne === "week" ?
                <div>
                    <FormGroup>
                        <FormControlLabel
                            control={
                                <Checkbox disabled={!hasDay(0)} checked={sunday} onChange={e => setSunday(e.target.checked)} name="sunday" />
                            }
                            label="Sunday"
                        />
                        <FormControlLabel
                            control={
                                <Checkbox disabled={!hasDay(1)} checked={monday} onChange={e => setMonday(e.target.checked)} name="monday" />
                            }
                            label="Monday"
                        />
                        <FormControlLabel
                            control={
                                <Checkbox disabled={!hasDay(2)} checked={tuesday} onChange={e => setTuesday(e.target.checked)} name="tuesday" />
                            }
                            label="Tuesday"
                        />
                        <FormControlLabel
                            control={
                                <Checkbox disabled={!hasDay(3)} checked={wednesday} onChange={e => setWednesday(e.target.checked)} name="wednesday" />
                            }
                            label="Wednesday"
                        />
                        <FormControlLabel
                            control={
                                <Checkbox disabled={!hasDay(4)} checked={thursday} onChange={e => setThursday(e.target.checked)} name="thursday" />
                            }
                            label="Thursday"
                        />
                        <FormControlLabel
                            control={
                                <Checkbox disabled={!hasDay(5)} checked={friday} onChange={e => setFriday(e.target.checked)} name="friday" />
                            }
                            label="Friday"
                        />
                        <FormControlLabel
                            control={
                                <Checkbox disabled={!hasDay(6)} checked={saturday} onChange={e => setSaturday(e.target.checked)} name="saturday" />
                            }
                            label="Saturday"
                        />
                    </FormGroup>
                </div> : <LocalizationProvider dateAdapter={AdapterDayjs}><DatePicker value={oneTime} onChange={newValue => newValue != null && setOneTime(newValue)}/></LocalizationProvider>}
                </div>}
                <button className='orangeCircularButton' onClick={send}>Make Appointment</button>
        </div>
    )
}

function AvailableRange({model, setter, volunteeringId, selected}: {model: ScheduleRange, setter:  React.Dispatch<React.SetStateAction<ScheduleRange | null | undefined>>, volunteeringId: number, selected: ScheduleRange | null | undefined}){
    const [opened, setOpened] = useState(false);

    return (
        <div className="scheduleWrapper">
            <div onClick={() => setter(prev => prev === model ? null : model)} className="schedule" onMouseEnter={() => setOpened(true)}
                 onMouseLeave={() => setOpened(false)}>
                <h2>{model.startTime.slice(0, -3)}-{model.endTime.slice(0, -3)}</h2>
                {model.oneTime !== null ?
                    <p className="oneTime">On {format(new Date(model.oneTime), "dd/MM/yyyy (E)")}</p> :
                    <div className="dayBoxes">
                        {model.weekDays.map((val, i) => val ? daysFilled[i] : daysEmpty[i])}
                    </div>}
                <div className={`schedInfo${(opened || model === selected) ? " opened" : ""}`}>
                    {model.minimumAppointmentMinutes > -1 && model.maximumAppointmentMinutes === -1 ?
                        <p>Must sign up to at least {model.minimumAppointmentMinutes} minutes</p> :
                        model.maximumAppointmentMinutes > -1 && model.minimumAppointmentMinutes === -1 ?
                            <p>Must sign up to at most {model.maximumAppointmentMinutes} minutes</p> :
                            model.maximumAppointmentMinutes > -1 && model.minimumAppointmentMinutes > -1 &&
                            <p>Must sign up to between {model.minimumAppointmentMinutes} and {model.maximumAppointmentMinutes} minutes</p>}
                    {model.restrict.length > 0 && <br/>}
                    {model.restrict.map(restrict =>
                        <p className="restrict">Limited to {restrict.amount} volunteers
                            from <b>{restrict.startTime.slice(0, -3)}</b> to <b>{restrict.endTime.slice(0, -3)}</b>
                        </p>)}
                </div>
                {(model.restrict.length > 0 || model.minimumAppointmentMinutes > -1 || model.maximumAppointmentMinutes > -1) &&
                    <FaChevronDown className={`triangle${(opened || model === selected) ? " rotriangle" : ""}`}/>}
            </div>
            <div className={`makerWrapper${model === selected ? " chosen" : ""}`}>
                <ActualAppointmentMaker key={model.id} volunteeringId={volunteeringId} range={model}/>
            </div>
        </div>
    )
}

function MakeAppointment() {
    const getLastSunday = (d: Date) => {
        var t = new Date(d);
        t.setDate(t.getDate() - t.getDay());
        return t;
    }
    const [ranges, setRanges] = useState<ScheduleRange[]>([])

    const [location, setLocation] = useState<Location>({id: 0, name: "", address: {city: "", street: "", address: ""}})
    const [group, setGroup] = useState(0)
    const [selectedRange, setSelectedRange] = useState<ScheduleRange | null | undefined>(null)

    let {id} = useParams();

    const fetchAppointments = async () => {
        try {
            setRanges(await getVolunteerAvailableRanges(parseInt(id!)))
            setLocation(await getUserAssignedLocationData(parseInt(id!)))
            setGroup(await getVolunteerGroup(parseInt(id!)))
        } catch (e) {
            alert(e)
        }
    }

    useEffect(() => {
        fetchAppointments()
    }, [])
    return (
        <div className='makeAppointment'>
            <h1>{location.id == -1 ? `Available appointment ranges for Group ${group} for ${localStorage.getItem("username")}` : `Available appointment ranges for Group ${group} at ${location.name} for ${localStorage.getItem("username")}`}</h1>
            <div className='schedules'>
                {ranges.map(range => <AvailableRange model={range} setter={setSelectedRange} volunteeringId={parseInt(id!)}
                                                     selected={selectedRange}/>)}
            </div>
        </div>
    )
}

export default MakeAppointment;