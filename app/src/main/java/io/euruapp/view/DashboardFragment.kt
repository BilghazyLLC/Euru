package io.euruapp.view


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import io.codelabs.util.bindView
import io.codelabs.widget.BaselineGridTextView
import io.codelabs.widget.CircularImageView
import io.euruapp.R
import io.euruapp.core.BaseFragment
import io.euruapp.model.User
import io.euruapp.util.ConstantsUtils
import io.codelabs.sdk.glide.GlideApp

/**
 * A simple [BaseFragment] subclass.
 */
class DashboardFragment : BaseFragment() {
    private val avatar: CircularImageView by bindView(R.id.avatar)
    private val username: BaselineGridTextView by bindView(R.id.username)
    private val regDate: BaselineGridTextView by bindView(R.id.reg_date)
    private val account: BaselineGridTextView by bindView(R.id.acct)
    //private val bill: BaselineGridTextView by bindView(R.id.bill)
//    private val grid: RecyclerView by bindView(R.id.grid_recents)
    private val scrollContainer: ScrollView by bindView(R.id.scroll_container)
    private val scrollContent: ViewGroup by bindView(R.id.scroll_content)
    private val completedJobs: ViewGroup by bindView(R.id.card_completed_jobs)
    private val pendingJobs: ViewGroup by bindView(R.id.card_pending_jobs)
    private val newJobs: ViewGroup by bindView(R.id.card_new_jobs)
    private val editProfile: ViewGroup by bindView(R.id.card_edit_profile)
    private var isNewData = false

//    private lateinit var adapter: RecentsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindUser()
        if (database.isLoggedIn)
            firestore.collection(ConstantsUtils.COLLECTION_USERS).document(database.user.key)
                .get()
                .addOnCompleteListener(requireActivity()) {
                    if (it.isSuccessful) {
                        if (it.result != null && it.result!!.exists()) {
                            val user = it.result?.toObject(User::class.java)
                            user?.setType(User.TYPE_BUSINESS)
                            database.user = user
                            isNewData = true
                            ConstantsUtils.logResult("Dashboard user: $user")
                            bindUser()
                        }
                    }
                }

        /*  //Init adapter
          adapter = RecentsAdapter()

          //Setup recyclerview
          grid!!.adapter = adapter
          grid!!.layoutManager = LinearLayoutManager(requireContext())
          grid!!.itemAnimator = SlideInItemAnimator()
          grid!!.addItemDecoration(GridMarginDecoration(5))
          grid!!.addItemDecoration(GridItemDividerDecoration(requireContext(), R.dimen.divider_height, R.color.divider))
          grid!!.setHasFixedSize(true)

          val models = ArrayList<RequestModel>(0)
          models.add(
              RequestModel.Builder()
                  .setDataKey(System.currentTimeMillis().toString())
                  .setLocation(EuruGeoPoint())
                  .setTitle("Repair of laptop")
                  .setProviderId(database.user.key)
                  .build()
          )
          models.add(
              RequestModel.Builder()
                  .setDataKey(System.currentTimeMillis().toString())
                  .setLocation(EuruGeoPoint())
                  .setTitle("Repair of refrigerator")
                  .setProviderId(database.user.key)
                  .build()
          )
          models.add(
              RequestModel.Builder()
                  .setDataKey(System.currentTimeMillis().toString())
                  .setLocation(EuruGeoPoint())
                  .setTitle("Repair of flat screen tv")
                  .setProviderId(database.user.key)
                  .build()
          )
          models.add(
              RequestModel.Builder()
                  .setDataKey(System.currentTimeMillis().toString())
                  .setLocation(EuruGeoPoint())
                  .setTitle("Repair of heater")
                  .build()
          )
          adapter!!.addAndResort(models)*/

        editProfile.setOnClickListener { editProfile(it) }
        completedJobs.setOnClickListener { viewCompletedJobs(it) }
        pendingJobs.setOnClickListener { viewPendingJobs(it) }
        newJobs.setOnClickListener { viewNewJobs(it) }

    }

    private fun bindUser() {
        //Get current user
        val user = database.user

        //Set username
        username.text = user.name

        //Set registration date
        regDate.text = auth.currentUser?.email

        //Load profile image
        try {
            GlideApp.with(requireContext())
                .load(user.getProfile())
                .placeholder(R.drawable.avatar_placeholder)
                .fallback(R.drawable.avatar_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .transition(withCrossFade())
                .circleCrop()
                .into(avatar)
        } catch (e: Exception) {
        }
    }

    internal fun editProfile(v: View) {
        //Replace current fragment with the business owner's profile screen
      //  requireActivity().supportFragmentManager.beginTransaction().replace(R.id.frame, ProfileFragment.newInstance())
        requireActivity().supportFragmentManager!!.beginTransaction().replace(R.id.frame, ProfileFragment.newInstance()).commit()
    }

    internal fun viewCompletedJobs(v: View) {
        JobsActivity.start(requireActivity(), ConstantsUtils.COMPLETED_JOBS)
    }

    internal fun viewPendingJobs(v: View) {
        JobsActivity.start(requireActivity(), ConstantsUtils.PENDING_JOBS)
    }

    internal fun viewNewJobs(v: View) {
        JobsActivity.start(requireActivity(), ConstantsUtils.NEW_JOBS)
    }

    /*class RecentsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), Divided {
        val state: BaselineGridTextView by bindView(R.id.model_state_text)
        val title: BaselineGridTextView by bindView(R.id.model_title)
        val colorBar: View by bindView(R.id.model_view)
        val timestamp: BaselineGridTextView by bindView(R.id.model_timestamp)
    }

    class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)*/

    /*inner class RecentsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var requestModels: MutableList<RequestModel> = ArrayList(0)
        private val inflater: LayoutInflater

        init {
            this.inflater = LayoutInflater.from(requireContext())
        }

        override fun getItemViewType(position: Int): Int {
            return if (requestModels.isEmpty()) TYPE_EMPTY else TYPE_RECENT_ITEM
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                TYPE_EMPTY -> EmptyViewHolder(inflater.inflate(R.layout.item_no_recent, parent, false))
                TYPE_RECENT_ITEM -> RecentsViewHolder(inflater.inflate(R.layout.item_recent, parent, false))
                else -> throw IllegalArgumentException("No viewholder found")
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (getItemViewType(position) == TYPE_RECENT_ITEM) {
                val model = requestModels[position]
                if (holder is RecentsViewHolder) {
                    holder.state!!.text = "Completed"
                    holder.timestamp!!.text =
                            DateUtils.getRelativeTimeSpanString(
                                model.timestamp,
                                System.currentTimeMillis(),
                                DateUtils.SECOND_IN_MILLIS
                            )
                    holder.title!!.text = model.title
                    holder.colorBar!!.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.colorGreenLight
                        )
                    )

                    holder.itemView.setOnClickListener { v ->
                        //todo: add click action for details
                    }

                }

            }
        }

        override fun getItemCount(): Int {
            return if (requestModels.isEmpty()) 1 else requestModels.size
        }

        fun addAndResort(models: List<RequestModel>) {
            if (!models.isEmpty()) {
                for (item in models) {
                    var add = true

                    for (i in requestModels.indices) {
                        add = item.hashCode() != requestModels[i].hashCode()
                    }

                    if (add) {
                        requestModels.add(item)
                        notifyItemRangeChanged(0, models.size)
                    }
                }
            }
        }

    }*/

    companion object {
        /*private val TYPE_EMPTY = -1
        private val TYPE_RECENT_ITEM = 0*/

        internal fun newInstance(): DashboardFragment {
            return DashboardFragment()
        }
    }

}// Required empty public constructor
