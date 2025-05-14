// FORGOT_PASSWORD START
// src/components/ResetPasswordWithCodePage.tsx
import { useState } from "react";
import { resetPassword, verifyPasswordResetCode } from "../api/user_api"; // Import both
import "../css/ResetPasswordWithCodePage.css";

interface ResetPasswordWithCodePageProps {
    email: string; // Email for which the reset was initiated
    onSwitchToLogin: () => void;
}

function ResetPasswordWithCodePage({ email, onSwitchToLogin }: ResetPasswordWithCodePageProps) {
    const [username, setUsername] = useState(""); // User needs to input their username
    const [code, setCode] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);
    const [isCodeVerified, setIsCodeVerified] = useState(false); // To manage UI flow

    const handleVerifyCode = async () => {
        if (!username) {
            setError("Please enter your username.");
            return;
        }
        if (!code || code.length !== 6) {
            setError("Please enter a valid 6-digit reset code.");
            return;
        }
        setIsLoading(true);
        setError(null);
        try {
            const verificationResponse = await verifyPasswordResetCode(username, code);
            // Assuming backend returns a specific success message like "Verification code is valid."
            if (verificationResponse.toLowerCase().includes("valid")) {
                setIsCodeVerified(true);
                setSuccessMessage("Code verified. Please enter your new password.");
            } else {
                setError(verificationResponse || "Invalid or expired verification code.");
            }
        } catch (e: any) {
            setError(e.message || "Code verification failed.");
        } finally {
            setIsLoading(false);
        }
    };

    const handleResetPassword = async () => {
        if (!newPassword || newPassword.length < 6) {
            setError("New password must be at least 6 characters long.");
            return;
        }
        if (newPassword !== confirmPassword) {
            setError("Passwords do not match.");
            return;
        }

        setIsLoading(true);
        setError(null);
        setSuccessMessage(null); // Clear previous success message

        try {
            // The 'code' is sent again as per ResetPasswordRequest DTO for re-validation
            const resetResponse = await resetPassword(username, code, newPassword);
            if (resetResponse.toLowerCase().includes("success")) {
                setSuccessMessage("Password has been reset successfully! Redirecting to login...");
                setTimeout(() => {
                    onSwitchToLogin();
                }, 2500);
            } else {
                setError(resetResponse || "Failed to reset password.");
                setIsCodeVerified(false); // If reset fails, likely need to re-verify code or start over
            }
        } catch (e: any) {
            setError(e.message || "An error occurred while resetting password.");
            setIsCodeVerified(false);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="reset-password-back">
            <div className="reset-password-page-container">
                <link
                    href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap"
                    rel="stylesheet"
                />
                <div className="reset-password-section">
                    <h1>Reset Your Password</h1>
                    <p style={{ marginBottom: '5px', color: '#555' }}>
                        A reset code was sent to: <strong>{email}</strong>.
                    </p>
                    
                    {error && <p className="error-message">{error}</p>}
                    {successMessage && <p className="success-message">{successMessage}</p>}

                    {!isCodeVerified && !successMessage && (
                        <div className="fields">
                             <p style={{ marginBottom: '10px', color: '#555' }}>
                                Please enter your username and the 6-digit code.
                            </p>
                            <input
                                type="text"
                                placeholder="Enter your username"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                disabled={isLoading}
                                className="password-input" // Reusing style, can be specific
                            />
                            <input
                                type="text"
                                placeholder="Enter 6-digit reset code"
                                value={code}
                                onChange={(e) => setCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
                                maxLength={6}
                                disabled={isLoading}
                                className="code-input"
                            />
                            <button
                                onClick={handleVerifyCode}
                                className="orangeCircularButton reset-button"
                                disabled={isLoading || !code || !username}
                            >
                                {isLoading ? "Verifying Code..." : "Verify Code"}
                            </button>
                        </div>
                    )}

                    {isCodeVerified && !successMessage && (
                        <div className="fields">
                             <p style={{ marginBottom: '10px', color: '#555' }}>
                                Code verified for user <strong>{username}</strong>. Enter your new password.
                            </p>
                            <input
                                type="password"
                                placeholder="Enter new password"
                                value={newPassword}
                                onChange={(e) => setNewPassword(e.target.value)}
                                disabled={isLoading}
                                className="password-input"
                            />
                            <input
                                type="password"
                                placeholder="Confirm new password"
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)}
                                disabled={isLoading}
                                className="password-input"
                            />
                            <button
                                onClick={handleResetPassword}
                                className="orangeCircularButton reset-button"
                                disabled={isLoading || !newPassword || !confirmPassword}
                            >
                                {isLoading ? "Resetting Password..." : "Reset Password"}
                            </button>
                        </div>
                    )}

                    <a
                        onClick={onSwitchToLogin}
                        className="link-button"
                        style={{ marginTop: '20px' }}
                    >
                        Back to Login
                    </a>
                </div>
            </div>
        </div>
    );
}

export default ResetPasswordWithCodePage;
// FORGOT_PASSWORD END