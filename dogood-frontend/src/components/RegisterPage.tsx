// src/components/RegisterPage.tsx
import { useState } from "react";
import { register } from "../api/user_api";
import { supabase } from "../api/general";
import "../css/LoginPage.css"; // Reuse login page styles

// Props Interface
interface RegisterPageProps {
    onSwitchToLogin: () => void;
    onAuthSuccess: (username: string, token: string) => void; // Kept for structure, but won't be called on success
    // VERIFICATION START
    onSwitchToVerifyEmail: (username: string) => void; // New prop to switch to verification view
    // VERIFICATION END
}

// Component Signature
// VERIFICATION START
function RegisterPage({ onSwitchToLogin, onAuthSuccess, onSwitchToVerifyEmail }: RegisterPageProps) {
// VERIFICATION END
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [phone, setPhone] = useState("");
    const [birthDate, setBirthDate] = useState("");
    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const [preview, setPreview] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
         if (e.target.files && e.target.files[0]) {
            const file = e.target.files[0];
            if (file.size > 5 * 1024 * 1024) { // 5MB limit
                setError("File size exceeds 5MB limit.");
                setSelectedFile(null); // Clear invalid file
                setPreview(null);
                return;
            }
            setSelectedFile(file);
            setError(null);
            const reader = new FileReader();
            reader.onloadend = () => {
                setPreview(reader.result as string);
            };
            reader.readAsDataURL(file);
        }
    };

    const handleRegister = async () => {
        setIsLoading(true);
        setError(null);
        let profilePicUrl = "";

        if (!username || !password || !name || !email || !phone || !birthDate) {
            setError("Please fill in all required fields marked with *.");
            setIsLoading(false);
            return;
        }
        const phoneRegex = /^(\+972|0)5\d-?\d{7}$/;
        if (!phoneRegex.test(phone)) {
             setError("Invalid phone number format.");
             setIsLoading(false);
             return;
        }
         const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
         if (!emailRegex.test(email)) {
             setError("Invalid email address format.");
             setIsLoading(false);
             return;
         }

        try {
            if (selectedFile) {
                 const fileExt = selectedFile.name.split('.').pop();
                 const fileName = `${username}-${Date.now()}.${fileExt}`; 
                 const filePath = fileName;

                 console.log("Uploading profile picture...");
                 const { error: uploadError } = await supabase.storage
                    .from("profile-pictures")
                    .upload(filePath, selectedFile, { cacheControl: '3600', upsert: false });

                 if (uploadError) {
                    console.error("Supabase upload error:", uploadError);
                    throw new Error("Image upload failed: " + uploadError.message);
                 }

                 const { data: urlData } = supabase.storage.from("profile-pictures").getPublicUrl(filePath);
                 profilePicUrl = urlData?.publicUrl || "";
                 console.log("Profile picture uploaded:", profilePicUrl);
            } else {
                console.log("No profile picture selected for upload.");
            }

            console.log("Calling register API...");
            let backendResponse = await register(username, password, name, email, phone, birthDate, profilePicUrl);
            console.log("Registration API call successful, backend returned:", backendResponse);

            if (backendResponse && typeof backendResponse === 'string' && backendResponse.toUpperCase() === "OK") {
                 // VERIFICATION START
                 // alert("Registration successful! Please check your email to verify your account."); // Inform user
                 onSwitchToVerifyEmail(username); // Switch to verification view, passing the username
                 // VERIFICATION END
            } else {
                console.warn("Unexpected response from registration API:", backendResponse);
                setError("Registration completed, but an unexpected response was received. Please try logging in or contact support.");
                // Fallback, though ideally backend should be consistent
                onSwitchToLogin();
            }

        } catch (e: any) {
            console.error("Registration failed:", e);
            // Check if the error message is from our APIResponse structure or a generic error
            const errorMessage = typeof e === 'string' ? e : (e.message || "An unknown error occurred during registration.");
            setError(errorMessage);

        } finally {
            setIsLoading(false); 
        }
    };

    return (
        <div className="back">
            <div className="loginPage">
                 <link
                    href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap"
                    rel="stylesheet"
                />
                <div className="loginSection" style={{width: '100%', borderLeft: 'none'}}>
                    <h1>Register</h1>
                    {error && <p style={{ color: 'red', textAlign: 'center', marginBottom: '10px', fontSize: '14px', fontWeight: 'bold' }}>{error}</p>}

                    <div className="profile-picture-upload" style={{ marginBottom: '15px', textAlign: 'center' }}>
                         <div
                            className="circle-placeholder"
                            onClick={() => !isLoading && document.getElementById('profilePicInput')?.click()}
                            style={{
                                width: '80px', height: '80px', borderRadius: '50%', backgroundColor: '#e0e0e0',
                                display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
                                fontSize: '1.5rem', color: '#777', cursor: isLoading ? 'not-allowed' : 'pointer', overflow: 'hidden', border: '1px dashed #ccc'
                            }}
                        >
                            {preview ? (
                                <img src={preview} alt="Preview" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                            ) : ("📷")}
                        </div>
                        <input
                            type="file" accept="image/*" id="profilePicInput"
                            style={{ display: 'none' }} onChange={handleFileChange} disabled={isLoading}
                        />
                         <p style={{ fontSize: '12px', color: '#666', marginTop: '5px' }}>Add Profile Picture (Optional, Max 5MB)</p>
                    </div>

                    <div className="fields" style={{ width: '80%', maxWidth: '400px' }}>
                        <label htmlFor="reg-username" style={{display: 'none'}}>Username</label>
                        <input id="reg-username" type="text" placeholder="Username*" value={username} onChange={e => setUsername(e.target.value)} style={{ margin: '4px 0', padding: '8px', fontSize: '14px' }} disabled={isLoading} required/>

                        <label htmlFor="reg-password" style={{display: 'none'}}>Password</label>
                        <input id="reg-password" type="password" placeholder="Password*" value={password} onChange={e => setPassword(e.target.value)} style={{ margin: '4px 0', padding: '8px', fontSize: '14px' }} disabled={isLoading} required/>

                        <label htmlFor="reg-name" style={{display: 'none'}}>Full Name</label>
                        <input id="reg-name" type="text" placeholder="Full Name*" value={name} onChange={e => setName(e.target.value)} style={{ margin: '4px 0', padding: '8px', fontSize: '14px' }} disabled={isLoading} required/>

                        <label htmlFor="reg-email" style={{display: 'none'}}>Email</label>
                        <input id="reg-email" type="email" placeholder="Email*" value={email} onChange={e => setEmail(e.target.value)} style={{ margin: '4px 0', padding: '8px', fontSize: '14px' }} disabled={isLoading} required/>

                        <label htmlFor="reg-phone" style={{display: 'none'}}>Phone</label>
                        <input id="reg-phone" type="tel" placeholder="Phone*" value={phone} onChange={e => setPhone(e.target.value)} style={{ margin: '4px 0', padding: '8px', fontSize: '14px' }} disabled={isLoading} required/>

                        <label htmlFor="birthDate" style={{fontSize: '12px', color: '#666', marginTop: '4px', textAlign: 'left', width: '100%'}}>Birth Date*:</label>
                        <input type="date" id="birthDate" value={birthDate} onChange={e => setBirthDate(e.target.value)} style={{ margin: '4px 0', padding: '8px', fontSize: '14px' }} disabled={isLoading} required/>

                        <button onClick={handleRegister} className="orangeCircularButton" style={{ margin: '10px 0', padding: '10px', fontSize: '16px' }} disabled={isLoading}>
                             {isLoading ? "Registering..." : "Register"}
                        </button>
                        <a
                            onClick={onSwitchToLogin}
                            style={{ margin: '10px 0', fontSize: '14px', textDecoration: 'underline', textAlign: 'center', color: '#555', cursor: isLoading ? 'not-allowed' : 'pointer' }}
                        >
                            Already have an account? Login
                        </a>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default RegisterPage;