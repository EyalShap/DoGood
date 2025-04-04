import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import {
    getUserByToken,
    updateUserFields,
    updateUserSkills,
    updateUserPreferences,
    // getIsAdmin is likely redundant if admin status comes with getUserByToken
    getUserApprovedHours,
    setLeaderboard,
    leaderboard,
    uploadCV,
    downloadCV,
    removeCV,
    generateSkillsAndPreferences,
    updateProfilePicture, // <-- Import the API function for updating the profile picture URL
} from "../api/user_api";
import './../css/MyProfile.css'; // Make sure this CSS file exists and styles appropriately
import User, { VolunteeringInHistory } from "../models/UserModel";
import ApprovedHours from "../models/ApprovedHoursModel";
import { getAppointmentsCsv, getUserApprovedHoursFormatted } from "../api/volunteering_api";
import { Switch } from "@mui/material";
import { supabase } from "../api/general"; // <-- Import supabase client

// Default profile picture if user has none or URL fails
import defaultProfilePic from './../assets/defaultProfilePic.jpg'; // <-- Make sure this path is correct

function MyProfilePage() {
    const navigate = useNavigate();

    // --- State Variables ---
    // User Details
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState(""); // For entering a new password
    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [phone, setPhone] = useState("");
    const [birthDate, setBirthDate] = useState("");
    const [skillsInput, setSkillsInput] = useState(""); // For the textarea input
    const [skills, setSkills] = useState(""); // For displaying current skills
    const [preferencesInput, setPreferencesInput] = useState(""); // For the textarea input
    const [preferences, setPreferences] = useState(""); // For displaying current preferences
    const [isAdmin, setIsAdmin] = useState(false);
    const [isLeaderboard, setIsLeaderboard] = useState(true);
    const [selectedCV, setSelectedCV] = useState<File | null>(null);
    const [cv, setCV] = useState<File | null>(null);
    const [key, setKey] = useState(0)
    const [model, setModel] = useState<User | null>(null);


    // Volunteering Data
    const [volunteeringsInHistory, setVolunteeringsInHistory] = useState<VolunteeringInHistory[]>([]);
    const [approvedHours, setApprovedHours] = useState<ApprovedHours[]>([]);

    // Export Feature States
    const [id, setId] = useState(""); // For PDF export (Teudat Zehut)
    const [selectedVolunteering, setSelectedVolunteering] = useState<number>(-1); // For PDF export
    const [numWeeks, setNumWeeks] = useState<string>(""); // For CSV export

    // Profile Picture States
    const [profilePic, setProfilePic] = useState<string | null>(null); // Holds URL for display (fetched or preview)
    const [selectedFile, setSelectedFile] = useState<File | null>(null); // Holds the file selected by the user
    const [isUploading, setIsUploading] = useState<boolean>(false); // Loading indicator for picture upload

    // --- Effects ---
    // Fetch user profile data when the component mounts
    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const profile = await getUserByToken(); // Fetch user data, including profilePicUrl
                setModel(profile); // Store the full user object
                setUsername(profile.username);
                setName(profile.name);
                setEmail(profile.emails[0] || ""); // Handle case where emails might be empty
                setPhone(profile.phone);
                setBirthDate(new Date(profile.birthDate).toISOString().split('T')[0]); // Format date for input type="date"
                setIsAdmin(profile.admin); // Set admin status directly from profile data
                setSkills(profile.skills.join(", "));
                setSkillsInput(profile.skills.join(", "));
                setPreferences(profile.preferredCategories.join(", "));
                setPreferencesInput(profile.preferredCategories.join(", "));
                setVolunteeringsInHistory(profile.volunteeringsInHistory);
                setApprovedHours(await getUserApprovedHours(profile.username));
                setIsLeaderboard(profile.leaderboard);

                try {
                    const cvBlob : Blob = await downloadCV();
                    const cvFile = new File([cvBlob], "cv", { type: cvBlob.type });
                    setCV(cvFile);
                }
                catch(e) {
                    setCV(null);
                }
                
                setModel(profile);
                console.log(profile);
                // Initialize the profilePic state with the fetched URL or null if none
                setProfilePic(profile.profilePicUrl || null);

                console.log("Fetched profile:", profile);
            } catch (e) {
                console.error("Failed to load profile:", e);
                alert("Failed to load profile: " + e);
                // Optionally navigate away or show an error message component
            }
        };
        fetchProfile();
    }, []); // Empty dependency array ensures this runs only once on mount

    // --- Event Handlers ---

    // Handles selecting a new profile picture file and generates a preview
    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            const file = e.target.files[0];
            setSelectedFile(file);

            // Generate a preview URL
            const reader = new FileReader();
            reader.onloadend = () => {
                setProfilePic(reader.result as string); // Update display state with preview
            };
            reader.readAsDataURL(file);
        }
    };

    // Handles uploading the selected picture to Supabase and updating the backend
    const handleProfilePictureUpdate = async () => {
        if (!selectedFile || !model) {
            alert("Please select a file first.");
            return;
        }
        setIsUploading(true); // Show loading state
        try {
            // 1. Define file path in Supabase (stable name using username)
            const fileExt = selectedFile.name.split('.').pop();
            const fileName = `${model.username}.${fileExt}`; // Ensures overwriting the same user's picture
            const filePath = fileName;

            // 2. Upload to Supabase Storage with upsert option
            const { error: uploadError } = await supabase.storage
                .from("profile-pictures") // Ensure this bucket name matches your Supabase setup
                .upload(filePath, selectedFile, {
                    cacheControl: '3600', // Cache control for optimization
                    upsert: true          // Overwrite if file exists for this user
                });

            if (uploadError) {
                throw new Error("Supabase upload error: " + uploadError.message);
            }

            // 3. Get the public URL (add timestamp to URL query to potentially bypass cache)
             const timestamp = `t=${new Date().getTime()}`;
             const { data: urlData } = supabase.storage
                 .from("profile-pictures")
                 .getPublicUrl(filePath);

            if (!urlData || !urlData.publicUrl) {
                 throw new Error("Could not get public URL after upload.");
            }
            // Construct URL, potentially with timestamp to help bypass caching
            const publicUrl = `${urlData.publicUrl}?${timestamp}`;
            const publicUrlForBackend = urlData.publicUrl; // Use the base URL for storing in backend


            // 4. Update backend database with the new (base) URL
            await updateProfilePicture(model.username, publicUrlForBackend);

            // 5. Update local state to reflect the change immediately
            setModel(prevModel => prevModel ? { ...prevModel, profilePicUrl: publicUrlForBackend } : null);
            setProfilePic(publicUrl); // Update display state with the timestamped URL to force refresh
            setSelectedFile(null); // Clear the selected file state

            alert("Profile picture updated successfully!");
            // Force update header potentially? Dispatch custom event or use context/global state
             window.dispatchEvent(new Event('profilePictureUpdated'));


        } catch (error: any) {
            console.error("Failed to update profile picture:", error);
            alert("Failed to update profile picture: " + error.message);
            // Optional: Revert preview back to the stored URL if upload fails
            setProfilePic(model?.profilePicUrl || null);
        } finally {
            setIsUploading(false); // Hide loading state
        }
    };

    // Handles updating basic user profile fields (name, password, email, phone)
    const handleProfileUpdate = async () => {
        if (!model) return; // Should not happen if loaded correctly
        try {
            const pass = password.trim().length > 0 ? password.trim() : null; // Send null if password field is empty
            await updateUserFields(model.username, pass, [email], name, phone);
            // Optionally clear password field after update for security
            setPassword("");
            alert("Profile details updated successfully!");
            // Might need to re-fetch or update model state if backend returns updated data
        } catch (e) {
            console.error("Failed to update profile details:", e);
            alert("Failed to update profile details: " + e);
        }
    };

    // Handles updating user skills
    const handleSkillsUpdate = async () => {
        if (!model) return;
        try {
            // Split by comma, trim whitespace, remove empty strings
            const updatedSkills = skillsInput.split(",")
                                      .map(skill => skill.trim())
                                      .filter(skill => skill.length > 0);
            await updateUserSkills(model.username, updatedSkills);
            setSkills(updatedSkills.join(", ")); // Update display state
            alert("Skills updated successfully!");
        } catch (e) {
             console.error("Failed to update skills:", e);
            alert("Failed to update skills: " + e);
        }
    };

    // Handles updating user preferences (categories)
    const handlePreferencesUpdate = async () => {
        if (!model) return;
        try {
            // Split by comma, trim whitespace, remove empty strings
            const updatedPreferences = preferencesInput.split(",")
                                               .map(pref => pref.trim())
                                               .filter(pref => pref.length > 0);
            await updateUserPreferences(model.username, updatedPreferences);
            setPreferences(updatedPreferences.join(", ")); // Update display state
            alert("Preferences updated successfully!");
        } catch (e) {
            console.error("Failed to update preferences:", e);
            alert("Failed to update preferences: " + e);
        }
    };

    // Handler for PDF export functionality
     const handleExport = async () => {
        if (selectedVolunteering < 0 || !id.trim()) {
            alert("Please enter your ID and select a volunteering.");
            return;
        }
        try {
            await getUserApprovedHoursFormatted(selectedVolunteering, id);
             // Note: This API likely triggers a file download, no state update needed here.
        } catch (e) {
             console.error("Failed to export approved hours PDF:", e);
            alert("Failed to export approved hours: " + e);
        }
    };

    // Handler for CSV export functionality
   const handleExportCsv = async () => {
        const weeks = parseInt(numWeeks);
        if (isNaN(weeks) || weeks <= 0) {
            alert("Please enter a valid positive number of weeks.");
            return;
        }
        try {
            await getAppointmentsCsv(weeks);
             // Note: This API likely triggers a file download.
        } catch (e) {
             console.error("Failed to export appointments CSV:", e);
            alert("Failed to export appointments: " + e);
        }
    };

    // Handler for toggling leaderboard visibility
    const toggleSwitch = async () => {
        if (!model) return;
        const newState = !isLeaderboard;
        try {
             setIsLeaderboard(newState); // Optimistic update
             await setLeaderboard(newState);
             // Update model state if needed
             setModel(prevModel => prevModel ? { ...prevModel, leaderboard: newState } : null);
             console.log("Leaderboard preference updated.");
        } catch (error) {
             console.error("Failed to update leaderboard preference:", error);
             alert("Failed to update leaderboard preference: " + error);
             setIsLeaderboard(!newState); // Revert optimistic update on error
        }
    };


    //--- Render Logic ---

    // Determine the image source: use the state `profilePic` (which holds preview or fetched URL), fallback to default
    const displayPic = profilePic || defaultProfilePic;

    // Show loading indicator or full page
    if (!model) {
        return <div>Loading profile...</div>; // Or a more sophisticated loading component
    }

    const onFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
        setSelectedCV(e.target.files![0]);
    }

    const onCVSubmit = async () => {
        try {
            if(selectedCV === null) {
                alert("Did not upload cv.");
            }
            else {
                await uploadCV(selectedCV);
                alert("CV uploaded successfully!");
                setSelectedCV(null);
                setCV(selectedCV);
                setKey(prevState => 1-prevState);
            }
        }
        catch (e) {
            alert(e);
        }
    };

    const onCVDownload = async () => {
        try {
            if(cv === null) {
                alert("Did not upload cv.");
            }
            else {
                const blob = await downloadCV(); // Get the file as a Blob
                const url = window.URL.createObjectURL(blob);
                const link = document.createElement("a");
                link.href = url;
                link.setAttribute("download", `${username}CV.pdf`); // Set the filename for the download
                document.body.appendChild(link);
                link.click();
                link.remove();
            }
        }
        catch (e) {
            alert(e);
        }
    };

    const onCVRemove = async () => {
        try {
            if(window.confirm("Are you sure you want to remove your cv?")) {
                await removeCV(); // Get the file as a Blob
                setSelectedCV(null);
                setCV(null);
            }
        }
        catch (e) {
            alert(e);
        }
    };

    const onCVExtract = async () => {
        try {
            await generateSkillsAndPreferences(); // Get the file as a Blob
            
            const profile = await getUserByToken();
            setSkills(profile.skills.join(", "));
            setSkillsInput(profile.skills.join(", "));
            setPreferences(profile.preferredCategories.join(", "));
            setPreferencesInput(profile.preferredCategories.join(", "));
        }
        catch (e) {
            alert(e);
        }
    };


    return (
        <div className="my-profile">
            <h1 className="bigHeader">My Profile</h1>

            {/* Profile Picture Section */}
            <div className="profile-picture-section">
                <div className="profile-picture-container">
                    <img
                        key={displayPic} // Add key to help React detect changes, especially after upload
                        src={displayPic}
                        alt={`${name}'s profile`} // More descriptive alt text
                        className="profile-picture-preview"
                        // Fallback to default image if the src URL is broken or fails to load
                        onError={(e) => {
                             if (e.currentTarget.src !== defaultProfilePic) {
                                 e.currentTarget.onerror = null; // Prevent infinite loop if default also fails
                                 e.currentTarget.src = defaultProfilePic;
                             }
                        }}
                    />
                </div>
                {/* Hidden file input triggered by the button */}
                <input
                    type="file"
                    accept="image/*" // Accept standard image types
                    id="profilePicInput"
                    style={{ display: "none" }}
                    onChange={handleFileChange}
                />
                {/* Button to open file selector */}
                <button
                    onClick={() => document.getElementById("profilePicInput")?.click()}
                    className="upload-button"
                    disabled={isUploading} // Disable while upload is in progress
                >
                    Choose Picture
                </button>
                {/* Button to initiate the upload and backend update */}
                <button
                    onClick={handleProfilePictureUpdate}
                    className="orangeCircularButton" // Using existing style class
                    disabled={!selectedFile || isUploading} // Disable if no file is selected or already uploading
                    style={{ marginTop: '10px' }} // Add some visual spacing
                >
                    {isUploading ? "Uploading..." : "Save Picture"}
                </button>
            </div>

            {/* Update Profile Details Section */}
            <div className="profile-section">
                <h2 className="profileSectionHeader">Update Profile Details</h2>
                <label>Username:</label>
                <input type="text" value={username} disabled />
                <label>Password:</label>
                <input
                    type="password"
                    placeholder="Enter new password (optional)" // Clarify usage
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    autoComplete="new-password" // Help browser password managers
                />
                <label>Name:</label>
                <input
                    type="text"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                />
                <label>Email:</label>
                 {/* Consider allowing multiple emails if backend supports it, otherwise keep single */}
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
                <button onClick={handleProfileUpdate} className="orangeCircularButton">
                    Update Profile Details
                </button>
            </div>

            <div className="cv-section">
                <h2 className="profileSectionHeader">Upload Your CV</h2>
                <p>Upload your CV to impress managers and extract your skills and preferences automatically!</p>
                <input type="file" accept="application/pdf" onChange={onFileUpload}  key={key}/>
                <div className="cvButtons">
                <div className="fileButtons" style={{marginTop:'20px'}}>
                <button onClick={onCVSubmit} className={`orangeCircularButton ${selectedCV === null ? 'disabledButton' : ''}`}>Upload CV</button>
                <button onClick={onCVDownload} className={`orangeCircularButton ${cv === null ? 'disabledButton' : ''}`} >Download CV</button>
                <button onClick={onCVRemove} className={`orangeCircularButton ${cv === null ? 'disabledButton' : ''}`} >Remove CV</button>
                </div>
                <button onClick={onCVExtract} className={`orangeCircularButton ${cv === null ? 'disabledButton' : ''}`} style={{marginTop:'20px'}}>Extract Skills And Preferences Automatically Using AI</button>
                </div>
            </div>

            {/* Update Skills Section */}
            <div className="list-section">
                <h2 className="profileSectionHeader">Update Skills</h2>
                <textarea
                    value={skillsInput}
                    onChange={(e) => setSkillsInput(e.target.value)}
                    placeholder="Enter skills separated by commas (e.g., driving, first aid, programming)"
                    rows={3} // Adjust size as needed
                />
                <button onClick={handleSkillsUpdate} className="orangeCircularButton">
                    Update Skills
                </button>
            </div>

            {/* Update Preferences Section */}
            <div className="list-section">
                <h2 className="profileSectionHeader">Update Preferences</h2>
                <textarea
                    value={preferencesInput}
                    onChange={(e) => setPreferencesInput(e.target.value)}
                    placeholder="Enter preferred volunteering categories separated by commas (e.g., animals, environment, elderly)"
                    rows={3} // Adjust size as needed
                />
                <button onClick={handlePreferencesUpdate} className="orangeCircularButton">
                    Update Preferences
                </button>
            </div>

             {/* Volunteering History Section */}
            <div className="history-section">
                <h2 className="profileSectionHeader">Volunteering History</h2>
                {volunteeringsInHistory.length > 0 ? (
                    <table className="history-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Org ID</th>
                                <th>Name</th>
                                <th>Description</th>
                                <th>Skills Used</th>
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
                    <p>No past volunteering records found.</p>
                )}
            </div>

             {/* Approved Hours Section */}
            <div className="history-section">
                 <h2 className="profileSectionHeader">Approved Hours</h2>
                 {approvedHours.length > 0 ? (
                     <table className="history-table">
                         <thead>
                             <tr>
                                 <th>Volunteering ID</th>
                                 <th>Date</th>
                                 <th>Start Time</th>
                                 <th>End Time</th>
                                 {/* Optionally calculate and display duration */}
                             </tr>
                         </thead>
                         <tbody>
                             {approvedHours.map((hours, index) => (
                                 <tr key={index}>
                                     <td>{hours.volunteeringId}</td>
                                     <td>{(new Date(hours.startTime)).toLocaleDateString()}</td>
                                     <td>{(new Date(hours.startTime)).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</td>
                                     <td>{(new Date(hours.endTime)).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</td>
                                 </tr>
                             ))}
                         </tbody>
                     </table>
                 ) : (
                     <p>No approved hours recorded yet.</p>
                 )}
            </div>

            {/* Profile Summary/Status Section */}
            <div className="status-section">
                 <h2 className="profileSectionHeader">Profile Summary</h2>
                 <p><strong>Username:</strong> {username}</p>
                 <p><strong>Name:</strong> {name}</p>
                 <p><strong>Email:</strong> {email}</p>
                 <p><strong>Phone:</strong> {phone}</p>
                 <p><strong>Birth Date:</strong> {birthDate}</p>
                 <p><strong>Current Skills:</strong> {skills || "None specified"}</p>
                 <p><strong>Current Preferences:</strong> {preferences || "None specified"}</p>
                 <p><strong>Administrator:</strong> {isAdmin ? "Yes" : "No"}</p>
                 <p><strong>Student Status:</strong> {model.student ? "Yes" : "No"}</p> {/* Based on backend logic for isStudent */}
                 {isAdmin && (
                     <button onClick={() => navigate('/reportList')} className="orangeCircularButton">
                         Go to Admin Reports
                     </button>
                 )}
            </div>

            {/* Export Approved Hours as PDF Section (Conditional) */}
            {model.student && ( // Only show if the user is marked as a student
                 <div className="export-section">
                     <h2>Export Approved Hours (PDF for Students)</h2>
                    <label htmlFor="studentIdInput">ID (Teudat Zehut):</label>
                     <input
                         id="studentIdInput"
                         type="text" // Use text, but could add pattern validation for numbers
                         placeholder="Enter Your ID Number"
                         value={id}
                         onChange={(e) => setId(e.target.value)}
                         required
                     />
                    <label htmlFor="volunteeringSelect">Select Volunteering for Report:</label>
                     <select
                         id="volunteeringSelect"
                         value={selectedVolunteering} // Controlled component
                         onChange={(e) => setSelectedVolunteering(parseInt(e.target.value))}
                         required
                     >
                         <option value={-1} disabled>-- Select a Volunteering --</option>
                         {/* Combine current and past volunteerings for selection */}
                         {model.volunteeringIds.map(vid => (
                             <option key={`current-${vid}`} value={vid}>Current: {vid}</option> // Add context if possible
                         ))}
                         {model.volunteeringsInHistory.map(hist => (
                             <option key={`history-${hist.id}`} value={hist.id}>
                                 Past: {hist.id} ({hist.name})
                             </option>
                         ))}
                     </select>
                     <button
                         disabled={selectedVolunteering < 0 || !id.trim()}
                         onClick={handleExport}
                         className="orangeCircularButton"
                         title={selectedVolunteering < 0 || !id.trim() ? "Please enter ID and select volunteering" : "Export PDF"}
                     >
                         Export PDF
                     </button>
                 </div>
             )}


            {/* Export Appointments as CSV Section */}
            <div className="export-section">
                <h2>Export Appointments to CSV</h2>
                <p>This CSV can be imported into Google Calendar or other calendar apps.</p>
                <label htmlFor="weeksInput">Number of weeks ahead:</label>
                <input
                    id="weeksInput"
                    type="number" // Use number type for better input control
                    min="1" // Minimum 1 week
                    placeholder="e.g., 4"
                    value={numWeeks}
                    onChange={(e) => setNumWeeks(e.target.value)} // Value is string, parsed in handler
                />
                <button
                    disabled={isNaN(parseInt(numWeeks)) || parseInt(numWeeks) <= 0}
                    onClick={handleExportCsv}
                    className="orangeCircularButton"
                    title={isNaN(parseInt(numWeeks)) || parseInt(numWeeks) <= 0 ? "Enter a valid number of weeks" : "Export CSV"}
                     >
                    Export CSV
                </button>
            </div>

            {/* Leaderboard Toggle Section */}
            <div className="leaderboard-section">
                <h2 className="profileSectionHeader">Leaderboard Preference</h2>
                <p>Choose whether to appear in the public leaderboard of volunteering hours.</p>
                <label style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}>
                    <span>Show me on the leaderboard:</span>
                    <Switch
                        className="switch"
                        checked={isLeaderboard}
                        onChange={toggleSwitch}
                        color="primary" // Or "secondary", "default"
                    />
                 </label>
            </div>
        </div>
    );
}

export default MyProfilePage;