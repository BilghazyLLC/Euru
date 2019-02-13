package io.euruapp.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.transition.TransitionManager
import com.google.android.material.snackbar.Snackbar
import io.codelabs.util.bindView
import io.codelabs.widget.CircularImageView
import io.euruapp.R
import io.euruapp.core.BaseActivity
import io.euruapp.model.User
import io.euruapp.util.ConstantsUtils.COLLECTION_USERS
import io.euruapp.util.ConstantsUtils.intentTo
import io.euruapp.util.glide.GlideApp

class AccountCompletion(override val layoutId: Int = R.layout.activity_account_completion) : BaseActivity() {
    private val username: EditText by bindView(R.id.username)
    private val loading: ProgressBar by bindView(R.id.loading)
    private val container: ViewGroup by bindView(R.id.container)
    private val content: ViewGroup by bindView(R.id.content)
    private val setupAccountBtn: Button by bindView(R.id.setup_account)
    private val avatar: CircularImageView by bindView(R.id.avatar)

    //To store user's profile image
    private var imageUri: Uri? = null

    override fun onViewCreated(intent: Intent, instanceState: Bundle?) {
        showLoading(false)
        setupAccountBtn.setOnClickListener { setupUserAccount() }
        avatar.setOnClickListener { openGallery() }
        username.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                setupAccountBtn.isEnabled = !s.isNullOrEmpty() && s.length > 4 /*&& imageUri != null*/
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/png", "image/jpeg"))
        if (intent.resolveActivity(packageManager) != null)
            startActivityForResult(intent, RC_GALLERY_IMAGE)
    }

    private fun setupUserAccount() {
        val _username = username.text.toString()

        when {
            _username.isEmpty() -> {
                showSnackbar("Please enter your username")
            }

            imageUri == null -> {
                showSnackbar("Please select a profile image")
            }

            else -> {
                showLoading()
                storage.child(COLLECTION_USERS).child(database.user.key + ".jpg").putFile(imageUri!!)
                    .addOnCompleteListener(this@AccountCompletion) { task ->
                        if (task.isSuccessful) {
                            task.result?.storage?.downloadUrl?.addOnCompleteListener { urlTask ->
                                if (urlTask.isSuccessful) {
                                    imageUri = urlTask.result
                                    firestore.collection(COLLECTION_USERS).document(database.user.key)
                                        .update(
                                            mapOf<String, Any?>(
                                                "name" to _username,
                                                "profile" to imageUri.toString(),
                                                "phone" to auth.currentUser?.phoneNumber
                                            )
                                        ).addOnCompleteListener(this@AccountCompletion) {
                                            if (it.isSuccessful) {
                                                intentTo(
                                                    this@AccountCompletion, if (database.user?.type == User.TYPE_BUSINESS) ProviderLoginActivity::class.java else HomeActivity::class.java
                                                )
                                                database.updateName(_username)
                                                database.updateProfile(imageUri.toString())
//                                                showUserSelectionDialog(this@AccountCompletion, database)
                                                finishAfterTransition()
                                            } else {
                                                showLoading(false)
                                                showSnackbar(it.exception?.localizedMessage)
                                            }
                                        }.addOnFailureListener(this@AccountCompletion) {
                                            showLoading(false)
                                            showSnackbar(it.localizedMessage)
                                        }
                                }
                            }
                        }
                    }
            }
        }
    }

    private fun showLoading(b: Boolean = true) {
        TransitionManager.beginDelayedTransition(container)
        if (b) {
            loading.visibility = View.VISIBLE
            content.visibility = View.GONE
        } else {
            loading.visibility = View.GONE
            content.visibility = View.VISIBLE
        }
    }

    private fun showSnackbar(s: String?) {
        Snackbar.make(container, s ?: "An error occurred", Snackbar.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == RC_GALLERY_IMAGE) {
            //Retrieve user's image from the gallery
            imageUri = data?.data

            GlideApp.with(this)
                .load(imageUri)
                .placeholder(R.drawable.avatar_placeholder)
                .circleCrop()
                .into(avatar)
        }
    }

    companion object {
        private const val RC_GALLERY_IMAGE = 9
    }
}