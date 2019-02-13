package io.euruapp.view

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import androidx.core.content.ContextCompat
import androidx.transition.TransitionManager
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.GeoPoint
import io.codelabs.util.bindView
import io.codelabs.widget.BaselineGridTextView
import io.codelabs.widget.ForegroundImageView
import io.euruapp.R
import io.euruapp.core.BaseActivity
import io.euruapp.model.Business
import io.euruapp.model.Category
import io.euruapp.model.EuruGeoPoint
import io.euruapp.model.User
import io.euruapp.util.ConstantsUtils
import io.euruapp.util.ConstantsUtils.intentTo
import io.euruapp.util.glide.GlideApp
import kotlinx.android.synthetic.main.activity_provider_login.*
import pub.devrel.easypermissions.EasyPermissions
import java.util.*

class ProviderLoginActivity(override val layoutId: Int = R.layout.activity_provider_login) : BaseActivity() {
    private val container: ViewGroup by bindView(R.id.container)
    private val content: ViewGroup by bindView(R.id.content)
    private val loading: ProgressBar by bindView(R.id.loading)
    private val username: TextInputEditText by bindView(R.id.username)
    private val business: TextInputEditText by bindView(R.id.business)
    private val phone: TextInputEditText by bindView(R.id.phone)
    private val desc: TextInputEditText by bindView(R.id.business_desc)
    private val address: TextInputEditText by bindView(R.id.address)
    private val avatar: ForegroundImageView  by bindView(R.id.login_avatar)
    private val category: Spinner by bindView(R.id.business_category)

    private var imageUri: Uri? = Uri.EMPTY

    private var isFromIntent = false

    //Checks the location permission enabled state
    private var hasLocationPermissions = false
    //	private String addressText;
    private var geoPoint: GeoPoint? = null
    private var registerButton: BaselineGridTextView? = null

    private var model: Business? = Business()

