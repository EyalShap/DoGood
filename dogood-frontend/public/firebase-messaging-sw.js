importScripts('https://www.gstatic.com/firebasejs/8.2.0/firebase-app.js');
importScripts('https://www.gstatic.com/firebasejs/8.2.0/firebase-messaging.js');

const firebaseConfig = {
    apiKey: "AIzaSyDvo1lrHi1OXPK7pSmPQTRbg1UBrNZSFxI",
    authDomain: "dogood-1039b.firebaseapp.com",
    projectId: "dogood-1039b",
    storageBucket: "dogood-1039b.firebasestorage.app",
    messagingSenderId: "424800092882",
    appId: "1:424800092882:web:f3aee6b71749ede087a30b",
    measurementId: "G-FKCDKBY7EE"
};

firebase.initializeApp(firebaseConfig);
const messaging = firebase.messaging();