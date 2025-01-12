import { getAllOrganizations } from '../api/organization_api'
import { useEffect, useState } from 'react'
import OrganizationModel from '../models/OrganizationModel';
import { useNavigate } from 'react-router-dom';
import { getAllReports, removeReport } from '../api/report_api';
import ReportModel from '../models/ReportModel';
import { getVolunteeringPost } from '../api/post_api';
import { VolunteeringPostModel } from '../models/VolunteeringPostModel';
import './../css/ReportList.css'

function ReportList() {
    const navigate = useNavigate();

    const [reports, setReports] = useState<ReportModel[]>([]);
    const [postTitles, setPostTitles] = useState<{ [key: number]: string }>({});
    
    const fetchReports = async () => {
        try {
            const reports = await getAllReports();
            setReports(reports);

            for (let report of reports) {
                try {
                    const post: VolunteeringPostModel = await getVolunteeringPost(report.reportedPostId);
                    const title: string = post.title;
                                
                    setPostTitles((prevTitles) => ({
                        ...prevTitles,
                        [report.id]: title,
                    }));
                } catch (e) {
                    alert(e);
                }
            }
        } catch (e) {
            // send to error page
            alert(e);
        }
    }

    useEffect(() => {
        fetchReports();
    }, [])

    const handleShowOnClick = (postId: number) => {
        navigate(`/volunteeringPost/${postId}`);
    };

    const handleDeleteOnClick = async (reportId: number) => {
        if(window.confirm("Are you sure you want to remove this report?")) {
            try {
                await removeReport(reportId);
                alert("Report removed successfully!");
                let newReports = reports.filter((report) => report.id !== reportId);
                setReports(newReports);
            }
            catch(e) {
                alert(e);
            }
        }
    };

    const fixDate = (dateJson: string) : string => {
        let date: Date = new Date(dateJson);
        let dateStr = `${date.getDate()}/${date.getMonth() + 1}/${date.getFullYear()}`;
        return dateStr;
    }
    
    return (
        <div>
            <div className="Reports">
                <h2>Reports</h2>
                {reports.length > 0 ? (
                    reports.map((report, index) => (
                        <div key={index} className="reportItem">
                            <div id = "info"> 
                                <h3>Report on post: {postTitles[report.id]}</h3>
                                <p>Reported by {report.reportingUser} on {fixDate(report.date)}</p>
                                <p>Issue: {report.description}</p>
                            </div>

                            <div id = "buttons">
                                <button onClick={() => handleShowOnClick(report.reportedPostId)}>Show Post</button>
                                <button onClick={() => handleDeleteOnClick(report.id)}>Delete Report</button>
                            </div>
                        </div>
                    ))
                ) : (
                    <p id = "noReports">No reports available.</p>
                )}
            </div>
            
        </div>
    )
}

export default ReportList