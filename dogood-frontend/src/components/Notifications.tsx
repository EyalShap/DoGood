import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import {format, isToday, isYesterday} from "date-fns";
import { getUserNotifications, logout, readNewUserNotifications } from '../api/user_api';
import './../css/Notifications.css'
import Notification from '../models/Notification'

function Notifications() {
    const navigate = useNavigate();
    const [notifications, setNotifications] = useState<Notification[]>([]);

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

    const fetchNotifications = async () => {
          try {
            setNotifications(await getUserNotifications());
            console.log("LOG: " + notifications)
          }
          catch (e) {
            alert(e);
          }
        }

    useEffect(() => {
      fetchNotifications();
    },[])
    
    return (

        <div className="notifications">
            {notifications.length === 0 ? <div className="emptyNotifications">You have no notifications.</div> :
            notifications.map((notification: Notification) => 
            <div className="notification-item" key={notification.id}>
                <div className="float-first-child-element">
                    <a href={notification.navigationURL} className={notification.isRead ? "dropdownNotification" : "dropdownNotificationNew"}>
                        {notification.message}
                    </a>
                </div>
                <div className="float-second-child-element">
                    <p className="timestamp">{transformTimestamp(notification.timestamp)}</p>
                </div>
            </div>
            )}
        </div>
      );
}

export default Notifications;
