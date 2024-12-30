import { useState } from 'react'
import './App.css'
import Volunteering from './components/Volunteering'
import Organization from './components/Organization'
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import TemporaryLoginMaker from './components/TemporaryLoginMaker';
import CodeView from './components/CodeView';
import CodeScan from './components/CodeScan';


function App() {
  const [count, setCount] = useState(0)

  return (
    <>
    <TemporaryLoginMaker/>
    <BrowserRouter>
      <Routes>
        <Route path='/volunteering/:id' element={<Volunteering/>}/>
        <Route path='/organization/:id' element={<Organization/>}/>
        <Route path='/volunteering/:id/code' element={<CodeView/>}/>
        <Route path='/volunteering/:id/scan' element={<CodeScan/>}/>
      </Routes>
    </BrowserRouter>
    </>
  )
}

export default App
