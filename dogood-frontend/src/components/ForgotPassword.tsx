// src/components/ForgotPassword.tsx
// FORGOT_PASSWORD_USERNAME_INPUT START
import { useState } from "react";
import { forgotPassword } from "../api/user_api"; // This API function will be updated
import "../css/LoginPage.css"; // Reuse styles

interface ForgotPasswordProps {
    onSwitchToLogin: () => void;
    // Now expects username to be passed to the next step,
    // as the backend will send the code to the email associated with this username.
    // The 'email' itself is not directly known by this page anymore for the next step.
    onUsernameSubmitted: (username: string) => void; 
}

export default function ForgotPassword({ onSwitchToLogin, onUsernameSubmitted }: ForgotPasswordProps) {
    const [username, setUsername] = useState(""); // Changed from email to username
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);

    const handleSubmit = async () => {
        if (!username.trim()) { // Validate username
            setError("Please enter your username.");
            return;
        }
        // Basic username validation (e.g., length) could be added here if desired
        // const usernameRegex = /^[a-zA-Z0-9_.-]{3,20}$/; 
        // if (!usernameRegex.test(username)) {
        //     setError("Please enter a valid username.");
        //     return;
        // }

        setIsLoading(true);
        setError(null);
        setSuccessMessage(null);

        try {
            // Call forgotPassword API with username
            const backendMessage = await forgotPassword(username); 
            // Backend returns a generic success message to prevent username enumeration.
            setSuccessMessage(backendMessage + " (If your username is registered, a code will be sent to the associated email.)"); 
            
            setTimeout(() => {
                 // Pass the username to the next step (ResetPasswordWithCodePage)
                 // The ResetPasswordWithCodePage will need this username to verify the code
                 // and for the resend functionality.
                 onUsernameSubmitted(username); 
            }, 3000); // Give user time to read the message

        } catch (e: any) {
            // This catch block will likely only handle network errors or unexpected server issues.
            setError(e.message || "An unexpected error occurred. Please try again.");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="back"> 
            <div className="loginPage"> 
                <div className="loginSection" style={{width: '100%', borderLeft: 'none'}}> 
                    <h1 className="mobileLogin">Forgot Password</h1> 
                    
                    {error && <p style={{ color: 'red', textAlign: 'center', marginBottom: '10px' }}>{error}</p>}
                    {successMessage && <p style={{ color: 'green', textAlign: 'center', marginBottom: '10px' }}>{successMessage}</p>}

                    {!successMessage && ( 
                        <div className="fields" style={{ width: '80%', maxWidth: '400px' }}> 
                            <p style={{ fontSize: '14px', textAlign: 'center', color: '#555', marginBottom: '15px' }}>
                                Enter your username and we'll send a code to your registered email address to reset your password.
                            </p>
                            <input
                                type="text" // Changed from email to text
                                placeholder="Enter your username" // Updated placeholder
                                value={username}
                                onChange={e => setUsername(e.target.value)}
                                disabled={isLoading}
                                style={{ margin: '5px 0', padding: '10px', fontSize: '16px' }}
                            />
                            <button
                                onClick={handleSubmit}
                                className="orangeCircularButton" 
                                disabled={isLoading || !username.trim()}
                                style={{ margin: '10px 0', padding: '10px', fontSize: '16px' }}
                            >
                                {isLoading ? "Sending..." : "Send Reset Code"}
                            </button>
                        </div>
                    )}

                    <a
                        onClick={onSwitchToLogin}
                        style={{ fontSize: '14px', textDecoration: 'underline', color: '#555', cursor: 'pointer', textAlign: 'center', marginTop: '20px' }}
                    >
                        Back to Login
                    </a>
                </div>
            </div>
        </div>
    );
}
// FORGOT_PASSWORD_USERNAME_INPUT END