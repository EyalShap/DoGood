import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'
import Volunteering from './components/Volunteering'
import Organization from './components/Organization'

function App() {
  const [count, setCount] = useState(0)

  return (
    <>
    <Volunteering/>
    </>
  )
}

export default App
