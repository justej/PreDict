package com.github.justej.predict.utils

import android.text.Editable

fun updateEditable(editable: Editable, newText: CharSequence) {
    editable.replace(0, editable.length, newText)
}

fun joinLines(strings: Iterable<String>): String {
    return strings.joinToString(separator = "\n")
}

fun <T> joinResources(resource: Iterable<T>): String {
    return resource.joinToString { resource.toString() }
}
