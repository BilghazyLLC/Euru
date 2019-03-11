package io.euruapp.view

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.transition.TransitionManager
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.tasks.OnCompleteListener
import io.codelabs.util.bindView
import io.codelabs.widget.CircularImageView
import io.euruapp.R
import io.euruapp.core.BaseActivity
import io.euruapp.model.Business
import io.euruapp.model.EuruGeoPoint
import io.euruapp.util.ConstantsUtils
import io.euruapp.util.glide.GlideApp
import java.util.*

class BusinessActivity(override val layoutId: Int = R.layout.activity_business) : BaseActivity() {


    private val container: ViewGroup by bindView(R.id.container)
    private val name: TextView by bindView(R.id.business_name)
    private val address: TextView by bindView(R.id.business_address)
    private val category: TextView by bindView(R.id.business_category)
    private val desc: TextView by bindView(R.id.business_desc)
    private val content: ViewGroup by bindView(R.id.scroll_container)
    private val toolbar: Toolbar by bindView(R.id.toolbar)
    private val avatar: CircularImageView by bindView(R.id.business_owner_avatar)
    private val loading: ViewGroup by bindView(R.id.loading_container)
    private var business: Business? = null
    private var uri: Uri? = Uri.EMPTY
    private var options: Array<CharSequence>? = null

    override fun onViewCreated(intent: Intent, instanceState: Bundle?) {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { v -> finishAfterTransition() }

        if (intent.hasExtra(EXTRA_BUSINESS)) {
            val parcelable = intent.getParcelableExtra<Parcelable>(EXTRA_BUSINESS)
            if (parcelable is Business) {
                business = parcelable
                bindUI()
            }
        }
    }

    private fun bindUI() {
        ConstantsUtils.logResult(business)
        name!!.text = business!!.name
        category!!.text = business!!.category
        desc!!.text = business!!.desc

        GlideApp.with(this)
            .load(business!!.image)
            .placeholder(R.drawable.avatar_placeholder)
            .circleCrop()
            .into(avatar!!)

        val geocoder = Geocoder(this, Locale.getDefault())

        try {
            val euruGeoPoint = business!!.addresses[0]
            val addressLine = geocoder.getFromLocation(euruGeoPoint.lat, euruGeoPoint.lng, 3)[0].getAddressLine(0)
            address!!.text = addressLine
        } catch (e: Exception) {
            ConstantsUtils.logResult(e.localizedMessage)
            address!!.text = String.format("Operation Type: %s", business!!.desc)
        }


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_gallery -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/png", "image/jpg"))
                if (intent.resolveActivity(packageManager) != null) {
                    startActivityForResult(intent, RC_GET_IMAGE)
                } else
                    ConstantsUtils.showToast(this, "Please install a file manager for this action")
                return true
            }

