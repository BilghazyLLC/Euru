package io.euruapp.view

import android.content.Context
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import io.codelabs.util.bindView
import io.codelabs.widget.BaselineGridTextView
import io.codelabs.widget.CircularImageView
import io.euruapp.R
import io.euruapp.model.EuruSearchableItem
import io.euruapp.model.User
import io.euruapp.util.ConstantsUtils
import io.euruapp.util.OnSearchableItemClickListener
import io.euruapp.util.glide.GlideApp
import java.util.*

class SearchAdapter(
    private val context: Context,
    private val listener: OnSearchableItemClickListener<EuruSearchableItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dataset: MutableList<EuruSearchableItem>
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val geocoder: Geocoder

    init {
        this.dataset = ArrayList(0)
        this.geocoder = Geocoder(context, Locale.getDefault())
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataset.isEmpty()) TYPE_EMPTY else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_EMPTY -> return EmptyListViewHolder(inflater.inflate(R.layout.item_empty_query, parent, false))
            TYPE_ITEM -> return SearchableItemViewHolder(inflater.inflate(R.layout.item_query, parent, false))
            else -> throw IllegalArgumentException("No valid ViewHolder found")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_ITEM && holder is SearchableItemViewHolder) {
            val searchableItem = dataset[position]

            if (searchableItem is User) {
                holder.username.text = searchableItem.name
                var addressLocation = "Not found"

                //get the user's location from the address line
                try {
                    val address = searchableItem.address
                    val location = geocoder.getFromLocation(address.lat, address.lng, 1)
                    if (!location.isEmpty()) {
                        addressLocation = location[0].getAddressLine(0)
                        holder.address!!.text = addressLocation
                    }
                } catch (ex: Exception) {
                    //When there is an exception, use the default defined text as the user's current location
                    ConstantsUtils.logResult(ex.localizedMessage)
                    holder.address?.text = addressLocation
                }

                GlideApp.with(context)
                    .load(searchableItem.profile)
                    .circleCrop()
                    .transition(withCrossFade())
                    .into(holder.avatar)

                //Add click listener for the view item
                holder.itemView.setOnClickListener { v -> listener.onItemClicked(position, searchableItem, false) }
                holder.itemView.setOnLongClickListener { v ->
                    listener.onItemClicked(position, searchableItem, true)
                    true
                }

            }
        }
    }

    override fun getItemCount(): Int {
        return if (dataset.isEmpty()) 1 else dataset.size    /*For the empty view*/
    }

    //Add a list of items
    internal fun addAndResort(data: List<EuruSearchableItem>) {
        if (!data.isEmpty()) {

            for (item in data) {
                var add = true

                for (i in dataset.indices) {
                    if (item.name == dataset[i].name) add = false
                }

                if (add) {
                    dataset.add(item)
                    notifyItemRangeChanged(0, data.size)
                }
            }
        }
    }

    //Add single item here
    internal fun addItem(item: EuruSearchableItem) {
        dataset.add(item)
        notifyDataSetChanged()
    }

    internal inner class EmptyListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    internal inner class SearchableItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val avatar: CircularImageView  by bindView(R.id.query_image)

        val username: BaselineGridTextView by bindView(R.id.query_name)
        val address: BaselineGridTextView? = null
    }

    companion object {
        private const val TYPE_EMPTY = 0
        private const val TYPE_ITEM = 1
    }

}
