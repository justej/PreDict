package com.github.justej.predict.model.data

import android.os.Parcel
import android.os.Parcelable

data class WordCard(
        val catchWordSpellings: String,
        val homonymDiscriminator: String,
        val transcription: String,
        val translation: String,
        val notes: String,
        val tags: List<String>,
        val examples: String,
        val audio: List<Audio>,
        val pictures: List<Picture>,
        val status: TrainingStatus) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.createStringArrayList() ?: listOf(),
            parcel.readString() ?: "",
            readListOf<Audio>(parcel),
            readListOf<Picture>(parcel),
            TrainingStatus.createFromParcel(parcel))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(homonymDiscriminator)
        parcel.writeString(catchWordSpellings)
        parcel.writeString(transcription)
        parcel.writeString(translation)
        parcel.writeString(notes)
        parcel.writeStringList(tags)
        parcel.writeString(examples)
        status.writeToParcel(parcel, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WordCard> {

        val EMPTY = WordCard("",
                "",
                "",
                "",
                "",
                emptyList(),
                "",
                emptyList(),
                emptyList(),
                TrainingStatus(0, 0, 0))

        private inline fun <reified T> readListOf(parcel: Parcel): List<T> {
            val list = ArrayList<T>()
            parcel.readList(list, T::class.java.classLoader)
            return list
        }

        override fun createFromParcel(parcel: Parcel): WordCard {
            return WordCard(parcel)
        }

        override fun newArray(size: Int): Array<WordCard?> {
            return arrayOfNulls(size)
        }

    }

}


class Picture(private val id: Int,
              private val value: ByteArray) {

    fun id() = id
    fun value() = value
    override fun toString(): String {
        return "Picture($id=$value)"
    }

}


class Audio(private val id: Int,
            private val value: ByteArray) {

    fun id() = id
    fun value() = value
    override fun toString(): String {
        return "Audio($id=$value)"
    }

}


data class TrainingStatus(private val findTranslation: Byte,
                          private val findWord: Byte,
                          private val spellWord: Byte) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readByte(),
            parcel.readByte(),
            parcel.readByte())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(findTranslation)
        parcel.writeByte(findWord)
        parcel.writeByte(spellWord)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TrainingStatus> {

        override fun createFromParcel(parcel: Parcel): TrainingStatus {
            return TrainingStatus(parcel)
        }

        override fun newArray(size: Int): Array<TrainingStatus?> {
            return arrayOfNulls(size)
        }
    }

}


enum class TrainingType {
    FIND_TRANSLATION_GIVEN_WORD,
    FIND_WORD_GIVEN_TRANSLATION,
    SPELL_WORD_GIVEN_TRANSLATION,
    FIND_WORD_GIVEN_PRONOUNCE
}


data class TrainingResult(val trainingType: TrainingType, val timestamp: Long, val result: Boolean)
