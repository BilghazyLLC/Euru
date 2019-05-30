package io.euruapp.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.iid.FirebaseInstanceId
import io.codelabs.sdk.util.toast
import io.euruapp.R
import io.euruapp.core.BaseActivity
import io.euruapp.databinding.ActivityPendingRegistrationBinding
import io.euruapp.util.ConstantsUtils
import java.util.*

class PendingRegistrationActivity(override val layoutId: Int = R.layout.activity_pending_registration) :
    BaseActivity() {

    private lateinit var binding: ActivityPendingRegistrationBinding

    override fun onViewCreated(intent: Intent, instanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pending_registration)

        // Send user device registration token to the server
        sendRegistration()

        // Bind user's data to UI
        binding.user = database.user
//        binding.business = Business("", "", "Zeniteck IT Solutions LLC", "+233550022344", "Laptop Repairs", "Beyond IT", "", mutableListOf())
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

                // todo: save user's profile information
            }
            negativeButton(text = "Dismiss") { it.dismiss() }
            cancelOnTouchOutside(false)
        }
    }

    fun registerBusiness(view: View) {
        val businessView = layoutInflater.inflate(R.layout.business_edit_dialog, null, false)


        MaterialDialog(this).show {
            title(text = "Register business")
            customView(view = businessView, dialogWrapContent = true)
            positiveButton(text = "Save") {
                it.dismiss()

                // todo: save user's profile information
            }
            negativeButton(text = "Dismiss") { it.dismiss() }
            cancelOnTouchOutside(false)
        }
    }

    fun updateAvatar(view: View) {}

}
