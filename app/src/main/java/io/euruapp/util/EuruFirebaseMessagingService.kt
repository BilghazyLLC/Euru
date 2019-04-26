package io.euruapp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.euruapp.R
import io.euruapp.core.BaseActivity
import io.euruapp.core.location.GPSTracker
import io.euruapp.model.Business
import io.euruapp.model.User
import io.euruapp.view.ServiceDetailsActivity
import io.euruapp.view.ServiceProviderDetailsActivity
import io.euruapp.viewmodel.UserDatabase
import kotlin.random.Random

class EuruFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(s: String?) {
        super.onNewToken(s)
        ConstantsUtils.logResult(String.format("Device token is: %s", s))

        //Send registration to server
        val user = UserDatabase(this).user
        if (user != null /*&& user.getType() == User.TYPE_BUSINESS*/) {

            val fb = FirebaseFirestore.getInstance()

            //Get the business model of the current service provider
            fb.collection(ConstantsUtils.COLLECTION_BUSINESS)
                .whereEqualTo("userUID", user.key)
                .limit(1)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val result = task.result
                        if (result == null || result.isEmpty) return@addOnCompleteListener

                        //Get the document from the result
                        val snapshot = result.documents[0]
                        if (snapshot.exists()) {
                            val business = snapshot.toObject(Business::class.java) ?: return@addOnCompleteListener

                            //Create hash map
                            val hashMap = hashMapOf<String, Any?>(
                                "key" to s,
                                "type" to user.type,
                                "uid" to user.key,
                                "timestamp" to System.currentTimeMillis().toString(),
                                "service" to business.category,
                                "token" to FirebaseInstanceId.getInstance().token,
                                "address.lat" to GPSTracker(application.applicationContext as BaseActivity)?.latitude,
                                "address.lng" to GPSTracker(application.applicationContext as BaseActivity)?.longitude
                            )

                            //Push data to database
                            FirebaseFirestore.getInstance().collection(ConstantsUtils.COLLECTION_TOKENS)
                                .document(user.key)
                                .set(hashMap)
                                .addOnCompleteListener { task1 ->
                                    ConstantsUtils.logResult(s)
                                    ConstantsUtils.logResult(if (task1.isSuccessful) "Device Token uploaded" else "Failed to upload device token")
                                }.addOnFailureListener { e -> ConstantsUtils.logResult(e.localizedMessage) }
                        }

                    }
                }.addOnFailureListener { e -> ConstantsUtils.logResult(e.localizedMessage) }

        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {

        if (remoteMessage != null) {
            // Debug notification
            ConstantsUtils.logResult("Notification received with payload: ${remoteMessage.data}")


            val database = UserDatabase(applicationContext)
            if (database.isLoggedIn) {
                //Create notification channel
                createNotificationChannel()

                when {
                    // Start: User requests notification
                    remoteMessage.data.containsKey("day") && database.user?.type == User.TYPE_BUSINESS -> {
                        //Create intent to service details screen
                        val intent = Intent(applicationContext, ServiceDetailsActivity::class.java)

                        //this is on of the main reason why we have al the systems in the move
                        //Parse and send data key to the ServiceDetailsActivity
                        intent.putExtra(
                            ServiceDetailsActivity.EXTRA_DATA,
                            remoteMessage.data?.get("requestModelKey")
                        )
                        intent.putExtra(
                            ServiceDetailsActivity.EXTRA_DATA_USER_KEY,
                            remoteMessage.data?.get("userKey")
                        )
                        intent.putExtra(
                            ServiceDetailsActivity.EXTRA_DATA_DAY,
                            remoteMessage.data?.get("day")
                        )

                        //Create pending intent for activity
                        val pi = PendingIntent.getActivity(
                            applicationContext,
                            RC_NOTIFICATION,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )

                        //Create notification bundle
                        val textTitle = remoteMessage.data?.get("header")
                        val textContent = remoteMessage.data?.get("body")

                        // Make sure sender does not receive the notification after sending the request
                        if (database.user.key != /*FirebaseAuth.getInstance().uid*/ remoteMessage.data?.get("userKey")) {
                            pushNotification(textTitle, textContent, pi)
                        }
                    }
                    // End: User requests notification

                    remoteMessage.data.containsKey("providerKey") && database.user.type != User.TYPE_BUSINESS -> {
                        //Create intent to service provider details screen
                        val intent = Intent(applicationContext, ServiceProviderDetailsActivity::class.java)

                        //Parse and send data key to the ServiceDetailsActivity
                        intent.putExtra(
                            ServiceProviderDetailsActivity.EXTRA_REQUEST_MODEL_ID,
                            remoteMessage.data?.get("requestId")
                        )
                        intent.putExtra(
                            ServiceProviderDetailsActivity.EXTRA_PROVIDER_UID,
                            remoteMessage.data?.get("providerKey")
                        )
                        intent.putExtra(
                            ServiceProviderDetailsActivity.EXTRA_CUSTOMER_UID,
                            remoteMessage.data?.get("userKey")
                        )

                        //Create pending intent for activity
                        val pi = PendingIntent.getActivity(
                            applicationContext,
                            RC_NOTIFICATION,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )

                        //Create notification bundle
                        val textTitle = remoteMessage.data?.get("header")
                        val textContent = remoteMessage.data?.get("body")

                        pushNotification(textTitle, textContent, pi)
                    }

                    remoteMessage.data.containsKey("type") && remoteMessage.data["type"] == "EURU-DEVICE-TOKEN-UPDATE" -> {
                        pushNotification("Token Update", remoteMessage.data["message"])
                    }

                    remoteMessage.data.containsKey("type") && database.user?.type != User.TYPE_BUSINESS -> {
                        val intent = Intent(applicationContext, ServiceProviderDetailsActivity::class.java)
                        //Parse and send data key to the ServiceDetailsActivity
                        intent.putExtra(
                            ServiceProviderDetailsActivity.EXTRA_REQUEST_MODEL_ID,
                            remoteMessage.data?.get("key")
                        )
                        intent.putExtra(
                            ServiceProviderDetailsActivity.EXTRA_PROVIDER_UID,
                            remoteMessage.data?.get("providerKey")
                        )
                        intent.putExtra(
                            ServiceProviderDetailsActivity.EXTRA_CUSTOMER_UID,
                            database.user?.key
                        )

                        //Create pending intent for activity
                        val pi = PendingIntent.getActivity(
                            applicationContext,
                            RC_NOTIFICATION,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )

                        when (remoteMessage.data["type"]) {

                            "completed" -> {
                                pushNotification(
                                    "Job update received",
                                    "Hurray! Your Job was completed successfully. Please tap here to view more details",
                                    pi
                                )
                            }

                            "pending" -> {
                                pushNotification(
                                    "Job update received",
                                    "The service provider for your recently accepted request has commenced progress on your job. You will be updated on any changes in real-time",
                                    pi
                                )
                            }

                        }
                    }

                    else -> {
                        ConstantsUtils.logResult("Remote data is null")
                    }
                }
            }
        } else ConstantsUtils.logResult("User is not logged in")
    }

    private fun pushNotification(textTitle: String?, textContent: String?, pi: PendingIntent? = null) {
        //Get notification service
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

        //Create notification builder
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(NOTIFICATION_ICON)
            .setContentTitle(textTitle)
            .setContentText(textContent)
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
            .setVibrate(longArrayOf(0, 200, 0, 200))
            .setAutoCancel(true)
            /*.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(MediaStore.Images.Media.getBitmap(applicationContext.contentResolver, imageUrl))
            )*/
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (pi != null) builder.setContentIntent(pi)

        if (manager != null) {
            //Send notification
            manager.notify(NOTIFICATION_ID, builder.build())

            //Wake the screen when the device is locked
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = powerManager.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.PARTIAL_WAKE_LOCK,
                EuruFirebaseMessagingService::class.java.canonicalName
            )
            wakeLock.acquire(15000)
        }
    }

    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "CHANNEL_ID"
        private const val NOTIFICATION_ICON = R.mipmap.ic_launcher
        private val NOTIFICATION_ID = Random(3).nextInt()
        private const val RC_NOTIFICATION = 1
    }

}
