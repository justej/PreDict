package com.github.justej.predict.activities

import android.app.Activity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.github.justej.predict.R
import com.github.justej.predict.model.data.WordCard
import com.github.justej.predict.utils.joinByteArrays
import com.github.justej.predict.utils.joinLines
import com.github.justej.predict.utils.updateEditable
import kotlinx.android.synthetic.main.activity_new_card.*

class NewCardPresenter(private val ui: Activity) {

    fun showWordCard(wordCard: WordCard) {
        // Mandatory fields
        if (wordCard.catchWordSpellings.isNotEmpty()) {
            val catchWord = ui.catchWordEdit
            updateEditable(catchWord.text, joinLines(wordCard.catchWordSpellings))
        }

        if (wordCard.translation.isNotEmpty()) {
            val translation = ui.translationEdit
            updateEditable(translation.text, joinLines(wordCard.translation))
        }

        // Optional fields
        showNonEmptyField(wordCard.transcription, R.id.transcriptionLabel, R.id.transcriptionEdit)
        showNonEmptyField(wordCard.notes, R.id.notesLabel, R.id.notesEdit)
        showNonEmptyField(wordCard.examples, R.id.examplesLabel, R.id.examplesEdit)
        showNonEmptyField(joinLines(wordCard.tags), R.id.tagsLabel, R.id.tagsEdit)
        showNonEmptyField(joinByteArrays(wordCard.audio), R.id.audioLabel, R.id.audioEdit)
        showNonEmptyField(joinByteArrays(wordCard.pictures), R.id.picturesLabel, R.id.picturesEdit)
    }

    private fun showNonEmptyField(value: String, labelId: Int, editId: Int) {
        val label = ui.findViewById<TextView>(labelId)
        val edit = ui.findViewById<EditText>(editId)
        if (value.isNotEmpty()) {
            updateEditable(edit.text, value)
            expand(label, edit)
        } else {
            collapse(label, edit)
        }
    }

    internal fun expand(label: TextView, edit: TextView): View {
        label.visibility = View.GONE
        edit.visibility = View.VISIBLE
        return edit
    }

    private fun collapse(label: TextView, edit: TextView) {
        label.visibility = View.VISIBLE
        edit.visibility = View.GONE
    }

}