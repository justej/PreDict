package com.github.justej.predict.activities

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.github.justej.predict.R
import com.github.justej.predict.model.data.PARAM_WORD
import com.github.justej.predict.model.data.PARAM_WORD_CARD
import com.github.justej.predict.model.data.WordCard
import com.github.justej.predict.utils.joinByteArrays
import com.github.justej.predict.utils.joinLines
import com.github.justej.predict.utils.updateEditable
import kotlinx.android.synthetic.main.activity_new_card.*
import java.util.stream.Collectors

/**
 * In this ui, a user fills out:
 * - catchword - mandatory
 * - homonymId
 * - transcription
 * - translation - mandatory
 * - notes
 * - tags
 * - examples of usage
 * - audio
 * - picture
 * - links to related words
 */
class NewCardActivity : AppCompatActivity() {

    private val presenter = NewCardPresenter(this)

    //region Lifecycle methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_card)

        showWordCard(WordCard.EMPTY)
        val word = intent?.extras?.getString(PARAM_WORD, "")
        word?.let { updateEditable(catchWordEdit.text, it) }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        val wordCard = WordCard(catchWordEdit.text.split("\n"),
                homonymIdEdit.text.toString(),
                transcriptionEdit.text.toString(),
                translationEdit.text.split("\n"),
                notesEdit.text.toString(),
                tagsEdit.text.split("\n"),
                examplesEdit.text.toString(),
                toListOfByteArray(audioEdit.text),
                toListOfByteArray(picturesEdit.text))

        outState?.putParcelable(PARAM_WORD_CARD, wordCard)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        val wordCard = (savedInstanceState?.get(PARAM_WORD_CARD) ?: WordCard.EMPTY) as WordCard
        showWordCard(wordCard)
    }

    //endregion

    //region Event handlers

    fun expandHomonymId(view: View) {
        expandLabelToEditText(homonymIdLabel, homonymIdEdit).requestFocus()
    }

    fun expandTranscription(view: View) {
        expandLabelToEditText(transcriptionLabel, transcriptionEdit).requestFocus()
    }

    fun expandNotes(view: View) {
        expandLabelToEditText(notesLabel, notesEdit).requestFocus()
    }

    fun expandTags(view: View) {
        expandLabelToEditText(tagsLabel, tagsEdit).requestFocus()
    }

    fun expandExamples(view: View) {
        expandLabelToEditText(examplesLabel, examplesEdit).requestFocus()
    }

    fun expandAudio(view: View) {
        expandLabelToEditText(audioLabel, audioEdit).requestFocus()
    }

    fun expandPictures(view: View) {
        expandLabelToEditText(picturesLabel, picturesEdit).requestFocus()
    }

    //endregion

    //TODO: add "save" and "discard" buttons

    private fun showWordCard(wordCard: WordCard) {
        // Mandatory fields
        if (wordCard.catchWordSpellings.isNotEmpty()) {
            updateEditable(catchWordEdit.text, joinLines(wordCard.catchWordSpellings))
        }

        if (wordCard.translation.isNotEmpty()) {
            updateEditable(translationEdit.text, joinLines(wordCard.translation))
        }

        // Optional fields
        showNonEmptyField(wordCard.homonymId, homonymIdLabel, homonymIdEdit)
        showNonEmptyField(wordCard.transcription, transcriptionLabel, transcriptionEdit)
        showNonEmptyField(wordCard.notes, notesLabel, notesEdit)
        showNonEmptyField(wordCard.examples, examplesLabel, examplesEdit)
        showNonEmptyField(joinLines(wordCard.tags), tagsLabel, tagsEdit)
        showNonEmptyField(joinByteArrays(wordCard.audio), audioLabel, audioEdit)
        showNonEmptyField(joinByteArrays(wordCard.pictures), picturesLabel, picturesEdit)

        catchWordEdit.requestFocus()
    }

    private fun showNonEmptyField(value: String, label: TextView, edit: EditText) {
        if (value.isNotEmpty()) {
            updateEditable(edit.text, value)
            expandLabelToEditText(label, edit)
        } else {
            collapseEditTextToLabel(label, edit)
        }
    }

    private fun expandLabelToEditText(label: TextView, edit: TextView): View {
        label.visibility = View.GONE
        edit.visibility = View.VISIBLE
        return edit
    }

    private fun collapseEditTextToLabel(label: TextView, edit: TextView) {
        label.visibility = View.VISIBLE
        edit.visibility = View.GONE
    }

    private fun toListOfByteArray(data: Editable): List<ByteArray> {
        val listOfData = data.split("\n")

        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> listOfData.stream()
                    .filter { it.isEmpty() }
                    .map { it.toByteArray() }
                    .collect(Collectors.toList())
            else -> {
                val output = mutableListOf<ByteArray>()
                for (datum in listOfData) {
                    if (datum.isEmpty()) {
                        continue
                    }

                    output.add(datum.toByteArray())
                }

                output
            }
        }
    }

}
