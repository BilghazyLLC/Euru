package io.euruapp.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.request.target.Target
import io.codelabs.util.bindView
import io.codelabs.widget.BaselineGridTextView
import io.euruapp.R
import io.euruapp.core.BaseActivity
import io.euruapp.model.Business
import io.euruapp.model.User
import io.euruapp.util.ConstantsUtils
import io.euruapp.util.ServiceRequestReceiver
import io.euruapp.util.glide.GlideApp
import pub.devrel.easypermissions.EasyPermissions
import java.util.*

/**
 * [User] can request a service from a service provider from this interface
 */
class RequestServiceActivity(override val layoutId: Int = R.layout.activity_request_service) : BaseActivity() {

    private val container: ViewGroup  by bindView(R.id.container)
    private val toolbar: Toolbar by bindView(R.id.toolbar)
    private val header: BaselineGridTextView by bindView(R.id.request_screen_title)
    private val body: EditText by bindView(R.id.request_body)
    private val img: ImageView  by bindView(R.id.request_image)

    private var hasCameraPermission = false
    private var hasPhoneCallPermission = false
    private var imageUri: Uri? = null
    private var business: Business? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null) {
                when (Objects.requireNonNull(intent.action)) {
                    ServiceRequestReceiver.EXTRA_SUCCESS -> ConstantsUtils.showToast(
                        applicationContext,
                        "Upload successful"
                    )
                    ServiceRequestReceiver.EXTRA_FAILURE -> ConstantsUtils.showToast(
                        applicationContext,
                        intent.getStringExtra(ServiceRequestReceiver.EXTRA_FAILURE)
                    )
                }
            } else {
                ConstantsUtils.logResult("Cannot get request service at this time")
            }
        }
    }

    override fun onViewCreated(intent: Intent, instanceState: Bundle?) {
        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener { v -> dismiss(null) }

        hasCameraPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        hasPhoneCallPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        //Get Intent data
        if (intent.hasExtra(EXTRA_BUSINESS_MODEL)) {
            if (intent.getParcelableExtra<Parcelable>(EXTRA_BUSINESS_MODEL) is Business) {
                business = intent.getParcelableExtra(EXTRA_BUSINESS_MODEL)
                bindBusiness()
            }
        } else {
            ConstantsUtils.showToast(this, "Cannot load business model")
            finishAfterTransition()
        }

    }

    override fun onStart() {
        super.onStart()
        registerResultListener()
    }

    override fun onStop() {
        super.onStop()
        unregisterResultListener()
    }

    private fun registerResultListener() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ServiceRequestReceiver.EXTRA_SUCCESS)
        intentFilter.addAction(ServiceRequestReceiver.EXTRA_FAILURE)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter)
    }

    private fun unregisterResultListener() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    fun dismiss(view: View?) {
        finishAfterTransition()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.request_service_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.menu_place_call)?.isVisible = !business?.phone.isNullOrEmpty()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_place_call -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || hasPhoneCallPermission) {
                    val phone = business?.phone
                    try {
                        val callIntent = Intent(Intent.ACTION_CALL).apply {
                            data = Uri.parse(String.format("tel:%s", phone))
                        }
                        if (callIntent.resolveActivity(packageManager) != null) {
                            startActivity(callIntent)
                        } else {
                            ConstantsUtils.showToast(
                                this@RequestServiceActivity,
                                "Cannot place phone call because there is no phone application to handle this action"
                            )
                        }
                    } catch (e: Exception) {
                        ConstantsUtils.showToast(this@RequestServiceActivity, e.localizedMessage)
                    }

                } else {
                    EasyPermissions.requestPermissions(
                        this@RequestServiceActivity,
                        "YOU NEED PERMISSION TO USE THE PHONE",
                        PHONE_PERMISSION,
                        Manifest.permission.CALL_PHONE
                    )
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun bindBusiness() {
        //Set header title
        header.text = String.format(getString(R.string.request_service_title), business!!.name)
    }

    fun imagePrompt(view: View) {
        @SuppressLint("NewApi", "LocalSuppress") val dialog = AlertDialog.Builder(this)
            .setTitle("Select image from...")
            .setItems(arrayOf<CharSequence>("Camera", "Gallery")) { dialog1, which ->
                dialog1.dismiss()

                when (which) {
                    0 -> {
                        //Open the camera and take a picture
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                            startActivityForResult(Intent.createChooser(intent, null), RC_CAMERA)
                        } else if (hasCameraPermission && intent.resolveActivity(packageManager) != null) {
                            startActivityForResult(Intent.createChooser(intent, null), RC_CAMERA)
                        } else {
                            EasyPermissions.requestPermissions(
                                this@RequestServiceActivity,
                                "YOU NEED PERMISSION TO USE THE CAMERA",
                                CAMERA_PERMISSION,
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                        }
                    }
                    1 -> {
                        //Open the gallery and pick an image
                        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
                        galleryIntent.type = "image/*"
                        galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/png", "image/jpeg"))
                        if (galleryIntent.resolveActivity(packageManager) != null)
                            startActivityForResult(galleryIntent, RC_GALLERY)
                        else
                            ConstantsUtils.showToast(this@RequestServiceActivity, "You need a image picker application")
                    }
                }

            }
            .setNegativeButton("Dismiss") { dialog1, _ -> dialog1.dismiss() }
            .create()

        //Show popup
        dialog.show()
    }

    //Server side
    fun sendMessage(view: View) {
        val message = body.text.toString()
        if (!TextUtils.isEmpty(message) /*&& imageUri != null*/) {

            // Service request receiver
            val requestService =
                Intent(ServiceRequestReceiver.POST_SERVICE, null, this, ServiceRequestReceiver::class.java)
            requestService.putExtra(ServiceRequestReceiver.REQUEST_BODY, message.trim { it <= ' ' })
            requestService.putExtra(ServiceRequestReceiver.REQUEST_USER_UID, database.user.key)
            requestService.putExtra(ServiceRequestReceiver.REQUEST_LOCATION, database.address)
            requestService.putExtra(ServiceRequestReceiver.REQUEST_SERVICE, business!!.category)
            requestService.putExtra(ServiceRequestReceiver.REQUEST_PROVIDER_UID, business!!.userUID)
            requestService.putExtra(
                ServiceRequestReceiver.REQUEST_IMAGE,
                if (imageUri == null) null else imageUri.toString()
            )
            startService(requestService)
            ConstantsUtils.showToast(this, "Request has been sent successfully")
            ConstantsUtils.showToast(this, "Service provider will soon accept your request", true)
            dismiss(view)
        } else {
            ConstantsUtils.showToast(
                this,
                "Please enter a short description about the type of service you want to request and upload an image too"
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_CAMERA && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            hasCameraPermission = true

            //open camera
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null)
                startActivityForResult(Intent.createChooser(intent, null), RC_CAMERA)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RC_CAMERA -> {
                    val bm = data!!.extras!!.get("data")
                    if (bm is Bitmap) {
                        GlideApp.with(this)
                            .asBitmap()
                            .load(bm)
                            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .circleCrop()
                            .placeholder(R.color.content_placeholder)
                            .into(img)
                        imageUri = data.data
                    } else {
                        ConstantsUtils.showToast(this, "Invalid image found")
                    }
                }

                RC_GALLERY -> {
                    //Get image uri
                    imageUri = data!!.data
                    ConstantsUtils.logResult(imageUri)
                    //Load image into view
                    GlideApp.with(this)
                        .asDrawable()
                        .load(imageUri)
                        .circleCrop()
                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .placeholder(R.color.content_placeholder)
                        .error(R.color.content_placeholder)
                        .into(img)
                }
            }
        }
    }

    companion object {
        const val EXTRA_BUSINESS_MODEL = "EXTRA_BUSINESS_MODEL"
        const val RC_CAMERA = 1
        const val RC_GALLERY = 2
        const val CAMERA_PERMISSION = 3
        const val PHONE_PERMISSION = 5
    }


}
