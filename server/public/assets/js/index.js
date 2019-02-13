$(document).ready(function () {
    console.log("Home page is ready");

    try {
        let app = firebase.app();
        let features = ['auth', 'database', 'firestore', 'messaging', 'storage'].filter(feature => typeof app[feature] === 'function');
        console.log(`Firebase SDK loaded with ${features.join(', ')}`);

    } catch (e) {
        console.error(e);
        console.log('Error loading the Firebase SDK, check the console.');

    }
});