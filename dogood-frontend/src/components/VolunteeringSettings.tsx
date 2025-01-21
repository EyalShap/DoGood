import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import '../css/VolunteeringSettings.css'
import Location from "../models/Location";
import { addVolunteeringLocation, getVolunteeringLocations, removeLocation, userHasSettingsPermission } from "../api/volunteering_api";
import Popup from "reactjs-popup";
import { SubmitHandler, useForm } from "react-hook-form";

interface LocationFormData {
    name: string;
    city: string;
    street: string;
    address: string;
}


function VolunteeringSettings() {
    const [locations, setLocations] = useState<Location[]>([])
    const [allowed, setAllowed] = useState(false);
    let { id } = useParams();
    const navigate = useNavigate();

    const { register, handleSubmit, formState: { errors }, reset } = useForm<LocationFormData>();

    const checkPermissions = async () => {
        try {
            var isAllowed = await userHasSettingsPermission(parseInt(id!));
            if (!isAllowed) {
                navigate("/homepage");
                alert("You are not a manager for this volunteering");
            }
            setAllowed(isAllowed);
        } catch (e) {
            alert(e)
        }
    }
    const fetchLocations = async () => {
        try {
            setLocations(await getVolunteeringLocations(parseInt(id!)))
        } catch (e) {
            //send to error page
            alert(e)
        }
    }


    const addLocation = async (data: LocationFormData, close: any) => {
        try {
            await addVolunteeringLocation(parseInt(id!), data.name, data.city, data.street, data.address);
            close();
            alert("Location created successfully!");
            fetchLocations();
        }
        catch (e) {
            //send to error page
            alert(e);
        }
    };

    const onRemove = async (locId: number) => {
        try {
            await removeLocation(parseInt(id!), locId);
            fetchLocations();
        }
        catch (e) {
            //send to error page
            alert(e);
        }
    };


    useEffect(() => {
        checkPermissions();
    }, [])

    useEffect(() => {
        if (allowed) {
            fetchLocations();
        }
    }, [allowed])

    return (
        <div className="settings">
            <div className="container">
                <h1>Current Locations:</h1>
                <div className="locations">
                    {locations.map(location =>
                        <div className='location'>
                            <h2>{location.name}</h2>
                            <p>{location.address.city}</p>
                            <p>{location.address.street}</p>
                            <p>{location.address.address}</p>
                            <button className="remove" onClick={() => onRemove(location.id)}>Remove Location</button>
                        </div>)}
                </div>
                <Popup trigger={<button>Add Location</button>} modal nested>
                    {/* 
// @ts-ignore */}
                    {close => (
                        <div className="modal">
                            <form className="create-location-form" onSubmit={handleSubmit(async (data) => addLocation(data, close))}>
                                <h1>Add Location</h1>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                                    <label htmlFor="name">Location Name:</label>
                                    <input
                                        id="name"
                                        {...register('name', {
                                            required: 'Location name is required',
                                            minLength: {
                                                value: 3,
                                                message: 'Must be at least 3 characters'
                                            },
                                            maxLength: {
                                                value: 50,
                                                message: 'Cannot exceed 50 characters'
                                            }
                                        })}
                                    />
                                    {errors.name && <p style={{ color: 'red' }}>{errors.name.message}</p>}
                                </div>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                                    <label htmlFor="city">City:</label>
                                    <input
                                        id="city"
                                        {...register('city', {
                                            required: 'City is required',
                                            minLength: {
                                                value: 2,
                                                message: 'Must be at least 2 characters'
                                            },
                                            maxLength: {
                                                value: 50,
                                                message: 'Cannot exceed 50 characters'
                                            }
                                        })}
                                    />
                                    {errors.city && <p style={{ color: 'red' }}>{errors.city.message}</p>}
                                </div>

                                <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                                    <label htmlFor="street">Street:</label>
                                    <input
                                        id="street"
                                        {...register('street', {
                                            required: 'Street is required',
                                            maxLength: {
                                                value: 50,
                                                message: 'Cannot exceed 50 characters'
                                            }
                                        })}
                                    />
                                    {errors.street && <p style={{ color: 'red' }}>{errors.street.message}</p>}
                                </div>

                                <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                                    <label htmlFor="address">Address:</label>
                                    <input
                                        id="address"
                                        {...register('address', {
                                            required: 'Address is required',
                                            maxLength: {
                                                value: 100,
                                                message: 'Cannot exceed 100 characters'
                                            }
                                        })}
                                    />
                                    {errors.address && <p style={{ color: 'red' }}>{errors.address.message}</p>}
                                </div>
                                <button type="submit">Add Location</button>
                            </form>
                        </div>
                    )}
                </Popup>
            </div>
        </div>
    )
}

export default VolunteeringSettings