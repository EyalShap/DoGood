// src/components/LoginAndRegister.tsx
import { useState } from "react";
import RegisterPage from "./RegisterPage.tsx";
import LoginPage from "./LoginPage.tsx";
import ForgotPassword from "./ForgotPassword.tsx";
import VerifyEmailPage from "./VerifyEmailPage.tsx";
// FORGOT_PASSWORD START
import ResetPasswordWithCodePage from "./ResetPasswordWithCodePage.tsx";
// FORGOT_PASSWORD END

// Define the possible views
// FORGOT_PASSWORD START
type AuthView = "login" | "register" | "forgotPassword" | "verifyEmail" | "resetPassword";
// FORGOT_PASSWORD END

interface LoginAndRegisterProps {
    onAuthSuccess: (username: string, token: string) => void;
}

function LoginAndRegister({ onAuthSuccess }: LoginAndRegisterProps) {
    const [view, setView] = useState<AuthView>("login");
    const [usernameToVerify, setUsernameToVerify] = useState<string | null>(null);
    // FORGOT_PASSWORD START
    const [emailForPasswordReset, setEmailForPasswordReset] = useState<string | null>(null);
    // FORGOT_PASSWORD END

    const switchToRegister = () => setView("register");
    const switchToLogin = () => {
        setUsernameToVerify(null);
        // FORGOT_PASSWORD START
        setEmailForPasswordReset(null);
        // FORGOT_PASSWORD END
        setView("login");
    };
    // FORGOT_PASSWORD START
    const switchToForgotPassword = () => setView("forgotPassword");
    // FORGOT_PASSWORD END
    const switchToVerifyEmail = (username: string) => {
        setUsernameToVerify(username);
        setView("verifyEmail");
    };
    // FORGOT_PASSWORD START
    const handleForgotPasswordEmailSubmitted = (email: string) => {
        setEmailForPasswordReset(email);
        setView("resetPassword"); 
    };
    // FORGOT_PASSWORD END


    return (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '100vh', width: '100%', fontFamily: 'Arial, sans-serif' }}>
            {view === "login" && (
                <LoginPage
                    onSwitchToRegister={switchToRegister}
                    onSwitchToForgot={switchToForgotPassword} 
                    onAuthSuccess={onAuthSuccess}
                />
            )}
            {view === "register" && (
                 <RegisterPage
                    onSwitchToLogin={switchToLogin}
                    onAuthSuccess={onAuthSuccess}
                    onSwitchToVerifyEmail={switchToVerifyEmail}
                 />
             )}
            {/* FORGOT_PASSWORD START */}
            {view === "forgotPassword" && (
                <ForgotPassword
                    onSwitchToLogin={switchToLogin}
                    onEmailSubmitted={handleForgotPasswordEmailSubmitted} 
                />
            )}
            {view === "resetPassword" && emailForPasswordReset && (
                <ResetPasswordWithCodePage
                    email={emailForPasswordReset} // Pass the email for which the code was sent
                    onSwitchToLogin={switchToLogin} 
                />
            )}
            {/* FORGOT_PASSWORD END */}
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