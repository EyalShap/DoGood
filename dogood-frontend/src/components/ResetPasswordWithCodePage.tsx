// FORGOT_PASSWORD START
// src/components/ResetPasswordWithCodePage.tsx
import { useState, useEffect } from "react";
import { resetPassword, verifyPasswordResetCode, resendVerificationCode } from "../api/user_api";
import "../css/ResetPasswordWithCodePage.css";

interface ResetPasswordWithCodePageProps {
    email: string; // Email for which the reset was initiated
    onSwitchToLogin: () => void;
}

function ResetPasswordWithCodePage({ email, onSwitchToLogin }: ResetPasswordWithCodePageProps) {
    const [username, setUsername] = useState("");
    const [code, setCode] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);
    
    const [formStep, setFormStep] = useState<"enterCode" | "enterNewPassword">("enterCode");
    const [verifiedUsername, setVerifiedUsername] = useState<string | null>(null);
        // RESEND-VERIFICATION-CODE START
    const [isResending, setIsResending] = useState(false);
    const [resendError, setResendError] = useState<string | null>(null);
    const [resendSuccess, setResendSuccess] = useState<string | null>(null);
    const [resendDisabled, setResendDisabled] = useState(false);
    const [countdown, setCountdown] = useState(0);

    useEffect(() => {
        let timer: NodeJS.Timeout;
        if (countdown > 0) {
            timer = setTimeout(() => setCountdown(countdown - 1), 1000);
        } else {
            setResendDisabled(false);
        }
        return () => clearTimeout(timer);
    }, [countdown]);

    const handleResendCode = async () => {
        if (!username.trim()) { // Use the username entered by the user
            setResendError("Please enter your username to resend the code.");
            return;
        }
        setIsResending(true);
        setResendError(null);
        setResendSuccess(null);
        setError(null); 
        setSuccessMessage(null);
        setResendDisabled(true);
        setCountdown(60);

        try {
            // The backend's resendVerificationCode uses the username to find the user and their primary email
            const responseMessage = await resendVerificationCode(username);
            setResendSuccess(responseMessage);
        } catch (e: any) {
            setResendError(e.message || e.toString() || "Failed to resend verification code.");
            setResendDisabled(false);
            setCountdown(0);
        } finally {
            setIsResending(false);
        }
    };
    // RESEND-VERIFICATION-CODE END

    const handleVerifyCode = async () => {
        if (!username.trim()) {
            setError("Please enter your username.");
            return;
        }
        if (!code || code.length !== 6) {
            setError("Please enter a valid 6-digit reset code.");
            return;
        }
        setIsLoading(true);
        setError(null);
        setSuccessMessage(null);
        try {
            const verificationResponse = await verifyPasswordResetCode(username, code);
            if (verificationResponse.toLowerCase().includes("valid")) {
                setVerifiedUsername(username);
                setFormStep("enterNewPassword");
                setSuccessMessage("Code verified. Please enter your new password.");
                setError(null);
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
        if (!verifiedUsername) {
            setError("Username not verified. Please verify the code first.");
            setFormStep("enterCode");
            return;
        }
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
        // setSuccessMessage(null); // Keep the "Code verified" message or clear it. Let's clear for cleaner final message.
        setSuccessMessage(null);


        try {
            const resetResponse = await resetPassword(verifiedUsername, code, newPassword);
            if (resetResponse.toLowerCase().includes("success")) {
                setSuccessMessage("Password has been reset successfully! Redirecting to login...");
                setError(null);
                setTimeout(() => {
                    onSwitchToLogin();
                }, 2500);
            } else {
                setError(resetResponse || "Failed to reset password. The code might have expired or an error occurred.");
            }
        } catch (e: any) {
            setError(e.message || "An error occurred while resetting password.");
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
                        Password reset initiated for email: <strong>{email}</strong>.
                    </p>
                    
                    {error && <p className="error-message">{error}</p>}
                    {successMessage && <p className="success-message">{successMessage}</p>}
                    {/* RESEND-VERIFICATION-CODE START */}
                    {resendError && <p className="error-message" style={{marginTop: '10px'}}>{resendError}</p>}
                    {resendSuccess && <p className="success-message" style={{marginTop: '10px'}}>{resendSuccess}</p>}
                    {/* RESEND-VERIFICATION-CODE END */}

                    {/* Step 1: Enter Username and Code */}
                    {formStep === "enterCode" && !(successMessage && successMessage.includes("Redirecting to login...")) && (
                        <div className="fields">
                             <p style={{ marginBottom: '10px', color: '#555' }}>
                                Please enter your username and the 6-digit code sent to your email.
                            </p>
                            <input
                                type="text"
                                placeholder="Enter your username"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                disabled={isLoading}
                                className="password-input" 
                            />
                            <input
                                type="text"
                                placeholder="Enter 6-digit reset code"
                                value={code}
                                onChange={(e) => setCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
                                maxLength={6}
                                disabled={isLoading}
                                className="code-input"
                                onKeyDown={(e) => { if (e.key === 'Enter') handleVerifyCode();}}
                            />
                            <button
                                onClick={handleVerifyCode}
                                className="orangeCircularButton reset-button"
                                disabled={isLoading || !code || !username}
                            >
                                {isLoading ? "Verifying Code..." : "Verify Code"}
                            </button>
                                                        {/* RESEND-VERIFICATION-CODE START */}
                            <div className="resend-section">
                                <button
                                    onClick={handleResendCode}
                                    className="link-button"
                                    disabled={isResending || resendDisabled || !username.trim() || (!!successMessage && successMessage.includes("Redirecting"))}
                                >
                                    {isResending ? "Sending..." : (resendDisabled ? `Resend Code (${countdown}s)` : "Resend Code")}
                                </button>
                            </div>
                            {/* RESEND-VERIFICATION-CODE END */}
                        </div>
                    )}

                    {/* Step 2: Enter New Password */}
                    {formStep === "enterNewPassword" && !(successMessage && successMessage.includes("Redirecting to login...")) && (
                        <div className="fields">
                             <p style={{ marginBottom: '10px', color: '#555' }}>
                                {successMessage && successMessage.includes("Code verified") ? successMessage : `Enter new password for ${verifiedUsername}.`}
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
                                onKeyDown={(e) => { if (e.key === 'Enter') handleResetPassword();}}
                            />
                            <button
                                onClick={handleResetPassword}
                                className="orangeCircularButton reset-button"
                                disabled={isLoading || !newPassword || !confirmPassword}
                            >
                                {isLoading ? "Resetting Password..." : "Set New Password"}
                            </button>
                        </div>
                    )}

                    {/* Always show Back to Login unless final success message (redirecting) is shown */}
                    {!(successMessage && successMessage.includes("Redirecting to login...")) && (
                        <a
                            onClick={onSwitchToLogin}
                            className="link-button"
                            style={{ marginTop: '20px' }}
                        >
                            Back to Login
                        </a>
                    )}
                </div>
            </div>
        </div>
    );
}

export default ResetPasswordWithCodePage;
// FORGOT_PASSWORD END