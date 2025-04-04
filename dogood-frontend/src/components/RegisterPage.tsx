import { useState } from "react";
import { useNavigate } from 'react-router-dom';
import { register } from "../api/user_api";
import { supabase } from "../api/general";

function RegisterPage({ changeState }: { changeState: React.Dispatch<React.SetStateAction<boolean>> }) {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [phone, setPhone] = useState("");
    const [birthDate, setBirthDate] = useState("");
    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const [preview, setPreview] = useState<string | null>(null);

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            const file = e.target.files[0];
            setSelectedFile(file);
            const reader = new FileReader();
            reader.onloadend = () => {
                setPreview(reader.result as string);
            };
            reader.readAsDataURL(file);
        }
    };

    const onRegister = async () => {
        try {
            let profilePicUrl = "";
            if (selectedFile) {
                // Generate a unique file name based on the username and current timestamp.
                const fileExt = selectedFile.name.split('.').pop();
                const fileName = `${username}-${Date.now()}.${fileExt}`;
                const filePath = fileName; // We'll upload at the root of the bucket.
                
                // Upload the file to the "profile-pictures" bucket.
                const { error } = await supabase.storage
                    .from("profile-pictures")
                    .upload(filePath, selectedFile, { cacheControl: '3600', upsert: false });
                if (error) {
                    alert("Error uploading image: " + error.message);
                    return;
                }
                // Retrieve the public URL for the uploaded image.
                const { data } = supabase.storage.from("profile-pictures").getPublicUrl(filePath);
                profilePicUrl = data.publicUrl;
            }
            // Pass the profilePicUrl as an extra parameter.
            let token = await register(username, password, name, email, phone, birthDate, profilePicUrl);
            localStorage.setItem("username", username);
            localStorage.setItem("token", token);
            changeState(false);
        } catch (e) {
            alert(e);
        }
    };

    return (
        <div>
            <link
                href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap"
                rel="stylesheet"
            />
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100vh', fontFamily: 'Montserrat, sans-serif' }}>
                <h1>Register</h1>
                
                {/* Hidden file input */}
                <input
                    type="file"
                    accept="image/*"
                    id="profilePicInput"
                    style={{ display: 'none' }}
                    onChange={handleFileChange}
                />
                
                {/* Circular photo holder */}
                <div className="profile-picture-upload" style={{ marginBottom: '20px' }}>
                    <div
                        className="circle-placeholder"
                        onClick={() => document.getElementById('profilePicInput')?.click()}
                        style={{
                            width: '100px',
                            height: '100px',
                            borderRadius: '50%',
                            backgroundColor: '#e0e0e0',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            fontSize: '2rem',
                            color: '#777',
                            cursor: 'pointer',
                            overflow: 'hidden'
                        }}
                    >
                        {preview ? (
                            <img
                                src={preview}
                                alt="Profile Preview"
                                style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                            />
                        ) : (
                            "+"
                        )}
                    </div>
                </div>
                
                <div style={{ display: 'flex', flexDirection: 'column', width: '300px' }}>
                    <input 
                        type="text" 
                        placeholder="Username" 
                        value={username} 
                        onChange={e => setUsername(e.target.value)} 
                        style={{ margin: '5px 0', padding: '10px', fontSize: '16px', fontFamily: 'Montserrat, sans-serif' }}
                    />
                    <input 
                        type="password" 
                        placeholder="Password" 
                        value={password} 
                        onChange={e => setPassword(e.target.value)} 
                        style={{ margin: '5px 0', padding: '10px', fontSize: '16px', fontFamily: 'Montserrat, sans-serif' }}
                    />
                    <input 
                        type="text" 
                        placeholder="Full Name" 
                        value={name} 
                        onChange={e => setName(e.target.value)} 
                        style={{ margin: '5px 0', padding: '10px', fontSize: '16px', fontFamily: 'Montserrat, sans-serif' }}
                    />
                    <input 
                        type="email" 
                        placeholder="Email" 
                        value={email} 
                        onChange={e => setEmail(e.target.value)} 
                        style={{ margin: '5px 0', padding: '10px', fontSize: '16px', fontFamily: 'Montserrat, sans-serif' }}
                    />
                    <input 
                        type="text" 
                        placeholder="Phone" 
                        value={phone} 
                        onChange={e => setPhone(e.target.value)} 
                        style={{ margin: '5px 0', padding: '10px', fontSize: '16px', fontFamily: 'Montserrat, sans-serif' }}
                    />
                    <input 
                        type="date" 
                        placeholder="Birth Date" 
                        value={birthDate} 
                        onChange={e => setBirthDate(e.target.value)} 
                        style={{ margin: '5px 0', padding: '10px', fontSize: '16px', fontFamily: 'Montserrat, sans-serif' }}
                    />
                    <button 
                        onClick={onRegister} 
                        className="orangeCircularButton"
                        style={{ margin: '10px 0', padding: '10px', fontSize: '16px', border: 'none', cursor: 'pointer' }}
                    >
                        Register
                    </button>
                    <a
                        onClick={() => changeState(false)}
                        style={{ margin: '10px 0', padding: '10px', fontSize: '16px', textDecoration:'underline', textAlign: 'center', color: 'black', border: 'none', borderRadius: '5px', cursor: 'pointer', fontFamily: 'Montserrat, sans-serif' }}
                    >
                        Already have an account?
                    </a>
                </div>
            </div>
        </div>
    );
}

export default RegisterPage;
