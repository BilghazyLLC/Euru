let allUsers = [];
let businesses = [];
let errors = [];
let jobs = [];
let requests = [];

// 
const loadUsers = async () => {
    await db.collection('euru_users')
    .orderBy('timestamp','desc')
    .get().then((snapshot) => {
        snapshot.forEach(doc => {
            allUsers.push(doc.data());
        });
    }).catch((err) => {
        console.log(err.message);
    });

    return allUsers;
};

// Get all data
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
};

// <a href="#" class="avatar avatar-sm" data-toggle="tooltip" data-original-title="Ryan Tompson">
//    <img alt="Image placeholder" src="${user.profile}" class="rounded-circle">
// </a>

$(document).ready(() => {
    let results = loadUsers();
    console.log(results);
    results.then((response) => {
        response.forEach(user => {
            $('#user-table').append(`
                <tr>
                    <td>
                        
                        ${user.name}
                    </td>
                    <td>
                        ${new Date(user.timestamp).toDateString()}
                    </td>
                    <td>
                        ${user.key}
                    </td>
                    <td>
                        ${user.type === 'TYPE_CUSTOMER' ? 'Customer' : 'Service Provider'}
                    </td>
                    <td class="text-center">
                        <a href="#user" class="btn btn-info">Copy Key</a>
                    </td>
                </tr>
                `);
        });
    })
    initDashboardPageCharts();

});

const showData = (key) => {
    console.log(key);
};