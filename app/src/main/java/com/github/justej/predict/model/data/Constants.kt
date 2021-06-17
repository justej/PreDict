package com.github.justej.predict.model.data

const val SPLASH_SCREEN_DURATION = 2000L
const val TAG_SYMBOL = "#"

const val PARAM_WORD = "word"
const val PARAM_HOMONYM_DISCRIMINATOR = "homonym_discriminator"
const val PARAM_WORD_CARD = "word_card"
const val PARAM_TRAIN_WORD_COUNT = "train_word_count"
const val PARAM_TRAIN_WORD_SUBSET = "train_word_subset"
const val PARAM_TRAIN_TYPE = "train_type"

enum class TrainWordSubset {
    ALL,
    TRAINED,
    UNTRAINED
}
