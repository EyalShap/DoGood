import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { getUserByToken, updateUserFields, updateUserSkills } from "../api/user_api";
import './../css/MyProfile.css'

function MyProfilePage() {
    const navigate = useNavigate();

    // States for user fields
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [phone, setPhone] = useState("");
    const [birthDate, setBirthDate] = useState("");
    const [skills, setSkills] = useState("");
    const [isAdmin, setIsAdmin] = useState(false);

    // Fetch user profile on load
    useEffect(() => {
        const fetchProfile = async () => {
            try {
                var profile = await getUserByToken();
                console.log(profile);
                setUsername(profile.username);
                setName(profile.name);
                setEmail(profile.emails[0]);
                setPhone(profile.phone);
                setBirthDate(new Date(profile.birthDate).toLocaleDateString());
                setIsAdmin(profile.isAdmin);
                const userSkills = profile.skills;
                setSkills(userSkills.join(", "));
            } catch (e) {
                alert("Failed to load profile: " + e);
            }
        };
        fetchProfile();
    }, []);

    // Handlers to update profile
    const handleProfileUpdate = async () => {
        try {
            let emails = [email];
            await updateUserFields(username,password,emails,name,birthDate);
            alert("Profile updated successfully!");
        } catch (e) {
            alert("Failed to update profile: " + e);
        }
    };

    const handleSkillsUpdate = async () => {
        try {
            const updatedSkills = skills.split(",").map(skill => skill.trim());
            await updateUserSkills(updatedSkills);
            alert("Skills updated successfully!");
        } catch (e) {
            alert("Failed to update skills: " + e);
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
                    onChange={e => setBirthDate(e.target.value)}
                />
                <button onClick={handleProfileUpdate}>Update Profile</button>
            </div>

            <div className="skills-section">
                <h2>Update Skills</h2>
                <textarea
                    value={skills}
                    onChange={e => setSkills(e.target.value)}
                    placeholder="Enter skills separated by commas"
                />
                <button onClick={handleSkillsUpdate}>Update Skills</button>
            </div>

            <div className="status-section">
                <h2>Profile Status</h2>
                <p><strong>Username:</strong> {username}</p>
                <p><strong>Name:</strong> {name}</p>
                <p><strong>Email:</strong> {email}</p>
                <p><strong>Phone:</strong> {phone}</p>
                <p><strong>Birth Date:</strong> {birthDate}</p>
                <p><strong>Skills:</strong> {skills}</p>
                <p><strong>Admin:</strong> {isAdmin ? "Yes" : "No"}</p>
                {isAdmin && (
                    <button onClick={() => navigate('/reportList')}>Go to Reports</button>
                )}
            </div>
        </div>
    );
}

export default MyProfilePage;
