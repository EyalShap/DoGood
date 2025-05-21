// src/components/ResetPasswordWithCodePage.tsx
import { useState, useEffect } from "react";
import { resetPassword, verifyPasswordResetCode, resendVerificationCode, VerifyPasswordResetCodeResponse } from "../api/user_api";
import "../css/ResetPasswordWithCodePage.css";

interface ResetPasswordWithCodePageProps {
    // email: string; // Email to display to the user (where the code was sent)
    usernameForReset: string; // The username that initiated the forgot password
    onSwitchToLogin: () => void;
}

function ResetPasswordWithCodePage({ usernameForReset, onSwitchToLogin }: ResetPasswordWithCodePageProps) {
    const [code, setCode] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);
    
    const [formStep, setFormStep] = useState<"enterCode" | "enterNewPassword">("enterCode");
    // verifiedUsername will be the same as usernameForReset if the code is valid for it.
    // No, verifyPasswordResetCode (if it takes username) will confirm this username.
    // If verifyPasswordResetCode takes email, it should return the username.
    // Let's assume verifyPasswordResetCode now takes username.

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
        if (!usernameForReset) { 
            setResendError("Username is missing. Cannot resend code.");
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
            const responseMessage = await resendVerificationCode(usernameForReset); 
            setResendSuccess(responseMessage);
        } catch (e: any) {
            setResendError(e.message || e.toString() || "Failed to resend verification code.");
            setResendDisabled(false);
            setCountdown(0);
        } finally {
            setIsResending(false);
        }
    };

    const handleVerifyCode = async () => {
        if (!usernameForReset) {
            setError("Username is missing for code verification.");
            return;
        }
        if (!code || code.length !== 6) {
            setError("Please enter a valid 6-digit reset code.");
            return;
        }
        setIsLoading(true);
        setError(null);
        setSuccessMessage(null);
        setResendError(null); 
        setResendSuccess(null);
        try {
            // verifyPasswordResetCode API takes username and code.
            // The backend UsersFacade.verifyPasswordResetCode(username, code)
            // will find the user by username, get their email, then validate the code from cache.
            const verificationResponse: string = await verifyPasswordResetCode(usernameForReset, code); 
            
            // The backend's verifyPasswordResetCode in UsersFacade returns a boolean.
            // The UserAPI's verifyPasswordResetCode returns a string "Verification code is valid." or error.
            // Let's assume the API wrapper `verifyPasswordResetCode` in `user_api.ts`
            // is updated to return an object like { message: string, username?: string }
            // where `username` is confirmed if the code is valid.
            // For now, we'll assume the string response indicates validity.
            if (verificationResponse.toLowerCase().includes("valid")) {
                // setVerifiedUsername(usernameForReset); // We already have usernameForReset
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
        // We use usernameForReset as the verified username for this flow
        if (!usernameForReset) { 
            setError("Username context lost. Please start over.");
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
        setSuccessMessage(null);
        setResendError(null); 
        setResendSuccess(null);

        try {
            const resetResponse = await resetPassword(usernameForReset, code, newPassword); 
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
                        {/* Displaying the username for context */}
                        A 6-digit code was sent to the email associated with username: <strong>{usernameForReset}</strong>.
                    </p>
                    
                    {error && <p className="error-message">{error}</p>}
                    {successMessage && <p className="success-message">{successMessage}</p>}
                    {resendError && <p className="error-message" style={{marginTop: '10px'}}>{resendError}</p>}
                    {resendSuccess && <p className="success-message" style={{marginTop: '10px'}}>{resendSuccess}</p>}

                    {formStep === "enterCode" && !(successMessage && successMessage.includes("Redirecting to login...")) && (
                        <div className="fields">
                             <p style={{ marginBottom: '10px', color: '#555' }}>
                                Please enter the 6-digit code.
                            </p>
                            <input
                                type="text"
                                placeholder="Enter 6-digit reset code"
                                value={code}
                                onChange={(e) => setCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
                                maxLength={6}
                                disabled={isLoading || isResending}
                                className="code-input"
                                onKeyDown={(e) => { if (e.key === 'Enter' && !isLoading && code.length === 6) handleVerifyCode();}}
                            />
                            <button
                                onClick={handleVerifyCode}
                                className="orangeCircularButton reset-button"
                                disabled={isLoading || isResending || !code || !usernameForReset} 
                            >
                                {isLoading ? "Verifying Code..." : "Verify Code"}
                            </button>
                            <div className="resend-section">
                                <button
                                    onClick={handleResendCode}
                                    className="link-button"
                                    disabled={isResending || resendDisabled || !usernameForReset || (!!successMessage && successMessage.includes("Redirecting"))}
                                >
                                    {isResending ? "Sending..." : (resendDisabled ? `Resend Code (${countdown}s)` : "Resend Code")}
                                </button>
                            </div>
                        </div>
                    )}

                    {formStep === "enterNewPassword" && !(successMessage && successMessage.includes("Redirecting to login...")) && (
                        <div className="fields">
                             <p style={{ marginBottom: '10px', color: '#555' }}>
                                {successMessage && successMessage.includes("Code verified") ? successMessage : `Enter new password for user ${usernameForReset}.`}
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
                                onKeyDown={(e) => { if (e.key === 'Enter' && !isLoading && newPassword && confirmPassword) handleResetPassword();}}
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