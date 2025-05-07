import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import {
    getUserByToken,
    getUserApprovedHours,
} from "../api/user_api";
import './../css/MyProfile.css';
import User from "../models/UserModel";
import ApprovedHours from "../models/ApprovedHoursModel";
import {getUserApprovedHoursFormatted} from "../api/volunteering_api";

function MyProfilePage() {
    const navigate = useNavigate();

    const [model, setModel] = useState<User | null>(null);
    const [approvedHours, setApprovedHours] = useState<ApprovedHours[]>([]);


    // Export PDF
    const [id, setId] = useState("");
    const [selectedVolunteering, setSelectedVolunteering] = useState(-1);


    // Volunteering Now

    // Fetch user profile on load
    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const profile = await getUserByToken();
                setApprovedHours(await getUserApprovedHours(profile.username));
                setModel(profile);
                console.log(profile);
            } catch (e) {
                alert("Failed to load profile: " + e);
            }
        };
        fetchProfile();
    }, []);


    const handleExport = async () => {
        try{
            await getUserApprovedHoursFormatted(selectedVolunteering,id)
        } catch(e){
            alert("Failed to export approved hours: " + e);
        }
    }


    return (
        <div className="my-profile">
            <h1 className="bigHeader">My Approved Hours</h1>
            <div className="history-section">
                <h2 className="profileSectionHeader">Approved hours</h2>
                {approvedHours.length > 0 ? (
                    <table className="history-table">
                        <thead>
                        <tr>
                            <th>Volunteering id</th>
                            <th>Date</th>
                            <th>Start time</th>
                            <th>End time</th>
                        </tr>
                        </thead>
                        <tbody>
                        {approvedHours.map((hours, index) => (
                            <tr key={index}>
                                <td>{hours.volunteeringId}</td>
                                <td>{(new Date(hours.startTime)).toLocaleDateString()}</td>
                                <td>{(new Date(hours.startTime)).toLocaleTimeString()}</td>
                                <td>{(new Date(hours.endTime)).toLocaleTimeString()}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                ) : (
                    <p>No Approved Hours available.</p>
                )}
            </div>

            {model !== null && model.student &&
                <div className="export-section">
                    <h2>Export approved hours as PDF</h2>
                    <label>ID (Teudat Zehut):</label>
                    <input
                        placeholder="Enter ID"
                        value={id}
                        onChange={(e) => setId(e.target.value)}
                    />
                    <label>Select primary volunteering to export hours from:</label>
                    <select defaultValue={-1} onChange={e => setSelectedVolunteering(parseInt(e.target.value))}>
                        <option value={-1}></option>
                        {model.volunteeringIds.map(id => <option value={id}>{id}</option>)}
                        {model.volunteeringsInHistory.map(hist => <option
                            value={hist.id}>{hist.id} ({hist.name})</option>)}
                    </select>
                    <button disabled={selectedVolunteering < 0} onClick={handleExport}
                            className="orangeCircularButton">Export
                    </button>
                </div>}
        </div>
    );
}

export default MyProfilePage;
