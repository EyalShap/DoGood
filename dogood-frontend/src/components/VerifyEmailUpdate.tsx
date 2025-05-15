// src/components/VerifyEmailUpdate.tsx (New File)
// UPDATE-EMAIL-VERIFICATION START
import { useState, useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { verifyEmailUpdateCode, requestEmailUpdateVerification, updateUserFields } from "../api/user_api";
import "../css/VerifyEmailPage.css"; // Reuse existing styles if applicable

function VerifyEmailUpdate() {
    const navigate = useNavigate();
    const location = useLocation();

    const {
        originalEmail,      // The email the code was sent to (user's current verified email)
        pendingUpdateData,  // { name, phone, newEmail, newPassword }
        username            // The username of the user being updated
    } = location.state || {};

    const [code, setCode] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);
    const [resendDisabled, setResendDisabled] = useState(false);
    const [countdown, setCountdown] = useState(0);

    useEffect(() => {
        if (!originalEmail || !pendingUpdateData || !username) {
            setError("Required information missing. Please return to your profile and try again.");
            // Consider navigating back to /my-profile or /login after a delay
            setTimeout(() => navigate('/my-profile', { replace: true }), 3000);
        }
    }, [originalEmail, pendingUpdateData, username, navigate]);

    useEffect(() => {
        let timer: NodeJS.Timeout;
        if (countdown > 0) {
            timer = setTimeout(() => setCountdown(countdown - 1), 1000);
        } else {
            setResendDisabled(false);
        }
        return () => clearTimeout(timer);
    }, [countdown]);

    const handleVerifyAndUpdate = async () => {
        if (code.length !== 6) {
            setError("Verification code must be 6 digits.");
            return;
        }
        setIsLoading(true);
        setError(null);
        setSuccessMessage(null);

        try {
            // Step 1: Verify the code against the original email
            await verifyEmailUpdateCode(originalEmail, code);
            setSuccessMessage("Code verified. Updating profile...");

            // Step 2: If code is valid, proceed to update the user's profile with pending data
            await updateUserFields(
                username,
                pendingUpdateData.newPassword,  // This can be null if password wasn't changed
                [pendingUpdateData.newEmail], // The new email address
                pendingUpdateData.name,
                pendingUpdateData.phone
                // No need to pass verificationCode and emailCodeWasSentTo here if
                // the backend's updateUserFields doesn't specifically require them
                // after a separate verify-update-code step.
                // However, if your updateUserFields *is* enhanced to take them for final check, include them:
                // code,
                // originalEmail
            );

            setSuccessMessage("Profile updated successfully! Redirecting to your profile...");
            setTimeout(() => {
                navigate('/my-profile');
            }, 2500);

        } catch (e: any) {
            setError(e.message || e.toString() || "Verification or update failed. Please try again.");
        } finally {
            setIsLoading(false);
        }
    };

    const handleResendCode = async () => {
        setIsLoading(true);
        setError(null);
        setResendDisabled(true);
        setCountdown(60);
        try {
            await requestEmailUpdateVerification(originalEmail);
            setSuccessMessage("A new verification code has been sent to " + originalEmail);
        } catch (e: any) {
            setError(e.message || e.toString() || "Failed to resend code.");
            setResendDisabled(false); // Allow retry if sending failed
            setCountdown(0);
        } finally {
            setIsLoading(false);
        }
    };
    
    if (!originalEmail || !pendingUpdateData || !username) {
        return (
            <div className="verify-email-back">
                <div className="verify-email-page">
                    <p>Loading or invalid access...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="verify-email-back">
            <div className="verify-email-page">
                <link
                    href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap"
                    rel="stylesheet"
                />
                <div className="verify-email-section">
                    <h1>Verify Email Change</h1>
                    <p style={{ marginBottom: '20px', color: '#555' }}>
                        A 6-digit verification code was sent to your email: <strong>{originalEmail}</strong>.
                        Please enter it below to confirm your profile changes.
                    </p>
                    {pendingUpdateData.newEmail !== originalEmail && (
                        <p style={{ marginBottom: '20px', color: '#555' }}>
                            You are attempting to change your email to: <strong>{pendingUpdateData.newEmail}</strong>.
                        </p>
                    )}
                     {pendingUpdateData.newPassword && (
                        <p style={{ marginBottom: '20px', color: '#555' }}>
                            Your password will also be updated.
                        </p>
                    )}


                    {error && <p className="error-message">{error}</p>}
                    {successMessage && !successMessage.includes("Redirecting") && <p className="success-message">{successMessage}</p>}
                    {successMessage && successMessage.includes("Redirecting") && <p className="success-message">{successMessage}</p>}


                    <div className="fields">
                        <input
                            type="text"
                            placeholder="Enter 6-digit code"
                            value={code}
                            onChange={(e) => setCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
                            maxLength={6}
                            disabled={isLoading || (!!successMessage && successMessage.includes("Redirecting"))}
                            className="code-input"
                            onKeyDown={(e) => { if (e.key === 'Enter' && !isLoading && code.length === 6) handleVerifyAndUpdate(); }}
                        />
                        <button
                            onClick={handleVerifyAndUpdate}
                            className="orangeCircularButton verify-button"
                            disabled={isLoading || code.length !== 6 || (!!successMessage && successMessage.includes("Redirecting"))}
                        >
                            {isLoading ? "Verifying & Updating..." : "Verify & Update Profile"}
                        </button>

                        <div className="resend-section">
                            <button
                                onClick={handleResendCode}
                                className="link-button"
                                disabled={isLoading || resendDisabled || (!!successMessage && successMessage.includes("Redirecting"))}
                            >
                                {resendDisabled ? `Resend Code (${countdown}s)` : "Resend Code"}
                            </button>
                        </div>

                        <a
                            onClick={() => navigate('/my-profile')}
                            className="link-button"
                            style={{ marginTop: '20px' }}
                        >
                            Cancel and Back to Profile
                        </a>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default VerifyEmailUpdate;
// UPDATE-EMAIL-VERIFICATION END