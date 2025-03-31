import { useState } from "react";
import { useNavigate } from 'react-router-dom';
import { register } from "../api/user_api";

function RegisterPage({ changeState } : { changeState:  React.Dispatch<React.SetStateAction<boolean>>}) {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [phone, setPhone] = useState("");
    const [birthDate, setBirthDate] = useState("");

    const onRegister = async () => {
        try {
            let token = await register(username, password, name, email, phone, birthDate);
            localStorage.setItem("username", username);
            localStorage.setItem("token", token);
            changeState(false);
        } catch (e) {
            alert(e);
        }
    };

    return (
        <div>
            <link
            href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap"
            rel="stylesheet"
            />
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100vh', fontFamily: 'Montserrat, sans-serif' }}>
            <h1>Register</h1>
            <div style={{ display: 'flex', flexDirection: 'column', width: '300px' }}>
                <input 
                    type="text" 
                    placeholder="Username" 
                    value={username} 
                    onChange={e => setUsername(e.target.value)} 
                    style={{ margin: '5px 0', padding: '10px', fontSize: '16px', fontFamily: 'Montserrat, sans-serif' }}
                />
                <input 
                    type="password" 
                    placeholder="Password" 
                    value={password} 
                    onChange={e => setPassword(e.target.value)} 
                    style={{ margin: '5px 0', padding: '10px', fontSize: '16px', fontFamily: 'Montserrat, sans-serif' }}
                />
                <input 
                    type="text" 
                    placeholder="Full Name" 
                    value={name} 
                    onChange={e => setName(e.target.value)} 
                    style={{ margin: '5px 0', padding: '10px', fontSize: '16px', fontFamily: 'Montserrat, sans-serif' }}
                />
                <input 
                    type="email" 
                    placeholder="Email" 
                    value={email} 
                    onChange={e => setEmail(e.target.value)} 
                    style={{ margin: '5px 0', padding: '10px', fontSize: '16px', fontFamily: 'Montserrat, sans-serif' }}
                />
                <input 
                    type="text" 
                    placeholder="Phone" 
                    value={phone} 
                    onChange={e => setPhone(e.target.value)} 
                    style={{ margin: '5px 0', padding: '10px', fontSize: '16px', fontFamily: 'Montserrat, sans-serif' }}
                />
                <input 
                    type="date" 
                    placeholder="Birth Date" 
                    value={birthDate} 
                    onChange={e => setBirthDate(e.target.value)} 
                    style={{ margin: '5px 0', padding: '10px', fontSize: '16px', fontFamily: 'Montserrat, sans-serif' }}
                />
                <button 
                    onClick={onRegister} 
                    className="orangeCircularButton"
                    style={{ margin: '10px 0', padding: '10px', fontSize: '16px', border: 'none', cursor: 'pointer' }}
                >
                Register
                </button>
                <a
                onClick={() => changeState(false)} // Navigate to the login page
                style={{ margin: '10px 0', padding: '10px', fontSize: '16px',textDecoration:'underline', textAlign: 'center', color: 'black', border: 'none', borderRadius: '5px', cursor: 'pointer', fontFamily: 'Montserrat, sans-serif' }}
                >
                    Already have an account?
            </a>
            </div>
            </div>
        </div>
    );
}

export default RegisterPage;
