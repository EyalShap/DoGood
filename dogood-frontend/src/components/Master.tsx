import { Link, Navigate, useLocation } from 'react-router-dom';
import { useEffect, useState } from 'react'
import './../css/Master.css'
import { logout } from '../api/user_api';
import { useNavigate } from 'react-router-dom';
import Header from './Header';
import Footer from './Footer';

function Master() {
    const navigate = useNavigate();
    const location = useLocation();
    const isRegisterPage = location.pathname === "/register";
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
            localStorage.removeItem("username");
            localStorage.removeItem("token");
            closeMenu();
        }catch(e){
            alert(e);
        }
    }
    const navigateToMyProfile = async () => {
        try{
            navigate('/my-profile');
        }catch(e){
            alert(e);
        }
    }

    return (
        <div className='mainContainer'>
            {/*{(!isLoginPage) && (!isRegisterPage)&&<div className='side-bar-container'>
            <div className="menu" onClick={handleShowMenu}>
                <div className="line"></div>
                <div className="line"></div>
                <div className="line"></div>
            </div>
            <button onClick={navigateToMyProfile} className='side-bar-button'>
                    My Profile
                </button></div>}
            {isOpen && (
                <div className="menu-options">
                <Link to="/organizationList" onClick={closeMenu}>Organizations List</Link>
                <Link to="/volunteeringPostList" onClick={closeMenu}>Volunteering Posts List</Link>
                <Link to="/managerRequestsList" onClick={closeMenu}>Manager Requests List</Link>
                {isAdmin && <Link to="/reportList" onClick={closeMenu}>Reports List</Link>}
                <Link to="/" onClick={onLogout}>Log out</Link>
                </div>
                
            )}*/}
            {/*!isLoginPage && <Header isAdmin={isAdmin}></Header>*/}

        </div>
    )
}

export default Master;