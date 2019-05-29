package io.euruapp.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.iid.FirebaseInstanceId
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
        
    }

    fun editUser(view: View) {}

    fun registerBusiness(view: View) {}

}
