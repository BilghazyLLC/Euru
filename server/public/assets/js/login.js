$(document).ready(() => {
    console.log("Page is ready");

});

// Sign in with email and password
const login = () => {
    // Get email and password
    var email = $('#email').val(),
     password = $('#password').val();

    //  Validate email and password
    if (validator.isEmpty(email) || !validator.isEmail(email)) {
        alert("Please enter a valid email address to get started...")
    } else if (validator.isEmpty(password)) {
        alert("Your password is required to continue...")
    } else {
        // Sign in admin
        auth.signInWithEmailAndPassword(email, password).then((firebaseUser) => {
            if (firebaseUser) {
                window.location.href = "dashboard.html"
            } else {
                alert("Unable to sign in user...");
                $('#email').val('')
                $('#password').val('')
            }
        }).catch((err) => {
            alert(err.message)
        });
    }
};