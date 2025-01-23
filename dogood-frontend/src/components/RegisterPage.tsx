import { useState } from "react";
import { useNavigate } from 'react-router-dom';
import { register } from "../api/user_api";

function RegisterPage() {
    const navigate = useNavigate();
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [phone, setPhone] = useState("");
    const [birthDate, setBirthDate] = useState("");

    const onRegister = async () => {
        try {
            let date = new Date(birthDate);
            let string_date = date.toLocaleString();
            let string = new Date(string_date).toISOString();
            console.log(string); // this will return
            let token = await register(username, password, name, email, phone, string);
            sessionStorage.setItem("username", username);
            sessionStorage.setItem("token", token);
            alert("Registration successful!");
            navigate('/homepage');
        } catch (e) {
            alert(e);
        }
    };

    return (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100vh', fontFamily: 'Arial, sans-serif' }}>
            <h1>Register</h1>
            <div style={{ display: 'flex', flexDirection: 'column', width: '300px' }}>
                <input 
                    type="text" 
                    placeholder="Username" 
                    value={username} 
                    onChange={e => setUsername(e.target.value)} 
                    style={{ margin: '5px 0', padding: '10px', fontSize: '16px' }}
                />
                <input 
                    type="password" 
                    placeholder="Password" 
                    value={password} 
                    onChange={e => setPassword(e.target.value)} 
                    style={{ margin: '5px 0', padding: '10px', fontSize: '16px' }}
                />
                <input 
                    type="text" 
                    placeholder="Full Name" 
                    value={name} 
                    onChange={e => setName(e.target.value)} 
                    style={{ margin: '5px 0', padding: '10px', fontSize: '16px' }}
                />
                <input 
                    type="email" 
                    placeholder="Email" 
                    value={email} 
                    onChange={e => setEmail(e.target.value)} 
                    style={{ margin: '5px 0', padding: '10px', fontSize: '16px' }}
                />
                <input 
                    type="text" 
                    placeholder="Phone" 
                    value={phone} 
                    onChange={e => setPhone(e.target.value)} 
                    style={{ margin: '5px 0', padding: '10px', fontSize: '16px' }}
                />
                <input 
                    type="date" 
                    placeholder="Birth Date" 
                    value={birthDate} 
                    onChange={e => setBirthDate(e.target.value)} 
                    style={{ margin: '5px 0', padding: '10px', fontSize: '16px' }}
                />
                <button 
                    onClick={onRegister} 
                    style={{ margin: '10px 0', padding: '10px', fontSize: '16px', backgroundColor: '#4CAF50', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer' }}
                >
                    Register
                </button>
            </div>
        </div>
    );
}

export default RegisterPage;
