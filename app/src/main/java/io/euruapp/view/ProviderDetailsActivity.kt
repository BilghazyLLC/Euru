package io.euruapp.view

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.load.engine.DiskCacheStrategy
import io.codelabs.util.bindView
import io.codelabs.widget.BaselineGridTextView
import io.codelabs.widget.CircularImageView
import io.euruapp.R
import io.euruapp.core.BaseActivity
import io.euruapp.model.Business
import io.euruapp.model.User
import io.euruapp.util.ConstantsUtils
import io.codelabs.sdk.glide.GlideApp
import java.util.*

class ProviderDetailsActivity(override val layoutId: Int = R.layout.activity_provider_details) : BaseActivity() {

    private val avatar: CircularImageView by bindView(R.id.avatar)
    private val name: BaselineGridTextView by bindView(R.id.business_name)
    private val address: BaselineGridTextView by bindView(R.id.business_address)
    private val service: BaselineGridTextView by bindView(R.id.business_service)
    private val desc: BaselineGridTextView by bindView(R.id.business_desc)
    private val ratingBar: AppCompatRatingBar by bindView(R.id.business_ratings)
    private val container: ViewGroup by bindView(R.id.container)
    private val toolbar: Toolbar by bindView(R.id.toolbar)

    private var business: Business? = null

    private var geocoder: Geocoder? = null

    override fun onViewCreated(intent: Intent, instanceState: Bundle?) {
        setSupportActionBar(toolbar)

        toolbar!!.setNavigationOnClickListener { v -> finishAfterTransition() }
        geocoder = Geocoder(this, Locale.getDefault())

        if (intent.hasExtra(EXTRA_BUSINESS_MODEL)) {
            val parcelable = intent.getParcelableExtra<Parcelable>(EXTRA_BUSINESS_MODEL)
            if (parcelable is Business) {
                bindBusiness(parcelable)
            }
        } else {
            ConstantsUtils.showToast(this, "Cannot load business model")
            finishAfterTransition()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun bindBusiness(business: Business?) {
        if (business == null) {
            ConstantsUtils.showToast(this, "Cannot find business model")
            return
        }

        this.business = business

        //Get business data
        name!!.text = business.name
        desc!!.text = if (business.desc == null) "No business description found" else business.desc
        service!!.text = business.category

        if (!business.addresses.isEmpty()) {
            val euruGeoPoint = business.addresses[0]
            try {
                val location = geocoder!!.getFromLocation(euruGeoPoint.lat, euruGeoPoint.lng, 1)
                if (!location.isEmpty()) {
                    //Set location
                    address!!.text = location[0].getAddressLine(0)
                }
            } catch (ex: Exception) {
                address!!.text = String.format("%d branches available", business.addresses.size)
            }

        }

        //Get user
        firestore.collection(ConstantsUtils.COLLECTION_USERS).document(business.userUID)
            .get().addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result!!.toObject(User::class.java)
                    if (user != null) {
                        name!!.text = user.name

                        GlideApp.with(applicationContext)
                            .asDrawable()
                            .load(user.profile)
                            .circleCrop()
                            .placeholder(R.drawable.avatar_placeholder)
                            .error(R.drawable.ic_player)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .into(avatar!!)
                    }
                } else {
                    ConstantsUtils.showToast(this@ProviderDetailsActivity, task.exception!!.localizedMessage)
                    finishAfterTransition()
                }
            }.addOnFailureListener(this) { e ->
                ConstantsUtils.showToast(this@ProviderDetailsActivity, e.localizedMessage)
                finishAfterTransition()
            }
    }

    fun dismissAction(view: View) {
        finishAfterTransition()
    }

    fun nextAction(view: View) {
        val b = Bundle(0)
        b.putParcelable(RequestServiceActivity.EXTRA_BUSINESS_MODEL, business)
        ConstantsUtils.intentTo(this, RequestServiceActivity::class.java, b)
        finishAfterTransition()
    }

    companion object {

        val EXTRA_BUSINESS_MODEL = "EXTRA_BUSINESS_MODEL"
    }
}
