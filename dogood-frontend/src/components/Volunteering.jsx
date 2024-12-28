import './../css/Volunteering.css'

function Volunteering() {
    let name = "Volunteering Name";
    let description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

  return (
    <div>
        <div className="volInfo">
            <div className='volInfoText'>
                <h1>{name}</h1>
                <p>{description}</p>
            </div>
            <div className='volInfoButtons'>
                <button>Settings</button>
                <button>View Join Requests</button>
                <button>View Hour Approval Requests</button>
            </div>
        </div>
    </div>
  )
}

export default Volunteering