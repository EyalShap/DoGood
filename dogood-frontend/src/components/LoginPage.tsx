// src/components/LoginPage.tsx
import { useState } from "react";
import { useNavigate } from 'react-router-dom';
import {login, registerFcmToken} from "../api/user_api";
import "../css/LoginPage.css"
import {requestForToken} from "../api/firebase/firebase.ts";

interface LoginPageProps {
    onSwitchToRegister: () => void;
    // FORGOT_PASSWORD START
    onSwitchToForgot: () => void; 
    // FORGOT_PASSWORD END
    onAuthSuccess: (username: string, token: string) => void;
}

// FORGOT_PASSWORD START
function LoginPage({ onSwitchToRegister, onSwitchToForgot, onAuthSuccess }: LoginPageProps) {
// FORGOT_PASSWORD END
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const isMobile = window.innerWidth <= 768;

    const handleLogin = async () => {
        setIsLoading(true);
        setError(null);
        try {
            let token = await login(username, password);
            onAuthSuccess(username, token);
            localStorage.setItem("username", username);
            localStorage.setItem("token", token);
            let fcmToken = await requestForToken();
            if(fcmToken){
                await registerFcmToken(username,fcmToken);
            }
            window.dispatchEvent(new Event('login'))
        } catch (e: any) {
            console.error("Login failed:", e);
            setError(e.message || "Login failed. Please check your credentials.");
            //alert(e);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="back">
        <div className = "loginPage"> 
            <link
            href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap"
            rel="stylesheet"
            />
            <link
            href="https://fonts.googleapis.com/css2?family=Lobster&display=swap"
            rel="stylesheet"
            />
        <div className="image">
            <h2 className="bigHeader welcome">Welcome To DoGood</h2>
        </div>
        <div className="loginSection">
            <h1 className="mobileLogin">{isMobile ? "Welcome To DoGood" : "Login"}</h1>
            <div className = 'fields' >
                        <input
                            onKeyDown={(e) => { if (e.key === 'Enter') handleLogin(); }}
                            type="text"
                            placeholder="Username"
                            value={username}
                            onChange={e => setUsername(e.target.value)}
                            style={{ margin: '5px 0', padding: '10px', fontSize: '16px', fontFamily: 'Montserrat, sans-serif' }}
                        />
                        <input
                            onKeyDown={(e) => { if (e.key === 'Enter') handleLogin(); }}
                            type="password"
                            placeholder="Password"
                            value={password}
                            onChange={e => setPassword(e.target.value)}
                            style={{ margin: '5px 0', padding: '10px', fontSize: '16px' }}
                        />
                <button
                    onClick={handleLogin} 
                    className="orangeCircularButton"
                    disabled={isLoading}
                    style={{ margin: '10px 0', padding: '10px', fontSize: '16px', border: 'none', cursor: 'pointer' }}
                >
                    Login
                </button>
                <a
                onClick={onSwitchToForgot} // Navigate to the register page
                style={{ margin: '10px 0', padding: '10px', fontSize: '16px',textDecoration:'underline', textAlign: 'center', color: 'black', border: 'none', borderRadius: '5px', cursor: 'pointer' }}
                >
                    Forgot Password?
            </a>

                <a
                onClick={onSwitchToRegister} // Navigate to the register page
                style={{ margin: '10px 0', padding: '10px', fontSize: '16px',textDecoration:'underline', textAlign: 'center', color: 'black', border: 'none', borderRadius: '5px', cursor: 'pointer' }}
                >
                    Not registered yet?
            </a>

            </div>
            </div>
        </div>
        </div>
    );
}

export default LoginPage;