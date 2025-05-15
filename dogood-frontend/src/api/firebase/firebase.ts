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

var messaging: any = null;
try{
    const app = initializeApp(firebaseConfig);

    messaging = getMessaging();
}catch(e){
}

export const requestForToken = async (): Promise<string | null> => {
    try{
        if (Notification.permission === 'denied') {
            return null;
        }
        if(messaging == null){
            return null;
        }
        return getToken(messaging,{ vapidKey: `BG7pYwEVn46W0K0WiLq-m1us2z8z_rEFmVsA8BNLgTBpfcwM6u0bLXVwiK3g280ap7uDTccSfL5e8oKOCrSPOsk` })
    }catch (e){
        return null;
    }
};