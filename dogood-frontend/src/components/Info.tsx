import {useState} from "react";
import {FaInfo} from "react-icons/fa";
import "../css/Info.css"

function Info({text}:{text: string}){

    const [isHovered, setIsHovered] = useState(false);

    return (
        <div className="infoinfo-container">
            <FaInfo
                className="infoinfo-button"
                onMouseEnter={() => setIsHovered(true)} // Show on hover
                onMouseLeave={() => setIsHovered(false)} // Hide when hover ends
            >
            </FaInfo>
            {isHovered && (
                <div className="infoinfo-tooltip">
                    {text.split('~').map((line, index) => (
                    <p key={index}>{line}</p>
                    ))}
                </div>
                )}
        </div>
    )
}

export default Info;