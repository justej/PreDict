package com.github.justej.predict.utils

import android.text.Editable

class StringUtils {

    companion object {

        fun normalize(dirty: CharSequence): String {
            return dirty
                    .split("\n")
                    .filter { it.isNotBlank() }
                    .map { it.trim() }
                    .distinct()
                    .joinToString("\n")
        }

        fun joinLines(strings: Iterable<String>): String {
            return strings.joinToString(separator = "\n")
        }

        fun <T> joinResources(resource: Iterable<T>): String {
            return resource.joinToString { resource.toString() }
        }

        fun updateEditable(editable: Editable, newText: CharSequence) {
            editable.replace(0, editable.length, newText)
        }

    }

}

