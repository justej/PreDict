package com.github.justej.predict.activities

import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.TextView
import com.github.justej.predict.R
import com.github.justej.predict.model.data.WORD_CARD
import com.github.justej.predict.model.data.WordCard
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
    private val presenter: NewCardPresenter = NewCardPresenter(this)
    private var wordCard = WordCard.EMPTY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_card)

        presenter.showWordCard(wordCard)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        wordCard = WordCard(wordCard.id,
                findViewById<TextView>(R.id.catchWordEdit).text.split("\n"),
                findViewById<TextView>(R.id.transcriptionEdit).text.toString(),
                findViewById<TextView>(R.id.translationEdit).text.split("\n"),
                findViewById<TextView>(R.id.notesEdit).text.toString(),
                findViewById<TextView>(R.id.tagsEdit).text.split("\n"),
                findViewById<TextView>(R.id.examplesEdit).text.toString(),
                findViewById<TextView>(R.id.audioEdit).text.split("\n").stream()
                        .filter { it != null && it.isEmpty() }
                        .map { it.toByteArray() }
                        .collect(Collectors.toList()),
                findViewById<TextView>(R.id.picturesEdit).text.split("\n").stream()
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
        presenter.expand(R.id.transcriptionLabel, R.id.transcriptionEdit)?.requestFocus()
    }

    fun expandNotes(view: View) {
        presenter.expand(R.id.notesLabel, R.id.notesEdit)?.requestFocus()
    }

    fun expandTags(view: View) {
        presenter.expand(R.id.tagsLabel, R.id.tagsEdit)?.requestFocus()
    }

    fun expandExamples(view: View) {
        presenter.expand(R.id.examplesLabel, R.id.examplesEdit)?.requestFocus()
    }

    fun expandAudio(view: View) {
        presenter.expand(R.id.audioLabel, R.id.audioEdit)?.requestFocus()
    }

    fun expandPictures(view: View) {
        presenter.expand(R.id.picturesLabel, R.id.picturesEdit)?.requestFocus()
    }

    //TODO: add "save" and "discard" buttons
}
