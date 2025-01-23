import { useEffect, useState } from "react"
import { scanCode } from "../api/volunteering_api";
import { useNavigate, useParams } from "react-router-dom";
import { Scanner } from '@yudiel/react-qr-scanner';

function CodeScan() {
    const navigate = useNavigate();

    const onScan = async (result: string) => {
        try{
            await scanCode(result);
            navigate(`/volunteering/${result.split(":")[0]}`)
        }catch(e){
            alert(e)
        }
    }

    return (
        <div>
            <h1>Code Scanner</h1>
            <Scanner onScan={(result) => onScan(result[0].rawValue)}/>
        </div>
    )
}

export default CodeScan;