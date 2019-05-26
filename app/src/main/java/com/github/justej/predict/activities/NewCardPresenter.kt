package com.github.justej.predict.activities

import android.app.Activity
import com.github.justej.predict.model.data.WordCard
import com.github.justej.predict.model.db.Persister

class NewCardPresenter(ui: Activity) {

    private val persister = Persister(ui)

    fun saveWordCard(wordCard: WordCard) {
        // TODO: before storing:
        // 1. check if word (one of spelling variants) already exists
        // 2. suggest to change homonym discriminator in a dialog
        persister.putWordCard(wordCard)
    }

}