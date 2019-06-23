package com.github.justej.predict.model.db

import android.content.Context
import androidx.room.Room
import com.github.justej.predict.model.data.WordCard
import kotlin.concurrent.thread

enum class ResourceType(val value: Int) {
    AUDIO(0),
    PICTURE(1)
}

class Persister(private val context: Context) {

    private val db by lazy {
        Room.databaseBuilder(context, WordsDatabase::class.java, WordsDatabase.NAME).build()
    }

    fun getWordCardByWord(word: String, homonymDiscriminator: String): WordCard {
        var wordCard: WordCard = WordCard.EMPTY
        thread {
            wordCard = db.wordDao().getWordCardByWord(word, homonymDiscriminator)
        }.join()

        return wordCard
    }

    fun getWordCardByWordLike(word: String): List<WordCard> {
        var res: List<WordCard> = listOf()
        thread {
            res = if (word.isEmpty()) {
                db.wordDao().getAllWordCards()
            } else {
                db.wordDao().getWordCardsByWordLike(word)
            }
        }.join()
        return res
    }

    fun getWordCardByTag(tag: String): List<WordCard> {
        return listOf()
    }

    fun insertOrUpdateWordCard(wordCard: WordCard, originalWordCard: WordCard) {
        thread {
            db.wordDao().insertOrUpdateWordCard(wordCard, originalWordCard)
        }.join()
    }

}
