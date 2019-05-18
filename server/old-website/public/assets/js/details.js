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

    // Load business information
    db.collection('euru_business')
        .where('userUID', '==', userData.key)
        .get().then(querySnapshot => {
            if (querySnapshot) {
                console.log(querySnapshot.docs[0]);
                
                var snapshot = querySnapshot.docs[0];
                if (snapshot && snapshot.exists) {
                    console.log(snapshot.data());

                    var business = snapshot.data();

                    // Setup Business information
                    $('#business-name').val(business.name);
                    $('#business-category').val(business.category);
                    $('#business-phone').val(business.phone);
                    $('#business-desc').val(business.desc);
                    $('#business-image').attr('src', business.image ? business.image : './assets/img/bg5.jpg').css({'width': '100%', 'height': '370px'});

                    //navigator.geolocation.getCurrentPosition()

                } else {
                    alert('This service provider has no regsitered business yet');
                }
            }
        })

};