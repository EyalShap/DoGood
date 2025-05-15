import { useState, useEffect } from 'react';
import './../css/Header.css'
import { getNewUserNotificationsAmount, getUserNotifications, logout, readNewUserNotifications } from '../api/user_api';
// REMOVE useNavigate from here, it's handled by App.tsx now
import { useNavigate } from 'react-router-dom';
import UserModel from '../models/UserModel';
import Notification from '../models/Notification' // Keep importing the original type
import { FaBell } from 'react-icons/fa';
import { Badge } from '@mui/material';
import {Client} from "@stomp/stompjs";
import {host} from "../api/general.ts";
import defaultImage from '../assets/defaultProfilePic.jpg';
import logoTitle from "../assets/title_dogood.png"
import logoIcon from "../assets/logo.png"
import {format, isToday, isYesterday} from "date-fns";

type Props = {
    user: UserModel | undefined;
};

const Header: React.FC<Props> = ({ user }) => {
  const navigate = useNavigate();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [dropdownOpenNotifications, setDropdownOpenNotifications] = useState(false);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [newNotificationsAmount, setNewNotificationsAmount] = useState<number>(0);
  const isMobile = window.innerWidth <= 768;
  const isAdmin = user === undefined ? false : user.admin;
  const [connected, setConnected] = useState(false);
  const profilePicture = user !== undefined && user!.profilePicUrl && user!.profilePicUrl !== "" ? user!.profilePicUrl : defaultImage;
  const [currentProfilePic, setCurrentProfilePic] = useState(profilePicture);

  useEffect(() => {
      setCurrentProfilePic(user !== undefined && user!.profilePicUrl && user!.profilePicUrl !== "" ? user!.profilePicUrl : defaultImage);
  }, [user?.profilePicUrl]);

  useEffect(() => {
      const handleProfilePicUpdate = () => {
          console.log("Header: Detected profile picture update event.");
           setCurrentProfilePic(user !== undefined && user!.profilePicUrl && user!.profilePicUrl !== "" ? user!.profilePicUrl : defaultImage);
      };
      window.addEventListener('profilePictureUpdated', handleProfilePicUpdate);
      return () => {
          window.removeEventListener('profilePictureUpdated', handleProfilePicUpdate);
      };
  }, [user]);

    const onLogout = async () => {
        try{
            await logout();
        }
        catch(e){
            alert(e);
        }
        localStorage.removeItem("username");
        localStorage.removeItem("token");
        setDropdownOpen(false);
        window.dispatchEvent(new Event('storage'))
        navigate('/');
    }


  const toggleDropdown = () => {
      setDropdownOpen(!dropdownOpen);
  };

  const toggleDropdownNotifications = async () => {
    setDropdownOpenNotifications(!dropdownOpenNotifications);
  };

  const fetchNotifications = async () => {
    if (!localStorage.getItem("token")) return;
    try {
      // Fetch notifications and sort them immediately by ID descending
      const fetchedNotifications = await getUserNotifications();
      // --- CHANGE 1: Sort by ID immediately after fetching ---
      const sortedNotifications = fetchedNotifications.sort((a, b) => b.id - a.id);
      setNotifications(sortedNotifications);
      // --- CHANGE 1 END ---
      setNewNotificationsAmount(await getNewUserNotificationsAmount());
    }
    catch (e) {
      console.error("Failed to fetch notifications:", e);
    }
  }

  const closeDropdown = () => {
      setDropdownOpen(false);
  };

  const closeDropdownNotifications = async () => {
      setDropdownOpenNotifications(false);
      console.log("Attempting to read new user notifications");
      try {
          await readNewUserNotifications();
          fetchNotifications(); // Re-fetch potentially updates read status visually if needed
      } catch(e) {
          console.error("Failed to mark notifications as read:", e);
      }
  };

  const handleLogoutClick = async () => {
      closeDropdown();
      try {
          await logout();
          console.log("Backend logout successful (optional)");
      } catch (e) {
          console.error("Backend logout failed (optional):", e);
      }
      onLogout();
  }

  const onLogo = async () => {
    navigate(`/`);
  }

  useEffect(() => {
    if (localStorage.getItem("token")) {
        fetchNotifications(); // Initial fetch and sort
    }

      let client: Client | null = null;
      if (localStorage.getItem("token") && !connected) {
           client = new Client({
              brokerURL: host+"/api/ws-message",
              connectHeaders: {
                  "Authorization": localStorage.getItem("token")!
              },
              reconnectDelay: 5000,
              onConnect: () => {
                  console.log("WS Connected!");
                  if(!connected) {
                      client?.subscribe(`/user/queue/notifications`, msg => {
                          let newNotif: Notification = JSON.parse(msg.body);
                          console.log("WS Received Notification:", newNotif);
                          // --- CHANGE 2: Add new notification and re-sort by ID ---
                          setNotifications(prevState =>
                              [...prevState, newNotif].sort((a, b) => b.id - a.id)
                          );
                          // --- CHANGE 2 END ---
                          setNewNotificationsAmount(prevState => prevState + 1);
                      });
                      setConnected(true);
                  }
              },
              onDisconnect: () => {
                  console.log("WS Disconnected!");
                  setConnected(false);
              },
              onStompError: (frame) => {
                   console.error('WS Broker reported error: ' + frame.headers['message']);
                   console.error('WS Additional details: ' + frame.body);
              },
          });
          console.log("Activating WS client...");
          client.activate();
      }

      return () => {
          if (client && client.active) {
              console.log("Deactivating WS client...");
              client.deactivate();
              setConnected(false);
          }
      }
  }, [localStorage.getItem("token")])

  // --- REMOVE the separate useEffect for sorting, as it's done inline now ---
  // useEffect(() => {
  //    // Sorting logic removed from here
  // }, [notifications.length]);
  // --- REMOVE END ---

    const transformTimestamp = (timestamp: string) => {
      let date = new Date(timestamp);
        if(isToday(date)) {
            return `${format(date, "H:mm")}`
        } else if(isYesterday(date)) {
            return `Yesterday at ${format(date, "H:mm")}`
        } else {
            return `${format(date, "MMMM do, yyyy")} at ${format(date, "H:mm")}`
        }
    }

    useEffect(() => {
      fetchNotifications();
        const client = new Client({
            brokerURL: host+"/api/ws-message",
            connectHeaders: {
                "Authorization": localStorage.getItem("token")!
            },
            reconnectDelay: 5000,
            onConnect: () => {
                console.log("Connected!");
                if(!connected) {
                    client.subscribe(`/user/queue/notifications`, msg => {
                        let newNotif: Notification = JSON.parse(msg.body);
                        console.log(newNotif)
                        setNotifications(prevState => prevState.concat([newNotif]).sort((a,b) => b.id - a.id));
                        setNewNotificationsAmount(prevState => prevState+1)
                    });
                    setConnected(true);
                }
            },
            onDisconnect: () => {
                setConnected(false);
            }
        });
        if(!connected) {
            client.activate();
        }
        return () => client.deactivate();
    },[])
    
    return (
        <header className="header">
            <link
            href="https://fonts.googleapis.com/css2?family=Lobster&display=swap"
            rel="stylesheet"
            />
            <link
            href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap"
            rel="stylesheet"
            />
            
          <div className="logo" onClick = {onLogo}>
              <img className="logoicon" src={logoIcon}/>
              {!isMobile && <img className="logotitle" src={logoTitle}/>}
          </div>
          <nav className="nav">
            <ul className="menuList">
              {!isMobile && <li className="menuListItem"><a href="/volunteeringPostList" className="navLink">Browse Posts</a></li>}
              {!isMobile && <li className="menuListItem"><a href="/organizationList" className="navLink">Browse Organizations</a></li>}
              {!isMobile && <li className="menuListItem"><a href="/myvolunteerings" className="navLink">My Volunteerings</a></li>}
              {!isMobile && <li className="menuListItem"><a href="/leaderboard" className="navLink">Leaderboard</a></li>}

              <div className="notification">
                <FaBell
                    className="notificationBell"
                    onClick={toggleDropdownNotifications}>
                 </FaBell>
                 {newNotificationsAmount > 0 &&
                 <div className="buttonBadge">{newNotificationsAmount}</div>}
                  {dropdownOpenNotifications && (
                    notifications.length == 0 ? 
                    <div className="dropdownMenu notificationsDropDown" onMouseLeave={closeDropdownNotifications}>
                    <p className="emptyNotifications">{"You have no notifications."}</p>
                    </div> :
                    <div className="dropdownMenu notificationsDropDown" onMouseLeave={closeDropdownNotifications}>
                      <div className="notification-scroll-container">
                        {notifications.map((notification: Notification) => (
                          <div className="notification-item" key={notification.id}>
                            <a href={notification.navigationURL}
                              className={notification.isRead ? "dropdownNotification" : "dropdownNotificationNew"}>
                              {notification.message}
                            </a>
                            <p className="timestamp">{transformTimestamp(notification.timestamp)}</p>
                          </div>
                        ))}
                      </div>
                      <a href={"/notifications"} className="notificationsPageLink">Notifications Page</a>
                    </div>
                    )}
              </div>

              <div className="profileWrapper">
                    <img
                        src={profilePicture}
                        alt=""
                        className="profileImage"
                        onClick={toggleDropdown}
                    />
                    {dropdownOpen && (
                        <div className="dropdownMenu" onMouseLeave={closeDropdown}>
                          {isMobile && <a href="/volunteeringPostList" className="dropdownItem">Browse Posts</a>}
                          {isMobile && <a href="/organizationList" className="dropdownItem">Browse Organizations</a>}
                            {isMobile && <a href="/myvolunteerings" className="dropdownItem">My Volunteerings</a>}
                              {isMobile && <a href="/leaderboard" className="dropdownItem">Leaderboard</a>}
                            <a href="/my-profile" className="dropdownItem">My Profile</a>
                            <a href="/summary" className="dropdownItem">Hours Summary</a>
                            <a href="/managerRequestsList" className="dropdownItem">My Requests</a>
                            {isAdmin && <a href="/reportList" className="dropdownItem">Admin Dashboard</a>}
                            <a className="dropdownItem" onClick = {onLogout}>Logout</a>
                        </div>
                    )}
                </div>
            </ul>
          </nav>
        </header>
      );
}

export default Header;