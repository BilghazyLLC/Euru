import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

// Init Application through admin
admin.initializeApp();

// Update firestore settings
const firestore = admin.firestore();
const settings = {
    timestampsInSnapshots: true
};
firestore.settings(settings);

export const tokenUploaded = functions.firestore.document('euru_tokens/{uid}').onCreate((change,context) => {
    const uid = context.params.uid;
    const token = change.data().key;
    return admin.messaging().sendToDevice(token,{
        data : {
            message: `We are glad to have you onboard, ${change.data().name}!`,
            type: "EURU-DEVICE-TOKEN-UPDATE",
            uid: uid,
            id: `${change.id}`
        }
    }).then((value) => {
        return console.log("token update message sent");
    }).catch((reason) => {
        if (reason) {
            return console.log(reason.message);
        }
    });
});

// Send service request notification to devices
export const sendUserRequest = functions.firestore.document('euru_requests/{userKey}/{dayOfWeek}/{requestId}')
    .onWrite((snapshot, ctx) => {
        
        const payload = {
            data: {
                requestModelKey: ctx.params.requestId,
                userKey: ctx.params.userKey,
                day: ctx.params.dayOfWeek,
                header: 'New request received',
                body: snapshot.after.data().title,
                image: snapshot.after.data().image
            }
        };

        // Log image url to the console
        if (payload.data.image != null || payload.data.image != '')
            console.log(`Image from data: ${payload.data.image}`);

        // 
        return admin.firestore().doc(`euru_tokens/${snapshot.after.data().providerId}`).get().then((change) => {
            if (change.data().type === 'TYPE_BUSINESS') {
                //Send request to the specific device
                return admin.messaging().sendToDevice(change.data().key, payload).then((result) => {
                    return console.log(`Request notification sent for business' topics.`, result);
                }).catch((err) => {
                    if (err) {
                        return console.log(err.message);
                    }
                });
            } else {
                return console.log("Not a business model");
            }
        }).catch((err) => {
            if (err) {
                return console.log(err.message);
            }
        });
    });

    // Send service request notification to devices
export const sendTestUserRequest = functions.firestore.document('test_requests/{userKey}/{dayOfWeek}/{requestId}')
.onWrite((snapshot, ctx) => {
    
    const payload = {
        data: {
            requestModelKey: ctx.params.requestId,
            userKey: ctx.params.userKey,
            day: ctx.params.dayOfWeek,
            header: 'New request received',
            body: snapshot.after.data().title,
            image: snapshot.after.data().image
        }
    };

    // Log image url to the console
    if (payload.data.image !== null || payload.data.image !== '')
        console.log(`Image from data: ${payload.data.image}`);

    // 
    return admin.firestore().doc(`euru_tokens/${snapshot.after.data().providerId}`).get().then((change) => {
        if (change.data().type === 'TYPE_BUSINESS') {
            //Send request to the specific device
            return admin.messaging().sendToDevice(change.data().key, payload).then((result) => {
                return console.log(`Request notification sent for business' topics.`, result);
            }).catch((err) => {
                if (err) {
                    return console.log(err.message);
                }
            });
        } else {
            return console.log("Not a business model");
        }
    }).catch((err) => {
        if (err) {
            return console.log(err.message);
        }
    });
});

// Update job reference
export const updateJobReference = functions.firestore.document('euru_jobs/{providerKey}/{path}/{requestId}')
    .onCreate((snapshot, context) => {
        const path = context.params.path;
        const providerKey = context.params.providerKey;
        const requestId = context.params.requestId;

        // Get the data
        const data = snapshot.data();

        if (path === 'pending') {
            return admin.firestore().doc(`euru_jobs/${providerKey}/new/${requestId}`).delete()
                .then((action) => {
                    console.log(`Job status changed to pending. Job ID: ${requestId} and Provider ID: ${providerKey}. Written on: ${action.writeTime.toDate()}`);

                    return admin.firestore().doc(`euru_tokens/${data.userKey}`).get().then((documentSnaphot) => {
                        // Retrieve user's token from the data model
                        const token = documentSnaphot.data().key;

                        // Send notification to the user's device
                        return admin.messaging().sendToDevice(token, {
                            data: {
                                key: requestId,
                                providerKey: providerKey,
                                type: 'pending'
                            }
                        }).then((r) => {
                            return console.log("Message sent to the device of the customer.");
                        }).catch((e) => {
                            return console.log(e.message);
                        });
                    }).catch((reason) => {
                        return console.log(reason.message);
                    });

                }).catch((err) => {
                    if (err) {
                        return console.log(err.message);
                    }
                });
        } else if (path === 'completed') {
            return admin.firestore().doc(`euru_jobs/${providerKey}/pending/${requestId}`).delete()
                .then((action) => {
                    console.log(`Job status changed to completed. Job ID: ${requestId} and Provider ID: ${providerKey}. Written on: ${action.writeTime.toDate()}`);
                    return admin.firestore().doc(`euru_tokens/${data.userKey}`).get().then((documentSnaphot) => {
                        // Retrieve user's token from the data model
                        const token = documentSnaphot.data().key;

                        // Send notification to the user's device
                        return admin.messaging().sendToDevice(token, {
                            data: {
                                key: requestId,
                                providerKey: providerKey,
                                type: 'completed'
                            }
                        }).then((r) => {
                            return console.log("Message sent to the device of the customer.");
                        }).catch((e) => {
                            return console.log(e.message);
                        });
                    }).catch((reason) => {
                        return console.log(reason.message);
                    });
                }).catch((err) => {
                    if (err) {
                        return console.log(err.message);
                    }
                });
        } else {
            return console.log("Path is for new jobs. No action needed");
        }
    });

// Send notification back to the customer making the request
export const sendStatusRequestToUser = functions.firestore.document('euru_jobs/{providerKey}/new/{requestId}')
    .onUpdate((snapshot, ctx) => {
        //Get user type from the user database
        const key = snapshot.after.data().userKey;
        return firestore.doc(`euru_tokens/${key}`)
            .get().then((response) => {
                const registrationToken = response.data().key;

                const payload = {
                    data: {
                        requestId: ctx.params.requestId,
                        providerKey: ctx.params.providerKey,
                        userKey: key,
                        header: 'Request accepted. Tap for more details',
                        body: snapshot.after.data().title,
                        image: snapshot.after.data().image
                    }
                }

                return admin.messaging().sendToDevice(registrationToken, payload).then((result) => {
                    return console.log("Notification sent to the customer's device successfully");
                }).catch((err) => {
                    if (err) {
                        return console.log(err);
                    }
                });

            }).catch((error) => {
                if (error) {
                    return console.log(error);
                }
            });
    });

export const postOnNewIssue = functions.crashlytics.issue().onNew(async (issue) => {
    const issueId = issue.issueId;
    const issueTitle = issue.issueTitle;
    const appName = issue.appInfo.appName;
    const appPlatform = issue.appInfo.appPlatform;
    const latestAppVersion = issue.appInfo.latestAppVersion;

    const dbMessage = `There is a new issue - ${issueTitle} (${issueId}) ` +
        `in ${appName}, version ${latestAppVersion} on ${appPlatform}`;

    await notifyDatabase(dbMessage);
    console.log(`Posted new issue ${issueId} successfully to Slack`);
});

const notifyDatabase = (message ? : any) => {
    let docRef = admin.firestore().collection('euru_errors').doc();
    message + ` Message ID: ${docRef.id}.`

    return docRef.set({
        crashData: message
    }).then((res) => {
        return console.log("Crash info sent successfully");
    }).catch((err) => {
        if (err) {
            return console.log(err.message);
        }
    });
};