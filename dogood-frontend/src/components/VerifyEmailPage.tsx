// src/components/VerifyEmailPage.tsx
// VERIFICATION START
import { useState } from "react";
import { verifyEmailCode } from "../api/user_api"; // resendVerificationCode is removed
import "../css/VerifyEmailPage.css";

interface VerifyEmailPageProps {
    username: string;
    onSwitchToLogin: () => void;
}

function VerifyEmailPage({ username, onSwitchToLogin }: VerifyEmailPageProps) {
    const [code, setCode] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);
    // Removed resendDisabled and countdown states

    const handleVerify = async () => {
        if (code.length !== 6) {
            setError("Verification code must be 6 digits.");
            return;
        }
        setIsLoading(true);
        setError(null);
        setSuccessMessage(null);
        try {
            const responseMessage = await verifyEmailCode(username, code);
            // Backend now directly returns success messages like "Email verified successfully." or "Email already verified."
            // Or throws an error with message like "Invalid or expired verification code."
            setSuccessMessage(responseMessage + " Redirecting to login...");
            setTimeout(() => {
                onSwitchToLogin();
            }, 2000);
        } catch (e: any) {
            // Error messages from backend (like "Invalid or expired verification code.") will be caught here
            setError(e || "Verification failed. Please try again or contact support if the issue persists.");
        } finally {
            setIsLoading(false);
        }
    };

    // Removed handleResendCode function

    return (
        <div className="verify-email-back">
            <div className="verify-email-page">
                <link
                    href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap"
                    rel="stylesheet"
                />
                <div className="verify-email-section">
                    <h1>Verify Your Email</h1>
                    <p style={{ marginBottom: '20px', color: '#555' }}>
                        A 6-digit verification code has been sent to your email address (check your spam folder if needed).
                        Please enter it below to complete your registration for user: <strong>{username}</strong>.
                    </p>

                    {error && <p className="error-message">{error}</p>}
                    {successMessage && <p className="success-message">{successMessage}</p>}

                    <div className="fields">
                        <input
                            type="text"
                            placeholder="Enter 6-digit code"
                            value={code}
                            onChange={(e) => setCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
                            maxLength={6}
                            disabled={isLoading || !!successMessage}
                            className="code-input"
                            onKeyDown={(e) => { if (e.key === 'Enter') handleVerify(); }}
                        />
                        <button
                            onClick={handleVerify}
                            className="orangeCircularButton verify-button"
                            disabled={isLoading || code.length !== 6 || !!successMessage}
                        >
                            {isLoading ? "Verifying..." : "Verify Email"}
                        </button>

                        {/* Resend section removed */}

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
        </div>
    );
}

export default VerifyEmailPage;
// VERIFICATION END