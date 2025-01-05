type ScheduleAppointment = {
    userId: string,
    volunteeringId: number,
    rangeId: number,
    startTime: string,
    endTime: string,
    weekDays: boolean[],
    oneTime: string
}

export default ScheduleAppointment;