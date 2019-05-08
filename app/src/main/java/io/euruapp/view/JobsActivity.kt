package io.euruapp.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.Query
import io.codelabs.recyclerview.Divided
import io.codelabs.util.bindView
import io.euruapp.R
import io.euruapp.core.BaseActivity
import io.euruapp.model.Business
import io.euruapp.model.Job
import io.euruapp.model.User
import io.euruapp.util.ConstantsUtils
import io.euruapp.util.ConstantsUtils.ProviderJobType
import io.euruapp.util.ConstantsUtils.intentTo
import io.euruapp.util.JobClickListener
import io.codelabs.sdk.glide.GlideApp
import kotlinx.android.synthetic.main.item_job.view.*
import java.util.*

class JobsActivity(override val layoutId: Int = R.layout.activity_jobs) : BaseActivity(), JobClickListener {


    private val grid: RecyclerView by bindView(R.id.grid)
    private val toolbar: Toolbar by bindView(R.id.toolbar)

    private var business: Business? = null
    private var jobType: String? = null
    private var adapter: JobAdapter? = null

    override fun onViewCreated(intent: Intent, instanceState: Bundle?) {
        //Setup toolbar
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { v -> finishAfterTransition() }

        //Setup recyclerview
        adapter = JobAdapter(this)
        grid.adapter = adapter
        val layoutManager = LinearLayoutManager(this)
        grid.layoutManager = layoutManager
        grid.setHasFixedSize(true)
        grid.itemAnimator = DefaultItemAnimator()
//        grid.addItemDecoration(DividerItemDecoration(this, layoutManager.orientation))

        //Get the job type
        if (intent.hasExtra(EXTRA_JOB_TYPE)) {
            jobType = intent.getStringExtra(EXTRA_JOB_TYPE)
            if (database.isLoggedIn) setupGridForJobType()
        }
    }

    private fun setupGridForJobType() {
        //Jobs
        when (jobType) {
            ConstantsUtils.NEW_JOBS -> {

                val calendar = Calendar.getInstance()
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                firestore.collection(ConstantsUtils.COLLECTION_REQUESTS).document(database.user.key)
                    .collection(dayOfWeek.toString())
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener(this@JobsActivity) { queryDocumentSnapshots, e ->
                        if (e != null) {
                            ConstantsUtils.showToast(this, "Failed to retrieve jobs")
                            return@addSnapshotListener
                        }

                        if (queryDocumentSnapshots == null) return@addSnapshotListener
                        val jobs = queryDocumentSnapshots.toObjects(Job::class.java)
                        adapter?.clear()
                        adapter?.addAndResort(jobs)
//                        logResult(queryDocumentSnapshots.documents)
                    }
            }

            else -> {
                firestore.collection(ConstantsUtils.COLLECTION_JOBS).document(database.user.key)
                    .collection(jobType!!)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener(this@JobsActivity) { queryDocumentSnapshots, e ->
                        if (e != null) {
                            ConstantsUtils.showToast(this, "Failed to retrieve jobs")
                            return@addSnapshotListener
                        }

                        if (queryDocumentSnapshots == null) return@addSnapshotListener
                        val jobs = queryDocumentSnapshots.toObjects(Job::class.java)
                        adapter?.clear()
                        adapter?.addAndResort(jobs)
                    }
            }
        }

        if (database.user.type == User.TYPE_BUSINESS) {
            //Business
            firestore.collection(ConstantsUtils.COLLECTION_BUSINESS)
                .whereEqualTo("userUID", database.user.key)
                .limit(1)
                .get()
                .addOnCompleteListener(this@JobsActivity) { task ->
                    if (task.isSuccessful) {
                        val result = task.result
                        if (result == null || result.isEmpty) return@addOnCompleteListener

                        //Get the document from the result
                        val snapshot = result.documents[0]
                        if (snapshot.exists()) {
                            business = snapshot.toObject(Business::class.java)
                            if (business == null) return@addOnCompleteListener
                            bindBusiness()
                        }

                    }
                }.addOnFailureListener(this@JobsActivity) { e -> ConstantsUtils.logResult(e.localizedMessage) }
        }
    }

    private fun bindBusiness() {
        ConstantsUtils.logResult("business model for JobsActivity: " + business!!)
    }

    inner class JobAdapter constructor(private val listener: JobClickListener) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val dataset = ArrayList<Job>(0)

