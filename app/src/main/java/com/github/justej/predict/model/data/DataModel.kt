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
        val pictures: List<Picture>) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.createStringArrayList() ?: listOf(),
            parcel.readString() ?: "",
            readListOf<Audio>(parcel),
            readListOf<Picture>(parcel))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(homonymDiscriminator)
        parcel.writeString(catchWordSpellings)
        parcel.writeString(transcription)
        parcel.writeString(translation)
        parcel.writeString(notes)
        parcel.writeStringList(tags)
        parcel.writeString(examples)
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
                emptyList())

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
