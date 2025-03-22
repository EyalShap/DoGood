import { useState } from 'react';
import './../css/Header.css'
import { logout } from '../api/user_api';
import { useNavigate } from 'react-router-dom';

type Props = { isAdmin: boolean };

const Header: React.FC<Props> = ({ isAdmin }) => {
  const navigate = useNavigate();
  const [dropdownOpen, setDropdownOpen] = useState(false);

    const toggleDropdown = () => {
        setDropdownOpen(!dropdownOpen);
    };

    const closeDropdown = () => {
        setDropdownOpen(false);
    };

    const closeMenu = () => {
      setDropdownOpen(false);
    }

    const onLogout = async () => {
      try{
        await logout();
        localStorage.removeItem("username");
        localStorage.removeItem("token");
        closeMenu();
      }
      catch(e){
        alert(e);
      }
    }

    const onLogo = async () => {
      navigate(`/homepage`);
    }
    
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
            
          <div className="logo" onClick = {onLogo}>doGood</div>
          <nav className="nav">
            <ul className="menuList">
              <li className="menuListItem"><a href="/volunteeringPostList" className="navLink">Browse Posts</a></li>
              <li className="menuListItem"><a href="/organizationList" className="navLink">Browse Organizations</a></li>
              <li className="menuListItem"><a href="/contact" className="navLink">My Volunteerings</a></li>
              <li className="menuListItem"><a href="/leaderboard" className="navLink">Leaderboard</a></li>
              <div className="profileWrapper">
                    <img
                        src="https://i.pinimg.com/564x/8c/f6/b0/8cf6b01e7f02e2befa711da2c9030f36.jpg"
                        alt="Profile"
                        className="profileImage"
                        onClick={toggleDropdown}
                    />
                    {dropdownOpen && (
                        <div className="dropdownMenu" onMouseLeave={closeDropdown}>
                            <a href="/my-profile" className="dropdownItem">My Profile</a>
                            <a href="/managerRequestsList" className="dropdownItem">My Requests</a>
                            {isAdmin && <a href="/reportList" className="dropdownItem">Reports</a>}
                            <a href="/" className="dropdownItem" onClick = {onLogout}>Logout</a>
                        </div>
                    )}
                </div>
            </ul>
          </nav>
        </header>
      );
}

export default Header;
