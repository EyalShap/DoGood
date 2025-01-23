import { useState } from 'react'
import './App.css'
import Volunteering from './components/Volunteering'
import Organization from './components/Organization'
import CreateVolunteering from './components/CreateVolunteering'
import OrganizationList from './components/OrganizationList'
import CreateOrganization from './components/CreateOrganization'
import { BrowserRouter, Routes, Route, useLocation } from 'react-router-dom';
import LoginPage from './components/LoginPage';
import CodeView from './components/CodeView';
import CodeScan from './components/CodeScan';
import HourApprovalRequestList from './components/HourApprovalRequestList';
import JoinRequestList from './components/JoinRequestList';
import ManagerRequestsList from './components/ManagerRequestsList'
import VolunteeringPostList from './components/VolunteeringPostList'
import VolunteeringPost from './components/VolunteeringPost'
import CreatePost from './components/CreatePost'
import ReportList from './components/ReportList'
import Homepage from './components/Homepage'
import RegisterPage from './components/RegisterPage'
import MakeAppointment from './components/MakeAppointment'
import VolunteeringSettings from './components/VolunteeringSettings'


function App() {

  return (
    <>
    <BrowserRouter>
      <Homepage />
      <Routes>
      <Route path="/" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />}/>
        <Route path='/reportList' element={<ReportList/>}/>
        <Route path='/volunteeringPostList' element={<VolunteeringPostList/>}/>
        <Route path='/managerRequestsList' element={<ManagerRequestsList/>}/>
        <Route path='/organizationList' element={<OrganizationList/>}/>
        <Route path='/createOrganization/:id' element={<CreateOrganization/>}/>
        <Route path='/volunteering/:id' element={<Volunteering/>}/>
        <Route path='/volunteering/:id/createVolunteeringPost/:postId' element={<CreatePost/>}/>
        <Route path='/organization/:id' element={<Organization/>}/>
        <Route path='/volunteering/:id/code' element={<CodeView/>}/>
        <Route path='/volunteering/:id/hrrequests' element={<HourApprovalRequestList/>}/>
        <Route path='/volunteering/:id/jrequests' element={<JoinRequestList/>}/>
        <Route path='/volunteering/:id/appointment' element={<MakeAppointment/>}/>
        <Route path='/volunteering/:id/settings' element={<VolunteeringSettings/>}/>
        <Route path='/scan' element={<CodeScan/>}/>
        <Route path='/organization/:id/createVolunteering' element={<CreateVolunteering/>}/>
        <Route path='/volunteeringPost/:id' element={<VolunteeringPost/>}/>
        
      </Routes>
    </BrowserRouter>
    </>
  )
}

export default App
