package com.plugable.mcommerceapp.crazypetals.utils.validation

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.widget.EditText
import com.plugable.mcommerceapp.crazypetals.utils.util.Regex

object EditTextValidations {
    const val MAX_NAME_LENGTH = 100
}

fun EditText.isValidEmail(): Boolean {
    val txt = text.trim()
    return !TextUtils.isEmpty(txt) && txt.matches(Patterns.EMAIL_ADDRESS.toRegex())
}

fun EditText.isEmpty(): Boolean {
    return TextUtils.isEmpty(text.trim())
}

fun EditText.clear() {
    setText("")
}

fun EditText.isValidMobileNumber(): Boolean {
    val txt = text.trim()
    return !TextUtils.isEmpty(txt) && txt.matches(Regex.PHONE_NUMBER.toRegex())
}

fun EditText.onTextChanged(textChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editableText: Editable?) {
            textChanged(editableText.toString())
        }

        override fun beforeTextChanged(
            editableText: CharSequence?,
            start: Int,
            count: Int,
            after: Int
        ) {
        }

        override fun onTextChanged(
            editableText: CharSequence?,
            start: Int,
            before: Int,
            count: Int
        ) {
        }

    })
}

fun EditText.placeCursorToEnd() {
    this.setSelection(this.text.length)
}
