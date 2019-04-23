package com.github.justej.predict.activities

import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.github.justej.predict.R
import com.github.justej.predict.model.data.WORD_CARD
import com.github.justej.predict.model.data.WordCard
import kotlinx.android.synthetic.main.activity_new_card.*
import java.util.stream.Collectors

/**
 * In this ui, a user fills out:
 * - catchword - mandatory
 * - transcription
 * - translation
 * - notes
 * - tags
 * - examples of usage
 * - audio
 * - picture
 * - links to related words
 */
class NewCardActivity : AppCompatActivity() {

    private val presenter = NewCardPresenter(this)
    private var wordCard = WordCard.EMPTY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_card)

        presenter.showWordCard(wordCard)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        wordCard = WordCard(catchWordEdit.text.split("\n"),
                wordCard.homonymId,
                transcriptionEdit.text.toString(),
                translationEdit.text.split("\n"),
                notesEdit.text.toString(),
                tagsEdit.text.split("\n"),
                examplesEdit.text.toString(),
                audioEdit.text.split("\n").stream()
                        .filter { it != null && it.isEmpty() }
                        .map { it.toByteArray() }
                        .collect(Collectors.toList()),
                picturesEdit.text.split("\n").stream()
                        .filter { it != null && it.isEmpty() }
                        .map { it.toByteArray() }
                        .collect(Collectors.toList()))

        outState?.putParcelable(WORD_CARD, wordCard)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        wordCard = if (savedInstanceState == null) {
            WordCard.EMPTY
        } else {
            savedInstanceState.get(WORD_CARD) as WordCard
        }

        presenter.showWordCard(wordCard)
    }

    fun expandTranscription(view: View) {
        presenter.expand(transcriptionLabel, transcriptionEdit).requestFocus()
    }

    fun expandNotes(view: View) {
        presenter.expand(notesLabel, notesEdit).requestFocus()
    }

    fun expandTags(view: View) {
        presenter.expand(tagsLabel, tagsEdit).requestFocus()
    }

    fun expandExamples(view: View) {
        presenter.expand(examplesLabel, examplesEdit).requestFocus()
    }

    fun expandAudio(view: View) {
        presenter.expand(audioLabel, audioEdit).requestFocus()
    }

    fun expandPictures(view: View) {
        presenter.expand(picturesLabel, picturesEdit).requestFocus()
    }

    //TODO: add "save" and "discard" buttons
}
