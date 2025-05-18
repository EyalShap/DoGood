import { getAllOrganizations, getOrganization, removeOrganization } from '../api/organization_api'
import { useEffect, useState } from 'react'
import OrganizationModel from '../models/OrganizationModel';
import { useNavigate } from 'react-router-dom';
import { banEmail, getAllOrganizationReports, getAllReports, getAllUserReports, getAllVolunteeringPostReports, getAllVolunteeringReports, getAllVolunteerPostReports, getBannedEmails, removeOrganizationReport, removeUserReport, removeVolunteeringPostReport, removeVolunteeringReport, removeVolunteerPostReport, unbanEmail } from '../api/report_api';
import ReportModel from '../models/ReportModel';
import { getVolunteeringPost, getVolunteerPost, removeVolunteeringPost, removeVolunteerPost } from '../api/post_api';
import { VolunteeringPostModel } from '../models/VolunteeringPostModel';
import './../css/ReportList.css'
import { VolunteerPostModel } from '../models/VolunteerPostModel';
import { PostModel } from '../models/PostModel';
import { getVolunteering, removeVolunteering } from '../api/volunteering_api';
import VolunteeringModel from '../models/VolunteeringModel';
import UserModel from '../models/UserModel';
import { banUser, getAllUserEmails, getIsAdmin, getUserByUsername } from '../api/user_api';
import Select from 'react-select';

