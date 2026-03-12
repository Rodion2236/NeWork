package ru.netology.nework.util

import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputEditText
import ru.netology.nework.R.id.text_watcher_tag

fun TextInputEditText.onTextChanged(action: (String) -> Unit) {
    val oldWatcher = getTag(text_watcher_tag) as? TextWatcher
    oldWatcher?.let { removeTextChangedListener(it) }

    val watcher = object : TextWatcherAdapter() {
        override fun afterTextChanged(s: Editable?) {
            action(s?.toString().orEmpty())
        }
    }

    setTag(text_watcher_tag, watcher)
    addTextChangedListener(watcher)
}