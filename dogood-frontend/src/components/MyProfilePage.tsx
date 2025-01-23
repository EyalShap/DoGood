import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { getUserByToken, updateUserFields, updateUserSkills, updateUserPreferences,getIsAdmin } from "../api/user_api";
import './../css/MyProfile.css';

function MyProfilePage() {
    const navigate = useNavigate();
    // States for user fields
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [phone, setPhone] = useState("");
    const [birthDate, setBirthDate] = useState("");
    const [skillsInput, setSkillsInput] = useState(""); // Separate input for skills
    const [skills, setSkills] = useState(""); // Displayed skills
    const [preferencesInput, setPreferencesInput] = useState(""); // Separate input for preferences
    const [preferences, setPreferences] = useState(""); // Displayed preferences
    const [isAdmin, setIsAdmin] = useState(false);

    // Fetch user profile on load
    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const profile = await getUserByToken();
                console.log(profile);
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
            } catch (e) {
                alert("Failed to load profile: " + e);
            }
        };
        fetchProfile();
    }, []);

    // Handlers to update profile
    const handleProfileUpdate = async () => {
        try {
            var pass = null;
            if(password.length > 0){ pass = password;}
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
            setSkills(updatedSkills.join(", ")); // Update displayed skills after confirmation
            alert("Skills updated successfully!");
        } catch (e) {
            alert("Failed to update skills: " + e);
        }
    };

    const handlePreferencesUpdate = async () => {
        try {
            const updatedPreferences = preferencesInput.split(",").map(preference => preference.trim());
            await updateUserPreferences(username, updatedPreferences);
            setPreferences(updatedPreferences.join(", ")); // Update displayed preferences after confirmation
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
                <input
                    type="text"
                    value={username}
                    onChange={e => setUsername(e.target.value)}
                    disabled
                />
                <label>Password:</label>
                <input
                    type="password"
                    placeholder="Enter new password"
                    value={password}
                    onChange={e => setPassword(e.target.value)}
                />
                <label>Name:</label>
                <input
                    type="text"
                    value={name}
                    onChange={e => setName(e.target.value)}
                />
                <label>Email:</label>
                <input
                    type="email"
                    value={email}
                    onChange={e => setEmail(e.target.value)}
                />
                <label>Phone:</label>
                <input
                    type="text"
                    value={phone}
                    onChange={e => setPhone(e.target.value)}
                />
                <label>Birth Date:</label>
                <input
                    type="date"
                    value={birthDate}
                    disabled // Make birth date field non-editable
                />
                <button onClick={handleProfileUpdate}>Update Profile</button>
            </div>

            <div className="skills-section">
                <h2>Update Skills</h2>
                <textarea
                    value={skillsInput}
                    onChange={e => setSkillsInput(e.target.value)}
                    placeholder="Enter skills separated by commas"
                />
                <button onClick={handleSkillsUpdate}>Update Skills</button>
            </div>

            <div className="preferences-section">
                <h2>Update Preferences</h2>
                <textarea
                    value={preferencesInput}
                    onChange={e => setPreferencesInput(e.target.value)}
                    placeholder="Enter preferences separated by commas"
                />
                <button onClick={handlePreferencesUpdate}>Update Preferences</button>
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
                {isAdmin && (
                    <button onClick={() => navigate('/reportList')}>Go to Reports</button>
                )}
            </div>
        </div>
    );
}

export default MyProfilePage;
