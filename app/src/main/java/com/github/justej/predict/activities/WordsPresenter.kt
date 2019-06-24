package com.github.justej.predict.activities

import android.app.Activity
import android.content.Intent
import com.github.justej.predict.model.data.PARAM_HOMONYM_DISCRIMINATOR
import com.github.justej.predict.model.data.PARAM_WORD
import com.github.justej.predict.model.data.TAG_SYMBOL
import com.github.justej.predict.model.data.WordCard
import com.github.justej.predict.model.db.Persister

class WordsPresenter(private val ui: Activity) {

    private val persister = Persister(ui)
    private var wordsToDisplay: List<WordCard> = listOf()
    private val words = mutableSetOf<WordCard>()

    private fun loadWordsFromDb() = persister.getWordCardByWordLike("").toMutableSet()

    fun loadWords() {
        words.clear()
        words.addAll(loadWordsFromDb())
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

    fun searchByWord(word: String) = words
            .filter { it.catchWordSpellings.split("/n").contains(word) }
            .any()

    fun searchByWordLike(word: String) = words.filter { it.catchWordSpellings.contains(word) }

    fun searchByTag(tag: String): List<WordCard> = persister.getWordCardByTag(tag)

    fun deleteWord(wordCard: WordCard) {
        persister.deleteWord(wordCard)
    }

    fun createOrEditWordCard(word: String, homonymDiscriminator: String) {
        val intent = Intent(ui, WordCardActivity::class.java)
        intent.putExtra(PARAM_WORD, word)
        intent.putExtra(PARAM_HOMONYM_DISCRIMINATOR, homonymDiscriminator)
        ui.startActivity(intent)
    }

}
