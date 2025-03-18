type ChatMessage = {
    id: number,
    sender: string,
    content: string,
    timeSent: string,
    userIsSender: boolean
}

export default ChatMessage;