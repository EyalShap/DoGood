import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { getIsAdmin, getUserByUsername } from "../api/user_api";
import './../css/MyProfile.css';

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

    return (
        <div className="my-profile">
            <h1>Profile Page of {username}</h1>

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
        </div>
    );
}

export default ProfilePage;
