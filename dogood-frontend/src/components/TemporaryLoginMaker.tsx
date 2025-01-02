import { useState } from "react"
import { useNavigate } from 'react-router-dom';

function TemporaryLoginMaker() {
    const navigate = useNavigate();
    const [username, setUsername] = useState("")

    const login = () => {
        sessionStorage.setItem("username", username);
        navigate('/homepage');
    }
  return (
    <div>
        <input value={username} onChange={e => setUsername(e.target.value)}/>
        <button onClick={login}>Login</button>
    </div>
  )
}

export default TemporaryLoginMaker