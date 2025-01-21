import { Link, useLocation } from 'react-router-dom';
import { useEffect, useState } from 'react'
import './../css/Homepage.css'
import { logout } from '../api/user_api';

function Homepage() {
    const location = useLocation();
    const isLoginPage = location.pathname === "/";
    const [isOpen, setIsOpen] = useState(false);
    const [isAdmin, setIsAdmin] = useState(true);

    const handleShowMenu = () => {
        setIsOpen(!isOpen);
    }

    const closeMenu = () => {
        setIsOpen(false);
    }

    const onLogout = async () => {
        try{
            await logout();
            sessionStorage.removeItem("username");
            sessionStorage.removeItem("token");
            closeMenu();
        }catch(e){
            alert(e);
        }
    }

    return (
        <div>
            {(!isLoginPage) && <div className="menu" onClick={handleShowMenu}>
                <div className="line"></div>
                <div className="line"></div>
                <div className="line"></div>
            </div>}

            {isOpen && (
                <div className="menu-options">
                <Link to="/organizationList" onClick={closeMenu}>Organizations List</Link>
                <Link to="/volunteeringPostList" onClick={closeMenu}>Volunteering Posts List</Link>
                <Link to="/managerRequestsList" onClick={closeMenu}>Manager Requests List</Link>
                {isAdmin && <Link to="/reportList" onClick={closeMenu}>Reports List</Link>}
                <Link to="/" onClick={onLogout}>Log out</Link>
                </div>
            )}
        </div>
    )
}

export default Homepage;