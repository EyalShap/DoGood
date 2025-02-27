import { useState } from "react";
import { useNavigate } from 'react-router-dom';
import { login } from "../api/user_api";

function LoginPage() {
    const navigate = useNavigate();
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");

    const onLogin = async () => {
        try {
            let token = await login(username, password);
            localStorage.setItem("username", username);
            localStorage.setItem("token", token);
            alert("Login successful!");
            navigate('/volunteeringPostList');
        } catch (e) {
            alert(e);
        }
    };

    return (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100vh', fontFamily: 'Arial, sans-serif' }}>
            <h1>Login</h1>
            <div style={{ display: 'flex', flexDirection: 'column', width: '300px' }}>
                <input
                    onKeyDown = {(e) =>{if (e.key === 'Enter') onLogin();} }
                    type="text" 
                    placeholder="Username" 
                    value={username} 
                    onChange={e => setUsername(e.target.value)} 
                    style={{ margin: '5px 0', padding: '10px', fontSize: '16px' }}
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
                    style={{ margin: '10px 0', padding: '10px', fontSize: '16px', backgroundColor: '#4CAF50', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer' }}
                >
                    Login
                </button>

                
                <a
                onClick={() => navigate('/register')} // Navigate to the register page
                style={{ margin: '10px 0', padding: '10px', fontSize: '16px',textDecoration:'underline', textAlign: 'center', color: 'black', border: 'none', borderRadius: '5px', cursor: 'pointer' }}
                >
                    Not registered yet?
            </a>
                
            </div>
        </div>
    );
}

export default LoginPage;
