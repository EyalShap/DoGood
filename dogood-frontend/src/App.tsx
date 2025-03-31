import {useEffect, useState} from 'react'
import './App.css'
import Volunteering from './components/Volunteering'
import Organization from './components/Organization'
import CreateVolunteering from './components/CreateVolunteering'
import OrganizationList from './components/OrganizationList'
import CreateOrganization from './components/CreateOrganization'
import { BrowserRouter, Routes, Route, useLocation } from 'react-router-dom';
import CodeView from './components/CodeView';
import CodeScan from './components/CodeScan';
import HourApprovalRequestList from './components/HourApprovalRequestList';
import JoinRequestList from './components/JoinRequestList';
import ManagerRequestsList from './components/ManagerRequestsList'
import VolunteeringPostList from './components/VolunteeringPostList'
import VolunteeringPost from './components/VolunteeringPost'
import CreatePost from './components/CreatePost'
import ReportList from './components/ReportList.tsx'
import MyProfilePage from './components/MyProfilePage'
import MakeAppointment from './components/MakeAppointment'
import VolunteeringSettings from './components/VolunteeringSettings'
import ProfilePage from './components/ProfilePage'
import Homepage from './components/Homepage'
import LeaderboardMap from './components/LeaderboardMap'
import VolunteerPost from './components/VolunteerPost'
import VolunteeringChat from "./components/VolunteeringChat.tsx";
import UserModel from "./models/UserModel.ts";
import {getUserByToken} from "./api/user_api.ts";
import LoginAndRegister from "./components/LoginAndRegister.tsx";
import Header from "./components/Header.tsx";
import Footer from "./components/Footer.tsx";
import EasterEgg from './components/EasterEgg.tsx'
import MyVolunteerings from "./components/MyVolunteerings.tsx";
import VolunteerPostChat from "./components/VolunteerPostChat.tsx";


function App() {
  const [user, setUser] = useState<UserModel>();
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const loggedInCheck = async () => {
    if(localStorage.getItem("token") != null){
      try {
        let um: UserModel = await getUserByToken();
        localStorage.setItem("username", um.username);
        setUser(um)
        window.dispatchEvent(new Event('storage'))
      }catch (e){
        localStorage.removeItem("token");
        localStorage.removeItem("username");
        window.dispatchEvent(new Event('storage'))
        alert("Your session has expired, please log in again")
      }
    }
  }

  useEffect(() => {
    const handleStorage = () => {
      setIsLoggedIn(localStorage.getItem("token") !== null)
    }

    window.addEventListener('storage', handleStorage)
    loggedInCheck();
    return () => window.removeEventListener('storage', handleStorage)
  }, []);

  return (
    <>
      {!isLoggedIn ? <LoginAndRegister/> :
          <BrowserRouter>
            <Header user={user}/>
            <div className='Routes'>
            <Routes>
              <Route path="/" element={<Homepage />} />
              <Route path = "/my-profile" element={<MyProfilePage />}/>
              <Route path="/leaderboard" element={<LeaderboardMap />}/>
              <Route path='/profile/:id' element={<ProfilePage/>}/>
              <Route path='/reportList' element={<ReportList/>}/>
              <Route path='/volunteeringPostList' element={<VolunteeringPostList/>}/>
              <Route path='/managerRequestsList' element={<ManagerRequestsList/>}/>
              <Route path='/organizationList' element={<OrganizationList/>}/>
              <Route path='/createOrganization/:id' element={<CreateOrganization/>}/>
              <Route path='/myvolunteerings' element={<MyVolunteerings/>}/>
              <Route path='/volunteering/:id' element={<Volunteering/>}/>
              <Route path='/volunteering/:id/createVolunteeringPost/:postId' element={<CreatePost/>}/>
              <Route path='/createVolunteerPost/:postId' element={<CreatePost/>}/>
              <Route path='/organization/:id' element={<Organization/>}/>
              <Route path='/volunteering/:id/code' element={<CodeView/>}/>
              <Route path='/volunteering/:id/hrrequests' element={<HourApprovalRequestList/>}/>
              <Route path='/volunteering/:id/jrequests' element={<JoinRequestList/>}/>
              <Route path='/volunteering/:id/appointment' element={<MakeAppointment/>}/>
              <Route path='/volunteering/:id/settings' element={<VolunteeringSettings/>}/>
              <Route path='/volunteering/:id/chat' element={<VolunteeringChat/>}/>
              <Route path='/scan' element={<CodeScan/>}/>
              <Route path='/organization/:id/createVolunteering' element={<CreateVolunteering/>}/>
              <Route path='/volunteeringPost/:id' element={<VolunteeringPost/>}/>
              <Route path='/volunteerPost/:id' element={<VolunteerPost/>}/>
              <Route path='/volunteerPost/:id/chat' element={<VolunteerPostChat other={false}/>}/>
              <Route path='/volunteerPost/:id/chat/:username' element={<VolunteerPostChat other={true}/>}/>
              <Route path='/easterEgg' element={<EasterEgg/>}/>
            </Routes>
            </div>
            <Footer/>
          </BrowserRouter>}
    </>
  )
}

export default App
