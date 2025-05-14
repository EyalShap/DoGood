import { useState, useEffect } from 'react';
import './../css/Header.css'
import { getNewUserNotificationsAmount, getUserNotifications, logout, readNewUserNotifications } from '../api/user_api';
import { useNavigate } from 'react-router-dom';
import UserModel from '../models/UserModel';
import Notification from '../models/Notification'
import { FaBell } from 'react-icons/fa';
import { Badge } from '@mui/material';
import {Client} from "@stomp/stompjs";
import {host} from "../api/general.ts";
import defaultImage from '../assets/defaultProfilePic.jpg';
import logoTitle from "../assets/title_dogood.png"
import logoIcon from "../assets/logo.png"
import {format, isToday, isYesterday} from "date-fns";

type Props = { user: UserModel | undefined };

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

    const toggleDropdown = () => {
        setDropdownOpen(!dropdownOpen);
    };

    const toggleDropdownNotifications = async () => {
      setDropdownOpenNotifications(!dropdownOpenNotifications);
    };

    const fetchNotifications = async () => {
      try {
        setNotifications(await getUserNotifications());
        setNewNotificationsAmount(await getNewUserNotificationsAmount());
      }
      catch (e) {
        alert(e);
      }
    }

    const closeDropdown = () => {
        setDropdownOpen(false);
    };
    
    const closeDropdownNotifications = () => {
        setDropdownOpenNotifications(false);
        // mark all new notifications as read
        console.log("attempting to read new user notifications");
        var result = readNewUserNotifications(); // ignore result list, already loads the full list in getUserNotifications
        console.log(result);
        fetchNotifications();
    };

    const closeMenu = () => {
      setDropdownOpen(false);
    }

    const onLogout = async () => {
      try{
        await logout();
      }
      catch(e){
        alert(e);
      }
        localStorage.removeItem("username");
        localStorage.removeItem("token");
        closeMenu();
        window.dispatchEvent(new Event('storage'))
        navigate('/');
    }

    const onLogo = async () => {
      navigate(`/`);
    }

    const transformTimestamp = (timestamp: string) => {
      let date = new Date(timestamp);
        if(isToday(date)){
            return `${format(date, "H:mm")}`
        }else if(isYesterday(date)){
            return `Yesterday at ${format(date, "H:mm")}`
        }else{
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
