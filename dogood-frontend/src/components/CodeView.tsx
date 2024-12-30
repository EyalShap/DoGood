import { useEffect, useState } from "react"
import { getCode } from "../api/volunteering_api";
import { useParams } from "react-router-dom";
import QRCode from "react-qr-code";

function CodeView() {
    const [code, setCode] = useState("");
    let { id } = useParams();
    const makeCode = async () => {
        try{
        setCode(await getCode(parseInt(id!), false));
        }catch(e){
            alert(e);
        }
    }

    useEffect(() => {
        makeCode();
        const intervalId = setInterval(() => {
            makeCode();
        }, 15000);
    
        // Cleanup function to clear the timeout if the component unmounts
        return () => clearInterval(intervalId);
      }, []);
    return (
        <div>
            <h1>Changing Code</h1>
            <QRCode value={code}/>
        </div>
    )
}

export default CodeView;