type ReportModel = {
    id: number,
    reportingUser: string, 
    reportedPostId: number,
    description: string,
    date: string
}

export default ReportModel;