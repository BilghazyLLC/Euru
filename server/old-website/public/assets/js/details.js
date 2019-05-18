$(document).on('ready', () => {
    var providerKey = window.localStorage.getItem('provider-key');

    db.collection('euru_pending_approvals')
        .doc(providerKey).get().then(snapshot => {
            if (snapshot.exists) {
                // setup UI
                bindUI(snapshot.data());
            } else {
                alert('Sorry! We could not find this service provider');
            }
        }).catch(err => {
            alert(err.message)
        });
});

const bindUI = (userData) => {
    console.log(userData);

    // Set details field
    $('#user-avatar').attr('src', userData.profile ? userData.profile : 'https://firebasestorage.googleapis.com/v0/b/bilghazyllc.appspot.com/o/default-avatar.png?alt=media&token=ac26c0ed-739d-491c-b301-0afcf9ecae40');
    $('#user-name').text(userData.name);
    $('#user-email').text(userData.phone);

    
};