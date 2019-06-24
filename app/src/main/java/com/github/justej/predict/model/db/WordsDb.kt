package com.github.justej.predict.model.db

import android.util.Log
import androidx.room.*
import com.github.justej.predict.model.data.Audio
import com.github.justej.predict.model.data.Picture
import com.github.justej.predict.model.data.WordCard
import java.util.*

private const val TAG = "WordsDb"

/**
 * How it works:
 *
 * word ----------------->|-> wordCardId -|-> transcription, translation, notes, examples
 * homonymDiscriminator ->|               |-> tagId -> tags
 *                                        |-> resourceId -> resources
 *
 *
 * For example:
 *
 * ("wrap", "noun") -> 13 -> ([ræp], "упаковка", "", "bubble wrap")
 *
 * ("color", "") --> 42 -|-> ([ˈkʌlər], "цвет", "", "the color of the sky")
 * ("colour", "") -> 42 -|
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
        val cardIds = getAllCardIds()
        return if (cardIds.isEmpty()) {
            listOf()
        } else {
            getWordCardsByIds(cardIds)
        }
    }

    fun getWordCardsByWordLike(word: String): List<WordCard> {
        val cardIds = getCardIdsByWordLike(word)
        return if (cardIds.isEmpty()) {
            listOf()
        } else {
            getWordCardsByIds(cardIds)
        }
    }

    fun getWordCardByWord(word: String, homonymDiscriminator: String): WordCard {
        val cardId = getCardIdByWord(word, homonymDiscriminator) ?: return WordCard.EMPTY

        val wordCards = getWordCardsByIds(listOf(cardId))
        // WordCards size must be 1 since for an existing card ID there must be a single card (it
        // must exist and must be exactly one)
        if (wordCards.size != 1) {
            Log.e(TAG, "Found zero or multiple word cards for wordId=$cardId")
            return WordCard.EMPTY
        }

        return wordCards[0]
    }

    companion object {

        val wordCardComparator = object : Comparator<WordCard> {
            override fun compare(one: WordCard?, two: WordCard?): Int {
                if (one == null) {
                    return if (two == null) 0 else -1
                }

                if (two == null) {
                    return 1
                }

                val res = one.catchWordSpellings.compareTo(two.catchWordSpellings)
                return if (res == 0) {
                    one.homonymDiscriminator.compareTo(two.homonymDiscriminator)
                } else {
                    res
                }
            }
        }

    }

    @Transaction
    open fun getWordCardsByIds(cardIds: List<Int>): List<WordCard> {
        if (cardIds.isEmpty()) {
            return listOf()
        }

        Log.d(TAG, "-- getWordCardsByIds: cardIds=$cardIds")

        return cardIds
                .map { cardId ->
                    val wordDtos = getWordByCardId(cardId)

                    val card = getWordCardById(cardId)

                    Log.d(TAG, "-- getWordCardsByIds: card=$card, wordDtos=$wordDtos")

                    if (wordDtos.isEmpty()) {
                        Log.w(TAG, "Couldn't find any word for cardId=$cardId")
                        if (card != null) {
                            Log.e(TAG, "Found an orphaned card (cardId=$cardId). Deleting it...")
                            deleteWordCardCascade(cardId)
                        }
                        return@map null
                    }

                    if (card == null) {
                        Log.w(TAG, "Couldn't find card for cardId=$cardId")
                        return@map null
                    }

                    // All spelling variants must have the same homonymDiscriminator
                    val homonymDiscriminator = wordDtos[0].homonymDiscriminator
                    if (wordDtos.size > 1) {
                        if (wordDtos.any { it.homonymDiscriminator != homonymDiscriminator }) {
                            Log.e(TAG, "Found multiple homonym discriminators for the same cardId=$cardId." +
                                    " Excluding the card from the search")
                            return@map null
                        }
                    }

                    val spellingVariants = wordDtos.joinToString("\n") { it.word }
                    val tags = getTagsByCardId(card.id)
                    val audios = getResourcesByCardId(card.id, ResourceType.AUDIO.value)
                            .map { Audio(it.id, it.resource) }
                    val pictures = getResourcesByCardId(card.id, ResourceType.PICTURE.value)
                            .map { Picture(it.id, it.resource) }
                    WordCard(spellingVariants, homonymDiscriminator, card.transcription, card.translation, card.notes, tags, card.examples, audios, pictures)
                }
                .filterNotNull()
                .sortedWith(wordCardComparator)
    }

    @Transaction
    open fun deleteWordCardCascade(cardId: Int) {
        Log.d(TAG, "-- Cascade deletion is not implemented yet (cardId=$cardId")
    }

    @Transaction
    open fun insertOrUpdateWordCard(wordCard: WordCard, originalWordCard: WordCard) {
        if (originalWordCard != WordCard.EMPTY) {
            val cardIds = originalWordCard.catchWordSpellings.split("\n")
                    .mapNotNull { getCardIdByWord(it, originalWordCard.homonymDiscriminator) }
            deleteWordsByCardId(cardIds)
        }

        val cardId = insertTranslation(wordCard.transcription, wordCard.translation, wordCard.notes, wordCard.examples)

        Log.d(TAG, "-- putWordCard: cardId=$cardId")

        val wordIds = wordCard.catchWordSpellings.split("\n")
                .map { insertIfNotExistsOrUpdateWord(it, wordCard.homonymDiscriminator, cardId) }

        Log.d(TAG, "-- putWordCard: wordIds=$wordIds")

        if (wordIds.isEmpty()) {
            Log.e(TAG, "Failed inserting words for cardId=$cardId")
        }

        val cardIdsBeforeCleanUp = getAllCardIds()
        Log.d(TAG, "-- putWordCard: before clean up: cardIds=$cardIdsBeforeCleanUp")

        cleanUpWordCards()

        val cardIdsAfterCleanUp = getAllCardIds()
        Log.d(TAG, "-- putWordCard: after clean up: cardIds=$cardIdsAfterCleanUp")
    }

    @Transaction
    open fun deleteWordCardByWord(word: String, homonymDiscriminator: String) {
        val cardId = getCardIdByWord(word, homonymDiscriminator) ?: return

        Log.i(TAG, "-- Word card with cardId=$cardId will be deleted")

        deleteWordsByCardId(listOf(cardId))
        deleteTranslationByCardId(listOf(cardId))
    }

    @Query("""SELECT wc.ID
        FROM WORD_CARDS wc""")
    abstract fun getAllCardIds(): List<Int>

    @Query("""SELECT w.CARD_ID
        FROM WORDS w
        WHERE w.WORD = :word AND w.HOMONYM_DISCRIMINATOR = :homonymDiscriminator
        LIMIT 1""")
    abstract fun getCardIdByWord(word: String, homonymDiscriminator: String): Int?

    @Query("""SELECT w.CARD_ID
        FROM WORDS w
        WHERE w.WORD LIKE '%' || :word || '%'""")
    abstract fun getCardIdsByWordLike(word: String): List<Int>

    @Query("""INSERT OR REPLACE
        INTO WORDS (WORD, HOMONYM_DISCRIMINATOR, CARD_ID)
        VALUES (:word, :homonymDiscriminator, :cardId)""")
    abstract fun insertIfNotExistsOrUpdateWord(word: String, homonymDiscriminator: String, cardId: Long): Long

    @Query("""SELECT w.*
        FROM WORDS w
        WHERE w.CARD_ID = :cardId
        ORDER BY w.WORD, w.HOMONYM_DISCRIMINATOR""")
    abstract fun getWordByCardId(cardId: Int): List<WordDto>

    @Query("""SELECT wc.*
        FROM WORD_CARDS wc
        WHERE wc.ID = :cardId
        LIMIT 1""")
    abstract fun getWordCardById(cardId: Int): WordCardDto?

    @Query("""SELECT t.TAG
        FROM TAGS t, TAGS_MAP tm
        WHERE t.ID = tm.CARD_ID AND tm.CARD_ID = :cardId
        ORDER BY t.TAG""")
    abstract fun getTagsByCardId(cardId: Int): List<String>

    @Query("""SELECT r.*
        FROM RESOURCES r, RESOURCES_MAP rm
        WHERE r.ID = rm.CARD_ID AND rm.CARD_ID = :cardId AND r.TYPE = :type""")
    abstract fun getResourcesByCardId(cardId: Int, type: Int): List<ResourceDto>

    @Query("""INSERT
        INTO WORD_CARDS (TRANSCRIPTION, TRANSLATION, NOTES, EXAMPLES)
        VALUES (:transcription, :translation, :notes, :examples )""")
    abstract fun insertTranslation(transcription: String, translation: String, notes: String, examples: String): Long

    @Query("""DELETE FROM WORD_CARDS
        WHERE NOT EXISTS(
            SELECT 1
            FROM WORDS w
            WHERE w.CARD_ID = ID)""")
    abstract fun cleanUpWordCards()

    @Query("""DELETE FROM WORDS
      WHERE CARD_ID IN (:cardIds)""")
    abstract fun deleteWordsByCardId(cardIds: List<Int>)

    @Query("""DELETE FROM WORD_CARDS
      WHERE ID IN (:cardIds)""")
    abstract fun deleteTranslationByCardId(cardIds: List<Int>)

}


@Entity(tableName = "WORD_CARDS")
data class WordCardDto(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "ID") val id: Int,
        @ColumnInfo(name = "TRANSCRIPTION") val transcription: String,
        @ColumnInfo(name = "TRANSLATION") val translation: String,
        @ColumnInfo(name = "NOTES") val notes: String,
        @ColumnInfo(name = "EXAMPLES") val examples: String)


@Entity(tableName = "WORDS",
        primaryKeys = ["WORD", "HOMONYM_DISCRIMINATOR"],
        foreignKeys = [
            ForeignKey(entity = WordCardDto::class,
                    parentColumns = ["ID"],
                    childColumns = ["CARD_ID"])])
data class WordDto(
        @ColumnInfo(name = "WORD", index = true) val word: String,
        @ColumnInfo(name = "HOMONYM_DISCRIMINATOR", index = true) val homonymDiscriminator: String,
        @ColumnInfo(name = "CARD_ID", index = true) val cardId: Int)


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
