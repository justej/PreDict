package com.github.justej.predict.activities

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ToggleButton
import com.github.justej.predict.R
import kotlinx.android.synthetic.main.activity_navigation.*
import kotlinx.android.synthetic.main.app_bar_navigation.*
import kotlinx.android.synthetic.main.content_navigation.*
import kotlinx.android.synthetic.main.words_item.view.*


const val TAG = "WordsActivity"

class WordsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val presenter = WordsPresenter(this)
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: TranslatedWordAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

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

    private fun inflateRecyclerView() {
        viewManager = LinearLayoutManager(this)
        viewAdapter = TranslatedWordAdapter(presenter)

        recyclerView = wordsView.apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    private fun configureSearchView() {
        val tagButton = ToggleButton(this)

        fun updateSearchQuery(updatedQuery: String?): String {
            val untaggedQuery = updatedQuery?.removePrefix(TAG_SYMBOL) ?: ""
            return if (tagButton.isChecked) {
                "$TAG_SYMBOL$untaggedQuery"
            } else {
                untaggedQuery
            }
        }

        tagButton.apply {
            text = TAG_SYMBOL
            textOn = TAG_SYMBOL
            textOff = TAG_SYMBOL
            setOnCheckedChangeListener { _, _ ->
                searchView.setQuery(updateSearchQuery(searchView.query.toString()), false)
            }
        }

        (searchView.getChildAt(0) as LinearLayout).addView(tagButton, 0)

        searchView.apply {
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

                    searchQuery = updateSearchQuery(updatedQuery)
                    updateListOfWords(searchQuery!!)
                    searchView.setQuery(searchQuery, false)

                    return true
                }

                private fun updateListOfWords(searchQuery: String) {
                    presenter.search(searchQuery)
                    viewAdapter.notifyDataSetChanged()
                }

            })
        }
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

}

class TranslatedWordAdapter(private val presenter: WordsPresenter) : RecyclerView.Adapter<TranslatedWordAdapter.ViewHolder>() {

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

    class ViewHolder(viewGroup: ViewGroup) : RecyclerView.ViewHolder(viewGroup)

}
