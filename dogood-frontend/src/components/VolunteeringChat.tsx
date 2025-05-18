import {useNavigate, useParams} from "react-router-dom";
import ChatMessage from "../models/ChatMessage.ts";
import {FormEvent, useEffect, useRef, useState} from "react";
import {deleteMessage, editMessage, getVolunteeringChatMessages, sendVolunteeringMessage} from "../api/chat_api.ts";
import {format, isToday, isYesterday} from "date-fns";
import "../css/Chat.css";
import {IoSend} from "react-icons/io5";
import {host} from "../api/general.ts";
import {Client} from "@stomp/stompjs";
import {FaEdit, FaRegTrashAlt, FaSave} from "react-icons/fa";

export function MessageComponent({model} : {model:ChatMessage}) {
    const navigate = useNavigate();
    const [enableEdit, setEnableEdit] = useState(false);
    const [editedText, setEditedText] = useState(model.content);

    const deleteSelf = async () => {
        try{
            await deleteMessage(model.id);
        }catch (e){
            alert(e)
        }
    }

    function contains_heb(str) {
        return (/[\u0590-\u05FF]/).test(str);
    }

    const editSelf = async () => {
        try{
            await editMessage(model.id,editedText);
            setEnableEdit(false);
        }catch (e){
            alert(e)
        }
    }

    const handleUserOnClick = async () => {
        navigate(`/profile/${model.sender}`);
    }

    const formatDate = (messageDate: string) => {
        let date = new Date(messageDate);
        if(isToday(date)){
            return `Today at ${format(date, "H:mm")}`;
        }else if(isYesterday(date)){
            return `Yesterday at ${format(date, "H:mm")}`;
        }
        return `${format(date, "MMMM do, yyyy")} at ${format(date, "H:mm")}`
    }
    return (
        <div className={`message ${model.userIsSender ? "messageSender" : "messageRecipient"}`}>
            <div className="senderRow">
                <p className="senderName" onClick={() => handleUserOnClick()}>{model.sender}</p>
                {model.edited ? <p className="sentOn">Edited {formatDate(model.timeEdited)}</p> : <p className="sentOn">{formatDate(model.timeSent)}</p>}
        </div>
{!enableEdit ? <h2 dir={contains_heb(model.content) ? "rtl" : "ltr"} className="messageContent">{model.content}</h2> :
            <input value={editedText} onChange={e => setEditedText(e.target.value)}
            onKeyDown={e => {
                if (e.key === 'Enter') {
                    editSelf()
            }}}/>}
            {model.userIsSender && (!enableEdit ?
                <div className="messageOptions">
                    <button className="delete" onClick={deleteSelf}><FaRegTrashAlt/></button>
                    <button className="edit" onClick={() => setEnableEdit(true)}><FaEdit/></button>
                </div> :
                <div className="messageOptions">
                    <button className="edit" onClick={editSelf}><FaSave/></button>
                </div>)}
        </div>
    )
}

function VolunteeringChat() {
    let {id} = useParams();
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
            if(typedMessage !== "") {
                await sendVolunteeringMessage(parseInt(id!), typedMessage);
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
                        }else if(body.type === "EDIT"){
                            let messageToEdit: ChatMessage = body.payload
                            setMessages(prevState => prevState.map(
                                message => message.id === messageToEdit.id ?
                                    ({ ...message, ["content"]: messageToEdit.content, ["edited"]: true, ["timeEdited"]: messageToEdit.timeEdited }) :
                                    message))
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