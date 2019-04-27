package com.github.justej.predict.model.data

import android.os.Parcel
import android.os.Parcelable

data class WordCard(val catchWordSpellings: List<String>,
                    val homonymId: String,
                    val transcription: String,
                    val translation: List<String>,
                    val notes: String,
                    val tags: List<String>,
                    val examples: String,
                    val audio: List<ByteArray>,
                    val pictures: List<ByteArray>) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.createStringArrayList() ?: listOf(),
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.createStringArrayList() ?: listOf(),
            parcel.readString() ?: "",
            parcel.createStringArrayList() ?: listOf(),
            parcel.readString() ?: "",
            readListOf<ByteArray>(parcel),
            readListOf<ByteArray>(parcel))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(homonymId)
        parcel.writeStringList(catchWordSpellings)
        parcel.writeString(transcription)
        parcel.writeStringList(translation)
        parcel.writeString(notes)
        parcel.writeStringList(tags)
        parcel.writeString(examples)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WordCard> {

        val EMPTY = WordCard(emptyList(),
                "",
                "",
                emptyList(),
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
