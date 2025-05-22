import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import '../css/VolunteeringSettings.css'
import Location from "../models/Location";
import {
    addImageToVolunteering,
    addVolunteeringLocation,
    clearConstantCodes, disableVolunteeringLocations,
    editVolunteering,
    generateSkillsAndCategories,
    getCode,
    getConstantCodes,
    getVolunteering,
    getVolunteeringApprovalType,
    getVolunteeringLocations,
    getVolunteeringScanType,
    removeImageFromVolunteering,
    removeLocation,
    removeVolunteering,
    updateVolunteeringCategories,
    updateVolunteeringScanDetails,
    updateVolunteeringSkills,
    userHasSettingsPermission
} from "../api/volunteering_api";
import Popup from "reactjs-popup";
import { useForm } from "react-hook-form";
import { ApprovalType, ScanType } from "../models/ScanTypes";
import { FormControl, FormControlLabel, FormLabel, Radio, RadioGroup } from "@mui/material";
import VolunteeringModel from "../models/VolunteeringModel";
import { QRCodeCanvas } from "qrcode.react";
import {supabase} from "../api/general.ts";
import Info from "./Info.tsx";
import { getVolunteeringName } from "../api/post_api.ts";
import PacmanLoader from "react-spinners/PacmanLoader";

interface LocationFormData {
    name: string;
    city: string;
    street: string;
    address: string;
}