    override fun onViewCreated(intent: Intent, instanceState: Bundle?) {
        toggleLoading(false)

        if (intent.hasExtra(EXTRA_BUSINESS)) {
            //Used to check whether the user is navigating from the ProfileFragment or from the AuthActivity
            model = intent.getParcelableExtra<Business>(EXTRA_BUSINESS)
            if (intent.hasExtra(FROM_INTENT)) {
                isFromIntent = intent.getBooleanExtra(FROM_INTENT, false)
                if (isFromIntent) business_model_save_button.text = "Save"
            }
            setupFields()
        }

        hasLocationPermissions = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val databaseUser = database.user
        GlideApp.with(this)
            .load(databaseUser.profile)
            .circleCrop()
            .placeholder(R.drawable.avatar_placeholder)
            .error(R.drawable.avatar_placeholder)
            .into(avatar)
        if (!databaseUser.profile.isNullOrEmpty())
            imageUri = Uri.parse(databaseUser.profile)
        username.setText(databaseUser.name?.trim { it <= ' ' } ?: "")

        val categories = arrayOf(
            Category.ARCHITECT,
            Category.AUTO_SERVICE,
            Category.BEAUTY,
            Category.CLEANERS,
            Category.ELECTRICIAN,
            Category.HOTELS,
            Category.EVENT_PLANNING,
            Category.PIZZA,
            Category.PLUMBERS,
            Category.PAINTER,
            Category.RESTAURANTS,
            Category.SEAMSTRESS,
            Category.DJ,
            Category.AIR_CONDITION_REPAIRS,
            Category.BAKER,
            Category.BARBER,
            Category.CAR_SPRAYER,
            Category.CAR_WASH,
            Category.CARPENTER,
            Category.TAXI_DRIVERS,
            Category.CAR_RENTALS,
            Category.SHOE_MAKER,
            Category.CATERER,
            Category.TEACHER
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
        category.adapter = adapter

        //Set default image for the user
        imageUri = auth.currentUser?.photoUrl

    }

    private fun setupFields() {
        business.setText(model?.name)
        category.prompt = model?.category
        desc.setText(model?.desc)
        phone.setText(model?.phone)
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onEnterAnimationComplete() {
        if (!hasLocationPermissions)
            EasyPermissions.requestPermissions(
                this, "Accept permission to access your current location in order to register",
                RC_PERMISSIONS, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_SMS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_PERMISSIONS && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ConstantsUtils.logResult("Permissions have been granted")
            hasLocationPermissions = true
        }
    }

    fun dismiss(view: View) {
        onBackPressed()
    }

    fun pickImage(view: View) {
        //Get image from the user's gallery
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        //Set image type to pick
        intent.type = "image/*"
        //Select only PNG and JPEG files
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
        //Check whether or not the user has a gallery application to handle our request
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(Intent.createChooser(intent, "Select profile image..."), RC_GALLERY)
        } else
            ConstantsUtils.showToast(this, "You need a gallery application to complete this action")
    }

    fun login(view: View) {
        registerButton = view as BaselineGridTextView
        if (!ConstantsUtils.validateField(username)) {
            ConstantsUtils.showToast(this, "Enter a username")
        } else if (!ConstantsUtils.validateField(phone)) {
            ConstantsUtils.showToast(this, "Enter a phone number")
        } else if (!ConstantsUtils.validateField(business)) {
            ConstantsUtils.showToast(this, "Enter a business name")
        } else if (ConstantsUtils.validateField(phone) && ConstantsUtils.validateField(business) && ConstantsUtils.validateField(
                username
            )
        ) {
            registerButton!!.isEnabled = false
            toggleLoading(true)
            if (imageUri != null && imageUri != auth.currentUser?.photoUrl) uploadImage() else pickLocation()
        } else
            ConstantsUtils.showToast(this, "Please complete the form to get started")
    }

    private fun uploadImage() {
        //Upload user's profile image to the storage bucket and generate a download url
        storage.child(database.user.key + ".jpg").putFile(imageUri!!)
            .addOnSuccessListener(this) { taskSnapshot ->
                if (taskSnapshot.task.isComplete) {
                    taskSnapshot.storage.downloadUrl.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            //get the download url for the image and update the imageUri variable globally
                            imageUri = task.result
                            pickLocation()
                        } else {
                            ConstantsUtils.showToast(
                                this@ProviderLoginActivity,
                                "Unable to retrieve the user's profile image details"
                            )
                            ConstantsUtils.logResult("User profile download url cannot be retrieved")
                        }
                    }
                } else {
                    toggleLoading(false)
                    registerButton!!.isEnabled = true
                    ConstantsUtils.showToast(this@ProviderLoginActivity, "Profile upload was interrupted")
                }
            }.addOnFailureListener(this) { e ->
                toggleLoading(false)
                registerButton!!.isEnabled = true
                ConstantsUtils.showToast(
                    this@ProviderLoginActivity,
                    "Failed to upload profile image. Please check your internet connection"
                )
                ConstantsUtils.logResult(e.localizedMessage)
            }
    }

    private fun registerBusiness() {
        var _phone = Objects.requireNonNull<Editable>(phone.text).toString()
        val _business = Objects.requireNonNull<Editable>(business.text).toString()
        val _username = Objects.requireNonNull<Editable>(username.text).toString()
        val _desc = Objects.requireNonNull<Editable>(desc.text).toString()

        if (!Patterns.PHONE.matcher(_phone).matches() || _phone.length < 10) {
            ConstantsUtils.showToast(this, "Enter a valid phone number")
            phone.requestFocus()
            return
        }

        //Standardize the phone number
        if (_phone.startsWith("0")) {
            val s = _phone.substring(1)
            _phone = String.format("+233%s", s)
        }
        ConstantsUtils.logResult(String.format("User's phone number: %s", _phone))

        //Get logged in user
        val user = database.user
        user.setName(_username)
        user.setProfile(imageUri?.toString())
        user.setPhone(_phone)
        user.setType(User.TYPE_BUSINESS)

        //Update user's data locally
        database.user = user

        //Generate list of business addresses
        val geoPoints = ArrayList<EuruGeoPoint>(0)
        geoPoints.add(EuruGeoPoint(geoPoint!!))

        //Create business data model
        val business = Business(
            user.getKey(), System.currentTimeMillis().toString(),
            _business, _phone, category.selectedItem.toString(), _desc, imageUri!!.toString(), geoPoints
        )

        //Create hash map of user's data
        toggleLoading(true)

        //Update user model
        val hashMap = HashMap<String, Any>(0)
        hashMap["name"] = user.getName()
        //hashMap["profile"] = user.getProfile()
        hashMap["phone"] = user.getPhone()

        //Store user in database
        firestore.collection(ConstantsUtils.COLLECTION_USERS).document(user.key).update(hashMap)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //Register business model after user's data has been successfully updated in the database
                    createBusinessModel(business, user)
                } else {
                    toggleLoading(false)
                    registerButton!!.isEnabled = true
                    ConstantsUtils.showToast(
                        this@ProviderLoginActivity,
                        "An error occurred while updating the user information"
                    )
                }
            }
            .addOnFailureListener(this) { e ->
                toggleLoading(false)
                registerButton!!.isEnabled = true
                ConstantsUtils.showToast(
                    this@ProviderLoginActivity,
                    "Failed to update user's profile information. Please check your internet connection"
                )
                ConstantsUtils.logResult(e.localizedMessage)
            }
    }

    private fun createBusinessModel(business: Business, user: User) {
        model = business
        val document = if (isFromIntent) firestore.collection(ConstantsUtils.COLLECTION_BUSINESS).document(model?.key!!)
        else firestore.collection(ConstantsUtils.COLLECTION_BUSINESS).document()


        business.setKey(document.id)
        business.setUserUID(user.getKey())


        //For debugging
        ConstantsUtils.logResult(business)

        if (isFromIntent) {
            document.update(
                mapOf<String, Any?>(
                    "name" to model?.name,
                    "phone" to model?.phone,
                    "desc" to model?.desc,
                    "image" to model?.image,
                    "category" to model?.category
                )
            ).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    ConstantsUtils.showToast(applicationContext, "Business updated successfully")
                    onBackPressed()
                }
            }
        } else {
            document.set(business).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    ConstantsUtils.showToast(this@ProviderLoginActivity, String.format("Welcome, %s", user.name))
                    intentTo(this@ProviderLoginActivity, HomeActivity::class.java)
                    finish()
                }
            }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Get the request code
        if (requestCode == RC_GALLERY) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                //Get image from uri
                imageUri = data.data

                //Load image into imageView
                GlideApp.with(this)
                    .load(imageUri)
                    .circleCrop()
                    .placeholder(R.drawable.avatar_placeholder)
                    .error(R.drawable.avatar_placeholder)
                    .into(avatar)

            }
        } else if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val place = PlacePicker.getPlace(this, data)
                val toastMsg = String.format("Place: %s", place.name)

                //For debugging only
                ConstantsUtils.logResult(toastMsg)

                //Get the address from the location picked
                try {
                    //					addressText = Objects.requireNonNull(place.getAddress()).toString();
                    geoPoint = GeoPoint(place.latLng.latitude, place.latLng.longitude)
                } catch (e: Exception) {
                    ConstantsUtils.showToast(this, "Address could not be found")
                    return
                }

                val materialDialog = MaterialDialog(this)
                materialDialog.setTitle("Business Address Confirmation")
                materialDialog.message(
                    null,
                    String.format("Do you wish to use: \n\"%s\"\nas your business address?", place.address)
                )
                materialDialog.positiveButton(null, "Proceed") { materialDialog1 ->
                    materialDialog1.dismiss()
                    registerBusiness()
                    Unit
                }
                materialDialog.negativeButton(null, "Change") { materialDialog1 ->
                    materialDialog1.dismiss()
                    pickLocation()
                    Unit
                }
                materialDialog.cancelOnTouchOutside(false)
                materialDialog.cancelable(false)
                materialDialog.show()
            }
        }
    }

    private fun pickLocation() {
        val builder = PlacePicker.IntentBuilder()
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST)
        } catch (e: GooglePlayServicesRepairableException) {
            registerButton!!.isEnabled = true
            ConstantsUtils.showToast(this, e.localizedMessage)
        } catch (e: GooglePlayServicesNotAvailableException) {
            registerButton!!.isEnabled = true
            ConstantsUtils.showToast(this, e.localizedMessage)
        }

    }

    override fun onBackPressed() {
        if (isFromIntent) {
            super.onBackPressed()
        } else {
            MaterialDialog(this).message(
                null,
                "Complete the business registration process. Cancelling this will result in the deletion of your user registration data. Do you wish to continue?"
            )
                .positiveButton(null, "Cancel registration") { materialDialog ->
                    materialDialog.dismiss()
                    database.logout()
                    intentTo(this@ProviderLoginActivity, AuthActivity::class.java)
                    finish()
                    Unit
                }
                .negativeButton(null, "Dismiss") { materialDialog ->
                    materialDialog.dismiss()
                    Unit
                }.show()
        }
    }

    companion object {
        private const val PLACE_PICKER_REQUEST = 1
        private const val RC_GALLERY = 8
        private const val RC_PERMISSIONS = 3
        const val FROM_INTENT = "FROM_INTENT"
        const val EXTRA_BUSINESS = "EXTRA_BUSINESS"
    }
}
