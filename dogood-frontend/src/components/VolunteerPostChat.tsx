import {useNavigate, useParams} from "react-router-dom";
import ChatMessage from "../models/ChatMessage.ts";
import {FormEvent, useEffect, useRef, useState} from "react";
import {
    deleteMessage,
    getPostChatMessages,
    getVolunteeringChatMessages,
    sendPostMessage,
    sendVolunteeringMessage
} from "../api/chat_api.ts";
import {format, isToday, isYesterday} from "date-fns";
import "../css/Chat.css";
import {IoSend} from "react-icons/io5";
import {host} from "../api/general.ts";
import {Client} from "@stomp/stompjs";
import {FaRegTrashAlt} from "react-icons/fa";
import {MessageComponent} from "./VolunteeringChat.tsx";

function VolunteerPostChat({ other } : { other: boolean}) {
    let { id , username} = useParams();
    const [messages, setMessages] = useState<ChatMessage[]>([]);
    const [typedMessage, setTypedMessage] = useState("");
    const [connected, setConnected] = useState(false);
    const userWith = other ? username : localStorage.getItem("username");
    const fetchMessages = async () => {
        try{
            setMessages(await getPostChatMessages(parseInt(id!),userWith!));
        }catch (e){
            alert(e);
        }
    }
    const bottomOfChat = useRef<HTMLDivElement>();
    const containerRef = useRef<HTMLDivElement>();
    const shouldScrollRef = useRef(true);

    const sendMessage = async (event: FormEvent) => {
        event.preventDefault();
        try{
            if(typedMessage !== "") {
                await sendPostMessage(parseInt(id!), userWith!, typedMessage);
                setTypedMessage("");
            }
        }catch (e){
            alert(e);
        }
    }

    useEffect(() => {
        fetchMessages();
        const client = new Client({
            brokerURL: host+"/api/ws-message",
            connectHeaders: {
                "Authorization": localStorage.getItem("token")!
            },
            reconnectDelay: 5000,
            onConnect: () => {
                console.log("Connected!");
                if(!connected) {
                    client.subscribe(`/topic/postchat/${id}/${userWith}`, msg => {
                        let body = JSON.parse(msg.body);
                        console.log(body)
                        let el = containerRef.current
                        shouldScrollRef.current =
                            el!.scrollHeight - el!.scrollTop - el!.clientHeight < 50;
                        if(body.type === "ADD"){
                            let newMessage: ChatMessage = body.payload
                            newMessage.userIsSender = newMessage.sender === localStorage.getItem("username")
                            setMessages(prevState => prevState.concat([newMessage]))
                        }else if(body.type === "DELETE"){
                            let messageToDelete: ChatMessage = body.payload
                            setMessages(prevState => prevState.filter(message => message.id !== messageToDelete.id))
                        }
                    });
                    setConnected(true);
                }
            },
            onDisconnect: () => {
                setConnected(false);
            }
        });
        if(!connected) {
            client.activate();
        }
        return () => client.deactivate();
    }, []);

    useEffect(() => {
        if(shouldScrollRef.current) {
            bottomOfChat.current!.scrollIntoView({behavior: 'smooth'});
        }
    }, [messages]);

    return (
        <div className="chat">
            <div ref={containerRef} className="messages">
                {messages.map(message => <MessageComponent model={message}/>)}
                <div ref={bottomOfChat}></div>
            </div>
            <form className="bottom-bar" onSubmit={sendMessage}>
                <input className="typing" onChange={e => setTypedMessage(e.target.value)} value={typedMessage}/>
                <button className="send" type="submit"><IoSend/></button>
            </form>
        </div>
    )
}

export default VolunteerPostChat