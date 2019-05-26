package com.github.justej.predict.activities

import android.app.Activity
import android.content.Intent
import com.github.justej.predict.model.data.PARAM_WORD
import com.github.justej.predict.model.data.TAG_SYMBOL
import com.github.justej.predict.model.db.Persister

class WordsPresenter(private val ui: Activity) {

    private val persister = Persister(ui)
    private var wordsToDisplay: List<String> = listOf()
    private val words by lazy { loadWordsFromDb() }

    private fun loadWordsFromDb() = persister.getWordCardByWordLike("").map { it.catchWordSpellings }.toMutableSet()

    fun loadWords() {
        wordsToDisplay = words.toList()
    }

    fun wordsCount() = wordsToDisplay.size

    fun word(position: Int) = wordsToDisplay[position]

    fun searchWordCard(query: String, wordsUpdater: (Boolean, String) -> Unit) {
        val q = query.removePrefix(TAG_SYMBOL)
        val isTag = query.startsWith(TAG_SYMBOL)
        wordsToDisplay = if (isTag) {
            searchByTag(q) // TODO: implement
        } else {
            searchByWordLike(q) // TODO: implement
        }

        wordsUpdater(isTag, query)
    }

    fun searchByWord(word: String) = words.contains(word)

    fun searchByWordLike(word: String) = words.filter { it.contains(word) }

    fun searchByTag(tag: String): List<String> = persister.getWordCardByTag(tag)
            .map { it.catchWordSpellings }

    fun addNewWord(word: String) {
        val intent = Intent(ui, NewCardActivity::class.java)
        intent.putExtra(PARAM_WORD, word)
        ui.startActivity(intent)
        words.addAll(loadWordsFromDb())
    }

}
