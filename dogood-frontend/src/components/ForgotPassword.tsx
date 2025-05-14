// src/components/ForgotPassword.tsx
// FORGOT_PASSWORD START
import { useState } from "react";
import { forgotPassword } from "../api/user_api";
import "../css/LoginPage.css"; // Reuse styles

interface ForgotPasswordProps {
    onSwitchToLogin: () => void;
    onEmailSubmitted: (email: string) => void;
}

export default function ForgotPassword({ onSwitchToLogin, onEmailSubmitted }: ForgotPasswordProps) {
    const [email, setEmail] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);

    const handleSubmit = async () => {
        if (!email) {
            setError("Please enter your email address.");
            return;
        }
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            setError("Please enter a valid email address.");
            return;
        }

        setIsLoading(true);
        setError(null);
        setSuccessMessage(null);

        try {
            // The backend always returns a generic success message for forgotPassword
            // to prevent email enumeration.
            const backendMessage = await forgotPassword(email);
            setSuccessMessage(backendMessage); // Display the generic message from backend
            
            // Proceed to the next step regardless of whether the email was actually found,
            // as per the backend's security design.
            // The actual verification of email existence happens implicitly when the user tries to use the code.
            setTimeout(() => {
                 onEmailSubmitted(email);
            }, 2500); // Give user time to read the message

        } catch (e: any) {
            // This catch block will likely only handle network errors or unexpected server issues,
            // not "email not found" errors, due to the backend's design.
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
                                Enter your email address and we'll send you a code to reset your password.
                            </p>
                            <input
                                type="email"
                                placeholder="Enter your email"
                                value={email}
                                onChange={e => setEmail(e.target.value)}
                                disabled={isLoading}
                                style={{ margin: '5px 0', padding: '10px', fontSize: '16px' }}
                            />
                            <button
                                onClick={handleSubmit}
                                className="orangeCircularButton" 
                                disabled={isLoading}
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
// FORGOT_PASSWORD END