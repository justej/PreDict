package com.github.justej.predict.activities

import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.*
import android.widget.LinearLayout
import android.widget.ToggleButton
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.justej.predict.R
import com.github.justej.predict.model.data.TAG_SYMBOL
import com.github.justej.predict.utils.Dialogs
import com.github.justej.predict.utils.StringUtils
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_navigation.*
import kotlinx.android.synthetic.main.app_bar.*
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

        recyclerView.requestFocus()
    }

    override fun onResume() {
        super.onResume()
        presenter.loadWords()
        viewAdapter.notifyDataSetChanged()
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
            R.id.nav_words -> {
            }

            R.id.nav_verbs -> {
            }

            R.id.nav_texts -> {
            }

            R.id.nav_statistics -> {
            }

            R.id.nav_settings -> {
            }

            R.id.nav_share -> {
            }

            R.id.nav_export -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    //endregion

    //region Event handlers

    fun addWord(view: View) {
        presenter.createOrEditWordCard(searchView.query.toString(), "")
        viewAdapter.notifyDataSetChanged()
    }

    //endregion

    fun updateWords(isTag: Boolean, query: String) {
        if (query.isNotEmpty() && !presenter.searchByWord(query) && !isTag) {
            addNewWordText.text = "Add new card for the word \"$query\""
            addNewWordText.visibility = View.VISIBLE
            wordsView.visibility = View.GONE
        } else {
            addNewWordText.visibility = View.GONE
        }

        if (presenter.searchByWordLike(query).isEmpty() && !isTag) {
            wordsView.visibility = View.GONE
        } else {
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
        val wordCard = presenter.word(position)
        holder.apply {
            val catchWordsSpellings = "<b>${StringUtils.escape(wordCard.catchWordSpellings.replace("\n", "; "))}</b>"
            val homonymDiscriminator = if (wordCard.homonymDiscriminator.isBlank()) {
                ""
            } else {
                " <sup><small>(${StringUtils.escape(wordCard.homonymDiscriminator)})</small></sup>"
            }

            val transcription = if (wordCard.transcription.isBlank()) {
                ""
            } else {
                " <i>[${StringUtils.escape(wordCard.transcription.replace("\n", "; "))}]</i>"
            }

            val cardText = catchWordsSpellings + homonymDiscriminator + transcription

            itemView.catchWordSpannable.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(cardText, Html.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml(cardText)
            }

            itemView.translationLabel.text = wordCard.translation
            viewGroup.setOnClickListener {
                presenter.createOrEditWordCard(wordCard.catchWordSpellings, wordCard.homonymDiscriminator)
            }
            viewGroup.setOnLongClickListener {
                Dialogs.newDialogYesNo(viewGroup.context,
                        "Delete the word card?",
                        { _: DialogInterface, _: Int ->
                            run {
                                presenter.deleteWord(wordCard)
                                notifyDataSetChanged()
                            }
                        },
                        { _: DialogInterface, _: Int -> })
                return@setOnLongClickListener true
            }
        }
    }

}

class ViewHolder(internal val viewGroup: ViewGroup) : RecyclerView.ViewHolder(viewGroup)
