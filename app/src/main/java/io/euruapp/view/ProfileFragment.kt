package io.euruapp.view

import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.Target
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import io.codelabs.recyclerview.Divided
import io.codelabs.recyclerview.GridItemDividerDecoration
import io.codelabs.recyclerview.GridMarginDecoration
import io.codelabs.recyclerview.SlideInItemAnimator
import io.codelabs.util.bindView
import io.codelabs.widget.BaselineGridTextView
import io.codelabs.widget.CircularImageView
import io.codelabs.widget.ForegroundImageView
import io.euruapp.R
import io.euruapp.core.BaseActivity
import io.euruapp.core.BaseFragment
import io.euruapp.model.Business
import io.euruapp.model.User
import io.euruapp.util.ConstantsUtils
import io.euruapp.util.ConstantsUtils.logResult
import io.euruapp.util.OnSearchableItemClickListener
import io.euruapp.util.glide.GlideApp
import kotlinx.android.synthetic.main.fragment_profile.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class ProfileFragment : BaseFragment(), OnSearchableItemClickListener<Business> {
    private val grid: RecyclerView by bindView(R.id.grid)
    private val username: BaselineGridTextView by bindView(R.id.username)
    private val address: BaselineGridTextView by bindView(R.id.address)
    private val phone: BaselineGridTextView by bindView(R.id.phone)
    private val avatar: CircularImageView by bindView(R.id.avatar)
    private val saveButton: Button by bindView(R.id.save)
    private val fabAddBusiness: FloatingActionButton by bindView(R.id.fab_add_business)

    private var adapter: BusinessListAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = BusinessListAdapter(requireContext(), this)
        val lm = LinearLayoutManager(requireContext())
        grid.layoutManager = lm
        grid.setHasFixedSize(true)
        grid.adapter = adapter
        grid.itemAnimator = SlideInItemAnimator()
        grid.addItemDecoration(GridItemDividerDecoration(requireContext(), R.dimen.divider_height, R.color.divider))
        grid.addItemDecoration(GridMarginDecoration(5))

        //Get the current user's data
        val databaseUser = database.user

        //Log user's data to the console
        ConstantsUtils.logResult(databaseUser)

        //Get user's data and update the UI
        if (databaseUser != null) {
            username.text = databaseUser.getName().trim { it <= ' ' }
            if (databaseUser.phone != null && !TextUtils.isEmpty(databaseUser.phone))
                phone.text = databaseUser.getPhone().trim { it <= ' ' }
            else {
                phone.text = "Signed in as: Business"
            }

            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val geoPoint = databaseUser.address

            try {
                address.text = geocoder.getFromLocation(geoPoint.lat, geoPoint.lng, 1)[0].getAddressLine(0)
            } catch (e: IndexOutOfBoundsException) {
                ConstantsUtils.logResult(e.localizedMessage)
                address.text = String.format(
                    "Account was created: %s",
                    DateUtils.getRelativeTimeSpanString(
                        databaseUser.timestamp,
                        System.currentTimeMillis(),
                        DateUtils.SECOND_IN_MILLIS
                    )
                )
            } catch (e: IOException) {
                ConstantsUtils.logResult(e.localizedMessage)
                address.text = String.format(
                    "Account was created: %s",
                    DateUtils.getRelativeTimeSpanString(
                        databaseUser.timestamp,
                        System.currentTimeMillis(),
                        DateUtils.SECOND_IN_MILLIS
                    )
                )
            }

            GlideApp.with(requireContext())
                .load(databaseUser.getProfile())
                .placeholder(R.drawable.avatar_placeholder)
                .error(R.drawable.ic_player)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .circleCrop()
                .into(avatar)

            if ((requireActivity() as BaseActivity).isNetworkAvailable) {
                try {
                    //Load user's data from the internet
                    firestore.collection(ConstantsUtils.COLLECTION_USERS).document(databaseUser.key)
                        .get().addOnCompleteListener(requireActivity()) { task ->
                            if (task.isSuccessful) {
                                val result = task.result
                                if (result != null && result.exists()) {
                                    //Get user's data
                                    val user = result.toObject(User::class.java)
                                    user?.setType(User.TYPE_BUSINESS)

                                    //Store user's data locally
                                    database.user = user

                                    //For debugging
                                    ConstantsUtils.logResult("Online user: " + user!!)

                                    if (user != null) {
                                        username.text = user.name

                                        GlideApp.with(requireActivity().applicationContext)
                                            .load(user.getProfile())
                                            .placeholder(R.drawable.avatar_placeholder)
                                            .error(R.drawable.ic_player)
                                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                            .circleCrop()
                                            .into(avatar)
                                    }

                                }
                            }
                        }
                } catch (ex: Exception) {
                    ConstantsUtils.logResult(ex.localizedMessage)
                }

            }

        } else
            ConstantsUtils.logResult("User is null and therefore cannot be displayed on the UI")


        username.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                saveButton.isEnabled = !TextUtils.isEmpty(s) && database.user.getName() !== s
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        phone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                saveButton.isEnabled = !TextUtils.isEmpty(s) && database.user.getPhone() !== s
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        saveButton.setOnClickListener { save() }
        fabAddBusiness.setOnClickListener { addBusiness() }

        val snackbar: Snackbar =
            Snackbar.make(profile_container, "Loading Business Data. Please wait...", Snackbar.LENGTH_INDEFINITE)
        snackbar.show()
        //get a collection of user's business
        firestore.collection(ConstantsUtils.COLLECTION_BUSINESS)
            .whereEqualTo("userUID", database.user.getKey())
            .get()
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    logResult(result?.documents)
                    if (result != null) {
                        if (snackbar.isShown) snackbar.dismiss()

                        if (result.documents.isEmpty()) {
                            ConstantsUtils.showToast(requireContext(),"Your internet connection is not reliable. Please try again later",true)
                            return@addOnCompleteListener
                        }

                        for (snapshot in result.documents) {
                            if (snapshot != null && snapshot.exists()) {
                                //Convert to business data model
                                val business = snapshot.toObject(Business::class.java)
                                adapter!!.addSingleBusinessModel(business)
                            }
                        }

                    } else {
                        if (snackbar.isShown) snackbar.dismiss()
                        MaterialDialog(requireActivity()).apply {
                            title(text = "Action required")
                            message(text = "It seems you have not registered a business yet. Tap register to create one")
                            positiveButton(text = "Register") {
                                it.dismiss()
                                val b = Bundle(0)
                                b.putBoolean(ProviderLoginActivity.FROM_INTENT, true)
                                b.putParcelable(ProviderLoginActivity.EXTRA_BUSINESS, Business())
                                ConstantsUtils.intentTo(requireActivity(), ProviderLoginActivity::class.java, b)
                            }
                        }.show()
                    }

                }
            }.addOnFailureListener(requireActivity()) {
                if (snackbar.isShown) snackbar.dismiss()
                ConstantsUtils.showToast(
                    requireContext(),
                    "Unable to get business information at this time. Please try again later"
                )
            }
    }

    /*override fun onStart() {
        super.onStart()



    }*/

    /**
     * Save [User] data to the [io.euruapp.viewmodel.UserDatabase]
     */
    private fun save() {
        //Get user details
        val _username = username.text.toString()
        val _phone = phone.text.toString()

        //Save user data
        val databaseUser = database.user
        databaseUser.setName(_username)
        databaseUser.setPhone(_phone)
    }

    private fun addBusiness() {
        val b = Bundle(0)
        b.putBoolean(ProviderLoginActivity.FROM_INTENT, true)
        b.putParcelable(ProviderLoginActivity.EXTRA_BUSINESS, Business())
        ConstantsUtils.intentTo(requireActivity(), ProviderLoginActivity::class.java, b)
    }

    override fun onItemClicked(position: Int, `object`: Business, isLongClick: Boolean) {
        if (database.isLoggedIn && database.user.key != `object`.userUID) {
            val b = Bundle(0)
            b.putParcelable(MapsActivity.EXTRA_BUSINESS_MODEL, `object`)
            ConstantsUtils.intentTo(requireActivity(), MapsActivity::class.java, b)
        } else {
            val b = Bundle(0)
            b.putParcelable(BusinessActivity.EXTRA_BUSINESS, `object`)
            ConstantsUtils.intentTo(requireActivity(), BusinessActivity::class.java, b)
        }
    }

    internal inner class BusinessListAdapter(
        private val context: Context,
        private val listener: OnSearchableItemClickListener<Business>
    ) : RecyclerView.Adapter<BusinessListAdapter.BusinessViewHolder>() {
        private val businesses: MutableList<Business> = ArrayList(0)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusinessViewHolder {
            return BusinessViewHolder(LayoutInflater.from(context).inflate(R.layout.item_business, parent, false))
        }

        override fun onBindViewHolder(holder: BusinessViewHolder, position: Int) {
            val business = businesses[position]

            holder.name!!.text = business.getName().trim { it <= ' ' }
            holder.address!!.text = String.format("%d Branch(es)", business.addresses.size)
            holder.itemView.setOnClickListener { v -> listener.onItemClicked(position, business, false) }

            GlideApp.with(requireContext())
                .load(business.image)
                .placeholder(R.drawable.avatar_placeholder)
                .error(R.drawable.avatar_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .circleCrop()
                .into(holder.avatar!!)
        }

        override fun getItemCount(): Int {
            return businesses.size
        }

        fun addBusinesses(businessList: List<Business>) {
            if (!businessList.isEmpty()) {
                for (business in businessList) {
                    var add = true
                    for (i in businesses.indices) {
                        if (business.key == businesses[i].key) add = false
                    }

                    if (add) {
                        businesses.add(business)
                        notifyItemRangeChanged(0, businessList.size)
                    }

                }

            }
        }

        fun addSingleBusinessModel(business: Business?) {
            if (business == null) {
                return
            }
            val filter = businesses.filter { it.key == business.key }
            if (filter.isEmpty()) {
                businesses.add(business)
                notifyDataSetChanged()
            }
        }

        internal inner class BusinessViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), Divided {
            val address: TextView by bindView(R.id.business_address)
            val name: TextView by bindView(R.id.business_name)
            val avatar: ForegroundImageView by bindView(R.id.business_picture)
        }
    }

    companion object {

        internal fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }

}
