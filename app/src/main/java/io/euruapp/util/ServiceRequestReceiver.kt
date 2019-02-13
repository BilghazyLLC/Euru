package io.euruapp.util

import android.app.IntentService
import android.content.Intent
import android.net.Uri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import io.euruapp.model.EuruGeoPoint
import io.euruapp.model.RequestModel
import io.euruapp.util.ConstantsUtils.logResult
import io.euruapp.util.ConstantsUtils.sendRequest
import java.util.*

class ServiceRequestReceiver : IntentService(TAG), OnCompleteListener<Void> {
    private var requestModel: RequestModel? = null

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return

        if (intent.action == POST_SERVICE) {

            //Create new request model builder
            val builder = RequestModel.Builder()
                .setLocation(intent.getParcelableExtra<EuruGeoPoint>(REQUEST_LOCATION))
                .setUserKey(intent.getStringExtra(REQUEST_USER_UID))
                .setCategory(intent.getStringExtra(REQUEST_SERVICE))
                .setTitle(intent.getStringExtra(REQUEST_BODY))
                .setProviderId(intent.getStringExtra(REQUEST_PROVIDER_UID))

            //Get image from intent
            val image = intent.getStringExtra(REQUEST_IMAGE)
            builder.setImage(if (image.isNullOrEmpty()) null else image)

            if (image != null && !image.isEmpty()) {
                logResult("Image will be available with url: $image")

                // Get default firebase storage reference
                val reference = FirebaseStorage.getInstance().reference

                //Create image URI
                val uri = Uri.parse(image)

                reference.child(System.currentTimeMillis().toString() + ".jpg").putFile(uri)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val storage = Objects.requireNonNull<UploadTask.TaskSnapshot>(task.result).storage
                            storage.downloadUrl.addOnCompleteListener { task1 ->
                                if (task1.isSuccessful) {
                                    logResult("image uploaded with url: " + task1.result!!)
                                    builder.setImage(task1.result.toString())
                                    requestModel = builder.build()
                                    sendRequest(
                                        requestModel!!,
                                        intent.getStringExtra(REQUEST_SERVICE),
                                        this
                                    )
                                }
                            }
                        }
                    }.addOnFailureListener { e ->
                        val i = Intent()
                        //Pass error to the calling activity through the intent
                        i.putExtra(EXTRA_FAILURE, e.localizedMessage)
                        i.action = EXTRA_FAILURE
                        LocalBroadcastManager.getInstance(this).sendBroadcast(i)
                    }

            } else {
                builder.setImage("")
                requestModel = builder.build()
                sendRequest(requestModel!!, intent.getStringExtra(REQUEST_SERVICE), this)
            }
        }
    }

    override fun onComplete(task: Task<Void>) {
        logResult("Request sent to server side successfully")
    }

    companion object {
        const val REQUEST_USER_UID = "REQUEST_USER_UID"
        const val REQUEST_PROVIDER_UID = "REQUEST_PROVIDER_UID"
        const val REQUEST_BODY = "REQUEST_BODY"
        const val REQUEST_IMAGE = "REQUEST_IMAGE"
        const val REQUEST_LOCATION = "REQUEST_LOCATION"
        const val REQUEST_SERVICE = "REQUEST_SERVICE"

        const val EXTRA_SUCCESS = "EXTRA_SUCCESS"
        const val EXTRA_FAILURE = "EXTRA_FAILURE"
        const val POST_SERVICE = "POST_SERVICE"

        private const val TAG = "ServiceRequestReceiver"
    }

}
