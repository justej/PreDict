package com.github.justej.predict.activities

import android.app.Activity
import com.github.justej.predict.model.data.WordCard
import com.github.justej.predict.model.db.Persister

class WordCardPresenter(ui: Activity) {

    private val persister = Persister(ui)

    fun getWordCard(word: String, homonymDiscriminator: String): WordCard {
        return persister.getWordCardByWord(word, homonymDiscriminator)
    }

    fun saveWordCard(wordCard: WordCard, originalWordCard: WordCard) {
        if (wordCard.catchWordSpellings.isBlank()) {
            return
        }

        persister.insertOrUpdateWordCard(wordCard, originalWordCard)
    }

    fun deleteWord(wordCard: WordCard) {
        persister.deleteWord(wordCard)
    }

}