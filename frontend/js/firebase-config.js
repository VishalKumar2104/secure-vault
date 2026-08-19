/**
 * Secure Vault - Firebase Authentication Configuration
 * 
 * Replace the configuration values below with your Firebase project credentials
 * from the Firebase Console (Project Settings -> General -> Your apps -> Web app).
 */
const firebaseConfig = {
    apiKey: "AIzaSyBJg8a3hLmin8qkwddvg80mWHJIsbAW3mE",
    authDomain: "vault-b07bd.firebaseapp.com",
    projectId: "vault-b07bd",
    storageBucket: "vault-b07bd.firebasestorage.app",
    messagingSenderId: "592188247077",
    appId: "1:592188247077:web:f855f258d84b73b9f343cc",
    measurementId: "G-NNFBLZJQR3"
};

// Initialize Firebase App if not already initialized
if (typeof firebase !== 'undefined') {
    if (!firebase.apps.length) {
        try {
            firebase.initializeApp(firebaseConfig);
            console.log("Firebase App initialized successfully.");
        } catch (e) {
            console.warn("Firebase initialization notice:", e.message);
        }
    }
}

/**
 * Helper to get active Firebase ID token
 */
async function getFirebaseToken() {
    try {
        if (typeof firebase !== 'undefined' && firebase.auth) {
            const currentUser = firebase.auth().currentUser;
            if (currentUser) {
                return await currentUser.getIdToken(false);
            }
        }
    } catch (e) {
        console.warn("Failed to retrieve Firebase token:", e);
    }
    return localStorage.getItem('sv_token');
}