        override fun getItemViewType(position: Int): Int {
            return if (dataset.isEmpty()) TYPE_EMPTY else TYPE_JOB
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                TYPE_EMPTY -> createEmptyViewHolder(parent)
                TYPE_JOB -> createJobViewHolder(parent)
                else -> throw IllegalArgumentException("No viewholder found")
            }
        }

        private fun createJobViewHolder(parent: ViewGroup): JobViewHolder {
            return JobViewHolder(layoutInflater.inflate(R.layout.item_job, parent, false))
        }

        private fun createEmptyViewHolder(parent: ViewGroup): EmptyViewHolder {
            return EmptyViewHolder(layoutInflater.inflate(R.layout.item_empty_job, parent, false))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (getItemViewType(position) == TYPE_JOB) {
                val job = dataset[position]

                if (holder is JobViewHolder) {
                    holder.view.job_name.text = job.title?.trim()
                    holder.view.job_category.text = job.category

                    holder.view.job_timestamp.text = DateUtils.getRelativeTimeSpanString(job.timestamp,System.currentTimeMillis(),DateUtils.SECOND_IN_MILLIS)

                    if (job.image.isNullOrEmpty()) holder.view.job_image.visibility = View.GONE
                    else {
                        GlideApp.with(holder.view.context)
                            .asBitmap()
                            .load(job.image)
                            .placeholder(R.color.content_placeholder)
                            .error(R.color.content_placeholder)
                            .fallback(R.color.content_placeholder)
                            .into(holder.view.job_image)
                    }

                    for (category in ConstantsUtils.categories) {
                        if (category.name == job.category) {
                            GlideApp.with(holder.view.context)
                                .asBitmap()
                                .load(category.image)
                                .circleCrop()
                                .placeholder(R.drawable.avatar_placeholder)
                                .error(R.drawable.avatar_placeholder)
                                .fallback(R.drawable.avatar_placeholder)
                                .into(holder.view.job_category_image)

                            holder.view.job_category_image.setOnClickListener {
                                val intent = Intent(this@JobsActivity, CategoryDetailsActivity::class.java)
                                intent.putExtra(CategoryDetailsActivity.EXTRA_CATEGORY, category)
                                this@JobsActivity.startActivity(intent)
                            }

                            return
                        }
                    }

                    holder.view.setOnClickListener { v ->
                        ConstantsUtils.logResult(job)
                        listener.onClick(position, job)
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            var count = 0
            count += if (dataset.isEmpty()) 1 else dataset.size
            return count
        }

        internal fun clear() = dataset.clear()

        internal fun addAndResort(newJobs: List<Job>) {
            if (newJobs.isEmpty()) return

            for (item in newJobs) {
                var add = true
                for (i in 0 until dataset.size) {
                    add = !dataset[i].dataKey.isNullOrEmpty() && dataset[i].dataKey != item.dataKey
                }

                if (add) {
                    dataset.add(item)
                    notifyItemRangeChanged(0, newJobs.size)
                }
            }

        }

        //Empty viewholder
        inner class EmptyViewHolder(val view: View) : RecyclerView.ViewHolder(view)

        //Job viewholder
        inner class JobViewHolder(val view: View) : RecyclerView.ViewHolder(view), Divided
    }

    override fun onClick(position: Int, data: Job) {
        if (!jobType.isNullOrEmpty() && jobType != ConstantsUtils.NEW_JOBS) {
            val b = Bundle(0)
            b.putString(ServiceProviderDetailsActivity.EXTRA_PROVIDER_UID, database.user.key)
            b.putString(ServiceProviderDetailsActivity.EXTRA_REQUEST_MODEL_ID, data.dataKey)
            b.putString(ServiceProviderDetailsActivity.EXTRA_CUSTOMER_UID, data.userKey)
            b.putString(ServiceProviderDetailsActivity.EXTRA_JOB_TYPE, jobType)
            intentTo(this@JobsActivity, ServiceProviderDetailsActivity::class.java, b)
        }
    }


    companion object {
        internal const val TYPE_EMPTY = -1
        internal const val TYPE_JOB = 0
        const val EXTRA_JOB_TYPE = "EXTRA_JOB_TYPE"

        // Start this activity from this function. Just call: JobsActivity.start(...) from any activity
        fun start(host: Activity, @ProviderJobType jobType: String) {
            val intent = Intent(host, JobsActivity::class.java)
            intent.putExtra(EXTRA_JOB_TYPE, jobType)
            host.startActivity(intent)
        }
    }
}
