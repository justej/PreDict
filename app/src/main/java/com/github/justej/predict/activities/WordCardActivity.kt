package com.github.justej.predict.activities

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.justej.predict.R
import com.github.justej.predict.model.data.*
import com.github.justej.predict.utils.StringUtils
import kotlinx.android.synthetic.main.activity_word_card.*
import kotlinx.android.synthetic.main.app_bar.*

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
class WordCardActivity : AppCompatActivity() {

    private val presenter = WordCardPresenter(this)
    private lateinit var originalWordCard: WordCard

    //region Lifecycle methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_card)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_check)

        val extras = intent?.extras
        val wordSpellings = extras?.getString(PARAM_WORD, "") ?: ""
        val homonymDiscriminator = extras?.getString(PARAM_HOMONYM_DISCRIMINATOR, "") ?: ""
        originalWordCard = if (wordSpellings == "") {
            WordCard.EMPTY
        } else {
            presenter.getWordCard(wordSpellings.split("\n")[0], homonymDiscriminator)
        }

        showWordCard(originalWordCard)

        title = if (originalWordCard == WordCard.EMPTY) {
            StringUtils.updateEditable(catchWordEdit.text, wordSpellings)
            getString(R.string.title_activity_add_word)
        } else {
            getString(R.string.title_activity_edit_word)
        }
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
        menuInflater.inflate(R.menu.word_card_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                saveWordCard()
                return true
            }

            R.id.clone -> {
                cloneWordCard()
                return true
            }

            R.id.discard -> {
                finish()
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveWordCard() {
        val wordCard = populateWordCard()
        var alertMessage: String? = null

        if (wordCard.catchWordSpellings.isBlank()) {
            alertMessage = "Word can't be empty"
        }

        val existingWords = wordCard.catchWordSpellings
                .split("\n")
                .filter { !originalWordCard.catchWordSpellings.contains(it) }
                .map { Pair(it, presenter.getWordCard(it, wordCard.homonymDiscriminator)) }
                .filter { it.second != WordCard.EMPTY }
                .map { it.first }

        if (existingWords.isNotEmpty()) {
            lateinit var firstPart: String
            lateinit var secondPart: String
            lateinit var thirdPart: String
            if (existingWords.size == 1) {
                firstPart = "Card for word "
                secondPart = " already exists.\n"
                thirdPart = " card"
            } else {
                firstPart = "Cards for words "
                secondPart = " already exist.\n"
                thirdPart = " cards"
            }

            alertMessage = firstPart + existingWords.joinToString(", ", "\"", "\"") +
                    secondPart + "You can change homonym discriminator or delete existing word" +
                    thirdPart + " first."
        }

        if (alertMessage != null) {
            AlertDialog.Builder(this)
                    .setMessage(alertMessage)
                    .setNeutralButton("Ok") { _: DialogInterface, _: Int -> }
                    .show()
            return
        }

        presenter.saveWordCard(wordCard, originalWordCard)
        finish()
    }

    private fun cloneWordCard() {
        title = getString(R.string.title_activity_add_word)
        val tmpCard = populateWordCard()
        originalWordCard = WordCard(tmpCard.catchWordSpellings,
                tmpCard.homonymDiscriminator + "(1)",
                tmpCard.transcription,
                tmpCard.translation,
                tmpCard.notes,
                tmpCard.tags,
                tmpCard.examples,
                tmpCard.audio,
                tmpCard.pictures)
        showWordCard(originalWordCard)
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

    private fun showWordCard(wordCard: WordCard) {
        // Mandatory fields
        if (wordCard.catchWordSpellings.isNotEmpty()) {
            StringUtils.updateEditable(catchWordEdit.text, wordCard.catchWordSpellings)
        }

        if (wordCard.translation.isNotEmpty()) {
            StringUtils.updateEditable(translationEdit.text, wordCard.translation)
        }

        // Optional fields
        showNonEmptyField(wordCard.homonymDiscriminator, homonymIdLabel, homonymIdEdit)
        showNonEmptyField(wordCard.transcription, transcriptionLabel, transcriptionEdit)
        showNonEmptyField(wordCard.notes, notesLabel, notesEdit)
        showNonEmptyField(wordCard.examples, examplesLabel, examplesEdit)
        showNonEmptyField(StringUtils.joinLines(wordCard.tags), tagsLabel, tagsEdit)
        showNonEmptyField(StringUtils.joinResources(wordCard.audio), audioLabel, audioEdit)
        showNonEmptyField(StringUtils.joinResources(wordCard.pictures), picturesLabel, picturesEdit)

        catchWordEdit.requestFocus()
    }

    private fun showNonEmptyField(value: String, label: TextView, edit: EditText) {
        if (value.isNotEmpty()) {
            StringUtils.updateEditable(edit.text, value)
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
        return WordCard(StringUtils.normalize(catchWordEdit.text),
                StringUtils.normalize(homonymIdEdit.text),
                StringUtils.normalize(transcriptionEdit.text),
                StringUtils.normalize(translationEdit.text),
                notesEdit.text.toString(),
                tagsEdit.text
                        .split("\n")
                        .map { it.trim() },
                StringUtils.normalize(examplesEdit.text),
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
