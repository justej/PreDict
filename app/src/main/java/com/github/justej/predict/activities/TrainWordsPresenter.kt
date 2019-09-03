package com.github.justej.predict.activities

import android.app.Activity
import android.util.Log
import com.github.justej.predict.model.data.TrainWordSubset
import com.github.justej.predict.model.data.WordCard
import com.github.justej.predict.model.db.Persister
import java.util.*
import kotlin.math.min

class TrainWordsPresenter(private val ui: Activity) {

    private val persister = Persister(ui)
    private lateinit var wordTrainer: WordTrainer

    fun initTraining(wordsSubset: TrainWordSubset, wordsCount: Int): WordCard? {
        // TODO: use wordsSubset and wordsCount
        if (wordsCount == 0) {
            // Get all words
        }

        val wordsToTrain = LinkedList(persister.getWordCardByWordLike(""))
        wordTrainer = WordTrainer(wordsToTrain)
        return wordTrainer.trainWord()
    }

    fun isCorrectAnswer(answer: String): Boolean {
        val isCorrect = wordTrainer.checkWord(answer)

        if (isCorrect) {
            wordTrainer.updateTrainWord()
        }

        return isCorrect
    }

    fun trainWord() = wordTrainer.trainWord()

    fun trainingProgress() = wordTrainer.progress()

    fun trainingFinished() = !wordTrainer.hasUntrainedWords()

}

class WordTrainer(private val trainWordCards: LinkedList<WordCard>) {

    private val TAG = "---WordTrainer"
    private val initialWordCount = trainWordCards.size
    private var currentWordCard = nextWord(trainWordCards)

    fun checkWord(answer: String): Boolean {
        val trainWordCard = currentWordCard
        if (trainWordCard == WordCard.EMPTY) {
            Log.e(TAG, "Word card doesn't exist!")
            return false
        }

        return trainWordCard.catchWordSpellings.split("\n").any { answer.toLowerCase() == it.toLowerCase() }
    }

    fun updateTrainWord() {
        if (currentWordCard == WordCard.EMPTY) {
            return
        }

        trainWordCards.remove(currentWordCard)
        currentWordCard = nextWord(trainWordCards)
    }

    fun trainWord() = currentWordCard

    fun hasUntrainedWords() = currentWordCard != WordCard.EMPTY

    fun hint(answer: String): String {
        val spellingVariants = currentWordCard.catchWordSpellings.split("\n")

        if (answer.isEmpty()) {
            return spellingVariants[0][0].toString() // first letter
        }

        return spellingVariants
                .map { spellingVariant ->
                    val len = min(answer.length, spellingVariant.length)
                    var notMatchingSymbolIdx = len

                    for (i in 0 until len) {
                        if (answer[i] != spellingVariant[i]) {
                            notMatchingSymbolIdx = i
                            break
                        }
                    }

                    if (notMatchingSymbolIdx == spellingVariant.length ) {
                        return spellingVariant
                    }

                    // notMatchingSymbolIdx is always less than spellingVariant.length
                    val hint = spellingVariant.take(notMatchingSymbolIdx + 1)

                    if (spellingVariants.contains(hint)) {
                        return hint // the best hint is a spelling variant
                    }

                    return@map hint
                }
                .sortedByDescending { it.length } // the longest hint
                .first()
    }

    fun progress() = 100 * (initialWordCount - trainWordCards.size) / initialWordCount

    private fun nextWord(wordCards: LinkedList<WordCard>): WordCard {
        return if (wordCards.isEmpty()) {
            WordCard.EMPTY
        } else {
            wordCards[0]
        }
    }

}
