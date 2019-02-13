package io.euruapp.view

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.Query
import io.codelabs.recyclerview.Divided
import io.codelabs.recyclerview.GridMarginDecoration
import io.codelabs.recyclerview.SlideInItemAnimator
import io.codelabs.util.bindView
import io.codelabs.widget.CircularImageView
import io.euruapp.R
import io.euruapp.core.BaseActivity
import io.euruapp.model.Business
import io.euruapp.model.Category
import io.euruapp.model.User
import io.euruapp.util.ConstantsUtils
import io.euruapp.util.glide.GlideApp
import java.util.*

class
CategoryDetailsActivity(override val layoutId: Int = R.layout.activity_category_details) : BaseActivity() {
    private val toolbar: Toolbar by bindView(R.id.toolbar)
    private val grid: RecyclerView by bindView(R.id.grid_details)

    override fun onViewCreated(intent: Intent, instanceState: Bundle?) {

        toolbar!!.setNavigationOnClickListener { v -> onBackPressed() }

        if (intent.hasExtra(EXTRA_CATEGORY)) {
            val category = intent.getParcelableExtra<Category>(EXTRA_CATEGORY)
            toolbar!!.title = category.name
            searchFor(category.name)
        } else {
            ConstantsUtils.showToast(this, "No data found for the category")
            finishAfterTransition()
        }
    }

    private fun searchFor(name: String) {
        grid!!.layoutManager = LinearLayoutManager(this)
        grid!!.addItemDecoration(GridMarginDecoration(4))
        grid!!.itemAnimator = SlideInItemAnimator()
        grid!!.setHasFixedSize(true)
        val adapter = ServiceAdapter(this@CategoryDetailsActivity)
        grid!!.adapter = adapter

        //Fetch data from the database
        firestore.collection(ConstantsUtils.COLLECTION_BUSINESS)
            .whereEqualTo("category", name)
            .orderBy("key", Query.Direction.ASCENDING)
            .limit(200)
            .get()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    if (result != null && !result.documents.isEmpty()) {
                        val objects = result.toObjects(Business::class.java)
                        adapter.addAndResort(objects)
                    }
                }
            }.addOnFailureListener(this) { e ->
                ConstantsUtils.logResult(e.localizedMessage)
                ConstantsUtils.showToast(this@CategoryDetailsActivity, e.localizedMessage)
            }
    }

    class ServiceAdapter(private val context: CategoryDetailsActivity) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val geocoder: Geocoder
        private val layoutInflater = LayoutInflater.from(context)

        private val dataset: MutableList<Business>

        init {
            dataset = ArrayList(0)
            this.geocoder = Geocoder(context, Locale.getDefault())
        }

        override fun getItemViewType(position: Int): Int {
            return if (dataset.isEmpty()) TYPE_EMPTY else TYPE_ITEM
        }

        override fun getItemId(position: Int): Long {
            return if (getItemViewType(position) == TYPE_EMPTY) RecyclerView.NO_POSITION.toLong() else dataset[position].hashCode().toLong()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            when (viewType) {
                TYPE_EMPTY -> return EmptyListViewHolder(
                    layoutInflater.inflate(
                        R.layout.item_empty_business,
                        parent,
                        false
                    )
                )
                TYPE_ITEM -> return BusinessViewHolder(layoutInflater.inflate(R.layout.item_service, parent, false))
                else -> return throw IllegalArgumentException("Cannot find viewholder")
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (getItemViewType(position) == TYPE_ITEM && holder is BusinessViewHolder) {
                val business = dataset[position]

                holder.itemView.setOnClickListener { v ->
                    val b = Bundle(0)
                    b.putParcelable(MapsActivity.EXTRA_BUSINESS_MODEL, business)
                    ConstantsUtils.intentTo(context, MapsActivity::class.java, b)
                }

                holder.name!!.text = String.format("(%s)", business.name.trim { it <= ' ' })

                try {
                    val addresses = business.addresses
                    if (!addresses.isEmpty()) {
                        val location = geocoder.getFromLocation(addresses[0].lat, addresses[0].lng, 1)
                        if (location != null && !location.isEmpty()) {
                            ConstantsUtils.logResult(location)

                            val addressLine = location[0].getAddressLine(0)
                            holder.address!!.text = addressLine
                        }
                    }
                } catch (ex: Exception) {
                    ConstantsUtils.logResult(ex.localizedMessage)
                    holder.address!!.text = "Not found"
                }

                context.firestore.collection(ConstantsUtils.COLLECTION_USERS).document(business.userUID)
                    .get()
                    .addOnCompleteListener(context) { task ->
                        if (task.isSuccessful) {
                            val result = task.result
                            if (result != null && result.exists()) {
                                val user = result.toObject(User::class.java)
                                if (user != null) {
                                    //Set owner name
                                    holder.owner!!.text = user.name

                                    //Load owner profile
                                    GlideApp.with(context)
                                        .load(user.getProfile())
                                        .placeholder(R.drawable.avatar_placeholder)
                                        .error(R.drawable.avatar_placeholder)
                                        .circleCrop()
                                        .into(holder.avatar!!)

                                }
                            }
                        }
                    }.addOnFailureListener(context) { e ->
                        ConstantsUtils.logResult(e.localizedMessage)
                        holder.owner!!.text = business.phone
                    }
            }
        }

        override fun getItemCount(): Int {
            return if (dataset.isEmpty()) 1 else dataset.size
        }

        fun addAndResort(businesses: List<Business>) {
            if (!businesses.isEmpty()) {
                for (business in businesses) {
                    ConstantsUtils.logResult(business)
                    var add = true

                    for (i in dataset.indices) {
                        if (dataset[i].hashCode() == business.hashCode() && business.name.isEmpty()) add = false
                    }

                    if (add) {
                        dataset.add(business)
                        notifyItemRangeChanged(0, businesses.size)
                    }

                }

            }
        }


        class BusinessViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), Divided {
            val name: TextView by bindView(R.id.business_name)
            val owner: TextView by bindView(R.id.business_owner)
            val address: TextView by bindView(R.id.business_address)
            val avatar: CircularImageView by bindView(R.id.business_owner_avatar)

        }

        class EmptyListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

        companion object {

            val TYPE_ITEM = 0
            val TYPE_EMPTY = -1
        }

    }

    companion object {
        val EXTRA_CATEGORY = "EXTRA_CATEGORY"
    }

}
