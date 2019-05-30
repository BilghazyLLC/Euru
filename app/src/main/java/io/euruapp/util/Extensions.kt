package io.euruapp.util

import android.widget.Spinner
import android.widget.TextView
import io.euruapp.model.Category

fun MutableList<Category>.extractNames(): MutableList<String> {
    return mutableListOf<String>().apply {
        for (item in this@extractNames) {
            add(item.name)
        }
    }
}

fun TextView.validate(): Boolean = this.text.toString().isNotEmpty()

fun Spinner.validate() : Boolean = this.selectedItem.toString().isNotEmpty()