            R.id.menu_edit -> {
                /*options = arrayOf("Business name", "Short description", "Category", "Location address")
                AlertDialog.Builder(this)
                    .setTitle("Choose what to update")
                    .setItems(options) { dialog, which ->
                        dialog.dismiss()
                        val b = Bundle(0)
                        b.putString(EditContentActivity.EXTRA_CONTENT_NAME, options!![which].toString())
                        b.putString(EditContentActivity.EXTRA_CONTENT_ID, options!![which].toString())
                        when (which) {
                            0 -> ConstantsUtils.intentTo(this, EditContentActivity::class.java, RC_EDIT_PROFILE, b)
                            1 -> ConstantsUtils.intentTo(this, EditContentActivity::class.java, RC_EDIT_PROFILE, b)
                            2 -> {
                                val categories = arrayOf<CharSequence>(
                                    Category.AIR_CONDITION_REPAIRS,
                                    Category.ARCHITECT,
                                    Category.AUTO_SERVICE,
                                    Category.BEAUTY,
                                    Category.DENTIST,
                                    Category.HEAT_AIR,
                                    Category.HOTELS,
                                    Category.INSURANCE,
                                    Category.PIZZA,
                                    Category.PLUMBERS,
                                    Category.PHYSICIANS,
                                    Category.RESTAURANTS,
                                    Category.SEAMSTRESS,
                                    Category.DJ,
                                    Category.AIR_CONDITION_REPAIRS,
                                    Category.BAKER,
                                    Category.BARBER,
                                    Category.CAR_SPRAYER,
                                    Category.CAR_WASH,
                                    Category.CARPENTER,
                                    Category.CAR_RENTALS,
                                    Category.TAXI_DRIVERS,
                                    Category.SHOE_MAKER,
                                    Category.CATERER,
                                    Category.TEACHER
                                )
                                AlertDialog.Builder(this).setItems(categories) { dialog12, which12 ->
                                    dialog12.dismiss()
                                    category.text = categories[which12]
                                    saveData()
                                }.setTitle("Pick a category")
                                    .setNegativeButton("Cancel") { dialog1, _ -> dialog1.dismiss() }
                                    .create().show()
                            }
                            3 -> try {
                                val build = PlacePicker.IntentBuilder().build(this)
                                startActivityForResult(build, RC_PICK_LOCATION)
                            } catch (e: GooglePlayServicesRepairableException) {
                                ConstantsUtils.logResult(e.localizedMessage)
                            } catch (e: GooglePlayServicesNotAvailableException) {
                                ConstantsUtils.logResult(e.localizedMessage)
                            }

                        }
                    }.setNegativeButton("Dismiss") { dialog, _ -> dialog.dismiss() }.create().show()*/
                val b = Bundle(0)
                b.putParcelable(ProviderLoginActivity.EXTRA_BUSINESS, business)
                b.putBoolean(ProviderLoginActivity.FROM_INTENT, true)
                ConstantsUtils.intentTo(this@BusinessActivity, ProviderLoginActivity::class.java, b)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RC_GET_IMAGE -> {
                    uri = if (data != null) data.data else Uri.EMPTY
                    GlideApp.with(this)
                        .load(uri)
                        .placeholder(R.drawable.avatar_placeholder)
                        .circleCrop()
                        .into(avatar)

                    toggleLoading(true)
                    ConstantsUtils.uploadImage(uri!!, OnCompleteListener { task ->
                        if (task.isSuccessful) {
                            uri = task.result
                            business?.setImage(uri.toString())
                            saveData()
                        } else {
                            toggleLoading(false)
                            ConstantsUtils.showToast(this, "Unable to save business profile image")
                        }
                    })
                }
                RC_PICK_LOCATION -> {
                    if (data == null) return
                    val place = PlacePicker.getPlace(this, data)
                    ConstantsUtils.logResult(place)

                    val addresses: MutableList<EuruGeoPoint>? = business?.getAddresses()
                    val euruGeoPoint: EuruGeoPoint? = addresses?.get(0)
                    euruGeoPoint?.lat = place.latLng.latitude
                    euruGeoPoint?.lng = place.latLng.longitude

                    //Replace with new address
                    addresses?.removeAt(0)
                    if (euruGeoPoint != null) addresses.add(euruGeoPoint)

                    //Overwrite the current branches
                    business?.setAddresses(addresses)
                    saveData()

                }
                RC_EDIT_PROFILE -> {

                    val content = data!!.getStringExtra(EXTRA_DATA)
                    val i = data.getIntExtra(EditContentActivity.EXTRA_CONTENT_ID, 0)
                    when (i) {
                        0 -> {
                            name.text = content
                            saveData()
                        }
                        1 -> {
                            desc.text = content
                            saveData()
                        }
                    }
                }
            }
        }
    }

    private fun saveData() {
        if (business == null) return
        toggleLoading(true)
        firestore.collection(ConstantsUtils.COLLECTION_BUSINESS).document(business!!.key)
            .set(business!!)
            .addOnFailureListener(this) { e ->
                toggleLoading(false)
                ConstantsUtils.logResult(e.localizedMessage)
            }.addOnCompleteListener(this) { task1 ->
                toggleLoading(false)
                ConstantsUtils.logResult(if (task1.isSuccessful) "Business updated" else "failed to update the business model")
            }
    }

    private fun toggleLoading(b: Boolean) {
        TransitionManager.beginDelayedTransition(container)
        if (b) {
            loading.visibility = View.VISIBLE
            content.visibility = View.GONE
        } else {
            loading.visibility = View.GONE
            content.visibility = View.VISIBLE
        }
    }

    companion object {
        const val EXTRA_BUSINESS = "EXTRA_BUSINESS"
        const val RC_EDIT_PROFILE = 4
        const val EXTRA_DATA = "EXTRA_DATA"
        private const val RC_GET_IMAGE = 8
        private const val RC_PICK_LOCATION = 5
    }

}