function VolunteeringSettings({volunteeringName, volunteeringDescription} : {volunteeringName: string, volunteeringDescription: string}) {
    const [name, setName] = useState<string>("")

    const [locations, setLocations] = useState<Location[]>([])
    const [allowed, setAllowed] = useState(false);
    let { id } = useParams();
    const navigate = useNavigate();

    const [images, setImages] = useState<string[]>([])
    const [skills, setSkills] = useState<string[]>([])
    const [codes, setCodes] = useState<string[]>([])
    const [categories, setCategories] = useState<string[]>([])
    const [scanType, setScanType] = useState<ScanType>("NO_SCAN");
    const [approvalType, setApprovalType] = useState<ApprovalType>("MANUAL");
    const [skillToAdd, setSkillToAdd] = useState("");
    const [categoryToAdd, setCategoryToAdd] = useState("");
    const [selectedFile, setSelectedFile] = useState<File | null>(null)
    const [key, setKey] = useState(0)
    const [locationsDisabled, setLocationsDisabled] = useState(false);

    const [skillsInput, setSkillsInput] = useState("");
    const [catsInput, setCatsInput] = useState("");

    const [editedName, setEditedName] = useState(volunteeringName);
    const [editedDesc, setEditedDesc] = useState(volunteeringName);

    const [loading, setLoading] = useState(false);

    const { register, handleSubmit, formState: { errors } } = useForm<LocationFormData>();

    const fetchName = async () => {
        try {
            let fetchedVolunteering: VolunteeringModel = await getVolunteering(parseInt(id!));
            setName(fetchedVolunteering.name);
            setEditedName(fetchedVolunteering.name);
            setEditedDesc(fetchedVolunteering.description);
        } catch (e) {
            //send to error page
            alert(e)
        }
    }

    const checkPermissions = async () => {
        try {
            var isAllowed = await userHasSettingsPermission(parseInt(id!));
            if (!isAllowed) {
                navigate("/volunteeringPostList");
                alert("You are not a manager for this volunteering");
            }
            setAllowed(isAllowed);
        } catch (e) {
            alert(e)
        }
    }

    const fetchLocations = async () => {
        try {
            let fetchedLocations: Location[] = await getVolunteeringLocations(parseInt(id!));
            setLocationsDisabled(false);
            fetchedLocations.forEach(location => location.id === -1 && setLocationsDisabled(true));
            setLocations(fetchedLocations)
        } catch (e) {
            //send to error page
            alert(e)
        }
    }

    const fetchScanDetails = async () => {
        try {
            setScanType(await getVolunteeringScanType(parseInt(id!)))
            setApprovalType(await getVolunteeringApprovalType(parseInt(id!)))
        } catch (e) {
            //send to error page
            alert(e)
        }
    }


    const fetchLists = async () => {
        try {
            let vol: VolunteeringModel = await getVolunteering(parseInt(id!));
            setSkills(vol.skills);
            setImages(vol.imagePaths ? vol.imagePaths.map(path => path.replace(/"/g, "")) : [])
            setCategories(vol.categories);

            setSkillsInput(vol.skills.join(", "));
            setCatsInput(vol.categories.join(", "));
        } catch (e) {
            //send to error page
            alert(e)
        }
    }

    const generate = async () => {
        setLoading(true);
        try {
            
            await generateSkillsAndCategories(parseInt(id!));
            fetchLists();
        } catch (e) {
            //send to error page
            alert(e)
        }
        finally {
            setLoading(false);
        }
    }

    const fetchCodes = async () => {
        try {
            setCodes(await getConstantCodes(parseInt(id!)));
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

    const disableLocations = async () => {
        try {
            await disableVolunteeringLocations(parseInt(id!));
            fetchLocations();
        }
        catch (e) {
            //send to error page
            alert(e);
        }
    };

    const sendScanDetails = async () => {
        try {
            await updateVolunteeringScanDetails(parseInt(id!), scanType, approvalType);
        }
        catch (e) {
            alert(e);
        }
    }

    const onRemoveSkill = async (skill: string) => {
        try {
            let newSkills = skills.filter(skl => skl !== skill);
            await updateVolunteeringSkills(parseInt(id!), newSkills);
            setSkills(newSkills);
        }
        catch (e) {
            //send to error page
            alert(e);
        }
    };

    const onRemoveCategory = async (category: string) => {
        try {
            let newCategs = categories.filter(ctg => ctg !== category);
            await updateVolunteeringCategories(parseInt(id!), newCategs);
            setCategories(newCategs);
        }
        catch (e) {
            //send to error page
            alert(e);
        }
    };


    const onAddSkill = async () => {
        try {
            let newSkills = skills.concat([skillToAdd]);
            await updateVolunteeringSkills(parseInt(id!), newSkills);
            setSkills(newSkills);
            setSkillToAdd("");
        }
        catch (e) {
            //send to error page
            alert(e);
        }
    };

    const onAddCategory = async () => {
        try {
            let newCategs = categories.concat([categoryToAdd]);
            await updateVolunteeringCategories(parseInt(id!), newCategs);
            setCategories(newCategs);
            setCategoryToAdd("");
        }
        catch (e) {
            //send to error page
            alert(e);
        }
    };

    const onRemove = async (locId: number) => {
        try {
            if(window.confirm("Are you sure you want to remove this location?")) {
                await removeLocation(parseInt(id!), locId);
                fetchLocations();
            }
        }
        catch (e) {
            //send to error page
            alert(e);
        }
    };

    const onRemoveImage = async (image: string) => {
        try {
            await removeImageFromVolunteering(parseInt(id!), `"${image}"`);
            setImages(images.filter(img => image !== img));
        }
        catch (e) {
            //send to error page
            alert(e);
        }
    };

    const onAddImage = async () => {
        try {
            let {data,error} =
                await supabase.storage.from("volunteering-photos")
                    .upload(`${id}/${selectedFile!.name!}`, selectedFile!, {
                        cacheControl: '3600',
                        upsert: false,
                    })
            if(data == null || error !== null){
                alert(error)
                console.log(error)
            }else {
                let filePath = data!.path;
                let response = await supabase.storage.from("volunteering-photos").getPublicUrl(filePath);
                let url = response.data.publicUrl;
                await addImageToVolunteering(parseInt(id!), url);
                setImages(images.concat([url]));
                setSelectedFile(null)
                setKey(prevState => 1-prevState)
            }
        }
        catch (e) {
            //send to error page
            alert(e);
        }
    };

    const onFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
        setSelectedFile(e.target.files![0])
    }

    const onGenerateCode = async () => {
        try {
            await getCode(parseInt(id!), true);
            fetchCodes();
        } catch (e) {
            alert(e);
        }
    }

    const onClearCodes = async () => {
        try {
            await clearConstantCodes(parseInt(id!));
            fetchCodes();
        } catch (e) {
            alert(e);
        }
    }

    const qrLink = (id: string) => {
        const canvas = document.getElementById(id);
        {/* 
// @ts-ignore */}
        const pngUrl = canvas.toDataURL("image/png").replace("image/png", "image/octet-stream");
        let downloadLink = document.createElement("a");
        downloadLink.href = pngUrl;
        downloadLink.download = `${id}.png`;
        document.body.appendChild(downloadLink);
        downloadLink.click();
        document.body.removeChild(downloadLink);
    };


    useEffect(() => {
        checkPermissions();
    }, [])


    useEffect(() => {
        if (allowed) {
            fetchName();
            fetchLocations();
            fetchScanDetails();
            fetchLists();
            fetchCodes();
        }
    }, [allowed])

    const saveSkillsOnClick = async () => {
        try {
            const updated = skillsInput.trim() !== "" ? skillsInput.split(",").map(skill => skill.trim()) : [];
            await updateVolunteeringSkills(parseInt(id!), updated);
            setSkills(updated);                
            alert("Skills updated successfully!");
            }
            catch (e) {
                //send to error page
                alert(e);
            }
        };
    
    const saveCatsOnClick = async () => {
        try {
            const updated = catsInput.trim() !== "" ? catsInput.split(",").map(skill => skill.trim()) : [];
            await updateVolunteeringCategories(parseInt(id!), updated);
            setCategories(updated);         
            alert("Categories updated successfully!");
        }
        catch (e) {
                //send to error page
            alert(e);
        }
    };

    const handleEditVolunteeringOnClick = async () => {
                try {
                    await editVolunteering(parseInt(id!), editedName, editedDesc);
                    setName(editedName);
                    alert("Changes Saved");
                }
                catch(e) {
                    alert(e);
                }
            
        }

        const handleRemoveVolunteeringOnClick = async () => {
            if(window.confirm("Are you sure you want to remove this volunteering?")) {
                try {
                    await removeVolunteering(parseInt(id!));
                    alert("Volunteering removed successfully!");
                    navigate(`/myvolunteerings`);
                }
                catch(e) {
                    alert(e);
                }
            }
        }

    return (
        <div id="postPage" className="postPage">
            <h2 className="bigHeader">{name} Settings</h2>
            <div className="volunteeringActions">
                <h1 className='settingHeader'>Edit Volunteering Name And Description:</h1>
                <input className="editInput" type="text" value={editedName} onChange={(e) => setEditedName(e.target.value)} />
                <input className="editInput" type="text" value={editedDesc} onChange={(e) => setEditedDesc(e.target.value)} />
                <button className="orangeCircularButton" onClick={handleEditVolunteeringOnClick}>Save Changes</button>
            </div>
            <div className="volunteeringActions">
                <h1 className='settingHeader'>Remove Volunteering:</h1>
                <button className="orangeCircularButton" onClick={handleRemoveVolunteeringOnClick}>Remove Volunteering</button>
            </div>
            <div className="volunteeringActions">
                <h1 className='settingHeader'>Current Locations:</h1>
                <div className="locations">
                    {locations.map(location =>
                        <div className={`location${location.id === -1 ? " disabledLocation" : ""}`}>
                            <h2>{location.name}</h2>
                            {location.id > -1 && <p>{location.address.city}</p>}
                            {location.id > -1 && <p>{location.address.street}</p>}
                            {location.id > -1 && <p>{location.address.address}</p>}
                            <button className="removeButton" onClick={() => onRemove(location.id)}>{location.id > -1 ? "Remove Location" : "Enable Locations?"}</button>
                        </div>)}
                </div>
                {!locationsDisabled && <Popup trigger={<button className="orangeCircularButton">Add Location</button>} modal nested>
                    {/* 
// @ts-ignore */}
                    {close => (
                        <div className="modal">
                            <form className="create-location-form"
                                  onSubmit={handleSubmit(async (data) => addLocation(data, close))}>
                                <h1 className="smallHeader" style={{fontSize:'25px', marginBottom:'20px'}}>Add Location</h1>
                                <div style={{display: 'flex', alignItems: 'center', gap: '1rem'}}>
                                    <label htmlFor="name" className="smallHeader" style={{fontSize:'17px'}}>Location Name:</label>
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
                                    {errors.name && <p style={{color: 'red'}}>{errors.name.message}</p>}
                                </div>
                                <div style={{display: 'flex', alignItems: 'center', gap: '1rem'}}>
                                    <label htmlFor="city" className="smallHeader" style={{fontSize:'17px'}}>City:</label>
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
                                    {errors.city && <p style={{color: 'red'}}>{errors.city.message}</p>}
                                </div>

                                <div style={{display: 'flex', alignItems: 'center', gap: '1rem'}}>
                                    <label htmlFor="street" className="smallHeader" style={{fontSize:'17px'}}>Street:</label>
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
                                    {errors.street && <p style={{color: 'red'}}>{errors.street.message}</p>}
                                </div>

                                <div style={{display: 'flex', alignItems: 'center', gap: '1rem'}}>
                                    <label htmlFor="address" className="smallHeader" style={{fontSize:'17px'}}>Number:</label>
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
                                    {errors.address && <p style={{color: 'red'}}>{errors.address.message}</p>}
                                </div>
                                <button type="submit" className="orangeCircularButton">Add Location</button>
                            </form>
                        </div>
                    )}
                </Popup>}
                {locations.length === 0 && <button onClick={disableLocations} className="orangeCircularButton">We don't have locations!</button>}
            </div>
            <div className="volunteeringActions">
            <h1 className='settingHeader settingHeader'>Volunteering Skills And Categories</h1>
            
                <h1 className='smallHeader'>Defining the skills and categories of your volunteering will help volunteers find this volunteering</h1>
                <button className={`orangeCircularButton ${loading ? 'disabledButton' : ''}`} onClick={generate}>Generate Skills and Categories with AI</button>
                {loading && <PacmanLoader color="#037b7b" size={25} />}
                {/*<div className="stringlist">
                    {skills.map(skill =>
                        <div className="skillcateg">
                            <p>{skill}</p>
                            <button onClick={() => onRemoveSkill(skill)} className="removeButton">X</button>
                        </div>)}
                </div>
                <input onChange={e => setSkillToAdd(e.target.value)} value={skillToAdd}/>
                <button className="orangeCircularButton" onClick={onAddSkill}>Add Skill</button>*/}
                <div className='catsAndSkills'>
                <div className='skills'>
                    <h2 className='volunteerPostheader'>Skills</h2>
                    <div className="list-section">
                        <textarea
                            value={skillsInput}
                            onChange={(e) => setSkillsInput(e.target.value)}
                            placeholder="Enter skills separated by commas"
                        />
                        <button onClick={saveSkillsOnClick} className="orangeCircularButton">Save</button>
                    </div>
                </div>
                <div className='cats'>
                    <h2 className='volunteerPostheader'>Categories</h2>
                    <div className="list-section">
                        <textarea
                            value={catsInput}
                            onChange={(e) => setCatsInput(e.target.value)}
                            placeholder="Enter categories separated by commas"
                        />
                        <button onClick={saveCatsOnClick} className="orangeCircularButton">Save</button>
                    </div>
                </div>
                </div>
            </div>
            {/*<div className="container">
                <h1>Volunteeering Categories:</h1>
                <Info text="Defining the skills and categories of your volunteering will help volunteers find this volunteering"/>
                <div className="stringlist">
                    {categories.map(category =>
                        <div className="skillcateg">
                            <p>{category}</p>
                            <button onClick={() => onRemoveCategory(category)} className="removeButton">X</button>
                        </div>)}
                </div>
                <input onChange={e => setCategoryToAdd(e.target.value)} value={categoryToAdd}/>
                <button className="orangeCircularButton" onClick={onAddCategory}>Add Category</button>
            </div>*/}
            {/*<div className="container">
                <button className="orangeCircularButton" onClick={generate}>Generate Skills and Categories with AI</button>
            </div>*/}
            {/*<div className="container">
                <h1>Photos:</h1>
                <div className="photos">
                    {images.map(image =>
                        <div className="photo">
                            <img src={image}/>
                            <button onClick={() => onRemoveImage(image)} className="removeButton">X</button>
                        </div>)}
                </div>
                <input type="file" onChange={onFileUpload} accept="image/*" key={key}/>
                <button className="orangeCircularButton" onClick={onAddImage}>Upload!</button>
            </div>*/}
            <div className="volunteeringActions">
                <h1 className='settingHeader settingHeader'>Volunteer Scanning:</h1>
                <h1 className="smallHeader">You can allow your volunteers to confirm their arrival automatically by showing a QR code to them.</h1>
                <FormControl sx={{ marginTop: '10px' }}>
                    <FormLabel sx={{ fontFamily: 'Montserrat, sans-serif', fontSize: '16px' }}>Choose type of confirming arrival using QR codes</FormLabel>
                    <RadioGroup
                        value={scanType}
                        onChange={e => setScanType(e.target.value as ScanType)}
                        name="radio-buttons-group"
                        className = 'selectCode'>
                        <FormControlLabel value="NO_SCAN" control={<Radio/>} label={<span>Disable Scanning</span>}/>
                        <FormControlLabel value="ONE_SCAN" control={<Radio/>} label={<span>One Scan<Info text="Volunteers will only need to scan a QR code once during an activity to confirm their arrival to the entire activity"/></span>}/>
                        <FormControlLabel value="DOUBLE_SCAN" control={<Radio/>} label={<span>Scan At the Start and End<Info text="Volunteers will have to confirm their arrival at the start of an activity and once again at the end."/></span>}/>
                    </RadioGroup>
                </FormControl>
                {scanType != "NO_SCAN" &&
                    <FormControl>
                        <FormLabel>Choose action on completion of scanning</FormLabel>
                        <RadioGroup
                            value={approvalType}
                            onChange={e => setApprovalType(e.target.value as ApprovalType)}
                            name="radio-buttons-group"
                            className = 'selectCode'>
                            <FormControlLabel value="MANUAL" control={<Radio/>} label={<span>Request Hours Approval<Info text="You will be able to manually confirm or deny the volunteer's reported hours"/></span>}/>
                            <FormControlLabel value="AUTO_FROM_SCAN" control={<Radio/>}
                                              label={<span>Automatically Approve Hours <Info text="Activities reported by volunteers via QR code scanning will be automatically approved"/></span>}/>
                        </RadioGroup>
                    </FormControl>}
                <button className="orangeCircularButton" onClick={sendScanDetails}>Confirm</button>
            </div>

            <div className="volunteeringActions">
                <h1 className='settingHeader settingHeader'>Manage Constant Codes:</h1>
                <h1 className="smallHeader">These QR codes will remain valid as long as they appear here. You can print them out and show them at the volunteering activity</h1>
                <div className="codes">
                    {codes.map((code, index) =>
                        <div className="code">
                            <QRCodeCanvas size={250} id={`qr${index}`} value={code} marginSize={5}
                                          style={{margin: "5px"}}/>
                            <button onClick={() => qrLink(`qr${index}`)} className="orangeCircularButton">Download</button>
                        </div>)}
                </div>
                <button className="orangeCircularButton" onClick={onGenerateCode}>Generate New Code</button>
                <button className="orangeCircularButton" onClick={onClearCodes}>Clear Codes</button>
            </div>
        </div>
    )
}

export default VolunteeringSettings