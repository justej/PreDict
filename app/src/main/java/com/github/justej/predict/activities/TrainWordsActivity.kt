package com.github.justej.predict.activities

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.justej.predict.R
import com.github.justej.predict.model.data.*
import kotlinx.android.synthetic.main.activity_train_words.*
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.train_common.*

private const val TAG = "TrainWordActivity"

class TrainWordsActivity : AppCompatActivity() {

    private val presenter = TrainWordsPresenter(this)
//    private var optionsIsExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_train_words)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

//        val editLayout = findViewById<LinearLayout>(R.id.editLayout)
//        val settingsLayout = findViewById<LinearLayout>(R.id.settingsLayout)
//        editLayout.visibility = View.GONE
//        settingsLayout.visibility = View.GONE

        val extras = intent?.extras

        if (extras == null) {
            Log.e(TAG, "Intent doesn't contain any extras. Can't proceed.")
            return
        }

        val wordsCount = extras.getInt(PARAM_TRAIN_WORD_COUNT)
        val wordsSubset = extras.get(PARAM_TRAIN_WORD_SUBSET) as TrainWordSubset
        val trainingType = TrainingType.valueOf(extras.get(PARAM_TRAIN_TYPE) as String)

        val wordCard = presenter.initTraining(wordsSubset, wordsCount, trainingType)

        title = title()
        questionTextLabel.text = wordCard?.translation ?: "No words found, sorry"
        answerEdit.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                answerEdit.setBackgroundColor(android.R.attr.editTextColor)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })
    }

    private fun title(): CharSequence {
        return "${getString(R.string.title_activity_train_words)} (${presenter.trainingProgress()}%)"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.train_words_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
//
//    fun onClickHandler(view: View) {
//        val editLayout = findViewById<LinearLayout>(R.id.editLayout)
//        val settingsLayout = findViewById<LinearLayout>(R.id.settingsLayout)
//        if (optionsIsExpanded) {
//            editLayout.visibility = View.GONE
//            settingsLayout.visibility = View.GONE
//        } else {
//            editLayout.visibility = View.VISIBLE
//            settingsLayout.visibility = View.VISIBLE
//        }
//        optionsIsExpanded = !optionsIsExpanded
//    }

    fun checkAnswer(view: View) {
        val answer = answerEdit.text.toString()

        if (!presenter.isCorrectAnswer(answer)) {
            answerEdit.setBackgroundColor(Color.RED)
            // TODO: set color back to default in X seconds
            return
        }

        title = title()

        if (presenter.trainingFinished()) {
            AlertDialog.Builder(this)
                    .setMessage("Training finished, congrats!")
                    .setPositiveButton("OK") { _: DialogInterface, _: Int -> finish() }
                    .show()
            return
        }

        val wordCard = presenter.trainWord()

        questionTextLabel.text = wordCard.translation
        answerEdit.text.clear()
    }

}
