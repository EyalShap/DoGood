type ChatMessage = {
    id: number,
    sender: string,
    content: string,
    timeSent: string,
    userIsSender: boolean,
    edited: boolean,
    timeEdited: string
}

export default ChatMessage;