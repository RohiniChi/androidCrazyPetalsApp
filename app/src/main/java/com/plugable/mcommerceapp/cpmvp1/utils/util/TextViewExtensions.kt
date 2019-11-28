package com.plugable.mcommerceapp.cpmvp1.utils.util

import android.os.Build
import android.text.Html
import android.widget.TextView

fun TextView.loadHtmlText(htmlText: String?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        text = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_COMPACT)
    } else {
        @Suppress("DEPRECATION")
        text = Html.fromHtml(htmlText)
    }
}

fun String.capitalizeAll(): String {
    return split(" ").joinToString(" ") { it.capitalize() }
}