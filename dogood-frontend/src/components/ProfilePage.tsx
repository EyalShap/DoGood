import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { banUser, getIsAdmin, getUserByUsername } from "../api/user_api";
import './../css/MyProfile.css';
import { createUserReport } from "../api/report_api";
import defaultImage from '../assets/defaultProfilePic.jpg';


function ProfilePage() {
    const navigate = useNavigate();
    let { id } = useParams();

    // States for user fields
    const [username, setUsername] = useState("");
    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [phone, setPhone] = useState("");
    const [birthDate, setBirthDate] = useState("");
    const [skillsInput, setSkillsInput] = useState(""); // Separate input for skills
    const [skills, setSkills] = useState(""); // Displayed skills
    const [preferencesInput, setPreferencesInput] = useState(""); // Separate input for preferences
    const [preferences, setPreferences] = useState(""); // Displayed preferences
    const [isAdmin, setIsAdmin] = useState(false);
    const [isActorAdmin, setIsActorAdmin] = useState(false);
    const [showReportDescription, setShowReportDescription] = useState(false);
    const [reportDescription, setReportDescription] = useState("");
    const [profilePic, setProfilePic] = useState("");

    // Fetch user profile on load
    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const profile = await getUserByUsername(id!);
                console.log(profile);
                setUsername(profile.username);
                setName(profile.name);
                setEmail(profile.emails[0]);
                setPhone(profile.phone);
                setBirthDate(new Date(profile.birthDate).toISOString().split('T')[0]);
                setIsAdmin(await getIsAdmin(profile.username));
                setIsActorAdmin(await getIsAdmin(localStorage.getItem("username") ?? ""));
                setSkills(profile.skills.join(", "));
                setSkillsInput(profile.skills.join(", "));
                setPreferences(profile.preferredCategories.join(", "));
                setPreferencesInput(profile.preferredCategories.join(", "));
                setProfilePic(profile.profilePicUrl ? profile.profilePicUrl : defaultImage)
            } catch (e) {
                alert("Failed to load profile: " + e);
            }
        };
        fetchProfile();
    }, []);

    
    const handleReportOnClick = async () => {
        setShowReportDescription(true);
    }
    
    const handleSubmitReportOnClick = async () => {
        try {
            await createUserReport(username, reportDescription);
            alert("Thank you for your report!");
        }
        catch(e) {
            alert(e);
        }
            setShowReportDescription(false);
            setReportDescription("");
        }
            
    const handleCancelReportOnClick = () => {
        setShowReportDescription(false);
        setReportDescription("");
    }

    const handleBanUserOnClick = async () => {
        if(window.confirm("Are you sure you want to ban this user?")) {
            try {
                await banUser(username);
                alert("User banned successfully!");
            }
            catch(e) {
                alert(e);
            }
        }
    }

    return (
        <div className="my-profile">
            <h1 className="bigHeader">Profile Page of {username}</h1>

            <div className="profile-picture-container">
                    <img
                        src={profilePic}
                        className="profile-picture-preview"
                    />
                </div>

            <div className="profile-section">
                <h2>Profile Details</h2>
                <label>Username:</label>
                <input
                    type="text"
                    value={username}
                    disabled
                />
                <label>Name:</label>
                <input
                    type="text"
                    value={name}
                    disabled
                />
                <label>Email:</label>
                <input
                    type="email"
                    value={email}
                    disabled
                />
                <label>Phone:</label>
                <input
                    type="text"
                    value={phone}
                    disabled
                />
                <label>Birth Date:</label>
                <input
                    type="date"
                    value={birthDate}
                    disabled // Make birth date field non-editable
                />
            </div>

            <div className="list-section">
                <h2>{username} Skills</h2>
                <textarea
                    value={skillsInput}
                    disabled
                />
            </div>

            <div className="list-section">
                <h2>{username} Preferences</h2>
                <textarea
                    value={preferencesInput}
                    disabled
                />
            </div>

            <div className="reportSection">
                {isActorAdmin && <button onClick={handleBanUserOnClick} className="orangeCircularButton">Ban</button>}
                <button onClick={handleReportOnClick} className="orangeCircularButton">Report</button>
                {showReportDescription && (
                    <div className="popup-window">
                        <div className="popup-header">
                            <span className="popup-title">Report</span>
                            <button className="cancelButton" onClick={handleCancelReportOnClick}>
                                X
                            </button>
                            </div>
                            <div className="popup-body">
                            <textarea placeholder="What went wrong?..." onClick={(e) => { e.stopPropagation()}} onChange={(e) => setReportDescription(e.target.value)}></textarea>
                            <button className="orangeCircularButton" onClick={handleSubmitReportOnClick}>
                                Submit
                            </button>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}

export default ProfilePage;
