package com.plugable.mcommerceapp.cpmvp1.utils.extension

import android.view.View


fun View.show() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.hide() {
    visibility = View.GONE
}
