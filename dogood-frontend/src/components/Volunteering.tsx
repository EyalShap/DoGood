import React, { useEffect, useState } from 'react'
import './../css/Volunteering.css'
import VolunteeringModel, { VolunteersToGroup } from '../models/VolunteeringModel'
import { addRestrictionToRange, addScheduleRangeToGroup, assignVolunteerToLocation, cancelAppointment, createNewGroup, finishVolunteering, getGroupLocations, getIsManager, getUserAssignedLocation, getVolunteerAppointments, getVolunteerGroup, getVolunteering, getVolunteeringGroups, getVolunteeringLocationGroupRanges, getVolunteeringLocations, getVolunteeringVolunteers, moveVolunteerGroup, removeGroup, removeRange, removeRestrictionFromRange, requestHoursApproval } from '../api/volunteering_api'
import { useNavigate, useParams } from "react-router-dom";
import ScheduleAppointment from '../models/ScheduleAppointment';
import { DayPilotCalendar } from '@daypilot/daypilot-lite-react';
import { DayPilot } from '@daypilot/daypilot-lite-react';
import Location from '../models/Location';

import ScheduleRange, { RestrictionTuple } from '../models/ScheduleRange';
import dayjs, { Dayjs } from 'dayjs';
import { DatePicker, DateTimePicker, LocalizationProvider, TimePicker } from '@mui/x-date-pickers';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { Checkbox, FormControl, FormControlLabel, FormGroup, FormLabel, Radio, RadioGroup } from '@mui/material';
import NumberInput from './NumberInput';
import Popup from 'reactjs-popup';



interface GroupToVolunteers {
    [key: number]: string[];
}

function GroupRow({ groupId, volunteers, deleteGroup, onDragStart, onDrop }: { groupId: number, volunteers: string[], deleteGroup: (groupId: number) => void, onDragStart: (e: React.DragEvent<HTMLParagraphElement>, volunteer: string, from: number) => void, onDrop: (e: any, to: number) => void }) {

    return (
        <div className='groupRow' onDragEnter={(e) => onDrop(e, groupId)}>
            <div className='groupHeader'>
                <h1 className='groupId'>Group {groupId}</h1>
                <button onClick={() => deleteGroup(groupId)}>Remove Group</button>
            </div>
            {volunteers.map(volunteer => <p draggable onDragStart={(e) => onDragStart(e, volunteer, groupId)} className='volunteerUsername'>{volunteer}</p>)}
            <hr />
        </div>
    )
}

