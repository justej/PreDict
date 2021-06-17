package com.github.justej.predict.activities

import com.github.justej.predict.model.data.TrainingStatus
import com.github.justej.predict.model.data.WordCard
import org.junit.Assert
import org.junit.Test
import java.util.*

private val TRAINING_STATUS = TrainingStatus(0, 0, 0)
private val WORD_CARD_AAA_BBB_CCC = WordCard("aaa\nbbb\nccc", "", "", "", "", listOf(), "", listOf(), listOf(), TRAINING_STATUS)
private val WORD_CARD_COLOR = WordCard("color\ncolour", "", "'kʌlə", "цвет", "", listOf(), "", listOf(), listOf(), TRAINING_STATUS)
private val WORD_CARD_ANALYZE = WordCard("analyze\nanalyse", "", "'æn(ə)laɪz", "анализировать", "", listOf(), "", listOf(), listOf(), TRAINING_STATUS)
private val WORD_CARD_ONE = WordCard("one", "", "wʌn", "один", "", listOf(), "", listOf(), listOf(), TRAINING_STATUS)
private val WORD_CARD_TWO = WordCard("two", "", "tuː", "два", "", listOf(), "", listOf(), listOf(), TRAINING_STATUS)
private val WORD_CARD_THREE = WordCard("three", "", "θriː", "три", "", listOf(), "", listOf(), listOf(), TRAINING_STATUS)

class WordTrainerTest {

    /*
    Given a train word,
    When I check a word and it is correct
    Then checkWord() returns true
     */
    @Test
    fun checkCorrectSingleSpellingWord() {
        val trainer = WordTrainer(LinkedList(listOf(WORD_CARD_ONE)))

        assert(WORD_CARD_ONE == trainer.trainWord())

        Assert.assertTrue(trainer.checkWord("one"))
    }

    /*
    Given a train word,
    When I check a word and it is correct
    Then checkWord() returns true for every spelling variant
     */
    @Test
    fun checkCorrectMultiSpellingWord() {
        val trainer = WordTrainer(LinkedList(listOf(WORD_CARD_COLOR)))

        assert(WORD_CARD_COLOR == trainer.trainWord())

        Assert.assertTrue(trainer.checkWord("color"))
        Assert.assertTrue(trainer.checkWord("colour"))
    }

    /*
    Given a train words,
    When I check a word and it is incorrect
    Then checkWord() returns false
     */
    @Test
    fun checkIncorrectWord() {
        val trainer = WordTrainer(LinkedList(listOf(WORD_CARD_ONE)))

        assert(WORD_CARD_ONE == trainer.trainWord())

        Assert.assertFalse(trainer.checkWord("incorrect"))
    }

    /*
    Given a list of three train words,
    When I update train word three times
    Then I get an empty word card for the third update only (when there's no untrained words anymore)
     */
    @Test
    fun updateTrainWord() {
        val trainer = WordTrainer(LinkedList(listOf(WORD_CARD_ONE, WORD_CARD_TWO, WORD_CARD_THREE)))
        Assert.assertNotEquals(WordCard.EMPTY, trainer.trainWord())

        trainer.updateTrainWord()
        Assert.assertNotEquals(WordCard.EMPTY, trainer.trainWord())

        trainer.updateTrainWord()
        Assert.assertNotEquals(WordCard.EMPTY, trainer.trainWord())

        // There's no untrained words anymore
        trainer.updateTrainWord()
        Assert.assertEquals(WordCard.EMPTY, trainer.trainWord())
    }

    /*
    Given there's no untrained words,
    When I update train word
    Then I still get an empty word card
     */
    @Test
    fun updateTrainWordWhenNoUntrainedWordsAnymore() {
        val trainer = WordTrainer(LinkedList(listOf()))
        Assert.assertEquals(WordCard.EMPTY, trainer.trainWord())

        trainer.updateTrainWord()
        Assert.assertEquals(WordCard.EMPTY, trainer.trainWord())
    }

    /*
    Given an empty answer,
    When I request for a hint
    Then I get the first letter of the first word
     */
    @Test
    fun hintEmptyAnswer() {
        val trainer = WordTrainer(LinkedList(listOf(WORD_CARD_AAA_BBB_CCC)))
        Assert.assertEquals(trainer.trainWord(), WORD_CARD_AAA_BBB_CCC)

        val hint = trainer.hint("")
        Assert.assertEquals("a", hint)
    }

    // colo     colo-sa  color-is  color    analy
    // colo-ur  colo-r   color     color    analy-ze
    // colo-r   colo-ur  colou-r   colou-r  analy-se
    // ----------------------------
    // color    color    color

    /*
    Given a correct incomplete answer,
    When I request for a hint and the hint doesn't match any spelling variant
    Then I get the next letter of the first best word fit
     */
    @Test
    fun hintIncompleteCorrectAnswer() {
        val trainerAnalyze = WordTrainer(LinkedList(listOf(WORD_CARD_ANALYZE)))
        Assert.assertEquals(trainerAnalyze.trainWord(), WORD_CARD_ANALYZE)

        Assert.assertEquals("analyz", trainerAnalyze.hint("analy"))
    }

    /*
    Given a correct incomplete answer,
    When I request for a hint and the hint matches one of the spelling variants
    Then I get the matching spelling variant and not the longest hint
     */
    @Test
    fun hintIncompleteCorrectAnswerMatchesSpellingVariant() {
        val trainer = WordTrainer(LinkedList(listOf(WORD_CARD_COLOR)))
        Assert.assertEquals(trainer.trainWord(), WORD_CARD_COLOR)

        Assert.assertEquals("color", trainer.hint("colo"))
    }

    /*
    Given a correct incomplete answer,
    When I request for a hint
    Then the hint will be shortened to the the best word fit
     */
    @Test
    fun hintIncorrectAnswerWhichIsShorterThanSpellingVariant() {
        val trainer = WordTrainer(LinkedList(listOf(WORD_CARD_COLOR)))
        Assert.assertEquals(trainer.trainWord(), WORD_CARD_COLOR)

        Assert.assertEquals("co", trainer.hint("calor"))
    }

    /*
    Given a correct incomplete answer,
    When I request for a hint
    Then the hint will be shortened to the the best word fit
     */
    @Test
    fun hintIncorrectAnswerWhichIsLongerThanSpellingVariant() {
        val trainer = WordTrainer(LinkedList(listOf(WORD_CARD_COLOR)))
        Assert.assertEquals(trainer.trainWord(), WORD_CARD_COLOR)

        Assert.assertEquals("color", trainer.hint("colorful"))
    }

}