function ReportList() {
    const navigate = useNavigate();
    const [searchBannedEmail, setSearchBannedEmail] = useState("");
    const [bannedEmails, setBannedEmails] = useState<string[]>([]);
    const [allBannedEmails, setAllBannedEmails] = useState<string[]>([]);
    const [allEmails, setAllEmails] = useState<{ label: string, value: string }[]>([]);
    const [selectedEmail, setSelectedEmail] = useState<string>("");
    const [volunteeringPostReports, setVolunteeringPostReports] = useState<ReportModel[]>([]);
    const [volunteerPostReports, setVolunteerPostReports] = useState<ReportModel[]>([]);
    const [userReports, setUserReports] = useState<ReportModel[]>([]);
    const [orgReports, setOrgReports] = useState<ReportModel[]>([]);
    const [volunteeringReports, setVolunteeringReports] = useState<ReportModel[]>([]);
    const [postTitles, setPostTitles] = useState<{ [key: string]: string }>({});
    const [volunteeringNames, setVolunteeringNames] = useState<{ [key: string]: string }>({});
    const [orgNames, setOrgNames] = useState<{ [key: string]: string }>({});
    const [userNames, setUserNames] = useState<{ [key: string]: string }>({});

    const fetchReports = async () => {
        try {
            const isAdmin = await getIsAdmin(localStorage.getItem("username") ?? "");
            if(!isAdmin) {
                navigate("/pageNotFound");
            }
            else {
            const bannedEmails = await getBannedEmails();
            setBannedEmails(bannedEmails);
            setAllBannedEmails(bannedEmails);

            const allEmails = (await getAllUserEmails()).filter(email => !bannedEmails.includes(email));
            setAllEmails(allEmails.map(email => ({ label: email, value: email })));

            const volunteeringPostsReports = await getAllVolunteeringPostReports();
            setVolunteeringPostReports(volunteeringPostsReports);

            const volunteerPostReports = await getAllVolunteerPostReports();
            setVolunteerPostReports(volunteerPostReports);

            const volunteeringReports = await getAllVolunteeringReports();
            setVolunteeringReports(volunteeringReports);

            const userReports = await getAllUserReports();
            setUserReports(userReports);

            const organizationReports = await getAllOrganizationReports();
            setOrgReports(organizationReports);

            for (let report of volunteeringPostsReports) {
                try {
                    const post: PostModel = await getVolunteeringPost(Number(report.reportedId));
                    const title: string = post.title;
                                
                    setPostTitles((prevTitles) => ({
                        ...prevTitles,
                        [report.reportedId]: title,
                    }));
                } catch (e) {
                    alert(e);
                }
            }

            for (let report of volunteerPostReports) {
                try {
                    const post: PostModel = await getVolunteerPost(Number(report.reportedId));
                    const title: string = post.title;
                                
                    setPostTitles((prevTitles) => ({
                        ...prevTitles,
                        [report.reportedId]: title,
                    }));
                } catch (e) {
                    alert(e);
                }
            }

            for (let report of volunteeringReports) {
                try {
                    const volunteering: VolunteeringModel = await getVolunteering(Number(report.reportedId));
                    const name: string = volunteering.name;
                                
                    setVolunteeringNames((prev) => ({
                        ...prev,
                        [report.reportedId]: name,
                    }));
                } catch (e) {
                    alert(e);
                }
            }

            for (let report of orgReports) {
                try {
                    const organization: OrganizationModel = await getOrganization(Number(report.reportedId));
                    const name: string = organization.name;
                                
                    setOrgNames((prev) => ({
                        ...prev,
                        [report.reportedId]: name,
                    }));
                } catch (e) {
                    alert(e);
                }
            }

            for (let report of userReports) {
                try {
                    const user: UserModel = await getUserByUsername(report.reportedId);
                    const name: string = user.name;
                                
                    setUserNames((prev) => ({
                        ...prev,
                        [report.reportedId]: name,
                    }));
                } catch (e) {
                    alert(e);
                }
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

    const handleShowOnClick = (object: string, id: string) => {
        navigate(`/${object}/${id}`);
    };

    const handleUnbanEmailOnClick = async (email: string) => {
        if(window.confirm("Are you sure you want to unban this email?")) {
            try {
                await unbanEmail(email);
                alert("Email unbanned successfully!");
                let newEmail = bannedEmails.filter((bannedEmail) => email !== bannedEmail);
                setBannedEmails(newEmail);
                setAllBannedEmails(newEmail);
                let newAllEmails = allEmails.concat({label: email, value: email});
                setAllEmails(newAllEmails);
            }
            catch(e) {
                alert(e);
            }
        }
    };

    const handleDeleteVolunteeringPostOnClick = async (reportingUser : string, date: string, reportedId : string) => {
        if(window.confirm("Are you sure you want to remove this report?")) {
            try {
                await removeVolunteeringPostReport(reportingUser, date, Number(reportedId));
                alert("Report removed successfully!");
                let newReports = volunteeringPostReports.filter((report) => report.reportingUser !== reportingUser || report.date !== date || report.reportedId !== reportedId.toString());
                setVolunteeringPostReports(newReports);
            }
            catch(e) {
                alert(e);
            }
        }
    };

    const handleDeleteVolunteerPostOnClick = async (reportingUser : string, date: string, reportedId : string) => {
        if(window.confirm("Are you sure you want to remove this report?")) {
            try {
                await removeVolunteerPostReport(reportingUser, date, Number(reportedId));
                alert("Report removed successfully!");
                let newReports = volunteerPostReports.filter((report) => report.reportingUser !== reportingUser || report.date !== date || report.reportedId !== reportedId.toString());
                setVolunteerPostReports(newReports);
            }
            catch(e) {
                alert(e);
            }
        }
    };

    const handleDeleteVolunteeringOnClick = async (reportingUser : string, date: string, reportedId : string) => {
        if(window.confirm("Are you sure you want to remove this report?")) {
            try {
                await removeVolunteeringReport(reportingUser, date, Number(reportedId));
                alert("Report removed successfully!");
                let newReports = volunteeringReports.filter((report) => report.reportingUser !== reportingUser || report.date !== date || report.reportedId !== reportedId.toString());
                setVolunteeringReports(newReports);
            }
            catch(e) {
                alert(e);
            }
        }
    };

    const handleDeleteOrganizationOnClick = async (reportingUser : string, date: string, reportedId : string) => {
        if(window.confirm("Are you sure you want to remove this report?")) {
            try {
                await removeOrganizationReport(reportingUser, date, Number(reportedId));
                alert("Report removed successfully!");
                let newReports = orgReports.filter((report) => report.reportingUser !== reportingUser || report.date !== date || report.reportedId !== reportedId.toString());
                setOrgReports(newReports);
            }
            catch(e) {
                alert(e);
            }
        }
    };

    const handleRemoveUserOnClick = async (reportingUser : string, date: string, reportedId : string) => {
        if(window.confirm("Are you sure you want to remove this report?")) {
            try {
                await removeUserReport(reportingUser, date, reportedId);
                alert("Report removed successfully!");
                let newReports = userReports.filter((report) => report.reportingUser !== reportingUser || report.date !== date || report.reportedId !== reportedId.toString());
                setUserReports(newReports);
            }
            catch(e) {
                alert(e);
            }
        }
    };

    const handleRemoveVolunteeringPostOnClick = async (reportedId : string) => {
        if(window.confirm("Are you sure you want to remove this volunteering post?")) {
            try {
                await removeVolunteeringPost(Number(reportedId));
                alert("Volunteering post removed successfully!");
                let newReports = volunteeringPostReports.filter((report) => report.reportedId !== reportedId.toString());
                setVolunteeringPostReports(newReports);
            }
            catch(e) {
                alert(e);
            }
        }
    }

    const handleRemoveVolunteerPostOnClick = async (reportedId : string) => {
        if(window.confirm("Are you sure you want to remove this volunteer post?")) {
            try {
                await removeVolunteerPost(Number(reportedId));
                alert("Volunteer post removed successfully!");
                let newReports = volunteerPostReports.filter((report) => report.reportedId !== reportedId.toString());
                setVolunteerPostReports(newReports);
            }
            catch(e) {
                alert(e);
            }
        }
    }

    const handleRemoveVolunteeringOnClick = async (reportedId : string) => {
        if(window.confirm("Are you sure you want to remove this volunteering?")) {
            try {
                await removeVolunteering(Number(reportedId));
                alert("Volunteering removed successfully!");
                let newReports = volunteeringReports.filter((report) => report.reportedId !== reportedId.toString());
                setVolunteeringReports(newReports);
            }
            catch(e) {
                alert(e);
            }
        }
    }

    const handleRemoveOrganizationOnClick = async (reportedId : string) => {
        if(window.confirm("Are you sure you want to remove this organization?")) {
            try {
                await removeOrganization(Number(reportedId));
                alert("Organization removed successfully!");
                let newReports = orgReports.filter((report) => report.reportedId !== reportedId.toString());
                setOrgReports(newReports);
            }
            catch(e) {
                alert(e);
            }
        }
    }

    const handleBanUserOnClick = async (reportedId : string) => {
        if(window.confirm("Are you sure you want to ban this user?")) {
            try {
                console.log(reportedId);
                await banUser(reportedId);
                alert("User banned successfully!");
                let newReports = userReports.filter((report) => report.reportedId !== reportedId.toString());
                setUserReports(newReports);
            }
            catch(e) {
                alert(e);
            }
        }
    }

    const fixDate = (dateJson: string) : string => {
        let date: Date = new Date(dateJson);
        let dateStr = `${date.getDate()}/${date.getMonth() + 1}/${date.getFullYear()}`;
        return dateStr;
    }

    const handleSelectedEmailChange = async (selectedValue: string) => {
        setSelectedEmail(selectedValue);
    };

    const handleBanEmailOnClick = async () => {
        if(window.confirm("Are you sure you want to ban this email?")) {
            try {
                await banEmail(selectedEmail);
                alert("Email banned successfully!");
                let newBanned = bannedEmails.concat(selectedEmail);
                setBannedEmails(newBanned);
                setAllBannedEmails(newBanned);
                let newAllEmails = allEmails.filter(email => email.value !== selectedEmail);
                setAllEmails(newAllEmails);
                setSelectedEmail("");
            }
            catch(e) {
                alert(e);
            }
        }
    };

    
    const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const newSearchValue = event.target.value; // Get the new value directly from the event
        setSearchBannedEmail(newSearchValue);
        let searchBannedEmails = allBannedEmails.filter((email) => email.toLocaleLowerCase().includes(newSearchValue.toLowerCase()));
        setBannedEmails(searchBannedEmails);
    };
    
    return (
        <div>
            <h2 className='bigHeader adminDashboardHeader'>Admin Dashboard</h2>
            <div className="Reports">
                <h2 className='reportsHeader'>Banned Emails</h2>
                <input id = "searchTextbox"
                    type="text"
                    value={searchBannedEmail}
                    onChange={handleSearchChange}
                    placeholder = "Search Banned Email..."
                />
                {bannedEmails.length > 0 ? (
                    bannedEmails.map((email, index) => (
                        <div key={index} className="reportItem">
                            <div id = "info"> 
                                <p className='reportDesc'>{email}</p>
                            </div>

                            <div id = "buttons">
                                <button className='orangeCircularButton' onClick={() => handleUnbanEmailOnClick(email)}>Unban Email</button>
                            </div>
                        </div>
                    ))
                ) : (
                    <p id = "noReports" className='smallHeader'>No banned emails available.</p>
                )}
                
                <div className='banNewEmail'>
                    <h2 className='reportsHeader'>Ban A new Email</h2>
                    <Select className = 'selectEmail' options={allEmails} onChange={(selected) => handleSelectedEmailChange(selected?.value ?? "")}></Select>
                    <button className='orangeCircularButton' onClick={handleBanEmailOnClick}>Ban Email</button>
                </div>
            </div>

            <div className="Reports">
                <h2 className='reportsHeader'>Volunteering Post Reports</h2>
                {volunteeringPostReports.length > 0 ? (
                    volunteeringPostReports.map((report, index) => (
                        <div key={index} className="reportItem">
                            <div id = "info"> 
                                <h3 className='smallHeader' onClick={() => handleShowOnClick("volunteeringPost", report.reportedId)}>Report on: {postTitles[report.reportedId]}</h3>
                                <p className='reportDesc'>Reported by {report.reportingUser} on {fixDate(report.date)}</p>
                                <p className='reportDesc'>Issue: {report.description}</p>
                            </div>

                            <div id = "buttons">
                                <button className='orangeCircularButton' onClick={() => handleDeleteVolunteeringPostOnClick(report.reportingUser, report.date, report.reportedId)}>Ignore Report</button>
                                <button className='orangeCircularButton' onClick={() => handleRemoveVolunteeringPostOnClick(report.reportedId)}>Delete Post</button>
                            </div>
                        </div>
                    ))
                ) : (
                    <p id = "noReports" className='smallHeader'>No reports available.</p>
                )}
            </div>

            <div className="Reports">
                <h2 className='reportsHeader'>Volunteer Post Reports</h2>
                {volunteerPostReports.length > 0 ? (
                    volunteerPostReports.map((report, index) => (
                        <div key={index} className="reportItem">
                            <div id = "info"> 
                                <h3 className='smallHeader' onClick={() => handleShowOnClick("volunteerPost", report.reportedId)}>Report on: {postTitles[report.reportedId]}</h3>
                                <p className='reportDesc'>Reported by {report.reportingUser} on {fixDate(report.date)}</p>
                                <p className='reportDesc'>Issue: {report.description}</p>
                            </div>

                            <div id = "buttons">
                                <button className='orangeCircularButton' onClick={() => handleDeleteVolunteerPostOnClick(report.reportingUser, report.date, report.reportedId)}>Ignore Report</button>
                                <button className='orangeCircularButton' onClick={() => handleRemoveVolunteerPostOnClick(report.reportedId)}>Delete Post</button>
                            </div>
                        </div>
                    ))
                ) : (
                    <p id = "noReports" className='smallHeader'>No reports available.</p>
                )}
            </div>

            <div className="Reports">
                <h2 className='reportsHeader'>Volunteering Reports</h2>
                {volunteeringReports.length > 0 ? (
                    volunteeringReports.map((report, index) => (
                        <div key={index} className="reportItem">
                            <div id = "info"> 
                                <h3 className='smallHeader' onClick={() => handleShowOnClick("volunteering", report.reportedId)}>Report on: {volunteeringNames[report.reportedId]}</h3>
                                <p className='reportDesc'>Reported by {report.reportingUser} on {fixDate(report.date)}</p>
                                <p className='reportDesc'>Issue: {report.description}</p>
                            </div>

                            <div id = "buttons">
                                <button className='orangeCircularButton' onClick={() => handleDeleteVolunteeringOnClick(report.reportingUser, report.date, report.reportedId)}>Ignore Report</button>
                                <button className='orangeCircularButton' onClick={() => handleRemoveVolunteeringOnClick(report.reportedId)}>Delete Volunteering</button>
                            </div>
                        </div>
                    ))
                ) : (
                    <p id = "noReports" className='smallHeader'>No reports available.</p>
                )}
            </div>

            <div className="Reports">
                <h2 className='reportsHeader'>Organization Reports</h2>
                {orgReports.length > 0 ? (
                    orgReports.map((report, index) => (
                        <div key={index} className="reportItem">
                            <div id = "info"> 
                                <h3 className='smallHeader' onClick={() => handleShowOnClick("organization", report.reportedId)}>Report on: {orgNames[report.reportedId]}</h3>
                                <p>Reported by {report.reportingUser} on {fixDate(report.date)}</p>
                                <p>Issue: {report.description}</p>
                            </div>

                            <div id = "buttons">
                                <button className='orangeCircularButton' onClick={() => handleDeleteOrganizationOnClick(report.reportingUser, report.date, report.reportedId)}>Ignore Report</button>
                                <button className='orangeCircularButton' onClick={() => handleRemoveOrganizationOnClick(report.reportedId)}>Delete Organization</button>
                            </div>
                        </div>
                    ))
                ) : (
                    <p id = "noReports" className='smallHeader'>No reports available.</p>
                )}
            </div>

            <div className="Reports">
                <h2 className='reportsHeader'>User Reports</h2>
                {userReports.length > 0 ? (
                    userReports.map((report, index) => (
                        <div key={index} className="reportItem">
                            <div id = "info"> 
                                <h3 className='smallHeader' onClick={() => handleShowOnClick("profile", report.reportedId)}>Report on: {userNames[report.reportedId]}</h3>
                                <p>Reported by {report.reportingUser} on {fixDate(report.date)}</p>
                                <p>Issue: {report.description}</p>
                            </div>

                            <div id = "buttons">
                                <button className='orangeCircularButton' onClick={() => handleRemoveUserOnClick(report.reportingUser, report.date, report.reportedId)}>Ignore Report</button>
                                <button className='orangeCircularButton' onClick={() => handleBanUserOnClick(report.reportedId)}>Ban User</button>
                            </div>
                        </div>
                    ))
                ) : (
                    <p id = "noReports" className='smallHeader'>No reports available.</p>
                )}
            </div>
            
        </div>
    )
}

export default ReportList