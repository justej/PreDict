package com.github.justej.predict.activities

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.github.justej.predict.R
import com.github.justej.predict.model.data.*
import com.github.justej.predict.utils.joinLines
import com.github.justej.predict.utils.joinResources
import com.github.justej.predict.utils.updateEditable
import kotlinx.android.synthetic.main.activity_new_card.*
import kotlinx.android.synthetic.main.app_bar.*
import java.util.*
import java.util.stream.Collectors
import java.util.stream.IntStream

/**
 * In this ui, a user fills out:
 * - catchword - mandatory
 * - homonymDiscriminator
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
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_check)

        showWordCard(WordCard.EMPTY)
        val word = intent?.extras?.getString(PARAM_WORD, "")
        word?.let { updateEditable(catchWordEdit.text, it) }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        val wordCard = populateWordCard()

        outState?.putParcelable(PARAM_WORD_CARD, wordCard)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        val wordCard = (savedInstanceState?.get(PARAM_WORD_CARD) ?: WordCard.EMPTY) as WordCard
        showWordCard(wordCard)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.new_card_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                presenter.saveWordCard(populateWordCard())
                finish()
                true
            }

            R.id.discard -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
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
            updateEditable(catchWordEdit.text, wordCard.catchWordSpellings)
        }

        if (wordCard.translation.isNotEmpty()) {
            updateEditable(translationEdit.text, wordCard.translation)
        }

        // Optional fields
        showNonEmptyField(wordCard.homonymDiscriminator, homonymIdLabel, homonymIdEdit)
        showNonEmptyField(wordCard.transcription, transcriptionLabel, transcriptionEdit)
        showNonEmptyField(wordCard.notes, notesLabel, notesEdit)
        showNonEmptyField(wordCard.examples, examplesLabel, examplesEdit)
        showNonEmptyField(joinLines(wordCard.tags), tagsLabel, tagsEdit)
        showNonEmptyField(joinResources(wordCard.audio), audioLabel, audioEdit)
        showNonEmptyField(joinResources(wordCard.pictures), picturesLabel, picturesEdit)

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

    private fun populateWordCard(): WordCard {
        return WordCard(catchWordEdit.text.toString(),
                homonymIdEdit.text.toString(),
                transcriptionEdit.text.toString(),
                translationEdit.text.toString(),
                notesEdit.text.toString(),
                tagsEdit.text.split("\n"),
                examplesEdit.text.toString(),
                toListOfAudios(audioEdit.text),
                toListOfPictures(picturesEdit.text))
    }

    private fun collapseEditTextToLabel(label: TextView, edit: TextView) {
        label.visibility = View.VISIBLE
        edit.visibility = View.GONE
    }

    private fun toListOfAudios(data: Editable): List<Audio> {
        val listOfData = data.split("\n")
        val audios = mutableListOf<Audio>()
        for ((index, value) in listOfData.withIndex()) {
            audios.add(Audio(index, value.toByteArray()))
        }

        return audios
    }

    private fun toListOfPictures(data: Editable): List<Picture> {
        val listOfData = data.split("\n")

        val pictures = mutableListOf<Picture>()
        for ((index, value) in listOfData.withIndex()) {
            pictures.add(Picture(index, value.toByteArray()))
        }

        return pictures
    }

}
