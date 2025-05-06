self.addEventListener('notificationclick', function(event) {
    event.notification.close();

    const urlToOpen = event.notification.data?.url || '/'; // fallback to homepage

    event.waitUntil(
        clients.matchAll({ type: 'window', includeUncontrolled: true }).then(function(clientList) {
            for (const client of clientList) {
                if (client.url === urlToOpen && 'focus' in client) {
                    return client.focus();
                }
            }
            if (clients.openWindow) {
                return clients.openWindow(urlToOpen);
            }
        })
    );
});

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

try {
    firebase.initializeApp(firebaseConfig);
    const messaging = firebase.messaging();
    messaging.setBackgroundMessageHandler((payload) => {
        const notification = {
            body: payload.data.body,
            icon: "/fcm-icon.png",
            badge: "/fcm-icon.png",
            data: { url: payload.data.click_action }
        }
        self.registration.showNotification(payload.data.title,notification);
        alert(payload)
    })
}catch (e){

}