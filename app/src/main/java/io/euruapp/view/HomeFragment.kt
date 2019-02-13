package io.euruapp.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import io.codelabs.recyclerview.Divided
import io.codelabs.recyclerview.GridItemDividerDecoration
import io.codelabs.recyclerview.GridMarginDecoration
import io.codelabs.recyclerview.SlideInItemAnimator
import io.codelabs.util.bindView
import io.codelabs.widget.ForegroundImageView
import io.euruapp.R
import io.euruapp.model.Category
import io.euruapp.util.ConstantsUtils
import io.euruapp.util.OnSearchableItemClickListener
import io.euruapp.util.glide.GlideApp

class HomeFragment : Fragment(), OnSearchableItemClickListener<Category> {
    private val grid: RecyclerView by bindView(R.id.grid)

    private var adapter: HomeListAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = HomeListAdapter(requireContext(), this)
        val lm = GridLayoutManager(requireContext(), 4)
        grid!!.layoutManager = lm
        grid!!.setHasFixedSize(true)
        grid!!.adapter = adapter
        grid!!.itemAnimator = SlideInItemAnimator()
        grid!!.addItemDecoration(GridItemDividerDecoration(requireContext(), R.dimen.divider_height, R.color.divider))
        grid!!.addItemDecoration(GridMarginDecoration(5))
    }

    override fun onItemClicked(position: Int, `object`: Category, isLongClick: Boolean) {
        val intent = Intent(requireActivity(), CategoryDetailsActivity::class.java)
        intent.putExtra(CategoryDetailsActivity.EXTRA_CATEGORY, `object`)
        requireActivity().startActivity(intent)
    }

    internal inner class HomeListAdapter(
        private val context: Context,
        private val listener: OnSearchableItemClickListener<Category>
    ) : RecyclerView.Adapter<HomeListAdapter.HomeListViewHolder>() {
        private val categories: List<Category>

        init {
            this.categories = ConstantsUtils.categories
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeListViewHolder {
            return HomeListViewHolder(LayoutInflater.from(context).inflate(R.layout.item_category, parent, false))
        }

        override fun onBindViewHolder(holder: HomeListViewHolder, position: Int) {
            val category = categories[position]

            holder.name.text = category.name.trim { it <= ' ' }
            holder.address.visibility = View.INVISIBLE   //TODO: unhide this when the category has address attached

            GlideApp.with(context)
                .asBitmap()
                .load(category.image)
                .placeholder(R.color.content_placeholder)
                .error(R.color.content_placeholder)
                .fallback(R.color.content_placeholder)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(holder.avatar)

            holder.itemView.setOnClickListener { v -> listener.onItemClicked(position, category, false) }

        }

        override fun getItemCount(): Int {
            return categories.size
        }

        //Divided items in the view holder
        internal inner class HomeListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), Divided {
            val address: TextView by bindView(R.id.business_address)
            val name: TextView by bindView(R.id.business_name)
            val avatar: ForegroundImageView by bindView(R.id.business_picture)
        }
    }

    companion object {

        internal fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

}
