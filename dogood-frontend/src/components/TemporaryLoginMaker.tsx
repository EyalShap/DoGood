import { useState } from "react"
import { useNavigate } from 'react-router-dom';
import { login } from "../api/user_api";

function TemporaryLoginMaker() {
    const navigate = useNavigate();
    const [username, setUsername] = useState("")
    const [password, setPassword] = useState("")

    const onLogin = async () => {
      try{
        let token = await login(username, password);
        sessionStorage.setItem("username", username);
        sessionStorage.setItem("token", token);
        navigate('/homepage');
      }catch(e){
        alert(e)
      }
    }
  return (
    <div>
        <input value={username} onChange={e => setUsername(e.target.value)}/>
        <input value={password} onChange={e => setPassword(e.target.value)}/>
        <button onClick={onLogin}>Login</button>
    </div>
  )
}

export default TemporaryLoginMaker