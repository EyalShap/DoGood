import { useState } from "react"

function TemporaryLoginMaker() {
    const [username, setUsername] = useState("")

    const login = () => {
        sessionStorage.setItem("username", username);
    }
  return (
    <div>
        <input value={username} onChange={e => setUsername(e.target.value)}/>
        <button onClick={login}>Login</button>
    </div>
  )
}

export default TemporaryLoginMaker