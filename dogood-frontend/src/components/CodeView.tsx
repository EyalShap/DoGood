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
        <div style={{
            width: "100%",
             display: "flex",
             flexDirection: "column",
             alignItems: "center",
             fontFamily: 'Montserrat, sans-serif',
             marginBottom: "20px"}}>
            <h1 className="bigHeader" style={{marginBottom: '50px', marginTop: '50px'}}>Changing Code</h1>
            <QRCode size={300} value={code}/>
        </div>
    )
}

export default CodeView;