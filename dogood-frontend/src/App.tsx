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
  // --- CHANGE 1: Initialize isLoggedIn based on localStorage presence ---
  // Check if token *and* username exist on initial load to set a sensible default.
  // This helps prevent a brief flash of the login screen if already logged in.
  const [isLoggedIn, setIsLoggedIn] = useState(!!localStorage.getItem("token") && !!localStorage.getItem("username"));
  // --- CHANGE 1 END ---

    // --- Function: loggedInCheck ---
    // Purpose: Validates token stored in localStorage by calling the backend.
    // Used for: Initial page load, subsequent checks triggered by events.
    const loggedInCheck = async () => {
      const token = localStorage.getItem("token");
      const username = localStorage.getItem("username"); // Also get username

      // Only proceed if both token and username exist in localStorage
      if (token && username) {
          try {
              console.log("loggedInCheck: Validating token..."); // Debug
              // Attempt to validate the token and get user data
              let um: UserModel = await getUserByToken(); // Validate token
              console.log("loggedInCheck: Token valid, user:", um.username); // Debug

              // Optional but good practice: Check if token's username matches localStorage
              if (localStorage.getItem("username") !== um.username) {
                   console.warn("Username mismatch between localStorage and token. Updating localStorage.");
                   localStorage.setItem("username", um.username); // Correct localStorage if needed
              }

              // If successful, update user state and logged-in status
              setUser(um);
              // Only set loggedIn to true *if it wasn't already* to avoid unnecessary re-renders
              if (!isLoggedIn) {
                  console.log("loggedInCheck: Setting isLoggedIn = true"); // Debug
                   setIsLoggedIn(true);
              }
          } catch (e) {
              // If getUserByToken fails (invalid token, network error, etc.)
              console.error("loggedInCheck: Token validation failed. Clearing credentials.", e); // Debug
              // Clear out the invalid/old credentials from localStorage
              localStorage.removeItem("token");
              localStorage.removeItem("username");
              // Reset user state and logged-in status
              setUser(undefined);
              if (isLoggedIn) { // Only update if state actually changes
                    console.log("loggedInCheck: Setting isLoggedIn = false due to validation failure."); // Debug
                    setIsLoggedIn(false); // Ensure state reflects logged out
              }
              // Notify other parts of the app (like Header) if necessary via storage event
               window.dispatchEvent(new Event('storage')); // Use storage event for consistency
          }
      } else {
          console.log("loggedInCheck: No token/username found in localStorage."); // Debug
          // Ensure logged out state if no credentials
          if (isLoggedIn) { // Only update state if it actually changed
               setUser(undefined);
               console.log("loggedInCheck: Setting isLoggedIn = false because no credentials found."); // Debug
               setIsLoggedIn(false);
          }
      }
  };

  // --- Function: handleAuthSuccess ---
  // Purpose: To handle the state transition immediately after a successful LOGIN or REGISTER action
  // Called by: RegisterPage (on successful register API call), LoginPage (on successful login API call)
  const handleAuthSuccess = async (username: string, token: string) => {
      console.log(`handleAuthSuccess: Called for ${username}. Setting localStorage and isLoggedIn=true.`); // Debug log
      // Step 1: Store the received credentials in localStorage
      localStorage.setItem("username", username);
      localStorage.setItem("token", token);

      // Step 2: Update the application's state to reflect logged-in status *immediately*
      // This ensures the UI switches to the logged-in view without waiting for getUserByToken
      if (!isLoggedIn) {
           console.log("handleAuthSuccess: Setting isLoggedIn = true"); // Debug
           setIsLoggedIn(true);
      }

      // Step 3: Attempt to fetch user data now for the header etc., but handle failure gracefully.
      try {
          console.log("handleAuthSuccess: Attempting immediate getUserByToken..."); // Debug
          const um: UserModel = await getUserByToken(); // Use the token we just set
          setUser(um); // Update user state if successful
          console.log("handleAuthSuccess: Immediate getUserByToken successful."); // Debug
      } catch (e) {
          // This might happen right after registration due to timing, or network issues.
          // We *don't* set isLoggedIn back to false. We proceed, trusting the token.
          console.warn("handleAuthSuccess: Immediate getUserByToken failed (possible timing issue). User state not set yet.", e); // Debug
          setUser(undefined); // Clear user state if fetch failed initially
      }
       // Step 4: Trigger storage event for consistency and cross-tab updates
       // This also implicitly triggers loggedInCheck if needed in other components/tabs.
       window.dispatchEvent(new Event('storage'));
  };

 // --- useEffect Hook ---
    // Purpose: Set up event listeners and perform the initial login check on component mount.
    useEffect(() => {
      // Handler for 'storage' event (catches localStorage changes across tabs/windows OR triggered manually)
      const handleStorage = (event?: StorageEvent) => { // Make event optional for manual dispatch
          // React if 'token' or 'username' specifically changed/removed, OR if manually dispatched
          if (event === undefined || event?.key === 'token' || event?.key === 'username') {
               console.log(`Storage event detected (key: ${event?.key ?? 'manual dispatch'}), running loggedInCheck.`); // Debug log
               loggedInCheck(); // Re-validate state based on storage change or manual trigger
          }
      };

      // Add listener when the component mounts
      window.addEventListener('storage', handleStorage);

      // Perform the initial check when the App component first loads
      console.log("App Mounted: Performing initial loggedInCheck."); // Debug log
      loggedInCheck();

      // Cleanup function: Remove listener when the component unmounts
      return () => {
          console.log("App Unmounting: Removing storage listener."); // Debug log
          window.removeEventListener('storage', handleStorage);
      };
      // --- CHANGE 2: Depend on isLoggedIn to re-run check if it changes externally ---
      // This helps ensure consistency if the state is somehow changed outside the normal flow,
      // although the storage event should generally handle this. Added mostly for robustness.
  }, [isLoggedIn]);
  // --- CHANGE 2 END ---
  // --- End useEffect Hook ---


  // --- Render Logic ---

  return (
    <>
      {!isLoggedIn ? (
                 // --- Pass onAuthSuccess prop to LoginAndRegister ---
                 <LoginAndRegister onAuthSuccess={handleAuthSuccess} />
             ) :
          <BrowserRouter>
            {/* --- CHANGE 3: Pass user state and a logout handler to Header --- */}
            <Header user={user} onLogout={() => {
                console.log("Logout requested from Header."); // Debug
                // Clear localStorage
                localStorage.removeItem("token");
                localStorage.removeItem("username");
                // Update state
                setUser(undefined);
                setIsLoggedIn(false);
                console.log("App state updated: isLoggedIn = false"); // Debug
                // Manually trigger storage event to ensure consistency and potential cleanup in other listeners
                window.dispatchEvent(new Event('storage'));
                // No need to navigate here, the conditional render will switch to LoginAndRegister
            }} />
            {/* --- CHANGE 3 END --- */}
            <div className='Routes'>
            <Routes>
              {/* Keep all existing routes */}
              <Route path="/" element={<Homepage />} />
              <Route path = "/my-profile" element={<MyProfilePage />}/>
              <Route path="/leaderboard" element={<LeaderboardMap />}/>
              <Route path='/profile/:id' element={<ProfilePage/>}/>
              <Route path='/reportList' element={<ReportList/>}/>
              <Route path='/volunteeringPostList' element={<VolunteeringPostList/>}/>
              <Route path='/managerRequestsList' element={<ManagerRequestsList/>}/>
              <Route path='/organizationList' element={<OrganizationList/>}/>
              <Route path='/createOrganization/:id/:quickstart' element={<CreateOrganization/>}/>
              <Route path='/myvolunteerings' element={<MyVolunteerings/>}/>
              <Route path='/volunteering/:id' element={<Volunteering/>}/>
              <Route path='/volunteering/:id/createVolunteeringPost/:postId/:quickstart' element={<CreatePost/>}/>
              <Route path='/createVolunteerPost/:postId' element={<CreatePost/>}/>
              <Route path='/organization/:id' element={<Organization/>}/>
              <Route path='/volunteering/:id/code' element={<CodeView/>}/>
              <Route path='/volunteering/:id/hrrequests' element={<HourApprovalRequestList/>}/>
              <Route path='/volunteering/:id/jrequests' element={<JoinRequestList/>}/>
              <Route path='/volunteering/:id/appointment' element={<MakeAppointment/>}/>
              <Route path='/volunteering/:id/settings' element={<VolunteeringSettings/>}/>
              <Route path='/volunteering/:id/chat' element={<VolunteeringChat/>}/>
              <Route path='/scan' element={<CodeScan/>}/>
              <Route path='/organization/:id/createVolunteering/:quickstart' element={<CreateVolunteering/>}/>
              <Route path='/volunteeringPost/:id' element={<VolunteeringPost/>}/>
              <Route path='/volunteerPost/:id' element={<VolunteerPost/>}/>
              <Route path='/volunteerPost/:id/chat' element={<VolunteerPostChat other={false}/>}/>
              <Route path='/volunteerPost/:id/chat/:username' element={<VolunteerPostChat other={true}/>}/>
              <Route path='/easterEgg' element={<EasterEgg/>}/>
              {/* Add a catch-all or redirect for logged-in users if needed */}
              {/* <Route path="*" element={<Navigate to="/" replace />} /> */}
            </Routes>
            </div>
            <Footer/>
          </BrowserRouter>}
    </>
  )
}

export default App