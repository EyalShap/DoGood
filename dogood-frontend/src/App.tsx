import { useState } from 'react'
import './App.css'
import Volunteering from './components/Volunteering'
import Organization from './components/Organization'
import CreateVolunteering from './components/CreateVolunteering'
import OrganizationList from './components/OrganizationList'
import CreateOrganization from './components/CreateOrganization'
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import TemporaryLoginMaker from './components/TemporaryLoginMaker';
import ManagerRequestsList from './components/ManagerRequestsList'


function App() {
  const [count, setCount] = useState(0)

  return (
    <>
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<TemporaryLoginMaker />} />
        <Route path='/managerRequestsList' element={<ManagerRequestsList/>}/>
        <Route path='/organizationList' element={<OrganizationList/>}/>
        <Route path='/createOrganization/:id' element={<CreateOrganization/>}/>
        <Route path='/volunteering/:id' element={<Volunteering/>}/>
        <Route path='/organization/:id' element={<Organization/>}/>
        <Route path='/organization/:id/createVolunteering' element={<CreateVolunteering/>}/>
        
      </Routes>
    </BrowserRouter>
    </>
  )
}

export default App
