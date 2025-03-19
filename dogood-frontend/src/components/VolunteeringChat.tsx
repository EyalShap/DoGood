import {useParams} from "react-router-dom";
import ChatMessage from "../models/ChatMessage.ts";
import {FormEvent, useEffect, useRef, useState} from "react";
import {deleteMessage, getVolunteeringChatMessages, sendVolunteeringMessage} from "../api/chat_api.ts";
import {format, isToday, isYesterday} from "date-fns";
import "../css/Chat.css";
import {IoSend} from "react-icons/io5";
import {host} from "../api/general.ts";
import {Client} from "@stomp/stompjs";
import {FaRegTrashAlt} from "react-icons/fa";

function MessageComponent({model} : {model:ChatMessage}) {
    const [timeSent, setTimeSent] = useState("");


    const deleteSelf = async () => {
        try{
            await deleteMessage(model.id);
        }catch (e){
            alert(e)
        }
    }

    useEffect(() => {
        let date = new Date(model.timeSent);
        if(isToday(date)){
            setTimeSent(`Today at ${format(date, "H:mm")}`)
        }else if(isYesterday(date)){
            setTimeSent(`Yesterday at ${format(date, "H:mm")}`)
        }else{
            setTimeSent(`${format(date, "MMMM do, YYYY")} at ${format(date, "H:mm")}`)
        }
    }, []);
    return (
        <div className={`message ${model.userIsSender ? "messageSender" : "messageRecipient"}`}>
            <div className="senderRow">
                <p className="senderName">{model.sender}</p>
                <p className="sentOn">{timeSent}</p>
            </div>
            <h2 className="messageContent">{model.content}</h2>
            {model.userIsSender &&
                <div className="messageOptions">
                <button onClick={deleteSelf}><FaRegTrashAlt /></button>
            </div>}
        </div>
    )
}

function VolunteeringChat() {
    let { id } = useParams();
    const [messages, setMessages] = useState<ChatMessage[]>([]);
    const [typedMessage, setTypedMessage] = useState("");
    const [connected, setConnected] = useState(false);
    const fetchMessages = async () => {
        try{
            setMessages(await getVolunteeringChatMessages(parseInt(id!)));
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
            await sendVolunteeringMessage(parseInt(id!), typedMessage);
            setTypedMessage("");
        }catch (e){
            alert(e);
        }
    }

    useEffect(() => {
        fetchMessages();
        const client = new Client({
            brokerURL: host+"/ws-message",
            connectHeaders: {
                "Authorization": localStorage.getItem("token")!
            },
            reconnectDelay: 5000,
            onConnect: () => {
                console.log("Connected!");
                if(!connected) {
                    client.subscribe(`/topic/volchat/${id}`, msg => {
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

export default VolunteeringChat