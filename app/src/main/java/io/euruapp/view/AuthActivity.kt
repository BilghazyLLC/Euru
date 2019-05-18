package io.euruapp.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.transition.TransitionManager
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.GeoPoint
import io.codelabs.util.bindView
import io.euruapp.BuildConfig
import io.euruapp.R
import io.euruapp.core.BaseActivity
import io.euruapp.model.EuruGeoPoint
import io.euruapp.model.User
import io.euruapp.util.ConstantsUtils
import io.euruapp.util.ConstantsUtils.intentTo
import pub.devrel.easypermissions.EasyPermissions
import java.util.*

class AuthActivity(override val layoutId: Int = R.layout.activity_auth) : BaseActivity(),
    GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private val container: ViewGroup by bindView(R.id.container)
    private val content: ViewGroup by bindView(R.id.content)
    private val loading: ProgressBar by bindView(R.id.loading)

    private var client: GoogleSignInClient? = null
    private var apiClient: GoogleApiClient? = null
    private var isProvider = false
    private var response: IdpResponse? = null

    override fun onViewCreated(intent: Intent, instanceState: Bundle?) {

        // Request user's location permission
        if (!EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            EasyPermissions.requestPermissions(
                this@AuthActivity,
                "Please enable access to your location in order to complete the login process",
                RC_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        client = GoogleSignIn.getClient(this, gso)


        apiClient = GoogleApiClient.Builder(this, this, this)
            .addApi(Auth.CREDENTIALS_API)
            .build()
        apiClient!!.connect()
        toggleLoading(false)


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_LOCATION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ConstantsUtils.logResult("Permission to access user's location granted successfully")
        }
    }

    private fun toggleLoading(show: Boolean) {
        TransitionManager.beginDelayedTransition(container)
        if (show) {
            loading.visibility = View.VISIBLE
            content.visibility = View.GONE
        } else {
            loading.visibility = View.GONE
            content.visibility = View.VISIBLE
        }
    }

    fun loginAsCustomer(view: View) {
        //        intentTo(this, UserAuthActivity.class);
        toggleLoading(true)
        //        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        //                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        //Replacing google login with firebase auth ui
        //        startActivityForResult(client.getSignInIntent(), RC_LOGIN);
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setLogo(R.drawable.app_logo)
                .setTosAndPrivacyPolicyUrls(
                    "https://bilghazy.firebaseapp.com/tos.html",
                    "https://bilghazy.firebaseapp.com/privacy.html"
                )
                .setIsSmartLockEnabled(!BuildConfig.DEBUG /* credentials */, true /* hints */)
                .setAvailableProviders(
                    Arrays.asList<AuthUI.IdpConfig>(
                        AuthUI.IdpConfig.GoogleBuilder().build(),
                        AuthUI.IdpConfig.PhoneBuilder().build(),
                        AuthUI.IdpConfig.EmailBuilder().build()
                    )
                )
                .build(),
            RC_LOGIN
        )
        isProvider = false
    }

    //Login using Google Account
    fun loginAsProvider(view: View) {
        toggleLoading(true)
        //        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        //                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        //Replacing google login with firebase auth ui
        //        startActivityForResult(client.getSignInIntent(), RC_LOGIN);
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setLogo(R.drawable.app_logo)
                .setTosAndPrivacyPolicyUrls(
                    "https://bilghazy.firebaseapp.com/tos.html",
                    "https://bilghazy.firebaseapp.com/privacy.html"
                )
                .setIsSmartLockEnabled(!BuildConfig.DEBUG /* credentials */, true /* hints */)
                .setAvailableProviders(
                    Arrays.asList<AuthUI.IdpConfig>(
                        AuthUI.IdpConfig.GoogleBuilder().build(),
                        AuthUI.IdpConfig.PhoneBuilder().build(),
                        AuthUI.IdpConfig.EmailBuilder().build()
                    )
                )
                .build(),
            RC_LOGIN
        )
        isProvider = true
    }

    override fun onStop() {
        super.onStop()
        apiClient!!.disconnect()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_LOGIN) {
            response = IdpResponse.fromResultIntent(data)
            // Successfully signed in
            if (resultCode == Activity.RESULT_OK) {
                updateUI(auth.currentUser)
            } else {
                toggleLoading(false)
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    ConstantsUtils.logResult("User cancelled login process")
                    return
                }

                if (response?.error!!.errorCode == ErrorCodes.NO_NETWORK) {
                    ConstantsUtils.showToast(applicationContext, "Unable to connect to the internet")
                    return
                }

                ConstantsUtils.showToast(applicationContext, "Unable to login user")

            }
        }
    }

    private fun updateUI(firebaseUser: FirebaseUser?) {
        if (firebaseUser == null) {
            toggleLoading(false)
            ConstantsUtils.showToast(
                this,
                "There was a problem in retrieving your account details"
            )
            return
        }

        //Get all users
        firestore.collection(ConstantsUtils.COLLECTION_USERS)
            .whereEqualTo("key", firebaseUser.uid)
            .get().addOnCompleteListener(this) { task ->
                if (task.isSuccessful && task.result != null) {
                    val documents = task.result!!.documents
                    if (documents.isEmpty()) {
                        ConstantsUtils.logResult("No documents found for the current user...")
                        //No user data found, so we query for pending approvals
                        if (isProvider) {
                            getPendingAccountForUser(firebaseUser)
                            return@addOnCompleteListener
                        }

                        // Create a new account for users who are not service providers
                        createNewUser(firebaseUser)
                    } else {
                        val snapshot = documents[0]
                        if (snapshot != null && snapshot.exists()) {
                            val user = snapshot.toObject(User::class.java)
                            if (isProvider) user?.setType(User.TYPE_BUSINESS) else user?.setType(User.TYPE_CUSTOMER)
                            database.user = user

                            ConstantsUtils.showToast(
                                this@AuthActivity,
                                String.format(
                                    "Welcome back, %s",
                                    firebaseUser.displayName ?: firebaseUser.phoneNumber ?: firebaseUser.email
                                )
                            )

                            intentTo(this@AuthActivity, HomeActivity::class.java)
                            finishAfterTransition()

                        } else {
                            toggleLoading(false)
                            ConstantsUtils.logResult("User data not found")
                            ConstantsUtils.showToast(
                                this@AuthActivity,
                                "Account cannot be null. There was a problem in retrieving your account details"
                            )
                        }
                    }
                } else {
                    toggleLoading(false)
                    ConstantsUtils.logResult("User data not found for key: " + firebaseUser.uid)
                    ConstantsUtils.showToast(
                        this@AuthActivity,
                        "There was a problem in retrieving your account details"
                    )
                }
            }
            .addOnFailureListener(this) {
                toggleLoading(false)
                ConstantsUtils.logResult("No internet")
                ConstantsUtils.showToast(
                    this@AuthActivity,
                    "Account cannot be null. There was a problem in retrieving your account details"
                )
            }
    }

    private fun createNewUser(firebaseUser: FirebaseUser, isPending: Boolean = false) {
        // Create new user account
        val user = User(
            firebaseUser.displayName,
            firebaseUser.uid,
            if (isProvider) User.TYPE_BUSINESS else User.TYPE_CUSTOMER,
            if (firebaseUser.photoUrl != null) firebaseUser.photoUrl.toString() else "",
            null,
            EuruGeoPoint(GeoPoint(tracker.latitude, tracker.longitude))
        )

        //Store user data in the database
        if (isPending) {
            firestore.collection(ConstantsUtils.COLLECTION_PENDING_REGISTRATION)
                .document(Objects.requireNonNull(firebaseUser.uid))
                .set(user)
                .addOnCompleteListener(this@AuthActivity) { task1 ->
                    if (task1.isSuccessful) {
                        toggleLoading(false)
                        //todo: add prefs here
                        intentTo(this, PendingRegistrationActivity::class.java)

                    } else {
                        toggleLoading(false)
                        ConstantsUtils.logResult("User data cannot be set")
                        ConstantsUtils.showToast(
                            this@AuthActivity,
                            "Account cannot be null. There was a problem in retrieving your account details"
                        )
                    }
                }
            return
        }

        firestore.collection(ConstantsUtils.COLLECTION_USERS)
            .document(Objects.requireNonNull(firebaseUser.uid))
            .set(user)
            .addOnCompleteListener(this@AuthActivity) { task1 ->
                if (task1.isSuccessful) {
                    toggleLoading(false)
                    database.user = user
                    ConstantsUtils.showToast(
                        this@AuthActivity,
                        String.format(
                            "Hello there, %s",
                            firebaseUser.displayName ?: firebaseUser.phoneNumber ?: firebaseUser.email
                        )
                    )

                    //Navigate the user to the provider login screen
                    if (response?.providerType == "phone" && user.name.isNullOrEmpty()) {
                        intentTo(
                            this@AuthActivity,
                            /*if (isProvider) ProviderLoginActivity::class.java else */
                            AccountCompletion::class.java
                        )
                        finish()
                    } else {
                        intentTo(
                            this@AuthActivity,
                            if (user.type == User.TYPE_BUSINESS) ProviderLoginActivity::class.java else HomeActivity::class.java
                        )
                    }
                } else {
                    toggleLoading(false)
                    ConstantsUtils.logResult("User data cannot be set")
                    ConstantsUtils.showToast(
                        this@AuthActivity,
                        "Account cannot be null. There was a problem in retrieving your account details"
                    )
                }
            }
    }

    private fun getPendingAccountForUser(firebaseUser: FirebaseUser) {
        firestore.collection(ConstantsUtils.COLLECTION_PENDING_REGISTRATION)
            .whereEqualTo("key", firebaseUser.uid)
            .get()
            .addOnCompleteListener(this@AuthActivity) {
                if (it.isSuccessful) {
                    if (it.result != null && it.result!!.documents.isNotEmpty()) {
                        toggleLoading(false)
                        database.user = User(
                            firebaseUser.displayName,
                            firebaseUser.uid,
                            User.TYPE_BUSINESS,
                            firebaseUser.photoUrl.toString(),
                            firebaseUser.phoneNumber,
                            EuruGeoPoint(GeoPoint(5.6545985,-0.1840723))
                        )
                        database.isPending = true
                        intentTo(this, PendingRegistrationActivity::class.java)
                        finishAfterTransition()
                    } else createNewUser(firebaseUser, true)
                }
            }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        try {
            ConstantsUtils.logResult("Google API failed to connect to service. Reason: " + connectionResult.errorMessage!!)
            apiClient!!.connect()
        } catch (e: Exception) {
            ConstantsUtils.logResult(e.localizedMessage)
        }
    }

    override fun onConnected(bundle: Bundle?) {
        ConstantsUtils.logResult("Google API connected")

        //Connected
        if (apiClient!!.isConnected) {
            val account = GoogleSignIn.getLastSignedInAccount(this)
            if (account != null) {
                ConstantsUtils.logResult(account.id)

                Auth.CredentialsApi.disableAutoSignIn(apiClient).addStatusListener { status ->
                    ConstantsUtils.logResult(
                        if (status.isSuccess)
                            "User account has been removed successfully"
                        else
                            "Unable to clear user's account"
                    )
                }

                //Google client auto sign in disabled
                client!!.signOut().addOnCompleteListener(this@AuthActivity) { task ->
                    ConstantsUtils.logResult(
                        if (task.isSuccessful)
                            "User is signed out"
                        else
                            "Unable to sign out user"
                    )
                }.addOnFailureListener(this@AuthActivity) { e -> ConstantsUtils.logResult(e.localizedMessage) }
            }
        }
    }

    override fun onConnectionSuspended(i: Int) {
        ConstantsUtils.logResult("Google API suspended")
    }

    companion object {

        //Static Request Code for the Google Login
        private const val RC_LOGIN = 123
        private const val RC_LOCATION = 12
    }
}
