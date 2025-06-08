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


var CACHE_NAME = "cache";
const version = "0.0.1";

// Install a service worker
this.addEventListener("install", (event) => {
    event.waitUntil(
        caches.open(CACHE_NAME).then((cache) => {
            return cache.addAll([
                "/offline.html",
                "/snail.png",
                "/family.png",
                "/logodogood.png",
                "/fcm-icon.png"
            ])
        })
    )
})

// Update a service worker
// Inside the service worker’s activate event, delete all
// previously cached files if necessary
self.addEventListener("activate", function (event) {
    event.waitUntil(
        caches.keys().then(function (cacheNames) {
            return Promise.all(
                cacheNames.map(function (cacheName) {
                    if (CACHE_NAME !== cacheName && cacheName.startsWith("cache")) {
                        return caches.delete(cacheName);
                    }
                })
            );
        })
    );
});

// Cache and return requests
self.addEventListener('fetch', event => {
    if (event.request.mode === 'navigate' ||
        (event.request.method === 'GET' && event.request.headers.get('accept').includes('text/html'))) {
        event.respondWith(
            fetch(event.request.url).catch(error => {
                return caches.match("offline.html");
            })
        );
    } else {
        // if the resources arent in the cache ,
        // they are requested from the server
        event.respondWith(caches.match(event.request)
            .then(function (response) {
                return response || fetch(event.request);
            })
        );
    }
});

//for notify user when worker file is updated
self.addEventListener('message', (event) => {
    if (event.data === 'SKIP_WAITING') {
        self.skipWaiting();
    }
});