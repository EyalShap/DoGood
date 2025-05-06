import {initializeApp} from "firebase/app";
import {getMessaging,getToken} from "firebase/messaging"

const firebaseConfig = {
    apiKey: "AIzaSyDvo1lrHi1OXPK7pSmPQTRbg1UBrNZSFxI",
    authDomain: "dogood-1039b.firebaseapp.com",
    projectId: "dogood-1039b",
    storageBucket: "dogood-1039b.firebasestorage.app",
    messagingSenderId: "424800092882",
    appId: "1:424800092882:web:f3aee6b71749ede087a30b",
    measurementId: "G-FKCDKBY7EE"
};

const app = initializeApp(firebaseConfig);

const messaging = getMessaging();

export const requestForToken = async (): Promise<string | null> => {
    try{
        return getToken(messaging,{ vapidKey: `NO` })
    }catch (e){
        return null;
    }
};