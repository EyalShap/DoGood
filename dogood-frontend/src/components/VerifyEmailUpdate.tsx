// src/components/VerifyEmailUpdate.tsx
// RESEND-VERIFICATION-CODE START
import { useState, useEffect } from "react"; // useEffect already imported
import { useLocation, useNavigate } from "react-router-dom";
// Import resendVerificationCode, verifyEmailUpdateCode, updateUserFields
import { verifyEmailUpdateCode, resendVerificationCode, updateUserFields } from "../api/user_api"; 
// RESEND-VERIFICATION-CODE END
import "../css/VerifyEmailPage.css"; 

function VerifyEmailUpdate() {
    const navigate = useNavigate();
    const location = useLocation();

    const {
        originalEmail,      
        pendingUpdateData,  
        username            
    } = location.state || {};

    const [code, setCode] = useState("");
    const [isLoading, setIsLoading] = useState(false); // For main verify & update action
    const [error, setError] = useState<string | null>(null);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);

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
        if (!username) { // Username is needed to find the user and their primary email for resending
            setResendError("User information is missing, cannot resend code.");
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
            // Backend's resendVerificationCode(username) sends to the user's current primary email.
            // This is correct as the user is verifying ownership of their account via their existing email.
            const responseMessage = await resendVerificationCode(username);
            setResendSuccess(responseMessage + " (Sent to " + originalEmail + ")");
        } catch (e: any) {
            setResendError(e.message || e.toString() || "Failed to resend verification code.");
            setResendDisabled(false);
            setCountdown(0);
        } finally {
            setIsResending(false);
        }
    };
    // RESEND-VERIFICATION-CODE END

    useEffect(() => {
        if (!originalEmail || !pendingUpdateData || !username) {
            setError("Required information missing. Please return to your profile and try again.");
            setTimeout(() => navigate('/my-profile', { replace: true }), 3000);
        }
    }, [originalEmail, pendingUpdateData, username, navigate]);


    const handleVerifyAndUpdate = async () => {
        if (code.length !== 6) {
            setError("Verification code must be 6 digits.");
            return;
        }
        setIsLoading(true);
        setError(null);
        setSuccessMessage(null);
        setResendError(null); 
        setResendSuccess(null);

        try {
            // The code was sent to 'originalEmail'. The backend's verifyEmailUpdateCode
            // should verify against this 'originalEmail'.
            await verifyEmailUpdateCode(originalEmail, code); 
            setSuccessMessage("Code verified. Updating profile...");

            await updateUserFields(
                username,
                pendingUpdateData.newPassword,  
                [pendingUpdateData.newEmail], 
                pendingUpdateData.name,
                pendingUpdateData.phone
                // No need to pass code and originalEmail again to updateUserFields
                // if verifyEmailUpdateCode already consumed/validated it sufficiently for the backend.
                // If updateUserFields *does* need them for a final check, pass them:
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
    
    if (!originalEmail || !pendingUpdateData || !username) {
        return (
            <div className="verify-email-back"><div className="verify-email-page"><p>Loading or invalid access...</p></div></div>
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
                    <h1>Verify Profile Change</h1>
                    <p style={{ marginBottom: '5px', color: '#555' }}>
                        A 6-digit verification code was sent to your email: <strong>{originalEmail}</strong>.
                        Please enter it below to confirm your profile changes.
                    </p>
                    {pendingUpdateData.newEmail !== originalEmail && (
                        <p style={{ marginTop:'5px', marginBottom: '10px', color: '#555', fontSize: '0.9rem' }}>
                            (You are attempting to change your email to: <strong>{pendingUpdateData.newEmail}</strong>)
                        </p>
                    )}
                     {pendingUpdateData.newPassword && (
                        <p style={{ marginTop:'5px', marginBottom: '10px', color: '#555', fontSize: '0.9rem' }}>
                            (Your password will also be updated upon successful verification)
                        </p>
                    )}


                    {error && <p className="error-message">{error}</p>}
                    {successMessage && !successMessage.includes("Redirecting") && <p className="success-message">{successMessage}</p>}
                    {successMessage && successMessage.includes("Redirecting") && <p className="success-message">{successMessage}</p>}
                    {/* RESEND-VERIFICATION-CODE START */}
                    {resendError && <p className="error-message" style={{marginTop: '10px'}}>{resendError}</p>}
                    {resendSuccess && <p className="success-message" style={{marginTop: '10px'}}>{resendSuccess}</p>}
                    {/* RESEND-VERIFICATION-CODE END */}


                    <div className="fields">
                        <input
                            type="text"
                            placeholder="Enter 6-digit code"
                            value={code}
                            onChange={(e) => setCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
                            maxLength={6}
                            disabled={isLoading || isResending || (!!successMessage && successMessage.includes("Redirecting"))}
                            className="code-input"
                            onKeyDown={(e) => { if (e.key === 'Enter' && !isLoading && !isResending && code.length === 6) handleVerifyAndUpdate(); }}
                        />
                        <button
                            onClick={handleVerifyAndUpdate}
                            className="orangeCircularButton verify-button"
                            disabled={isLoading || isResending || code.length !== 6 || (!!successMessage && successMessage.includes("Redirecting"))}
                        >
                            {isLoading ? "Verifying & Updating..." : "Verify & Update Profile"}
                        </button>

                        {/* RESEND-VERIFICATION-CODE START */}
                        <div className="resend-section">
                            <button
                                onClick={handleResendCode}
                                className="link-button"
                                disabled={isResending || resendDisabled || (!!successMessage && successMessage.includes("Redirecting"))}
                            >
                                {isResending ? "Sending..." : (resendDisabled ? `Resend Code (${countdown}s)` : "Resend Code")}
                            </button>
                        </div>
                        {/* RESEND-VERIFICATION-CODE END */}

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