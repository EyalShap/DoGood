// src/components/LoginAndRegister.tsx
import { useState } from "react";
import RegisterPage from "./RegisterPage.tsx";
import LoginPage from "./LoginPage.tsx";
import ForgotPassword from "./ForgotPassword.tsx";
import VerifyEmailPage from "./VerifyEmailPage.tsx";
import ResetPasswordWithCodePage from "./ResetPasswordWithCodePage.tsx";

type AuthView = "login" | "register" | "forgotPassword" | "verifyEmail" | "resetPassword";

interface LoginAndRegisterProps {
    onAuthSuccess: (username: string, token: string) => void;
}

function LoginAndRegister({ onAuthSuccess }: LoginAndRegisterProps) {
    const [view, setView] = useState<AuthView>("login");
    const [usernameToVerify, setUsernameToVerify] = useState<string | null>(null);
    // FORGOT_PASSWORD_USERNAME_INPUT START
    // Store the username that initiated the forgot password flow
    const [usernameForPasswordReset, setUsernameForPasswordReset] = useState<string | null>(null);
    // FORGOT_PASSWORD_USERNAME_INPUT END
    // const [register, setRegister] = useState(false); // This state seems unused

    const switchToRegister = () => setView("register");
    const switchToLogin = () => {
        setUsernameToVerify(null);
        // FORGOT_PASSWORD_USERNAME_INPUT START
        setUsernameForPasswordReset(null); // Clear username for reset
        // FORGOT_PASSWORD_USERNAME_INPUT END
        setView("login");
    };
    const switchToForgotPassword = () => setView("forgotPassword");
    
    const switchToVerifyEmail = (username: string) => {
        setUsernameToVerify(username);
        setView("verifyEmail");
    };

    // FORGOT_PASSWORD_USERNAME_INPUT START
    // This handler is called when ForgotPasswordPage successfully submits a username
    const handleForgotPasswordUsernameSubmitted = (username: string) => {
        setUsernameForPasswordReset(username); // Store the username
        setView("resetPassword"); 
    };
    // FORGOT_PASSWORD_USERNAME_INPUT END


    return (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '100vh', width: '100%', fontFamily: 'Arial, sans-serif' }}>
            {view === "login" && (
                <LoginPage
                    onSwitchToRegister={switchToRegister}
                    onSwitchToForgot={switchToForgotPassword} 
                    onAuthSuccess={onAuthSuccess}
                    // changeState={setRegister} // This prop seems unused in LoginPage
                />
            )}
            {view === "register" && (
                 <RegisterPage
                    onSwitchToLogin={switchToLogin}
                    onAuthSuccess={onAuthSuccess} // Though not called directly on success from here
                    onSwitchToVerifyEmail={switchToVerifyEmail}
                 />
             )}
            {view === "forgotPassword" && (
                <ForgotPassword
                    onSwitchToLogin={switchToLogin}
                    // FORGOT_PASSWORD_USERNAME_INPUT START
                    onUsernameSubmitted={handleForgotPasswordUsernameSubmitted} // Updated prop name
                    // FORGOT_PASSWORD_USERNAME_INPUT END
                />
            )}
            {/* FORGOT_PASSWORD_USERNAME_INPUT START */}
            {/* Pass usernameForPasswordReset to ResetPasswordWithCodePage */}
            {/* The 'email' prop for ResetPasswordWithCodePage is now effectively the username's email,
                but ResetPasswordWithCodePage itself will use the username for API calls.
                We need to pass the username to ResetPasswordWithCodePage.
                The backend will send the code to the email associated with usernameForPasswordReset.
                ResetPasswordWithCodePage needs the *username* for resending and verifying.
                Let's rename the prop for ResetPasswordWithCodePage to `usernameForReset` for clarity.
            */}
            {view === "resetPassword" && usernameForPasswordReset && (
                <ResetPasswordWithCodePage
                    // The `email` prop here is a bit of a misnomer now if the page internally
                    // uses the username for resend/verify. The key is that the code was sent
                    // to the email *associated* with `usernameForPasswordReset`.
                    // For clarity, let's assume ResetPasswordWithCodePage will use `usernameForPasswordReset`
                    // for its operations, and the `email` prop is just for display ("code sent to email of user X").
                    // The prompt for ResetPasswordWithCodePage was to use `email` prop for resend.
                    // Let's stick to the previous turn's ResetPasswordWithCodePage which expects `email`
                    // and internally uses it for resend/verify, assuming backend can map email back to user.
                    // This is confusing. If backend uses username, this should be username.
                                                    // If backend uses email for resend/verify, this should be the email.
                                                    // Given the backend UsersFacade.resendVerificationCode(username),
                                                    // ResetPasswordWithCodePage needs the username.
                                                    // Let's assume the backend /forgot-password returns the email
                                                    // to which the code was sent, or we pass the username.
                                                    // For now, let's assume ResetPasswordWithCodePage needs the *username*
                                                    // to call resendVerificationCode(username) and verifyPasswordResetCode(username, code)
                                                    // The `email` prop for ResetPasswordWithCodePage will be used for display only.
                                                    // The actual username is `usernameForPasswordReset`.
                                                    // Let's adjust ResetPasswordWithCodePage to take `usernameForReset`
                    usernameForReset={usernameForPasswordReset} // NEW PROP
                    onSwitchToLogin={switchToLogin} 
                />
            )}
            {/* FORGOT_PASSWORD_USERNAME_INPUT END */}
            {view === "verifyEmail" && usernameToVerify && (
                <VerifyEmailPage
                    username={usernameToVerify}
                    onSwitchToLogin={switchToLogin}
                />
            )}
        </div>
    );
}

export default LoginAndRegister;