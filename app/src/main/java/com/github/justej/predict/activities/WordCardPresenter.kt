package com.github.justej.predict.activities

import android.app.Activity
import com.github.justej.predict.model.data.WordCard
import com.github.justej.predict.model.db.Persister

class WordCardPresenter(ui: Activity) {

    private val persister = Persister(ui)

    fun getWordCard(word: String, homonymDiscriminator: String): WordCard? {
        return persister.getWordCardByWord(word, homonymDiscriminator)
    }

    fun saveWordCard(wordCard: WordCard) {
        // TODO: before storing:
        // 1. check if the word isn't empty
        // 2. check if the word (one of spelling variants) already exists
        // 3. suggest to change homonym discriminator in a dialog

        if (wordCard.catchWordSpellings == "") {
            return
        }

        persister.putWordCard(wordCard)
    }

}