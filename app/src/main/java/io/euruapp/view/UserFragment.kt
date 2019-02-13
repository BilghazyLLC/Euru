package io.euruapp.view


import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import io.codelabs.util.bindView
import io.euruapp.R
import io.euruapp.core.BaseActivity
import io.euruapp.core.BaseFragment
import io.euruapp.model.User
import io.euruapp.util.ConstantsUtils
import io.euruapp.util.glide.GlideApp
import kotlinx.android.synthetic.main.fragment_user.*
import java.util.*

/**
 * A simple [BaseFragment] subclass.
 */
class UserFragment : BaseFragment() {


    private val avatar: ImageView by bindView(R.id.avatar)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //  cameraButton.setOnClickListener { openGallery() }

        //Load image into avatar
        val user = database.user
        GlideApp.with(this)
            .load(user.profile ?: R.drawable.placeholderuser)
            .circleCrop()
            .placeholder(R.drawable.avatar_placeholder)
            .into(avatar)

        userName.text = user.name
        userEmail.text = auth.currentUser?.email ?: auth.currentUser?.phoneNumber
        user_requests_count.text = getString(R.string.loading)
        user_pending_request_count.text = getString(R.string.loading)// Update user stats
        getUserRequestCount()

        card_user_requests.setOnClickListener {
            JobsActivity.start(requireActivity(), ConstantsUtils.NEW_JOBS)
        }
        card_user_pending_jobs.setOnClickListener {
            JobsActivity.start(requireActivity(), ConstantsUtils.PENDING_JOBS)
        }
    }

    private fun getUserRequestCount() {
        ConstantsUtils.logResult("Current User: ${database.user}")
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        // Get user's sent requests
        firestore.collection("${ConstantsUtils.COLLECTION_REQUESTS}/${database.user?.key}/$dayOfWeek")
            .get().addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val size = task.result?.size()
                    user_requests_count.text = size.toString()
                }
            }

        // Get user's pending requests
        firestore.collection("${ConstantsUtils.COLLECTION_JOBS}/${database.user?.key}/${ConstantsUtils.PENDING_JOBS}")
            .get().addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val size = task.result?.size()
                    user_pending_request_count.text = size.toString()
                }
            }
    }

    internal fun openGallery() {
        //Get image from the gallery
        val i = Intent(Intent.ACTION_GET_CONTENT)

        //Get only images
        i.type = "image/*"

        //Take only .jpeg and .png images
        i.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))

        //Start activity
        if (i.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(i, REQ_IMAGE)
        } else {
            Toast.makeText(
                requireContext(),
                "Please download an image gallery application for this action",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_IMAGE && resultCode == RESULT_OK) {

            //Get image from response
            val uri = data!!.data

            //For debugging
            ConstantsUtils.logResult("Image URI is: " + uri!!)

            //Load image into avatar
            GlideApp.with(this)
                .load(uri)
                .circleCrop()
                .placeholder(R.drawable.avatar_placeholder)
                .into(avatar)

            //Check internet connection
            if ((requireActivity() as BaseActivity).isNetworkAvailable) {
                ConstantsUtils.uploadImage(uri, OnCompleteListener { task ->
                    val user = database.user
                    val currentUser = User(
                        user.getName(),
                        user.getKey(),
                        user.getType(),
                        task.result.toString(),
                        user.getPhone(),
                        user.getAddress()
                    )
                    database.user = currentUser
                    ConstantsUtils.logResult(String.format("User updated successfully as: %s", currentUser.toString()))
                })
            } else {
                ConstantsUtils.showToast(
                    requireContext(),
                    "Cannot update profile image. Please check your internet connection"
                )

                //Load default image
                GlideApp.with(this)
                    .load(database.user.getProfile())
                    .circleCrop()
                    .placeholder(R.drawable.avatar_placeholder)
                    .into(avatar)
            }

        }

    }

    companion object {

        val REQ_IMAGE = 8

        internal fun newInstance(): UserFragment {
            return UserFragment()
        }
    }
}// Required empty public constructor