function AppointmentCalender({ volunteeringId }: { volunteeringId: number }) {
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
        date.setDate(startDate.getDate() + d * 7)
        setStartDate(date)
    }

    const appointmentToEvents = (appoint: ScheduleAppointment): DayPilot.EventData[] => {
        if (appoint.oneTime !== null) {
            return [{
                text: "One Time Appointment",
                id: 0,
                start: `${appoint.oneTime}T${appoint.startTime}`,
                end: `${appoint.oneTime}T${appoint.endTime}`,
            }]
        }
        let eventlist: DayPilot.EventData[] = []
        let days = []
        for (let i = 0; i < 7; i++) {
            if (appoint.weekDays[i]) {
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

    const fetchAppointments = async () => {
        try {
            setAppointments(await getVolunteerAppointments(volunteeringId))
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
    }, [appointments, startDate, ready])


    const onCancel = async (startTime: DayPilot.Date) => {
        try {
            await cancelAppointment(volunteeringId, startTime.getHours(), startTime.getMinutes());
            fetchAppointments();
        }
        catch (e) {
            alert(e)
        }
    }

    const onRequest = async (startDate: DayPilot.Date, endDate: DayPilot.Date) => {
        try {
            await requestHoursApproval(volunteeringId, startDate.toDateLocal().toISOString(), endDate.toDateLocal().toISOString());
            alert("Request Sent Successfully!")
        }
        catch (e) {
            alert(e)
        }
    }

    const isMobile = width <= 768;

    return (
        <div>
            <div>
                <button onClick={() => addWeeks(-1)}>Last Week</button>
                <button onClick={() => addWeeks(1)}>Next Week</button>
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
                        eventMoveHandling='Disabled'
                        eventResizeHandling='Disabled'
                        contextMenu={new DayPilot.Menu(
                            {
                                items: [
                                    {
                                        text: "Cancel Appointment",
                                        onClick: args => onCancel(args.source.start())
                                    },
                                    {
                                        text: "Request Approval",
                                        onClick: args => onRequest(args.source.start(), args.source.end())
                                    }
                                ]
                            }
                        )}
                        headerTextWrappingEnabled={true} />
                </div></div>
        </div>
    )
}

function RangeMaker({ groupId, locId, volunteeringId, refreshRanges }: { groupId: number, locId: number, volunteeringId: number, refreshRanges: () => void }) {
    const [startTime, setStartTime] = useState(dayjs('2024-01-01T00:00'));
    const [endTime, setEndTime] = useState(dayjs('2024-01-01T00:00'));
    const [weekOrOne, setWeekOrOne] = useState("one");

    const [oneTime, setOneTime] = useState(dayjs());

    const [sunday, setSunday] = useState(false);
    const [monday, setMonday] = useState(false);
    const [tuesday, setTuesday] = useState(false);
    const [wednesday, setWednesday] = useState(false);
    const [thursday, setThursday] = useState(false);
    const [friday, setFriday] = useState(false);
    const [saturday, setSaturday] = useState(false);

    const [minimumMinutes, setMinimumMinutes] = useState(-1);
    const [maximumMinutes, setMaximumMinutes] = useState(-1);

    const [showMinMin, setShowMinMin] = useState(false)
    const [showMaxMin, setShowMaxMin] = useState(false)

    const hasDay = (day: number) => {
        return true;
    }

    const getOneTime = () => {
        if (weekOrOne === "week") {
            return null;
        }
        return oneTime.format('YYYY-MM-DD');
    }

    const getWeekDays = () => {
        if (weekOrOne === "week") {
            return [sunday, monday, tuesday, wednesday, thursday, friday, saturday];
        }
        return null;
    }

    const send = async () => {
        try {
            await addScheduleRangeToGroup(volunteeringId, groupId, locId, minimumMinutes, maximumMinutes, startTime.hour(), startTime.minute(),
                endTime.hour(), endTime.minute(), getOneTime(), getWeekDays());
            refreshRanges();
        } catch (e) {
            alert(e)
        }
    }


    return (
        <div className='maker'>
            <p>Create a new range</p>
            <LocalizationProvider dateAdapter={AdapterDayjs}>
                <TimePicker className='timePicker' ampm={false} label="Start Time" value={startTime} onChange={newValue => newValue != null && setStartTime(newValue)} />
                <TimePicker className='timePicker' ampm={false} label="End Time" value={endTime} onChange={newValue => newValue != null && setEndTime(newValue)} />
            </LocalizationProvider>
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
                    </div> : <LocalizationProvider dateAdapter={AdapterDayjs}><DatePicker value={oneTime} onChange={newValue => newValue != null && setOneTime(newValue)} /></LocalizationProvider>}
            </div>
            <div>
                <FormControlLabel control={<Checkbox onChange={e => {
                    setShowMinMin(e.target.checked)
                    e.target.checked ? setMinimumMinutes(0) : setMinimumMinutes(-1)
                }} />} label="Minimum Appointment Minutes?" />
                {showMinMin && <NumberInput value={minimumMinutes} onChange={(_, val) => val != null && setMinimumMinutes(val)} min={0} />}
            </div>
            <div>
                <FormControlLabel control={<Checkbox onChange={e => {
                    setShowMaxMin(e.target.checked)
                    e.target.checked ? setMaximumMinutes(0) : setMaximumMinutes(-1)
                }} />} label="Maximum Appointment Minutes?" />
                {showMaxMin && <NumberInput value={maximumMinutes} onChange={(_, val) => val != null && setMaximumMinutes(val)} min={Math.max(0, minimumMinutes)} />}
            </div>
            <button className='sendButton' onClick={send}>Create Range</button>
        </div>
    )
}

function RestrictionMaker({ volunteeringId, groupId, locId, range, refreshRange }: { volunteeringId: number, groupId: number, locId: number, range: ScheduleRange, refreshRange: (range: ScheduleRange | null) => void }) {
    const [startTime, setStartTime] = useState(dayjs('2024-01-01T' + range.startTime));
    const [endTime, setEndTime] = useState(dayjs('2024-01-01T' + range.endTime));
    const [amount, setAmount] = useState(0);

    const send = async () => {
        try {
            await addRestrictionToRange(volunteeringId, groupId, locId, range.id, startTime.hour(), startTime.minute(), endTime.hour(), endTime.minute(), amount);
            refreshRange(range);
        } catch (e) {
            alert(e)
        }
    }

    const remove = async (restrict: RestrictionTuple) => {
        try {
            let sTime = dayjs('2024-01-01T' + restrict.startTime);
            await removeRestrictionFromRange(volunteeringId, groupId, locId, range.id, sTime.hour(), sTime.minute());
            refreshRange(range);
        } catch (e) {
            alert(e)
        }
    }

    const onDelete = async () => {
        try {
            await removeRange(volunteeringId, range.id);
            refreshRange(null);
        } catch (e) {
            alert(e)
        }
    }

    return (
        <div className='maker'>
            <p>Selected Range: {range.startTime}-{range.endTime}, ID: {range.id}</p>
            {range.oneTime !== null && <p>At {range.oneTime}</p>}
            <p>Current Restrictions:</p>
            {range.restrict.map(restriction => <div className='restriction'>
                <div className='restrictionData'>
                    <p>Starts at: {restriction.startTime}</p>
                    <p>Ends at: {restriction.endTime}</p>
                    <p>Limited to {restriction.amount} volunteers</p>
                </div>
                <button onClick={() => remove(restriction)}>Remove</button>
            </div>)}
            <LocalizationProvider dateAdapter={AdapterDayjs}>
                <TimePicker className='timePicker' ampm={false} label="Start Time" value={startTime} onChange={newValue => newValue != null && setStartTime(newValue)} minTime={dayjs('2024-01-01T' + range.startTime)} maxTime={dayjs('2024-01-01T' + range.endTime)} />
                <TimePicker className='timePicker' ampm={false} label="End Time" value={endTime} onChange={newValue => newValue != null && setEndTime(newValue)} minTime={dayjs('2024-01-01T' + range.startTime)} maxTime={dayjs('2024-01-01T' + range.endTime)} />
            </LocalizationProvider>
            <NumberInput value={amount} onChange={(_, val) => val != null && setAmount(val)} min={0} />
            <button className='sendButton' onClick={send}>Add Restriction</button>
            <button className='sendButton' onClick={onDelete}>Delete Range</button>
        </div>
    )
}

function ManageRangesPanel({ rerender, volunteeringId, groups }: { rerender: number, volunteeringId: number, groups: number[] }) {
    const getLastSunday = (d: Date) => {
        var t = new Date(d);
        t.setDate(t.getDate() - t.getDay());
        return t;
    }
    const [startDate, setStartDate] = useState(getLastSunday(new Date))
    const [locations, setLocations] = useState<Location[]>([])
    const [selectedLocation, setSelectedLocation] = useState<number>(-1)
    const [selectedGroup, setSelectedGroup] = useState<number>(-1)
    const [ranges, setRanges] = useState<ScheduleRange[]>([])
    const [width, setWidth] = useState<number>(window.innerWidth);
    const [events, setEvents] = useState<DayPilot.EventData[]>([])
    const [selectedRange, setSelectedRange] = useState<ScheduleRange | null | undefined>(null)

    const fetchLocations = async () => {
        try {
            setLocations(await getVolunteeringLocations(volunteeringId))
        } catch (e) {
            alert(e)
        }
    }

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
        if (range.minimumAppointmentMinutes > 0 && range.maximumAppointmentMinutes > 0) {
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

    const fetchRanges = async () => {
        try {
            setRanges(await getVolunteeringLocationGroupRanges(volunteeringId, selectedGroup, selectedLocation))
        } catch (e) {
            alert(e)
        }
    }

    const refreshRange = async (range: ScheduleRange | null) => {
        try {
            setRanges(await getVolunteeringLocationGroupRanges(volunteeringId, selectedGroup, selectedLocation))
            setSelectedRange(range)
        } catch (e) {
            alert(e)
        }
    }

    useEffect(() => {
        updateEvents();
    }, [ranges, startDate])

    const isMobile = width <= 768;

    useEffect(() => {
        fetchLocations();
        window.addEventListener('resize', handleWindowSizeChange);
        return () => {
            window.removeEventListener('resize', handleWindowSizeChange);
        }
    }, [])

    useEffect(() => {
        console.log(selectedLocation)
        if (selectedGroup > -1 && selectedLocation > -1) {
            fetchRanges();
        }
    }, [selectedGroup, selectedLocation])

    return (
        <div>
            <select onChange={e => setSelectedLocation(parseInt(e.target.value))}>
                <option value={-1}></option>
                {locations.map(location => <option value={location.id}>{location.name}</option>)}
            </select>
            <select onChange={e => setSelectedGroup(parseInt(e.target.value))}>
                <option value={-1}></option>
                {groups.map(group => <option value={group}>Group {group}</option>)}
            </select>
            {selectedGroup > -1 && selectedLocation > -1 &&
                <div className='rangePanel'>
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
                                eventMoveHandling='Disabled'
                                headerTextWrappingEnabled={true}
                                onEventClicked={args => setSelectedRange(ranges.find(range => range.id == args.e.id()))} />
                        </div></div>
                    <RangeMaker volunteeringId={volunteeringId} groupId={selectedGroup} locId={selectedLocation} refreshRanges={fetchRanges} />
                    {selectedRange === null || selectedRange === undefined ? <></> :
                        <RestrictionMaker refreshRange={refreshRange} volunteeringId={volunteeringId} groupId={selectedGroup} locId={selectedLocation} range={selectedRange!} />}
                </div>}
        </div>
    )
}

function LocationSelector({ volunteeringId, assignUser }: { volunteeringId: number, assignUser: (locId: number) => void }) {
    const [groupId, setGroupId] = useState(-1);
    const [ready, setReady] = useState(false);
    const [locations, setLocations] = useState<Location[]>([]);

    const fetchGroup = async () => {
        setGroupId(await getVolunteerGroup(volunteeringId));
        setReady(true);
    }

    const fetchLocations = async () => {
        setLocations(await getGroupLocations(volunteeringId, groupId));
    }

    useEffect(() => {
        fetchGroup()
    }, [])

    useEffect(() => {
        if (ready) {
            fetchLocations();
        }
    }, [groupId, ready])

    return (
        <div className='locationSelector'>
            <h1 className='chooseLocation'>Choose Location</h1>
            <div className='locationButtons'>
                {locations.map(location =>
                    <button onClick={() => assignUser(location.id)} className='location'>
                        <h2>{location.name}</h2>
                        <p>{location.address.city}</p>
                        <p>{location.address.street}</p>
                        <p>{location.address.address}</p>
                    </button>)}
            </div>
        </div>
    )
}

function HourRequestMaker({ volunteerindId, close }: { volunteerindId: number, close: any }) {
    const [startTime, setStartTime] = useState<Dayjs | null>(dayjs('2024-01-01T00:00:000'));
    const [endTime, setEndTime] = useState<Dayjs | null>(dayjs('2024-01-01T00:00:000'));
    const [date, setDate] = useState<Dayjs | null>(dayjs())

    const onRequest = async () => {
        try {
            let dateString = date?.toISOString()!.split('T')[0];
            let startString = startTime?.toISOString()!.split('T')[1];
            let endString = endTime?.toISOString()!.split('T')[1];
            await requestHoursApproval(volunteerindId, `${dateString}T${startString}`, `${dateString}T${endString}`);
            alert("Request Sent Successfully!")
            close();
        } catch (e) {
            alert(e)
        }
    }

    return (
        <div className='maker'>
            <div className='pickers'>
                <LocalizationProvider dateAdapter={AdapterDayjs}>
                    <DatePicker className='datetimepicker' label="Date" value={date} onChange={newValue => setDate(newValue)} maxDate={dayjs()}/>
                    <TimePicker ampm={false} className='datetimepicker' label="Start Time" value={startTime} onChange={(newValue) => setStartTime(newValue)}/>
                    <TimePicker ampm={false} className='datetimepicker' label="End Time" value={endTime} onChange={(newValue) => setEndTime(newValue)} minTime={endTime!}/>
                </LocalizationProvider>
            </div>
            <button onClick={onRequest}>Request</button>
        </div>
    )
}

function Leaver({ volunteerindId, close }: { volunteerindId: number, close: any }) {
    const [experience, setExperience] = useState("");

    const onRequest = async () => {
        try {
            await finishVolunteering(volunteerindId, experience);
            alert("Goodbye!")
            close();
        } catch (e) {
            alert(e)
        }
    }

    return (
        <div className='maker'>
            <div className='leave'>
                <h1>We're sad to see you go! Please leave your experience</h1>
                <input value={experience} onChange={e => setExperience(e.target.value)}/>
            </div>
            <button onClick={onRequest}>Goodbye</button>
        </div>
    )
}

function Volunteering() {
    const navigate = useNavigate();
    const [model, setModel] = useState<VolunteeringModel>({ id: -1, orgId: -1, name: "", description: "", skills: [], categories: [] });
    const [groups, setGroups] = useState<GroupToVolunteers>({});
    let { id } = useParams();
    const [isManager, setIsManager] = useState(false);
    const [ready, setReady] = useState(false);
    const [permissionsLoaded, setPemissionsLoaded] = useState(false)
    const [hasLocation, setHasLocation] = useState(false)
    const [rerenderManager, setRenenderManager] = useState(3);

    const [currentDragVolunteer, setCurrentDragVolunteer] = useState("");
    const [currentDragFrom, setCurrentDragFrom] = useState(-1);

    const fetchVolunteering = async () => {
        try {
            let found = await getVolunteering(parseInt(id!));
            await setModel(found);
            let volunteers: VolunteersToGroup = await getVolunteeringVolunteers(parseInt(id!));
            let groupList: number[] = await getVolunteeringGroups(parseInt(id!));
            let fetchedGroups: GroupToVolunteers = {};
            groupList.forEach(group => {
                fetchedGroups[group] = []
            })
            Object.entries(volunteers).forEach(([key, value]) => {
                fetchedGroups[value].push(key)
            })
            setGroups(fetchedGroups);
            setReady(true);
        } catch (e) {
            //send to error page
            alert(e)
        }
    }

    const updatePermissions = async () => {
        try {
            setIsManager(await getIsManager(model.orgId))
            setPemissionsLoaded(true)
        } catch (e) {
            //send to error page
            alert(e)
        }
    }

    const updateHasLocation = async () => {
        try {
            setHasLocation((await getUserAssignedLocation(model.id)) > -1)
        } catch (e) {
            //send to error page
            alert(e)
        }
    }

    useEffect(() => {
        fetchVolunteering();
    }, [])
    useEffect(() => {
        if (ready) {
            updatePermissions();
        }
    }, [model, ready])

    useEffect(() => {
        if (permissionsLoaded && !isManager) {
            updateHasLocation();
        }
    }, [isManager, permissionsLoaded])

    const handlePostVolunteeringOnClick = () => {
        navigate(`./createVolunteeringPost/-1`);
    }

    const deleteGroup = async (groupId: number) => {
        try {
            await removeGroup(parseInt(id!), groupId);
            fetchVolunteering();
        } catch (e) {
            alert(e)
        }
    }

    const onAddNewGroup = async () => {
        try {
            await createNewGroup(parseInt(id!));
            fetchVolunteering();
            setRenenderManager(5 - rerenderManager);
        } catch (e) {
            alert(e)
        }
    }

    const onDragStart = (e: React.DragEvent<HTMLParagraphElement>, volunteer: string, from: number) => {
        setCurrentDragVolunteer(volunteer);
        setCurrentDragFrom(from);
    }

    const onDrop = async (e: React.DragEvent<HTMLParagraphElement>, to: number) => {
        e.preventDefault();
        try {
            if (to !== currentDragFrom) {
                console.log("DROP")
                await moveVolunteerGroup(parseInt(id!), to, currentDragVolunteer);
                fetchVolunteering();
            }
        } catch (e) {
            alert(e)
        }
    }

    const assignUserToLocation = async (locId: number) => {
        try {
            await assignVolunteerToLocation(parseInt(id!), locId);
            fetchVolunteering();
            updateHasLocation();
        } catch (e) {
            alert(e);
        }
    }

    return (
        <div>
            <div className="volInfo">
                <div className='volInfoText'>
                    <h1>{model.name}</h1>
                    <p>{model.description}</p>
                </div>
                {isManager ?
                    <div className='volInfoButtons'>
                        <button onClick={() => navigate("./settings")}>Settings</button>
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
                    <Popup trigger={<button>Leave</button>} modal nested>
                        {/* 
                    // @ts-ignore */}
                        {close => (
                            <div className="modal">
                                <Leaver close={close} volunteerindId={parseInt(id!)} />
                            </div>
                        )}
                    </Popup>
                </div>}

            {isManager &&
                <div className='postVolunteering'>
                    <button onClick={handlePostVolunteeringOnClick}>Post Volunteering</button>
                </div>}

            {isManager ?
                <div className="volunteers">
                    <button onClick={() => onAddNewGroup()}>New Group</button>
                    {Object.entries(groups).map(([key, value]) => <GroupRow onDragStart={onDragStart} onDrop={onDrop} deleteGroup={deleteGroup} groupId={parseInt(key)} volunteers={value} />)}
                </div> : <></>}
            {permissionsLoaded && !isManager && hasLocation ? <AppointmentCalender volunteeringId={parseInt(id!)} /> : <></>}
            {!isManager && hasLocation ?
                <div className='scanButtons'>
                    <button onClick={() => navigate("./appointment")}>Make An Appointment</button>
                    <Popup trigger={<button>Request Hours Manually</button>} modal nested>
                        {/* 
                    // @ts-ignore */}
                        {close => (
                            <div className="modal">
                                <HourRequestMaker close={close} volunteerindId={parseInt(id!)} />
                            </div>
                        )}
                    </Popup>
                </div> : <></>}
            {!isManager && !hasLocation && <LocationSelector assignUser={assignUserToLocation} volunteeringId={parseInt(id!)} />}
            {isManager && <ManageRangesPanel rerender={rerenderManager} volunteeringId={parseInt(id!)} groups={Object.keys(groups).map(group => parseInt(group))} />}
        </div>
    )
}

export default Volunteering