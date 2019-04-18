let allUsers = [];
let businesses = [];
let errors = [];
let jobs = [];
let requests = [];

$(document).ready(() => {
    let results = getData();
    console.log(results);

});

// Get users data
const getData = async () => {
    const results = [];
    // Get all users
    await db.collection('euru_users').get().then((snapshot) => {
        snapshot.forEach(doc => {
            allUsers.push(doc.data());
            results.push(doc.data());
        });
    }).catch((err) => {
        console.log(err.message);
    });

    // get all errors
    await db.collection('euru_errors').get().then((snapshot) => {
        snapshot.forEach(doc => {
            errors.push(doc.data());
            results.push(doc.data());
        });
    }).catch((err) => {
        console.log(err.message);
    })

    // Get all businesses
    await db.collection('euru_business').get().then((snapshot) => {
        snapshot.forEach(doc => {
            businesses.push(doc.data());
            results.push(doc.data());
        });
    }).catch((err) => {
        console.log(err.message);
    })

    // get all jobs
    await db.collection('euru_jobs').get().then((snapshot) => {
        snapshot.forEach(doc => {
            jobs.push(doc.data());
            results.push(doc.data());
        });
    }).catch((err) => {
        console.log(err.message);
    });

    // Get all requests
    await db.collection('euru_requests').get().then((snapshot) => {
        snapshot.forEach(doc => {
            requests.push(doc.data());
            results.push(doc.data());
        });
    }).catch((err) => {
        console.log(err.message);
    });

    // Return all records
    return results;
}