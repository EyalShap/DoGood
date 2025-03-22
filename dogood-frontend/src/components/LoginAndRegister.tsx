import {useState} from "react";
import RegisterPage from "./RegisterPage.tsx";
import LoginPage from "./LoginPage.tsx";

function LoginAndRegister() {
    const [register, setRegister] = useState(false);

    return (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100vh', fontFamily: 'Arial, sans-serif' }}>
            {register ? <RegisterPage changeState={setRegister}/> : <LoginPage changeState={setRegister}/>}
        </div>
    );
}

export default LoginAndRegister;
