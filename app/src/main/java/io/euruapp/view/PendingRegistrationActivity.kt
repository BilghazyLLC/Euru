package io.euruapp.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.databinding.DataBindingUtil
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.iid.FirebaseInstanceId
import io.codelabs.sdk.util.debugLog
import io.codelabs.sdk.util.toast
import io.euruapp.R
import io.euruapp.core.BaseActivity
import io.euruapp.databinding.ActivityPendingRegistrationBinding
import io.euruapp.model.Business
import io.euruapp.util.ConstantsUtils
import io.euruapp.util.extractNames
import io.euruapp.util.validate
import java.util.*

class PendingRegistrationActivity(override val layoutId: Int = R.layout.activity_pending_registration) :
    BaseActivity() {

    private lateinit var binding: ActivityPendingRegistrationBinding
    // Business ID
    private var businessId: String? = ""

    override fun onViewCreated(intent: Intent, instanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pending_registration)

        // Send user device registration token to the server
        sendRegistration()

        // Bind user's data to UI
        binding.user = database.user

        getBusinessForProvider()
    }

    private fun getBusinessForProvider() {
        try {
            firestore.collection(ConstantsUtils.COLLECTION_BUSINESS)
                .whereEqualTo("userUID", database.user?.key)
                .get()
                .addOnCompleteListener(this) {
                    if (it.isSuccessful) {
                        val b = it.result?.documents?.isNotEmpty()
                        if (b != null && b) {
                            binding.business = it.result!!.documents[0].toObject(Business::class.java)
                            businessId = binding.business?.key
                            if (binding.business != null && binding.business!!.addresses!!.isEmpty()) allowLocationRegistration()
                        } else showTutorial()
                    } else showTutorial()
                }
        } catch (ex: Exception) {
            debugLog(ex.localizedMessage)
        }
    }

    private fun showTutorial() {
        MaterialDialog(this).show {
            title(text = "Welcome to Euru Dashboard")
            message(res = R.string.business_registration_tutorial)
            positiveButton(text = "Ok") { it.dismiss() }
            cancelOnTouchOutside(false)
        }
    }

    private fun sendRegistration() {
        //Send registration to server
        //todo: fix AUTHENTICATION_FAILED exception
        val user = database.user
        auth.signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
                    if (it.isSuccessful) {
                        //Create hash map
                        val hashMap = HashMap<Any, Any?>(0)
                        hashMap["key"] = it.result?.token
                        hashMap["type"] = user.type
                        hashMap["uid"] = user.key
                        hashMap["name"] = user.name
                        hashMap["timestamp"] = System.currentTimeMillis().toString()
                        hashMap["service"] = null
                        hashMap["address"] = GeoPoint(tracker.latitude, tracker.longitude)

                        //Push data to database
                        firestore.collection(ConstantsUtils.COLLECTION_TOKENS)
                            .document(user.key)
                            .set(hashMap)
                            .addOnCompleteListener { task1 ->
                                ConstantsUtils.logResult(it.result?.token)
                                ConstantsUtils.logResult(if (task1.isSuccessful) "Device Token uploaded" else "Failed to upload device token")
                            }.addOnFailureListener { e -> ConstantsUtils.logResult(e.localizedMessage) }
                    } else {
                        ConstantsUtils.logResult("ID Token error: ${it.exception?.localizedMessage}")
                    }
                }
            }
        }
    }

    fun cancelRegistration(view: View) {
        MaterialDialog(this).show {
            title(res = R.string.cancel_registration)
            message(text = "Do you wish to cancel your ${getString(R.string.app_name)} registration?")
            positiveButton(text = "Ok") { dialog ->
                dialog.dismiss()

                val snackbar = Snackbar.make(binding.container, "Please wait", Snackbar.LENGTH_LONG)
                snackbar.show()
                firestore.collection(ConstantsUtils.COLLECTION_PENDING_REGISTRATION).document(database.user.key)
                    .delete()
                    .addOnCompleteListener(this@PendingRegistrationActivity) {
                        if (it.isSuccessful) {
                            database.logout()
                            database.isPending = false
                            startActivity(Intent(this@PendingRegistrationActivity, AuthActivity::class.java)).also {
                                toast("Your registration was cancelled successfully")
                                finishAfterTransition()
                            }
                        } else {
                            snackbar.setText("Unable to delete your registration")
                                .setDuration(BaseTransientBottomBar.LENGTH_SHORT)
                                .show()
                        }
                    }.addOnFailureListener(this@PendingRegistrationActivity) {
                        snackbar.setText(it.localizedMessage)
                            .setDuration(BaseTransientBottomBar.LENGTH_SHORT)
                            .show()
                    }
            }
            negativeButton(text = "Dismiss") { it.dismiss() }
        }
    }

    fun editUser(view: View) {
        val profileView = layoutInflater.inflate(R.layout.profile_edit_dialog, null, false)
        val usernameUser = profileView.findViewById<TextInputEditText>(R.id.profile_username).apply {
            setText(binding.user?.name)
        }

        val phoneUser = profileView.findViewById<TextInputEditText>(R.id.profile_phone).apply {
            setText(binding.user?.phone)
        }

        val aboutUser = profileView.findViewById<TextInputEditText>(R.id.profile_about)

        MaterialDialog(this).show {
            title(text = "Update profile information")
            customView(view = profileView, dialogWrapContent = true)
            positiveButton(text = "Save") {
                it.dismiss()

                val snackbar = Snackbar.make(binding.container, "Please wait", Snackbar.LENGTH_LONG)
                snackbar.show()

                firestore.collection(ConstantsUtils.COLLECTION_PENDING_REGISTRATION).document(database.user.key)
                    .update(
                        mapOf<String, Any?>(
                            "phone" to phoneUser.text.toString(),
                            "name" to usernameUser.text.toString(),
                            "aboutMe" to aboutUser.text.toString()
                        )
                    ).addOnCompleteListener {
                        database.user?.apply {
                            phone = phoneUser.text.toString()
                            name = usernameUser.text.toString()
                            database.user = this
                        }
                    }.addOnFailureListener { exception ->
                        snackbar.setText(
                            exception.localizedMessage ?: "Unable to register your business"
                        ).setDuration(BaseTransientBottomBar.LENGTH_LONG).show()
                    }

            }
            negativeButton(text = "Dismiss") { it.dismiss() }
            cancelOnTouchOutside(false)
        }
    }

    fun registerBusiness(view: View) {
        val businessView = layoutInflater.inflate(R.layout.business_edit_dialog, null, false)
        val businessName = businessView.findViewById<TextInputEditText>(R.id.business_name)
        val businessDesc = businessView.findViewById<TextInputEditText>(R.id.business_desc)
        val businessPhone = businessView.findViewById<TextInputEditText>(R.id.business_phone)
        val businessCategory = businessView.findViewById<Spinner>(R.id.business_category).apply {
            adapter = ArrayAdapter(
                this@PendingRegistrationActivity,
                android.R.layout.simple_dropdown_item_1line,
                ConstantsUtils.categories.extractNames()
            )
        }

        MaterialDialog(this).show {
            title(text = "Register business")
            customView(view = businessView, dialogWrapContent = true)
            positiveButton(text = "Continue") {
                it.dismiss()

                val snackbar = Snackbar.make(
                    binding.container,
                    "Registering your business. This might take some time",
                    Snackbar.LENGTH_INDEFINITE
                )

                if (businessName.validate() && businessDesc.validate() && businessPhone.validate() && businessCategory.validate()) {
                    snackbar.show()
                    val document = firestore.collection(ConstantsUtils.COLLECTION_BUSINESS).document()
                    businessId = document.id
                    // create business
                    val business = Business(
                        database.user?.key!!, document.id, businessName.text.toString(), businessPhone.text.toString(),
                        businessCategory.selectedItem.toString(), businessDesc.text.toString(), "", mutableListOf()
                    )
                    document.set(business)
                        .addOnFailureListener { exception ->
                            snackbar.setText(exception.localizedMessage).setDuration(BaseTransientBottomBar.LENGTH_LONG)
                                .show()
                        }
                        .addOnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                snackbar.setText(
                                    task.exception?.localizedMessage ?: "Unable to register your business"
                                )
                                    .setDuration(BaseTransientBottomBar.LENGTH_LONG).show()
                                return@addOnCompleteListener
                            }

                            snackbar.setText(
                                "Business registered successfully"
                            ).setDuration(BaseTransientBottomBar.LENGTH_LONG).show()
                            binding.business = business
                            allowLocationRegistration()
                        }
                } else {
                    toast("Fill in all required fields", true)
                }
            }
            negativeButton(text = "Dismiss") { it.dismiss() }
            cancelOnTouchOutside(false)
        }
    }

    private fun allowLocationRegistration() {
        MaterialDialog(this).show {
            title(text = "Almost done...")
            message(text = "Add a location for your business to complete this process")
            positiveButton(text = "Pick location") {
                it.dismiss()

                try {
                    val intent = PlacePicker.IntentBuilder().build(this@PendingRegistrationActivity)
                    startActivityForResult(intent, RC_LOCATION)
                } catch (e: Exception) {
                    toast("Unable to pick your location. Please try again later")
                    debugLog(e.localizedMessage)
                }
            }
            cancelOnTouchOutside(false)
            cancelable(false)
        }
    }

    fun updateAvatar(view: View) {
        //todo:
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_LOCATION && resultCode == Activity.RESULT_OK) {
            val place = PlacePicker.getPlace(this, data)
            val latLng = place.latLng
            val address = place.address
            val name = place.name

            debugLog("Place: ${place.latLng}")
        } else {
            toast("Unable to pick your location. Please try again later")
        }
    }

    fun pickLocation(view: View) {
        try {
            val intent = PlacePicker.IntentBuilder().build(this@PendingRegistrationActivity)
            startActivityForResult(intent, RC_LOCATION)
        } catch (e: Exception) {
            toast("Unable to pick your location. Please try again later")
            debugLog(e.localizedMessage)
        }
    }

    companion object {
        private const val RC_LOCATION = 9

    }
}
