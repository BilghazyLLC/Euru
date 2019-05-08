package io.euruapp.view

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.codelabs.util.bindView
import io.codelabs.widget.BaselineGridTextView
import io.codelabs.widget.ForegroundImageView
import io.euruapp.R
import io.euruapp.core.BaseActivity
import io.euruapp.model.RequestModel
import io.euruapp.model.User
import io.euruapp.util.ConstantsUtils
import io.codelabs.sdk.glide.GlideApp
import kotlinx.android.synthetic.main.activity_service_provider_details.*
import java.util.*

/**
 * This activity shows the details of a service provider who has accepted an offer from a customer
 */
class ServiceProviderDetailsActivity(override val layoutId: Int = R.layout.activity_service_provider_details) :
    BaseActivity(), OnMapReadyCallback {

    private val container: ViewGroup by bindView(R.id.container)
    private val content: ViewGroup by bindView(R.id.content)
    private val loading: ProgressBar by bindView(R.id.loading)
    private val toolbar: Toolbar by bindView(R.id.toolbar)
    //    private val requestCustomer: BaselineGridTextView by bindView(R.id.request_customer_details)
    private val requestTitle: BaselineGridTextView by bindView(R.id.request_details)
    private val requestImage: ForegroundImageView by bindView(R.id.request_image_source)
    private val requestLocation: BaselineGridTextView by bindView(R.id.request_location)

    private var map: GoogleMap? = null
    private lateinit var mapFragment: SupportMapFragment

    private var provider: User? = User()
    private var requestModel: RequestModel? = RequestModel()

    private var jobType: String? = null


    override fun onViewCreated(intent: Intent, instanceState: Bundle?) {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        //Get map fragment
        mapFragment =
                supportFragmentManager?.findFragmentById(R.id.request_location_map) as SupportMapFragment

        provider_username.text = "Loading..."
        provider_phone.text = "Loading..."

        if (intent.hasExtra(EXTRA_PROVIDER_UID)) {
            val providerId = intent.getStringExtra(EXTRA_PROVIDER_UID)
            val userKey = intent.getStringExtra(EXTRA_CUSTOMER_UID)
            val requestModelKey = intent.getStringExtra(EXTRA_REQUEST_MODEL_ID)
            jobType = intent.getStringExtra(EXTRA_JOB_TYPE)
            setupData(providerId, userKey, requestModelKey)
        } else {
            ConstantsUtils.showToast(applicationContext, "Unable to get retrieve data")
            finishAfterTransition()
        }
    }

    private fun setupData(providerId: String?, userKey: String?, key: String?) {
        ConstantsUtils.logResult("Provider Key: $providerId")
        ConstantsUtils.logResult("User key: $userKey")
        ConstantsUtils.logResult("Key: $key")

        toggleLoading()

        // Get the customer's details
        firestore.document("${ConstantsUtils.COLLECTION_USERS}/$userKey")
            .get().addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //Init provider here
                    val user = task.result?.toObject(User::class.java)

                    if (database.user.key != userKey) {
                        GlideApp.with(this@ServiceProviderDetailsActivity)
                            .asBitmap()
                            .load(user?.profile)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .circleCrop()
                            .dontAnimate()
                            .placeholder(R.drawable.avatar_placeholder)
                            .error(R.drawable.ic_player)
                            .into(avatar)


                        provider_username.text = user?.name ?: "Username not found"
                        provider_phone.text = user?.phone ?: "No phone number found"
                    }

                }
            }.addOnFailureListener(this) { exception ->
                toggleLoading(false)
                ConstantsUtils.showToast(applicationContext, exception.localizedMessage)
            }

        // Get the service provider
        firestore.document("${ConstantsUtils.COLLECTION_USERS}/$providerId")
            .get().addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //Init provider here
                    provider = task.result?.toObject(User::class.java)

                    if (database.user.key != providerId) {
                        GlideApp.with(this@ServiceProviderDetailsActivity)
                            .asBitmap()
                            .load(provider?.profile)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .circleCrop()
                            .dontAnimate()
                            .placeholder(R.drawable.avatar_placeholder)
                            .error(R.drawable.ic_player)
                            .into(avatar)


                        provider_username.text = provider?.name ?: "Username not found"
                        provider_phone.text = provider?.phone ?: "No phone number found"
                    }
                }
            }.addOnFailureListener(this) { exception ->
                toggleLoading(false)
                ConstantsUtils.showToast(applicationContext, exception.localizedMessage)
            }

        // get the request model details
        firestore.document("${ConstantsUtils.COLLECTION_JOBS}/$providerId/$jobType/$key")
            .get().addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    toggleLoading(false)
                    requestModel = task.result?.toObject(RequestModel::class.java)
                    bindModel()
                }
            }.addOnFailureListener(this) { exception ->
                toggleLoading(false)
                ConstantsUtils.showToast(applicationContext, exception.localizedMessage)
            }
    }

    private fun bindModel() {
        ConstantsUtils.logResult("Request model: $requestModel")
        mapFragment.getMapAsync(this)

        //Set text for button

        if (!jobType.isNullOrEmpty()) {
            when (jobType) {
                ConstantsUtils.NEW_JOBS -> {
                    TransitionManager.beginDelayedTransition(container, AutoTransition())
                    accept_service_request_button.visibility = View.VISIBLE
                    accept_service_request_button.text = getString(R.string.start_job)
                }
                ConstantsUtils.PENDING_JOBS -> {
                    TransitionManager.beginDelayedTransition(container, AutoTransition())
                    accept_service_request_button.visibility = View.VISIBLE
                    accept_service_request_button.text = getString(R.string.finish_job)
                }
                ConstantsUtils.COMPLETED_JOBS -> {
                    //do nothing
                }
            }
        }

        //Load image
        if (requestModel?.image != null && requestModel?.image!!.isNotEmpty()) {
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
            TransitionManager.beginDelayedTransition(container)
            requestImage.visibility = View.GONE
        }

//        requestCustomer.text = requestModel?.userKey ?: "Loading..."
        requestTitle.text = requestModel?.title
        requestLocation.text = "Requesting location info..."

        if (!requestModel?.userKey.isNullOrEmpty()) {//Get user data and update username field
            firestore.collection(ConstantsUtils.COLLECTION_USERS).document(requestModel?.userKey!!)
                .get().addOnCompleteListener(this) {
                    if (it.isSuccessful) {
                        val user = it.result?.toObject(User::class.java)
                        ConstantsUtils.logResult(user)
                        //                    requestCustomer.text = user?.name
                    }
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

    fun endJob(v: View?) {
        if (!jobType.isNullOrEmpty() && requestModel != null) {
            when (jobType) {
                ConstantsUtils.NEW_JOBS -> {
                    ConstantsUtils.showToast(applicationContext, "Starting job now...")
                    firestore.collection("${ConstantsUtils.COLLECTION_JOBS}/${database.user.key}/${ConstantsUtils.PENDING_JOBS}")
                        .document(requestModel?.dataKey!!)
                        .set(requestModel!!)
                        .addOnFailureListener {
                            ConstantsUtils.logResult(it.localizedMessage)
                        }
                        .addOnCompleteListener { }
                    finishAfterTransition()
                }

                ConstantsUtils.PENDING_JOBS -> {
                    ConstantsUtils.showToast(applicationContext, "Job completed successfully...")
                    firestore.collection("${ConstantsUtils.COLLECTION_JOBS}/${database.user.key}/${ConstantsUtils.COMPLETED_JOBS}")
                        .document(requestModel?.dataKey!!)
                        .set(requestModel!!)
                        .addOnFailureListener {
                            ConstantsUtils.logResult(it.localizedMessage)
                        }
                        .addOnCompleteListener { }
                    finishAfterTransition()
                }
            }
        }
    }

    companion object {
        const val EXTRA_PROVIDER_UID = "EXTRA_PROVIDER_UID"
        const val EXTRA_CUSTOMER_UID = "EXTRA_CUSTOMER_UID"
        const val EXTRA_REQUEST_MODEL_ID = "EXTRA_REQUEST_MODEL_ID"
        const val EXTRA_JOB_TYPE = "EXTRA_JOB_TYPE"
    }
}