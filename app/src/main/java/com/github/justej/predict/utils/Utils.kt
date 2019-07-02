package com.github.justej.predict.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
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

        fun escape(text: String): String {
            return text.replace("<", "&lt;").replace(">", "&gt;")
        }

    }

}

class Dialogs {

    companion object {

        fun newDialogYesNo(ctx: Context, msg: String, positiveButtonListener: (DialogInterface, Int) -> Unit, negativeButtonListener: (DialogInterface, Int) -> Unit): AlertDialog? {
            return AlertDialog.Builder(ctx)
                    .setMessage(msg)
                    .setPositiveButton("Yes", positiveButtonListener)
                    .setNegativeButton("No", negativeButtonListener)
                    .show()
        }

    }

}

