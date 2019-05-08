package io.euruapp.view

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.iid.FirebaseInstanceId
import io.codelabs.util.bindView
import io.codelabs.widget.BaselineGridTextView
import io.codelabs.widget.ForegroundImageView
import io.euruapp.R
import io.euruapp.core.BaseActivity
import io.euruapp.model.RequestModel
import io.euruapp.model.User
import io.euruapp.util.ConstantsUtils
import io.euruapp.util.ConstantsUtils.intentTo
import io.codelabs.sdk.glide.GlideApp
import java.util.*

/**
 * Shows details about service request model to the service provider
 */
class ServiceDetailsActivity(override val layoutId: Int = R.layout.activity_service_details) : BaseActivity(),
    OnMapReadyCallback {


    private val container: ViewGroup by bindView(R.id.container)
    private val content: ViewGroup by bindView(R.id.content)
    private val loading: ProgressBar by bindView(R.id.loading)
    private val toolbar: Toolbar by bindView(R.id.toolbar)
    private val acceptButton: Button by bindView(R.id.accept_request_button)
    private val requestCustomer: BaselineGridTextView by bindView(R.id.request_customer_details)
    private val requestTitle: BaselineGridTextView by bindView(R.id.request_details)
    private val requestImage: ForegroundImageView by bindView(R.id.request_image_source)
    private val requestLocation: BaselineGridTextView by bindView(R.id.request_location)

    private var map: GoogleMap? = null
    private lateinit var mapFragment: SupportMapFragment
    private var isAccepted: Boolean = false

    private var requestModel: RequestModel? = RequestModel()

    override fun onViewCreated(intent: Intent, instanceState: Bundle?) {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        //Get map fragment
        mapFragment =
                supportFragmentManager?.findFragmentById(R.id.request_location_map) as SupportMapFragment

        if (intent.hasExtra(EXTRA_DATA)) {
            val requestModelKey = intent.getStringExtra(EXTRA_DATA)
            requestDataFromServer(
                requestModelKey, intent.getStringExtra(EXTRA_DATA_DAY), intent.getStringExtra(
                    EXTRA_DATA_USER_KEY
                )
            )
        } else {
            ConstantsUtils.showToast(applicationContext, "Unable to get service request key")
            finishAfterTransition()
        }
    }

    //Get request model's data from the database
    private fun requestDataFromServer(requestkey: String?, day: String?, userKey: String?) {
        ConstantsUtils.logResult("Service request key is: $requestkey")
        if (requestkey.isNullOrEmpty() || day.isNullOrEmpty() || userKey.isNullOrEmpty()) {
            ConstantsUtils.showToast(applicationContext, "Unable to get service request key")
            finishAfterTransition()
            return
        }

        toggleLoading()
//        val calendar = Calendar.getInstance()
//        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        firestore.collection(ConstantsUtils.COLLECTION_REQUESTS).document(userKey).collection(day).document(requestkey)
            .get().addOnCompleteListener(this) { task ->
                toggleLoading(false)
                if (task.isSuccessful) {
                    requestModel = task.result?.toObject(RequestModel::class.java)
                    ConstantsUtils.logResult("Result for request model: ${task.result?.id}")
                    if (requestModel != null) {
                        bindModel()
                    } else {
                        ConstantsUtils.showToast(applicationContext,"Unable to retrieve request model. We will notify the customer to resend the request")
                        //todo: fix crash here
                        //sendNotificationToCustomer(userKey)
                        finishAfterTransition()
                    }
                } else {
                    ConstantsUtils.showToast(applicationContext, "Unable to retrieve data. Please try again later")
                }
            }.addOnFailureListener(this) {
                toggleLoading(false)
                ConstantsUtils.logResult(it.localizedMessage)
                ConstantsUtils.showToast(applicationContext, "Unable to retrieve data. Please try again later")
            }
    }

    /*private fun sendNotificationToCustomer(userKey: String) {
        val to = "a_unique_key" // the notification key
        val msgId = AtomicInteger()
        FirebaseMessaging.getInstance().send(
            RemoteMessage.Builder(to)
            .setMessageId(msgId.get().toString())
            .addData("hello", "world")
            .build())
    }*/

    private fun bindModel() {
        ConstantsUtils.logResult(requestModel)


        mapFragment.getMapAsync(this)

        //Enable button
        acceptButton.isEnabled = requestModel != null

        //Load image
        if (!requestModel?.image.isNullOrEmpty()) {
            GlideApp.with(this)
                .asDrawable()
                .load(requestModel?.image)
                .placeholder(R.color.content_placeholder)
                .fallback(R.color.content_placeholder)
                .error(R.color.content_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(requestImage)
        } else {
            requestImage.visibility = View.GONE
        }

        requestCustomer.text = requestModel?.userKey ?: "Loading..."
        requestTitle.text = requestModel?.title
        requestLocation.text = "Requesting location info..."

        //Get user data and update username field
        firestore.collection(ConstantsUtils.COLLECTION_USERS).document(requestModel?.userKey!!)
            .get().addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    val user = it.result?.toObject(User::class.java)
                    ConstantsUtils.logResult(user)
                    requestCustomer.text = user?.name
                }
            }

        //Get address line
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addressLine =
                geocoder.getFromLocation(requestModel?.location?.lat!!, requestModel?.location?.lng!!, 1)?.get(0)
                    ?.getAddressLine(0)
            requestLocation.text = addressLine
        } catch (ex: Exception) {
            ConstantsUtils.logResult(ex.localizedMessage)
            requestLocation.text = "Unable to get location information. Try again later"
        }

    }

    override fun onMapReady(p0: GoogleMap?) {
        map = p0

        //Add location pointer
        if (requestModel != null) {
            //Location of request
            val location = requestModel?.location

            //Create latlng for customer's address
            val addressCustomer = LatLng(location!!.lat, location.lng)

            //Create marker
            val marker = MarkerOptions()
                .position(addressCustomer)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                .title(requestModel?.title)
                .snippet(requestModel?.category)

            //Move camera of the map to the location of the user
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(addressCustomer, 17.0f))

            //Add marker to map
            map?.addMarker(marker)?.showInfoWindow()
        }

    }

    private fun toggleLoading(show: Boolean = true) {
        TransitionManager.beginDelayedTransition(container, AutoTransition())
        if (show) {
            loading.visibility = View.VISIBLE
            content.visibility = View.GONE
        } else {
            loading.visibility = View.GONE
            content.visibility = View.VISIBLE
        }
    }

    fun acceptRequest(v: View?) {
        intentTo(this@ServiceDetailsActivity, HomeActivity::class.java)
        finishAfterTransition()
        ConstantsUtils.showToast(applicationContext, "Please wait while we process this request...")


        //This will update the request model with the provider's id
        requestModel?.providerId = database.user.key

        if (intent.hasExtra(EXTRA_DATA)) {
            val userKey = intent.getStringExtra(EXTRA_DATA)
            val day = intent.getStringExtra(EXTRA_DATA_DAY)
            val requestkey = intent.getStringExtra(EXTRA_DATA_USER_KEY)

            //Delete the request model from the new jobs database
            firestore.collection(ConstantsUtils.COLLECTION_REQUESTS).document(userKey).collection(day)
                .document(requestkey)
                .delete()
                .addOnFailureListener {
                    ConstantsUtils.logResult(it.localizedMessage)
                    ConstantsUtils.showToast(applicationContext, it.localizedMessage)
                }.addOnCompleteListener {
                    if (it.isSuccessful) {
                        ConstantsUtils.logResult("Job deleted from new requests successfully")
                    } else ConstantsUtils.showToast(applicationContext, it.exception?.localizedMessage)
                }
        }

        //Upload request to the collection of pending jobs for this service provider
        val doc = firestore.collection(
            String.format(
                "%s/%s/%s",
                ConstantsUtils.COLLECTION_JOBS,
                database.user.key,
                ConstantsUtils.NEW_JOBS
            )
        ).document()

        //Update the key of the request model
        requestModel?.dataKey = doc.id

        //Store the request model in the database
        doc.set(requestModel!!)
            .addOnFailureListener {
                ConstantsUtils.logResult(it.localizedMessage)
                ConstantsUtils.showToast(applicationContext, it.localizedMessage)
            }.addOnCompleteListener {
                if (it.isSuccessful) {
                    ConstantsUtils.showToast(applicationContext, "Request accepted successfully")
                    acceptButton.isEnabled = !it.isSuccessful
                    isAccepted = true
                    addAdditionalProps()
                } else ConstantsUtils.showToast(applicationContext, it.exception?.localizedMessage)
            }

    }

    private fun addAdditionalProps() {
        firestore.collection(
            String.format(
                "%s/%s/%s",
                ConstantsUtils.COLLECTION_JOBS,
                database.user.key,
                ConstantsUtils.NEW_JOBS
            )
        ).document(requestModel?.dataKey!!)
            .update(
                mapOf<String, Any?>(
                    "deviceToken" to FirebaseInstanceId.getInstance().token,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).addOnFailureListener { }
            .addOnCompleteListener { }
    }

    override fun onBackPressed() {
        if (isAccepted) {
            //Navigate the user back to the jobs list when the back button is pressed
            JobsActivity.start(this@ServiceDetailsActivity, ConstantsUtils.NEW_JOBS)
            finish()
        } else {
            MaterialDialog(this).apply {
                title(text = "Confirm action")
                message(text = "Do you wish to decline this request?")
                positiveButton(text = "Yes") { materialDialog ->
                    materialDialog.dismiss()

                    //Navigate the user back to the jobs list when the back button is pressed
                    JobsActivity.start(this@ServiceDetailsActivity, ConstantsUtils.NEW_JOBS)
                    finish()
                }
                negativeButton(text = "No") { materialDialog ->
                    materialDialog.dismiss()
                }
            }.show()
        }
    }

    companion object {
        const val EXTRA_DATA = "EXTRA_DATA"
        const val EXTRA_DATA_USER_KEY = "EXTRA_DATA_USER_KEY"
        const val EXTRA_DATA_DAY = "EXTRA_DATA_DAY"
    }
}