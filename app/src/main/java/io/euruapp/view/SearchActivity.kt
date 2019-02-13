package io.euruapp.view

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.transition.Transition
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.codelabs.util.bindView
import io.euruapp.R
import io.euruapp.core.BaseActivity
import io.euruapp.model.EuruSearchableItem
import io.euruapp.model.User
import io.euruapp.util.ConstantsUtils
import io.euruapp.util.OnSearchableItemClickListener
import io.euruapp.viewmodel.SearchRepository

/**
 * Search for some business or service or category
 */
class SearchActivity(override val layoutId: Int = R.layout.activity_search) : BaseActivity(), OnSearchableItemClickListener<EuruSearchableItem> {

    //Bind views
    private val searchBack: ImageButton by bindView(R.id.searchback)
    private val searchBackContainer: ViewGroup  by bindView(R.id.searchback_container)
    private val searchView: SearchView by bindView(R.id.search_view)
    private val searchBackground: View  by bindView(R.id.search_background)
    private val progress: ProgressBar by bindView(android.R.id.empty)
    private val results: RecyclerView  by bindView(R.id.search_results)
    private val container: ViewGroup by bindView(R.id.container)
    private val searchToolbar: ViewGroup by bindView(R.id.search_toolbar)
    private val resultsContainer: ViewGroup by bindView(R.id.results_container)
    private val scrim: View by bindView(R.id.scrim)
    private val resultsScrim: View by bindView(R.id.results_scrim)

    private var adapter: SearchAdapter? = null
    private var repository: SearchRepository? = null
    private var transitions = SparseArray<Transition>(0)
    private var noResults: TextView? = null

    override fun onViewCreated(intent: Intent, instanceState: Bundle?) {
        setupSearchView()

        //Setup adapter
        adapter = SearchAdapter(this, this)
        results!!.adapter = adapter
        val layoutManager = LinearLayoutManager(this)
        results!!.layoutManager = layoutManager
        results!!.addItemDecoration(DividerItemDecoration(this, layoutManager.orientation))
        results!!.setHasFixedSize(true)

        repository = object : SearchRepository(this, this) {
            override fun onDataLoaded(data: List<EuruSearchableItem>?) {
                if (data != null) adapter!!.addAndResort(data)
                ConstantsUtils.showToast(this@SearchActivity, data!!)
            }
        }

        setupTransitions()
        onNewIntent(intent)

    }

    override fun onNewIntent(intent: Intent?) {
        if (intent != null && intent.hasExtra(SearchManager.QUERY)) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            if (!TextUtils.isEmpty(query)) {
                searchView!!.setQuery(query, false)
                searchFor(query)
            }
        }
    }

    private fun searchFor(query: String) {
        repository!!.searchFor(query)
    }

    private fun setupTransitions() {
        // TODO: 10/22/2018 Add transitions
    }

    private fun setupSearchView() {
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        if (searchManager != null) {
            searchView!!.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            searchView!!.queryHint = getString(R.string.search_hint)
            searchView!!.inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS
            searchView!!.imeOptions =
                    (searchView!!.imeOptions or EditorInfo.IME_ACTION_SEARCH or EditorInfo.IME_FLAG_NO_EXTRACT_UI
                            or EditorInfo.IME_FLAG_NO_FULLSCREEN)
            searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(s: String): Boolean {
                    searchFor(s)
                    return true
                }

                override fun onQueryTextChange(s: String): Boolean {
                    if (!TextUtils.isEmpty(s)) {
                        clearResults()
                    } else
                        searchFor(s)
                    return true
                }
            })
        }
    }

    private fun clearResults() {
        // TODO: 10/9/2018
    }

    fun dismiss(view: View) {
        finishAfterTransition()
    }

    override fun onItemClicked(position: Int, `object`: EuruSearchableItem, isLongClick: Boolean) {
        if (`object` is User) {
            //todo: do your things here
            ConstantsUtils.logResult(`object`)

            when (`object`.type) {
                User.TYPE_BUSINESS -> {
                }
                User.TYPE_CUSTOMER -> {
                }
                else -> {
                }
            }

        }
    }
}
