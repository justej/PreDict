package com.github.justej.predict.activities

import android.app.Activity

const val TAG_SYMBOL = "#"

class WordsPresenter(private val activity: Activity) {
    // TODO: replace the stub with real requests to DB
    private val words = listOf("one", "two", "three", "four", "five")
    private var wordsToDisplay: List<String> = words.map { it }

    fun wordsCount() = wordsToDisplay.size

    fun word(position: Int) = wordsToDisplay[position]

    fun search(query: String) {
        val q = query.removePrefix(TAG_SYMBOL)
        wordsToDisplay = if (query.startsWith(TAG_SYMBOL)) {
            searchByTag(q) // TODO: implement
        } else {
            searchByWord(q) // TODO: implement
        }
    }

    private fun searchByWord(word: String) = words
            .map { return@map if (it.contains(word)) it else null }
            .filterNotNull()

    private fun searchByTag(tag: String): List<String> = words
            .map { return@map if (it.contains(tag)) "$TAG_SYMBOL$it" else null }
            .filterNotNull()
}
