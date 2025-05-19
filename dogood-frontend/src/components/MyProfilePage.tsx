import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import {
    getUserByToken,
    updateUserFields,
    updateUserSkills,
    updateUserPreferences,
    getIsAdmin,
    getUserApprovedHours,
    setLeaderboard,
    leaderboard,
    uploadCV,
    downloadCV,
    removeCV,
    generateSkillsAndPreferences,
    updateProfilePicture,
    requestEmailUpdateVerification,
    changePassword,
    setNotifyRecommendation, setRemindActivity,
} from "../api/user_api";
import './../css/MyProfile.css';
import User, { VolunteeringInHistory } from "../models/UserModel";
import ApprovedHours from "../models/ApprovedHoursModel";
import {getAppointmentsCsv, getUserApprovedHoursFormatted} from "../api/volunteering_api";
import { Switch } from "@mui/material";
import PacmanLoader from "react-spinners/PacmanLoader";
import { supabase } from "../api/general";
import defaultImage from '../assets/defaultProfilePic.jpg';

function MyProfilePage() {
    const navigate = useNavigate();

    // States for user fields
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [originalEmail, setOriginalEmail] = useState("");
    const [phone, setPhone] = useState("");
    const [birthDate, setBirthDate] = useState("");
    const [skillsInput, setSkillsInput] = useState("");
    const [skills, setSkills] = useState("");
    const [preferencesInput, setPreferencesInput] = useState("");
    const [preferences, setPreferences] = useState("");
    const [isAdmin, setIsAdmin] = useState(false);
    const [isLeaderboard, setIsLeaderboard] = useState(true);
    const [isRemind, setIsRemind] = useState(true);
    const [isNotify, setIsNotify] = useState(true);
    const [selectedCV, setSelectedCV] = useState<File | null>(null);
    const [cv, setCV] = useState<File | null>(null);
    const [loading, setLoading] = useState(false);
    const [key, setKey] = useState(0)
    const [model, setModel] = useState<User | null>(null);

    // Volunteering History
    const [volunteeringsInHistory, setVolunteeringsInHistory] = useState<VolunteeringInHistory[]>([]);


    // Export CSV
    const [numWeeks, setNumWeeks] = useState("");

    // Profile Picture States
    const [profilePic, setProfilePic] = useState<string | null>(null);
    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const [isUploading, setIsUploading] = useState<boolean>(false);

    // UPDATE-EMAIL-VERIFICATION START
    const [isLoadingUpdate, setIsLoadingUpdate] = useState(false); // For profile update process
    const [errorUpdate, setErrorUpdate] = useState<string | null>(null);
    // UPDATE-EMAIL-VERIFICATION END
    // PASSWORD-CHANGE-NO-EMAIL START
    const [oldPassword, setOldPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmNewPassword, setConfirmNewPassword] = useState("");
    const [isChangingPassword, setIsChangingPassword] = useState(false);
    const [passwordChangeError, setPasswordChangeError] = useState<string | null>(null);
    const [passwordChangeSuccess, setPasswordChangeSuccess] = useState<string | null>(null);
    // PASSWORD-CHANGE-NO-EMAIL END

    // Volunteering Now

    // Fetch user profile on load
    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const profile = await getUserByToken();
                setUsername(profile.username);
                setName(profile.name);
                setEmail(profile.emails[0]);
                setOriginalEmail(profile.emails[0]);
                setPhone(profile.phone);
                setBirthDate(new Date(profile.birthDate).toISOString().split('T')[0]);
                setIsAdmin(await getIsAdmin(profile.username));
                setSkills(profile.skills.join(", "));
                setSkillsInput(profile.skills.join(", "));
                setPreferences(profile.preferredCategories.join(", "));
                setPreferencesInput(profile.preferredCategories.join(", "));
                setVolunteeringsInHistory(profile.volunteeringsInHistory);
                setIsLeaderboard(profile.leaderboard);
                setIsNotify(profile.notifyRecommendations)
                setIsRemind(profile.remindActivity)

                try {
                    const cvBlob : Blob = await downloadCV();
                    const cvFile = new File([cvBlob], "cv", { type: cvBlob.type });
                    setCV(cvFile);
                }
                catch(e) {
                    setCV(null);
                }
                setProfilePic(profile.profilePicUrl);
                setModel(profile);
                console.log(profile);
            } catch (e) {
                alert("Failed to load profile: " + e);
            }
        };
        fetchProfile();
    }, []);

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

    // UPDATE-EMAIL-VERIFICATION START
    const handleProfileUpdate = async () => {
        setErrorUpdate(null);
        setIsLoadingUpdate(true);

        const newEmailTrimmed = email.trim();
        const newPasswordTrimmed = password.trim(); // Password from the input field

        const emailActuallyChanged = newEmailTrimmed !== originalEmail;
        // Password changed if the password field is not empty AND different from some known state (if we stored old pass hash)
        // For simplicity, we'll consider it "changed" if the field is non-empty, triggering verification.
        const passwordFieldIsNonEmpty = newPasswordTrimmed.length > 0;

        if (emailActuallyChanged) {
            // Email has changed, verification is required for the original email
            const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
            if (!emailRegex.test(newEmailTrimmed)) {
                alert("Invalid new email address format.");
                setIsLoadingUpdate(false);
                return;
            }
            if (passwordFieldIsNonEmpty && newPasswordTrimmed.length < 6) {
                alert("New password must be at least 6 characters long.");
                setIsLoadingUpdate(false);
                return;
            }

            try {
                await requestEmailUpdateVerification(originalEmail); // Send code to original email

                const pendingUpdateData = {
                    name: name,
                    phone: phone,
                    newEmail: newEmailTrimmed, // The intended new email
                    newPassword: passwordFieldIsNonEmpty ? newPasswordTrimmed : null,
                };

                alert("A verification code has been sent to your current email address: " + originalEmail + ". Please verify to update your profile.");
                navigate('/verify-email-update', {
                    state: {
                        originalEmail: originalEmail, // Email where code was sent
                        pendingUpdateData: pendingUpdateData,
                        username: username // Pass username for the final updateUserFields call
                    }
                });
            } catch (e: any) {
                const errorMsg = e.message || e.toString();
                alert("Failed to initiate email change verification: " + errorMsg);
                setErrorUpdate("Failed to initiate email change verification: " + errorMsg);
            } finally {
                setIsLoadingUpdate(false);
            }
        } else {
            // Email has NOT changed. Update other fields directly.
            // If password field is non-empty, it means user wants to change password.
            // The existing updateUserFields can handle this (backend might or might not require verification for password-only change).
            // For this task, we assume password-only changes don't need this *new* verification flow.
            // If they do, the logic would be similar to email change, sending code to originalEmail.
            
            // Let's assume for now that if email didn't change, we proceed with a direct update.
            // If password field is empty, pass null.
            const passwordToUpdate = passwordFieldIsNonEmpty ? newPasswordTrimmed : null;
            if (passwordToUpdate && passwordToUpdate.length < 6) {
                 alert("New password must be at least 6 characters long.");
                 setIsLoadingUpdate(false);
                 return;
            }

            try {
                await updateUserFields(username, passwordToUpdate, [originalEmail], name, phone);
                alert("Profile updated successfully!");
                if (passwordToUpdate) setPassword(""); // Clear password field after successful update
            } catch (e: any) {
                const errorMsg = e.message || e.toString();
                alert("Failed to update profile: " + errorMsg);
                setErrorUpdate("Failed to update profile: " + errorMsg);
            } finally {
                setIsLoadingUpdate(false);
            }
        }
    };
    // UPDATE-EMAIL-VERIFICATION END
        // PASSWORD-CHANGE-NO-EMAIL START
    const handleChangePassword = async () => {
        setPasswordChangeError(null);
        setPasswordChangeSuccess(null);

        if (!oldPassword || !newPassword || !confirmNewPassword) {
            setPasswordChangeError("All password fields are required.");
            return;
        }
        if (newPassword.length < 6) {
            setPasswordChangeError("New password must be at least 6 characters long.");
            return;
        }
        if (newPassword !== confirmNewPassword) {
            setPasswordChangeError("New passwords do not match.");
            return;
        }

        setIsChangingPassword(true);
        try {
            const result = await changePassword({
                username: username,
                oldPassword: oldPassword,
                newPassword: newPassword,
            });
            setPasswordChangeSuccess(result || "Password updated successfully!");
            setOldPassword("");
            setNewPassword("");
            setConfirmNewPassword("");
        } catch (e: any) {
            setPasswordChangeError(e.message || e.toString() || "Failed to change password.");
        } finally {
            setIsChangingPassword(false);
        }
    };
    // PASSWORD-CHANGE-NO-EMAIL END

    const handleSkillsUpdate = async () => {
        try {
            const updatedSkills = skillsInput === "" ? [] : skillsInput.split(",").map(skill => skill.trim());
            await updateUserSkills(username, updatedSkills);
            setSkills(updatedSkills.join(", "));
            alert("Skills updated successfully!");
        } catch (e) {
            alert("Failed to update skills: " + e);
        }
    };

    const handlePreferencesUpdate = async () => {
        try {
            const updatedPreferences = preferencesInput === "" ? [] : preferencesInput.split(",").map(preference => preference.trim());
            await updateUserPreferences(username, updatedPreferences);
            setPreferences(updatedPreferences.join(", "));
            alert("Preferences updated successfully!");
        } catch (e) {
            alert("Failed to update preferences: " + e);
        }
    };

    const handleExportCsv = async () => {
        try{
            await getAppointmentsCsv(parseInt(numWeeks))
        } catch(e){
            alert("Failed to export appointments: " + e);
        }
    }

    const toggleSwitch = async () => {
        let newState = !isLeaderboard;
        setIsLeaderboard(newState);
        await setLeaderboard(newState);
    }

    const toggleSwitchNotify = async () => {
        let newState = !isNotify;
        setIsNotify(newState);
        await setNotifyRecommendation(newState);
    }

    const toggleSwitchRemind = async () => {
        let newState = !isRemind;
        setIsRemind(newState);
        await setRemindActivity(newState);
    }

    const displayPic = (!profilePic || profilePic === "") ? defaultImage : profilePic;

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
            setLoading(true);
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
        finally {
            setLoading(false);
        }
    };


    return (
        <div className="my-profile">
            <h1 className="bigHeader">My Profile</h1>
            {/* UPDATE-EMAIL-VERIFICATION START */}
            {errorUpdate && <p style={{color: 'red', textAlign: 'center'}}>{errorUpdate}</p>}
            {/* UPDATE-EMAIL-VERIFICATION END */}
            {/* Profile Picture Section */}
            <div className="profile-picture-section">
                <div className="profile-picture-container">
                    <img
                        key={displayPic} // Add key to help React detect changes, especially after upload
                        src={displayPic!}
                        alt={`${name}'s profile`} // More descriptive alt text
                        className="profile-picture-preview"
                        // Fallback to default image if the src URL is broken or fails to load
                        onError={(e) => {
                            if (e.currentTarget.src !== defaultImage) {
                                e.currentTarget.onerror = null; // Prevent infinite loop if default also fails
                                e.currentTarget.src = defaultImage;
                            }
                        }}
                    />
                </div>
                <div className="profilePicButtons">
                    {/* Hidden file input triggered by the button */}
                    <input
                        type="file"
                        accept="image/*" // Accept standard image types
                        id="profilePicInput"
                        style={{display: "none"}}
                        onChange={handleFileChange}
                    />
                    {/* Button to open file selector */}
                    <button
                        onClick={() => document.getElementById("profilePicInput")?.click()}
                        className="upload-button orangeCircularButton"
                        disabled={isUploading} // Disable while upload is in progress
                    >
                        Choose Picture
                    </button>
                    {/* Button to initiate the upload and backend update */}
                    <button
                        onClick={handleProfilePictureUpdate}
                        className="orangeCircularButton" // Using existing style class
                        disabled={!selectedFile || isUploading} // Disable if no file is selected or already uploading
                    >
                        {isUploading ? "Uploading..." : "Save Picture"}
                    </button>
                </div>
            </div>
            <div className="profile-section">
                <h2 className="profileSectionHeader">Update Profile</h2>
                <label>Username:</label>
                <input type="text" value={username} disabled/>
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
                <input type="date" value={birthDate} disabled/>
                {/* UPDATE-EMAIL-VERIFICATION START */}
                <button onClick={handleProfileUpdate} className="orangeCircularButton" disabled={isLoadingUpdate}>
                    {isLoadingUpdate ? "Processing..." : "Update Profile"}
                </button>
                {/* UPDATE-EMAIL-VERIFICATION END */}
            </div>
            {/* PASSWORD-CHANGE-NO-EMAIL START */}
            <div className="profile-section">
                <h2 className="profileSectionHeader">Change Password</h2>
                {passwordChangeError && <p style={{color: 'red', textAlign: 'center'}}>{passwordChangeError}</p>}
                {passwordChangeSuccess && <p style={{color: 'green', textAlign: 'center'}}>{passwordChangeSuccess}</p>}

                <label htmlFor="oldPassword">Current Password:</label>
                <input
                    id="oldPassword"
                    type="password"
                    placeholder="Enter your current password"
                    value={oldPassword}
                    onChange={(e) => setOldPassword(e.target.value)}
                    disabled={isChangingPassword}
                />
                <label htmlFor="newPassword">New Password:</label>
                <input
                    id="newPassword"
                    type="password"
                    placeholder="Enter new password"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    disabled={isChangingPassword}
                />
                <label htmlFor="confirmNewPassword">Confirm New Password:</label>
                <input
                    id="confirmNewPassword"
                    type="password"
                    placeholder="Confirm new password"
                    value={confirmNewPassword}
                    onChange={(e) => setConfirmNewPassword(e.target.value)}
                    disabled={isChangingPassword}
                />
                <button onClick={handleChangePassword} className="orangeCircularButton" disabled={isChangingPassword}>
                    {isChangingPassword ? "Changing..." : "Change Password"}
                </button>
            </div>
            {/* PASSWORD-CHANGE-NO-EMAIL END */}

            <div className="cv-section">
                <h2 className="profileSectionHeader">Upload Your CV</h2>
                <p>Upload your CV to impress managers and extract your skills and preferences automatically!</p>
                <input type="file" accept="application/pdf" onChange={onFileUpload} key={key}/>
                <div className="cvButtons">
                    <div className="fileButtons" style={{marginTop: '20px'}}>
                        <button onClick={onCVSubmit}
                                className={`orangeCircularButton ${selectedCV === null ? 'disabledButton' : ''}`}>Upload
                            CV
                        </button>
                        <button onClick={onCVDownload}
                                className={`orangeCircularButton ${cv === null ? 'disabledButton' : ''}`}>Download CV
                        </button>
                        <button onClick={onCVRemove}
                                className={`orangeCircularButton ${cv === null ? 'disabledButton' : ''}`}>Remove CV
                        </button>
                    </div>
                    <button onClick={onCVExtract}
                            className={`orangeCircularButton ${cv === null || loading ? 'disabledButton' : ''}`}
                            style={{marginTop: '20px'}}>Extract Skills And Preferences Automatically Using AI
                    </button>
                    {loading && <PacmanLoader color="#037b7b" size={25}/>}
                </div>
            </div>

            <div className="list-section">
                <h2 className="profileSectionHeader">Update Skills</h2>
                <textarea
                    value={skillsInput}
                    onChange={(e) => setSkillsInput(e.target.value)}
                    placeholder="Enter skills separated by commas"
                />
                <button onClick={handleSkillsUpdate} className="orangeCircularButton">Update Skills</button>
            </div>

            <div className="list-section">
                <h2 className="profileSectionHeader">Update Preferences</h2>
                <textarea
                    value={preferencesInput}
                    onChange={(e) => setPreferencesInput(e.target.value)}
                    placeholder="Enter preferences separated by commas"
                />
                <button onClick={handlePreferencesUpdate} className="orangeCircularButton">Update Preferences</button>
            </div>

            <div className="history-section">
                <h2 className="profileSectionHeader">Volunteering History</h2>
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
            <div className="status-section">
                <h2 className="profileSectionHeader">Profile Status</h2>
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
                    <button onClick={() => navigate('/reportList')} className="orangeCircularButton">Go to
                        Reports</button>
                )}
            </div>

            <div className="export-section">
                <h2>Export your appointments as a CSV</h2>
                <p>This CSV can later be imported into Google Calendar</p>
                <label>Number of weeks ahead</label>
                <input
                    placeholder="Enter number of weeks"
                    value={numWeeks}
                    onChange={(e) => setNumWeeks(e.target.value)}
                />
                <button disabled={Number.isNaN(numWeeks)} onClick={handleExportCsv}
                        className="orangeCircularButton">Export
                </button>
            </div>

            <div className="leaderboard-section">
                <h2 className="profileSectionHeader">Set Leaderboard</h2>
                <p>Set here if you would like to appear in the leaderboard of volunteering hours.</p>
                <Switch className='switch' checked={isLeaderboard} onChange={toggleSwitch}/>
            </div>

            <div className="leaderboard-section">
                <h2 className="profileSectionHeader">Recommendations Notifications</h2>
                <p>Set here if you would like to be notified about new posts that might be relevant for you.</p>
                <Switch className='switch' checked={isNotify} onChange={toggleSwitchNotify}/>
            </div>

            <div className="leaderboard-section">
                <h2 className="profileSectionHeader">Remind Me Before Activities</h2>
                <p>Set here if you would like to be notified an hour before a volunteering appointment.</p>
                <Switch className='switch' checked={isRemind} onChange={toggleSwitchRemind}/>
            </div>
        </div>
    );
}

export default MyProfilePage;
