import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import {
    getUserByToken,
    updateUserFields,
    updateUserSkills,
    updateUserPreferences,
    getIsAdmin,
    getUserApprovedHours,
} from "../api/user_api";
import './../css/MyProfile.css';
import User, { VolunteeringInHistory } from "../models/UserModel";
import ApprovedHours from "../models/ApprovedHoursModel";
import { getUserApprovedHoursFormatted } from "../api/volunteering_api";

function MyProfilePage() {
    const navigate = useNavigate();

    // States for user fields
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [phone, setPhone] = useState("");
    const [birthDate, setBirthDate] = useState("");
    const [skillsInput, setSkillsInput] = useState("");
    const [skills, setSkills] = useState("");
    const [preferencesInput, setPreferencesInput] = useState("");
    const [preferences, setPreferences] = useState("");
    const [isAdmin, setIsAdmin] = useState(false);
    const [model, setModel] = useState<User | null>(null);

    // Volunteering History
    const [volunteeringsInHistory, setVolunteeringsInHistory] = useState<VolunteeringInHistory[]>([]);
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
                setUsername(profile.username);
                setName(profile.name);
                setEmail(profile.emails[0]);
                setPhone(profile.phone);
                setBirthDate(new Date(profile.birthDate).toISOString().split('T')[0]);
                setIsAdmin(await getIsAdmin(profile.username));
                setSkills(profile.skills.join(", "));
                setSkillsInput(profile.skills.join(", "));
                setPreferences(profile.preferredCategories.join(", "));
                setPreferencesInput(profile.preferredCategories.join(", "));
                setVolunteeringsInHistory(profile.volunteeringsInHistory);
                setApprovedHours(await getUserApprovedHours(profile.username));
                setModel(profile);
                console.log(profile);
            } catch (e) {
                alert("Failed to load profile: " + e);
            }
        };
        fetchProfile();
    }, []);

    // Handlers to update profile
    const handleProfileUpdate = async () => {
        try {
            const pass = password.length > 0 ? password : null;
            await updateUserFields(username, pass, [email], name, phone);
            alert("Profile updated successfully!");
        } catch (e) {
            alert("Failed to update profile: " + e);
        }
    };

    const handleSkillsUpdate = async () => {
        try {
            const updatedSkills = skillsInput.split(",").map(skill => skill.trim());
            await updateUserSkills(username, updatedSkills);
            setSkills(updatedSkills.join(", "));
            alert("Skills updated successfully!");
        } catch (e) {
            alert("Failed to update skills: " + e);
        }
    };

    const handlePreferencesUpdate = async () => {
        try {
            const updatedPreferences = preferencesInput.split(",").map(preference => preference.trim());
            await updateUserPreferences(username, updatedPreferences);
            setPreferences(updatedPreferences.join(", "));
            alert("Preferences updated successfully!");
        } catch (e) {
            alert("Failed to update preferences: " + e);
        }
    };

    return (
        <div className="my-profile">
            <h1>My Profile</h1>

            <div className="profile-section">
                <h2>Update Profile</h2>
                <label>Username:</label>
                <input type="text" value={username} disabled />
                <label>Password:</label>
                <input
                    type="password"
                    placeholder="Enter new password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />
                <label>Name:</label>
                <input
                    type="text"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                />
                <label>Email:</label>
                <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                />
                <label>Phone:</label>
                <input
                    type="text"
                    value={phone}
                    onChange={(e) => setPhone(e.target.value)}
                />
                <label>Birth Date:</label>
                <input type="date" value={birthDate} disabled />
                <button onClick={handleProfileUpdate}>Update Profile</button>
            </div>

            <div className="list-section">
                <h2>Update Skills</h2>
                <textarea
                    value={skillsInput}
                    onChange={(e) => setSkillsInput(e.target.value)}
                    placeholder="Enter skills separated by commas"
                />
                <button onClick={handleSkillsUpdate}>Update Skills</button>
            </div>

            <div className="list-section">
                <h2>Update Preferences</h2>
                <textarea
                    value={preferencesInput}
                    onChange={(e) => setPreferencesInput(e.target.value)}
                    placeholder="Enter preferences separated by commas"
                />
                <button onClick={handlePreferencesUpdate}>Update Preferences</button>
            </div>

            <div className="history-section">
                <h2>Volunteering History</h2>
                {volunteeringsInHistory.length > 0 ? (
                    <table className="history-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Organization ID</th>
                                <th>Name</th>
                                <th>Description</th>
                                <th>Skills</th>
                                <th>Categories</th>
                            </tr>
                        </thead>
                        <tbody>
                            {volunteeringsInHistory.map((history, index) => (
                                <tr key={index}>
                                    <td>{history.id}</td>
                                    <td>{history.orgId}</td>
                                    <td>{history.name}</td>
                                    <td>{history.description}</td>
                                    <td>{history.skills.join(", ")}</td>
                                    <td>{history.categories.join(", ")}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                ) : (
                    <p>No volunteering history available.</p>
                )}
            </div>
            <div className="history-section">
                <h2>Approved hours</h2>
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

            <div className="status-section">
                <h2>Profile Status</h2>
                <p><strong>Username:</strong> {username}</p>
                <p><strong>Name:</strong> {name}</p>
                <p><strong>Email:</strong> {email}</p>
                <p><strong>Phone:</strong> {phone}</p>
                <p><strong>Birth Date:</strong> {birthDate}</p>
                <p><strong>Skills:</strong> {skills}</p>
                <p><strong>Preferences:</strong> {preferences}</p>
                <p><strong>Admin:</strong> {isAdmin ? "Yes" : "No"}</p>
                <p><strong>Student:</strong> {model?.student ? "Yes" : "No"}</p>
                {isAdmin && (
                    <button onClick={() => navigate('/reportList')}>Go to Reports</button>
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
                    {model.volunteeringsInHistory.map(hist => <option value={hist.id}>{hist.id} ({hist.name})</option>)}
                </select>
                <button disabled={selectedVolunteering < 0} onClick={async () => getUserApprovedHoursFormatted(selectedVolunteering,id)}>Export</button>
            </div>}
        </div>
    );
}

export default MyProfilePage;
