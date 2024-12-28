import { useState } from 'react'
import './App.css'
import Volunteering from './components/Volunteering'
import Organization from './components/Organization'
import { BrowserRouter, Routes, Route } from 'react-router-dom';


function App() {
  const [count, setCount] = useState(0)

  return (
    <BrowserRouter>
      <Routes>
        <Route path='/volunteering/:id' element={<Volunteering/>}/>
        <Route path='/organization/:id' element={<Organization/>}/>
      </Routes>
    </BrowserRouter>
  )
}

export default App
