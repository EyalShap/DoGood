import { DayPilotCalendar, DayPilot } from '@daypilot/daypilot-lite-react';
import './../css/Volunteering.css'
import "./../css/MakeAppointment.css"
import { useEffect, useState } from 'react';
import ScheduleRange from '../models/ScheduleRange';
import { getUserAssignedLocationData, getVolunteerAvailableRanges, getVolunteerGroup, makeAppointment } from '../api/volunteering_api';
import { useParams } from 'react-router-dom';
import Location from '../models/Location';
import { DatePicker, LocalizationProvider, TimePicker } from '@mui/x-date-pickers';
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";
import dayjs from 'dayjs';
import { Checkbox, FormControl, FormControlLabel, FormGroup, FormLabel, Radio, RadioGroup } from '@mui/material';

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
        }catch(e){
            alert(e)
        }
    }


    return (
        <div className='maker'>
            <p>Selected Range: {range.startTime}-{range.endTime}, ID: {range.id}</p>
            {range.oneTime !== null && <p>At {range.oneTime}</p>}
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
                <button className='sendButton' onClick={send}>Make Appointment</button>
        </div>
    )
}

function MakeAppointment() {
    const getLastSunday = (d: Date) => {
        var t = new Date(d);
        t.setDate(t.getDate() - t.getDay());
        return t;
    }
    const [startDate, setStartDate] = useState(getLastSunday(new Date))
    const [ranges, setRanges] = useState<ScheduleRange[]>([])
    const [events, setEvents] = useState<DayPilot.EventData[]>([])
    const [ready, setReady] = useState(false)
    const [width, setWidth] = useState<number>(window.innerWidth);

    const [location, setLocation] = useState<Location>({ id: 0, name: 0, address: { city: "", street: "", address: "" } })
    const [group, setGroup] = useState(0)
    const [selectedRange, setSelectedRange] = useState<ScheduleRange | null | undefined>(null)

    let { id } = useParams();

    const handleWindowSizeChange = () => {
        setWidth(window.innerWidth);
    }


    const addWeeks = (d: number) => {
        let date = new Date(startDate)
        date.setDate(startDate.getDate() + d * 7)
        setStartDate(date)
    }

    const rangeToEvents = (range: ScheduleRange): DayPilot.EventData[] => {
        var limitText = ``
        if (range.minimumAppointmentMinutes > 0 && range.minimumAppointmentMinutes > 0) {
            limitText += `Must do between ${range.minimumAppointmentMinutes} to ${range.maximumAppointmentMinutes} minutes`;
        }
        else if (range.minimumAppointmentMinutes > 0) {
            limitText += `Must do at least ${range.minimumAppointmentMinutes} minutes`;
        }
        else if (range.maximumAppointmentMinutes > 0) {
            limitText += `Must do at most ${range.maximumAppointmentMinutes} minutes`;
        }
        if (range.oneTime !== null) {
            return [{
                text: `One Time Available Appointment Range\n${limitText}\nID: ${range.id}`,
                id: range.id,
                start: `${range.oneTime}T${range.startTime}`,
                end: `${range.oneTime}T${range.endTime}`,
            }]
        }
        let eventlist: DayPilot.EventData[] = []
        let days = []
        for (let i = 0; i < 7; i++) {
            if (range.weekDays[i]) {
                days.push(i)
            }
        }
        days.forEach(d => {
            let appointmentDay = new DayPilot.Date(startDate, true)
            appointmentDay = appointmentDay.addDays(d)
            let dayString = appointmentDay.toString().split("T")[0]
            let ev: DayPilot.EventData = {
                text: `Weekly Appointment Range\n${limitText}\nID: ${range.id}`,
                id: range.id,
                start: `${dayString}T${range.startTime}`,
                end: `${dayString}T${range.endTime}`
            }
            eventlist.push(ev)
        })
        return eventlist
    }

    const updateEvents = () => {
        let eventList: DayPilot.EventData[] = [];
        ranges.forEach(appoint => {
            eventList = eventList.concat(rangeToEvents(appoint))
        })
        setEvents(eventList)
    }

    const fetchAppointments = async () => {
        try {
            setRanges(await getVolunteerAvailableRanges(parseInt(id!)))
            setLocation(await getUserAssignedLocationData(parseInt(id!)))
            setGroup(await getVolunteerGroup(parseInt(id!)))
            setReady(true)
        } catch (e) {
            alert(e)
        }
    }

    useEffect(() => {
        fetchAppointments()
        window.addEventListener('resize', handleWindowSizeChange);
        return () => {
            window.removeEventListener('resize', handleWindowSizeChange);
        }
    }, [])

    useEffect(() => {
        if (ready) {
            updateEvents()
        }
    }, [ranges, startDate, ready])

    const isMobile = width <= 768;

    return (
        <div className='makeAppointment'>
            <h1>Available appointment ranges for Group {group} at {location.name} for {localStorage.getItem("username")}</h1>
            <div className='weekButtons'>
                <button className='left weekButton' onClick={() => addWeeks(-1)}>← Last Week</button>
                <button className='right weekButton' onClick={() => addWeeks(1)}>Next Week →</button>
            </div>
            <div className='calender'>
                <div className='innercalender'>
            <DayPilotCalendar
                startDate={new DayPilot.Date(startDate, true)}
                viewType='Week'
                headerDateFormat='dddd dd/MM/yyyy'
                events={events}
                height={isMobile ? 100 : 300}
                cellHeight={isMobile ? 15 : 30}
                headerTextWrappingEnabled={true}
                eventMoveHandling='Disabled'
                onEventClicked={args => setSelectedRange(ranges.find(range => range.id == args.e.id()))} />
                </div></div>
            {selectedRange === null || selectedRange === undefined ? <></> :
                <ActualAppointmentMaker volunteeringId={parseInt(id!)} range={selectedRange!} />
            }
        </div>
    )
}

export default MakeAppointment;