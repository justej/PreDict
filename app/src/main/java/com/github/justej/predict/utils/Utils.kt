package com.github.justej.predict.utils

import android.text.Editable

fun updateEditable(editable: Editable, newText: CharSequence) {
    editable.replace(0, editable.length, newText)
}

fun joinLines(strings: Iterable<String>): String {
    return strings.joinToString(separator="\n")
}

fun joinByteArrays(barrays: Iterable<ByteArray>): String {
    val sbuilder = StringBuilder()
    var audioNumber = 1

    for (barray in barrays) {
        sbuilder.append("binary item #").append(audioNumber).append("\n")
        audioNumber++
    }

    if (sbuilder.endsWith("\n")) {
        sbuilder.removeSuffix("\n")
    }

    return sbuilder.toString()
}
