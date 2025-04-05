import { useState } from "react";
import { useNavigate } from 'react-router-dom';
import { login } from "../api/user_api";
import "../css/LoginPage.css"

function LoginPage({ changeState } : { changeState:  React.Dispatch<React.SetStateAction<boolean>>}) {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const isMobile = window.innerWidth <= 768;

    const onLogin = async () => {
        try {
            let token = await login(username, password);
            localStorage.setItem("username", username);
            localStorage.setItem("token", token);
            window.dispatchEvent(new Event('login'))
        } catch (e) {
            alert(e);
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
                    onKeyDown = {(e) =>{if (e.key === 'Enter') onLogin();} }
                    type="text" 
                    placeholder="Username" 
                    value={username} 
                    onChange={e => setUsername(e.target.value)} 
                    style={{ margin: '5px 0', padding: '10px', fontSize: '16px', fontFamily: 'Montserrat, sans-serif' }}
                />
                <input 
                    onKeyDown = {(e) =>{if (e.key === 'Enter') onLogin();} }
                    type="password" 
                    placeholder="Password" 
                    value={password} 
                    onChange={e => setPassword(e.target.value)} 
                    style={{ margin: '5px 0', padding: '10px', fontSize: '16px' }}
                />
                <button
                    onClick={onLogin} 
                    className="orangeCircularButton"
                    style={{ margin: '10px 0', padding: '10px', fontSize: '16px', border: 'none', cursor: 'pointer' }}
                >
                    Login
                </button>

                
                <a
                onClick={() => changeState(true)} // Navigate to the register page
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
