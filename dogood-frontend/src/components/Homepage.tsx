import { useNavigate } from 'react-router-dom';

function Homepage() {
    const navigate = useNavigate();

    const handleOrganizationListOnClick = () => {
        navigate("/organizationList");
    }

    const handleVolunteeringPostListOnClick = () => {
        navigate("/volunteeringPostList");
    }

    const handleManagerRequestListOnClick = () => {
        navigate("/managerRequestsList");
    }

    const handleReportsListOnClick = () => {
        navigate("/reportList");
    }

    return (
        <div>
            <button onClick = {handleOrganizationListOnClick}>Organizations List</button>
            <button onClick = {handleVolunteeringPostListOnClick}>Volunteering Posts List</button>
            <button onClick = {handleManagerRequestListOnClick}>Manager Requests List</button>
            <button onClick = {handleReportsListOnClick}>Reports List</button>
        </div>
    )
}

export default Homepage;