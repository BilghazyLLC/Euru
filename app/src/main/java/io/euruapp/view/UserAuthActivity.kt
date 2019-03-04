package io.euruapp.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.transition.TransitionManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.GeoPoint
import io.codelabs.util.bindView
import io.euruapp.R
import io.euruapp.core.BaseActivity
import io.euruapp.model.EuruGeoPoint
import io.euruapp.model.User
import io.euruapp.util.ConstantsUtils
import io.euruapp.util.ConstantsUtils.intentTo
import io.euruapp.util.ConstantsUtils.showToast
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * [User] login activity
 */
class UserAuthActivity(override val layoutId: Int = R.layout.activity_user_auth) : BaseActivity(), EasyPermissions.PermissionCallbacks, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {
    private val container: ViewGroup by bindView(R.id.container)
    private val content: ViewGroup by bindView(R.id.content)
    private val loading: ProgressBar  by bindView(R.id.loading)
    private val username: TextInputEditText by bindView(R.id.username)
    private val phone: TextInputEditText by bindView(R.id.phone)

    private var currentUser: User? = null
    private var client: GoogleSignInClient? = null
    private var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null
    private var geoPoint: GeoPoint? = null
    private var apiClient: GoogleApiClient? = null

    override fun onViewCreated(intent: Intent, instanceState: Bundle?) {
        geoPoint = GeoPoint(ConstantsUtils.DEFAULT_LAT, ConstantsUtils.DEFAULT_LNG)

        apiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build()

        if (EasyPermissions.hasPermissions(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            getCurrentLocation()
        } else
            EasyPermissions.requestPermissions(
                PermissionRequest.Builder(
                    this, RC_LOC_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ).setRationale("Enable location permission in order to complete registration process").build()
            )

        toggleLoading(false)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            //.requestIdToken(getString(R.string.euru_client_id))
            .requestEmail()
            .build()
        client = GoogleSignIn.getClient(this, gso)

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                ConstantsUtils.logResult("onVerificationCompleted:$phoneAuthCredential")
                loginAndFinish()
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                toggleLoading(false)
                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    ConstantsUtils.showToast(this@UserAuthActivity, e.getLocalizedMessage())
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    ConstantsUtils.showToast(this@UserAuthActivity, e.getLocalizedMessage())

                } else
                    ConstantsUtils.showToast(this@UserAuthActivity, e.localizedMessage)

            }

            override fun onCodeSent(
                verificationId: String?,
                forceResendingToken: PhoneAuthProvider.ForceResendingToken?
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                ConstantsUtils.logResult("onCodeSent: Verification ID: " + verificationId!!)
                ConstantsUtils.logResult("onCodeSent: Token: " + forceResendingToken!!)
            }

