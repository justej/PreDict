package com.github.justej.predict.activities

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.*
import android.widget.LinearLayout
import android.widget.ToggleButton
import com.github.justej.predict.R
import com.github.justej.predict.model.data.TAG_SYMBOL
import kotlinx.android.synthetic.main.activity_navigation.*
import kotlinx.android.synthetic.main.app_bar_navigation.*
import kotlinx.android.synthetic.main.content_navigation.*
import kotlinx.android.synthetic.main.words_item.view.*


private const val TAG = "WordsActivity"

class WordsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val presenter = WordsPresenter(this)
    private val viewAdapter = TranslatedWordAdapter(presenter)
    private lateinit var recyclerView: RecyclerView

    //region Lifecycle methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        inflateRecyclerView()
        configureSearchView()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.navigation_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.search_menu_item -> true // TODO: replace the placeholder
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // TODO: Handle navigation view item clicks
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    //endregion

    //region Event handlers

    fun addNewWord(view: View) {
        presenter.addNewWord(searchView.query.toString())
    }

    //endregion

    fun updateWords(isTag: Boolean, query: String) {
        if (presenter.wordsCount() == 0 && !isTag) {
            addNewWordText.text = "Add new card for the word \"$query\""
            addNewWordText.visibility = View.VISIBLE
            wordsView.visibility = View.GONE
        } else {
            addNewWordText.visibility = View.GONE
            wordsView.visibility = View.VISIBLE
        }

        viewAdapter.notifyDataSetChanged()
    }

    private fun inflateRecyclerView() {
        recyclerView = wordsView.apply {
            layoutManager = LinearLayoutManager(this@WordsActivity)
            adapter = viewAdapter
        }
    }

    private fun configureSearchView() {
        val tagButton = ToggleButton(this)
        tagButton.apply {
            text = TAG_SYMBOL
            textOn = TAG_SYMBOL
            textOff = TAG_SYMBOL
            setOnCheckedChangeListener { _, state ->
                searchView.setQuery(updateSearchQuery(state, searchView.query.toString()), false)
            }
        }

        searchView.apply {
            (getChildAt(0) as LinearLayout).addView(tagButton, 0)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                var searchQuery: String? = null

                override fun onQueryTextSubmit(p0: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(updatedQuery: String?): Boolean {
                    if (updatedQuery == null || updatedQuery == searchQuery) {
                        return false
                    }

                    val isHashed = updatedQuery.startsWith(TAG_SYMBOL)
                    val wasHashed = searchQuery?.startsWith(TAG_SYMBOL) ?: false
                    if (isHashed != wasHashed) {
                        tagButton.isChecked = isHashed
                    }

                    searchQuery = updateSearchQuery(isHashed, updatedQuery)
                    presenter.searchWordCard(searchQuery!!) { isTag, query -> updateWords(isTag, query) }
                    searchView.setQuery(searchQuery, false)

                    return true
                }

            })
        }
    }

    companion object {

        fun updateSearchQuery(isTag: Boolean, updatedQuery: String?): String {
            val untaggedQuery = updatedQuery?.removePrefix(TAG_SYMBOL) ?: ""
            return if (isTag) {
                "$TAG_SYMBOL$untaggedQuery"
            } else {
                untaggedQuery
            }
        }

    }

}

class TranslatedWordAdapter(private val presenter: WordsPresenter) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val translationLayout = LayoutInflater.from(parent.context)
                .inflate(R.layout.words_item, parent, false) as ViewGroup
        return ViewHolder(translationLayout)
    }

    override fun getItemCount(): Int {
        return presenter.wordsCount()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.wordLabel.text = presenter.word(position)
    }

}

class ViewHolder(viewGroup: ViewGroup) : RecyclerView.ViewHolder(viewGroup)
