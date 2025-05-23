type ScheduleRange = {
    id: number,
    startTime: string,
    endTime: string,
    weekDays: boolean[],
    oneTime: string,
    restrict: RestrictionTuple[]
    minimumAppointmentMinutes: number,
    maximumAppointmentMinutes: number
}

export type RestrictionTuple = {
    startTime: string,
    endTime: string,
    amount: number
}

export default ScheduleRange;