            override fun onCodeAutoRetrievalTimeOut(s: String?) {
                toggleLoading(false)
                ConstantsUtils.showToast(
                    this@UserAuthActivity,
                    "it seems to be taking too long to verify your phone number. Please try again"
                )
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onStart() {
        super.onStart()
        if (apiClient != null) apiClient!!.connect()
    }

    override fun onDestroy() {
        if (apiClient != null && apiClient!!.isConnected) apiClient!!.disconnect()
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        if (apiClient != null && apiClient!!.isConnected) apiClient!!.disconnect()
    }

    override fun onResume() {
        super.onResume()
        if (apiClient != null) apiClient!!.connect()
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val location = task.result
                if (location != null)
                    geoPoint = GeoPoint(location.latitude, location.longitude)
            }
        }.addOnFailureListener(this) { e ->
            ConstantsUtils.logResult(e.localizedMessage)
            ConstantsUtils.showToast(this@UserAuthActivity, "Cannot get last location")
        }
    }

    fun googleLogin(view: View) {
        toggleLoading(true)
        startActivityForResult(client!!.signInIntent, RC_GOOGLE_LOGIN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_GOOGLE_LOGIN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                updateUI(account)
            } catch (e: ApiException) {
                toggleLoading(false)
                ConstantsUtils.showToast(this@UserAuthActivity, "Check your internet connection")
            } catch (e: Exception) {
                toggleLoading(false)
                ConstantsUtils.showToast(this@UserAuthActivity, "Error occurred: " + e.localizedMessage)
            }

        }
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        if (account == null || account.id == null) {
            toggleLoading(false)
            ConstantsUtils.showToast(
                this,
                "Account cannot be null. There was a problem in retrieving your account details"
            )
            return
        }

        //Get all users
        firestore.collection(ConstantsUtils.COLLECTION_USERS).whereEqualTo("key", account.id)
            .get().addOnCompleteListener(this) { task ->
                if (task.isSuccessful && task.result != null) {
                    val documents = task.result!!.documents
                    if (documents.isEmpty()) {
                        //No user data found, so we create a new one
                        val user = User(
                            account.displayName, account.id!!, User.TYPE_CUSTOMER,
                            account.photoUrl.toString(), null, EuruGeoPoint(geoPoint!!)
                        )
                        currentUser = user

                        //Store user data in the database
                        firestore.collection(ConstantsUtils.COLLECTION_USERS)
                            .document(account.id!!)
                            .set(user)
                            .addOnCompleteListener(this@UserAuthActivity) { task ->
                                if (task.isSuccessful) {
                                    toggleLoading(false)
                                    loginAndFinish()
                                } else {
                                    toggleLoading(false)
                                    ConstantsUtils.logResult("User data cannot be set")
                                    ConstantsUtils.showToast(
                                        this@UserAuthActivity,
                                        "Account cannot be null. There was a problem in retrieving your account details"
                                    )
                                }
                            }
                    } else {
                        val snapshot = documents[0]
                        if (snapshot != null && snapshot.exists()) {
                            currentUser = snapshot.toObject(User::class.java) //Convert to User data model

                            //
                            if (currentUser!!.phone != null && !currentUser!!.phone.isEmpty()) {
                                //Verify phone number
                                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                    currentUser!!.phone, // Phone number to verify
                                    60, // Timeout duration
                                    TimeUnit.SECONDS, // Unit of timeout
                                    this@UserAuthActivity, // Activity (for callback binding)
                                    callbacks!!
                                )        // OnVerificationStateChangedCallbacks
                            } else
                                loginAndFinish()

                        } else {
                            toggleLoading(false)
                            ConstantsUtils.logResult("User data not found")
                            ConstantsUtils.showToast(
                                this@UserAuthActivity,
                                "Account cannot be null. There was a problem in retrieving your account details"
                            )
                        }
                    }
                } else {
                    toggleLoading(false)
                    ConstantsUtils.logResult("User data not found for key: " + account.id!!)
                    ConstantsUtils.showToast(
                        this@UserAuthActivity,
                        "Account cannot be null. There was a problem in retrieving your account details"
                    )
                }
            }
            .addOnFailureListener(this) { e ->
                toggleLoading(false)
                ConstantsUtils.logResult("No internet")
                ConstantsUtils.showToast(
                    this@UserAuthActivity,
                    "Account cannot be null. There was a problem in retrieving your account details"
                )
            }
    }

    private fun toggleLoading(b: Boolean) {
        TransitionManager.beginDelayedTransition(container!!)
        if (b) {
            loading!!.visibility = View.VISIBLE
            content!!.visibility = View.GONE
        } else {
            loading!!.visibility = View.GONE
            content!!.visibility = View.VISIBLE
        }
    }

    fun dismiss(view: View) {
        onBackPressed()
    }

    fun phoneLogin(view: View) {
        if (!ConstantsUtils.validateField(username!!)) {
            ConstantsUtils.showToast(this, "Enter a valid username")
            username!!.requestFocus()
            return
        } else if (!ConstantsUtils.validateField(phone!!)) {
            ConstantsUtils.showToast(this, "Enter a valid phone number")
            phone!!.requestFocus()
            return
        }

        //Phone number and username are both not empty
        var originalNumber = Objects.requireNonNull<Editable>(phone!!.text).toString()

        if (originalNumber.startsWith("0")) {
            //E.g. 0554022344
            val strippedNumber = originalNumber.substring(1)
            originalNumber = String.format("+233%s", strippedNumber)   //+233554022344
        }

        //Check for phone number validity
        if (!Patterns.PHONE.matcher(originalNumber).matches()) {
            ConstantsUtils.showToast(this, "Your phone number is invalid")
            phone!!.requestFocus()
            return
        }

        val newNumber = originalNumber
        toggleLoading(true)
        ConstantsUtils.showToast(this, "Setting up your account...")

        //Get all documents which match the following condition in the database
        firestore.collection(ConstantsUtils.COLLECTION_USERS)
            .whereEqualTo("phone", originalNumber)  //Compare phone number fields
            .get().addOnCompleteListener(this, OnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    if (task.result!!.isEmpty) {
                        toggleLoading(false)
                        ConstantsUtils.showToast(this@UserAuthActivity, "User cannot be found")
                    } else {
                        for (snapshot in task.result!!) {
                            if (snapshot != null && snapshot.exists()) {
                                val user = snapshot.toObject(User::class.java)  //Convert to User data model
                                if (user != null && user.getName().equals(
                                        Objects.requireNonNull<Editable>(username!!.text).toString(),
                                        ignoreCase = true
                                    )
                                ) {
                                    currentUser = user

                                    //Verify phone number
                                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                        newNumber, // Phone number to verify
                                        15, // Timeout duration
                                        TimeUnit.SECONDS, // Unit of timeout
                                        this@UserAuthActivity, // Activity (for callback binding)
                                        callbacks!!
                                    )        // OnVerificationStateChangedCallbacks
                                    return@OnCompleteListener
                                }
                            }
                        }
                    }
                } else {
                    toggleLoading(false)
                    ConstantsUtils.showToast(
                        this@UserAuthActivity,
                        "Cannot access your information at this time. Please try again later"
                    )
                }
            }).addOnFailureListener(this) { e ->
                toggleLoading(false)
                ConstantsUtils.showToast(this@UserAuthActivity, "Please check your internet connection")
            }

    }

    private fun loginAndFinish() {
        currentUser?.setType(User.TYPE_CUSTOMER)
        currentUser?.setAddress(EuruGeoPoint(geoPoint!!))
        database.user = currentUser
        intentTo(this@UserAuthActivity, HomeActivity::class.java)
        showToast(this@UserAuthActivity, String.format("Welcome back, %s!", currentUser?.getName()))
        finishAfterTransition()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        if (requestCode == RC_LOC_CODE) getCurrentLocation()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (requestCode == RC_LOC_CODE)
            EasyPermissions.requestPermissions(
                PermissionRequest.Builder(
                    this, RC_LOC_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ).setRationale("Enable location permission in order to complete registration process").build()
            )
    }

    override fun onConnected(bundle: Bundle?) {
        ConstantsUtils.logResult("Connected to location services")
    }

    override fun onConnectionSuspended(i: Int) {
        ConstantsUtils.logResult("Connection to location services suspended")
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        ConstantsUtils.logResult("Connection to location services failed")
    }

    companion object {

        private const val RC_LOC_CODE = 2

        private const val RC_GOOGLE_LOGIN = 3
    }
}
