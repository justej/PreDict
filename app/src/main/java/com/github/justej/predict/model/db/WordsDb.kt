package com.github.justej.predict.model.db

import android.util.Log
import androidx.room.*
import com.github.justej.predict.model.data.Audio
import com.github.justej.predict.model.data.Picture
import com.github.justej.predict.model.data.WordCard

private const val TAG = "WordsDb"
/**
 *  WordCard(
 *      wordId: Int (PK)
 *      homonymDiscriminator: String (PK)
 *      word: String
 *      transcription: String
 *      translation: String
 *      notes: String
 *      examples: String
 *      tags: List<String>
 *      audios: List<Resource>
 *      pictures: List<Resource>
 *  )
 *
 * word -> wordId ------->
 *                        |-- wordCard(wordId, homonymDiscriminator) -> translation
 * homonymDiscriminator ->
 *
 * tag -> tagId ------------->
 *                            |-- tagMap(tagId, cardId)
 * word -> wordId -> cardId ->
 */
@Database(entities = [
    ResourceDto::class,
    ResourceMap::class,
    TagsDto::class,
    TagsMap::class,
    WordDto::class,
    WordCardDto::class],
        version = 1)
abstract class WordsDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao

    companion object {
        const val NAME = "WORDS"
    }
}


@Dao
abstract class WordDao {

    fun getAllWordCards(): List<WordCard> {
        val wordIds = getAllWordIds()
        return if (wordIds.isEmpty()) {
            listOf()
        } else {
            getWordCardsByWordIds(wordIds)
        }
    }

    fun getWordCardsByWordLike(word: String): List<WordCard> {
        val wordIds = getWordIdByWordLike(word)
        return if (wordIds.isEmpty()) {
            listOf()
        } else {
            getWordCardsByWordIds(wordIds)
        }
    }

    fun getWordCardByWord(word: String, homonymDiscriminator: String): WordCard {
        val wordId = getWordIdByWord(word, homonymDiscriminator)
        if (wordId == 0) {
            return WordCard.EMPTY
        }

        val wordCards = getWordCardsByWordIds(listOf(wordId))
        // WordCards size must be 1 since for an existing word ID the must be a single card (it must
        // exist and must be only one)
        if (wordCards.size != 1) {
            Log.e(TAG, "Found zero or multiple word cards for wordId=$wordId")
            return WordCard.EMPTY
        }
        return wordCards[0]
    }

    @Transaction
    open fun getWordCardsByWordIds(wordIds: List<Int>): List<WordCard> {
        if (wordIds.isEmpty()) {
            return listOf()
        }

        return wordIds.map { wordId ->
            val wordDtos = getWordsById(wordId)
            val spellingVariants = wordDtos.joinToString("\n") { it.word }
            val homonymDiscriminator = wordDtos[0].homonymDiscriminator
            val card = getWordCardByWordId(wordId)
            if (card == null) {
                Log.w(TAG, "Couldn't find a word card for wordId=$wordId")
                return@map null
            }
            val tags = getTagsByCardId(card.id)
            val audios = getResourcesByCardId(card.id, ResourceType.AUDIO.value)
                    .map { Audio(it.id, it.resource) }
            val pictures = getResourcesByCardId(card.id, ResourceType.PICTURE.value)
                    .map { Picture(it.id, it.resource) }
            WordCard(spellingVariants, homonymDiscriminator, card.transcription, card.translation, card.notes, tags, card.examples, audios, pictures)
        }.filterNotNull()
    }

    @Query("""SELECT w.ID
        FROM WORDS w""")
    abstract fun getAllWordIds(): List<Int>

    @Query("""SELECT w.ID
        FROM WORDS w
        WHERE w.WORD = :word AND w.HOMONYM_DISCRIMINATOR = :homonymDiscriminator
        LIMIT 1""")
    abstract fun getWordIdByWord(word: String, homonymDiscriminator: String): Int

    @Query("""INSERT OR REPLACE INTO WORDS (ID, WORD, HOMONYM_DISCRIMINATOR) VALUES (
        (SELECT w.ID
        FROM WORDS w
        WHERE w.WORD = :word AND w.HOMONYM_DISCRIMINATOR = :homonymDiscriminator
        LIMIT 1), :word, :homonymDiscriminator)""")
    abstract fun insertIfNotExistsAndGetWordIdByWord(word: String, homonymDiscriminator: String): Long

    @Query("""SELECT w.*
        FROM WORDS w
        WHERE w.ID = :id
        ORDER BY w.WORD""")
    abstract fun getWordsById(id: Int): List<WordDto>

    @Query("""SELECT wc.*
        FROM WORD_CARDS wc
        WHERE wc.WORD_ID = :wordId
        LIMIT 1""")
    abstract fun getWordCardByWordId(wordId: Int): WordCardDto?

