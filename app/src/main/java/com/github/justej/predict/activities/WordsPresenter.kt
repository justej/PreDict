package com.github.justej.predict.activities

import android.app.Activity
import android.content.Intent
import com.github.justej.predict.model.data.PARAM_WORD
import com.github.justej.predict.model.data.TAG_SYMBOL

// TODO: replace the stub with real requests to DB
private val words = listOf("one", "two", "three", "four", "five")

class WordsPresenter(private val ui: Activity) {

    private var wordsToDisplay: List<String> = listOf()

    init {
        wordsToDisplay = words.map { it }
    }

    fun wordsCount() = wordsToDisplay.size

    fun word(position: Int) = wordsToDisplay[position]

    fun searchWordCard(query: String, wordsUpdater: (Boolean, String) -> Unit) {
        val q = query.removePrefix(TAG_SYMBOL)
        val isTag = query.startsWith(TAG_SYMBOL)
        wordsToDisplay = if (isTag) {
            searchByTag(q) // TODO: implement
        } else {
            searchByWord(q) // TODO: implement
        }

        wordsUpdater(isTag, query)
    }

    private fun searchByWord(word: String) = words
            .map { return@map if (it.contains(word)) it else null }
            .filterNotNull()

    private fun searchByTag(tag: String): List<String> = words
            .map { return@map if (it.contains(tag)) "$TAG_SYMBOL$it" else null }
            .filterNotNull()

    fun addNewWord(word: String) {
        val intent = Intent(ui, NewCardActivity::class.java)
        intent.putExtra(PARAM_WORD, word)
        ui.startActivity(intent)
    }

}
