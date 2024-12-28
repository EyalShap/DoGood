import './../css/Organization.css'

function Organization() {
    let name = "Magen David Adom";
    let description = "Very very amazing organization.";
    let email = "mada@gmail.com";
    let phone_number = "054-8103254";

  return (
    <div>
        <div className = "orgInfo">
            <div className='orgInfoText'>
                <h1>{name}</h1>
                <p>{description}</p>
            </div>

            <div className = "orgInfoContact">
                <h2>Contact Us:</h2>
                <p>Email: <a href={`mailto:${email}`}>{email}</a></p>
                <p>Phone: {phone_number}</p>
            </div>

            <div className = 'orgInfoButtons'>
                <button>Create Volunteering</button>
            </div>
        </div>
    </div>
  )
}

export default Organization