    @Query("""SELECT t.TAG
        FROM TAGS t, TAGS_MAP tm
        WHERE t.ID = tm.CARD_ID AND tm.CARD_ID = :cardId
        ORDER BY t.TAG""")
    abstract fun getTagsByCardId(cardId: Int): List<String>

    @Query("""SELECT r.*
        FROM RESOURCES r, RESOURCES_MAP rm
        WHERE r.ID = rm.CARD_ID AND rm.CARD_ID = :cardId AND r.TYPE = :type""")
    abstract fun getResourcesByCardId(cardId: Int, type: Int): List<ResourceDto>

    @Query("""SELECT w.ID
        FROM WORDS w
        WHERE w.WORD LIKE '%' || :word || '%'""")
    abstract fun getWordIdByWordLike(word: String): List<Int>

    @Transaction
    open fun putWordCard(wordCard: WordCard) {
        val wordIds = wordCard.catchWordSpellings.split("\n")
                .map { dirtyWord ->
                    val cleanWord = dirtyWord.trim()
                    insertIfNotExistsAndGetWordIdByWord(cleanWord, wordCard.homonymDiscriminator)
                }

        if (wordIds.size != 1) {
            Log.e(TAG, "Found ${wordIds.size} word IDs instead of a single ID")
            return
        }

        val wordId = wordIds[0].toInt()
        insertOrUpdateTranslation(wordId, wordCard.transcription, wordCard.translation, wordCard.notes, wordCard.examples)
    }

    @Query("""INSERT OR REPLACE INTO WORD_CARDS(WORD_ID, TRANSCRIPTION, TRANSLATION, NOTES, EXAMPLES) VALUES (:wordId, :transcription, :translation, :notes, :examples )""")
    abstract fun insertOrUpdateTranslation(wordId: Int, transcription: String, translation: String, notes: String, examples: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun putWord(word: WordDto)

}


@Entity(tableName = "WORD_CARDS",
        indices = [Index(value = ["WORD_ID"], unique = true)],
        foreignKeys = [
            ForeignKey(entity = WordDto::class,
                    parentColumns = ["ID"],
                    childColumns = ["WORD_ID"],
                    onDelete = ForeignKey.CASCADE)])
data class WordCardDto(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "ID") val id: Int,
        @ColumnInfo(name = "WORD_ID") val wordId: Int,
        @ColumnInfo(name = "TRANSCRIPTION") val transcription: String,
        @ColumnInfo(name = "TRANSLATION") val translation: String,
        @ColumnInfo(name = "NOTES") val notes: String,
        @ColumnInfo(name = "EXAMPLES") val examples: String)


@Entity(tableName = "WORDS",
        indices = [Index(value = ["WORD", "HOMONYM_DISCRIMINATOR"], unique = true)])
data class WordDto(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "ID") val id: Int,
        @ColumnInfo(name = "WORD", index = true) val word: String,
        @ColumnInfo(name = "HOMONYM_DISCRIMINATOR", index = true) val homonymDiscriminator: String)


@Entity(tableName = "TAGS")
data class TagsDto(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "ID") val id: Int,
        @ColumnInfo(name = "TAG") val tag: String)


@Entity(tableName = "TAGS_MAP",
        primaryKeys = ["TAG_ID", "CARD_ID"],
        foreignKeys = [
            ForeignKey(entity = TagsDto::class,
                    parentColumns = ["ID"],
                    childColumns = ["TAG_ID"]),
            ForeignKey(entity = WordCardDto::class,
                    parentColumns = ["ID"],
                    childColumns = ["CARD_ID"])])
data class TagsMap(
        @ColumnInfo(name = "TAG_ID", index = true) val tagId: Int,
        @ColumnInfo(name = "CARD_ID", index = true) val wordId: Int)


@Entity(tableName = "RESOURCES")
data class ResourceDto(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "ID") val id: Int,
        @ColumnInfo(name = "TYPE") val type: Byte,
        @ColumnInfo(name = "RESOURCE") val resource: ByteArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResourceDto

        if (id != other.id) return false
        if (!resource.contentEquals(other.resource)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + resource.contentHashCode()
        return result
    }

}


@Entity(tableName = "RESOURCES_MAP",
        primaryKeys = ["RESOURCE_ID", "CARD_ID"],
        foreignKeys = [
            ForeignKey(entity = ResourceDto::class,
                    parentColumns = ["ID"],
                    childColumns = ["RESOURCE_ID"]),
            ForeignKey(entity = WordCardDto::class,
                    parentColumns = ["ID"],
                    childColumns = ["CARD_ID"])])
data class ResourceMap(
        @ColumnInfo(name = "RESOURCE_ID", index = true) val resourceId: Int,
        @ColumnInfo(name = "CARD_ID", index = true) val cardId: Int)
