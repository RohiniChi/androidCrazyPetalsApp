package com.plugable.mcommerceapp.cpmvp1.utils.validation

import android.graphics.Paint
import android.widget.TextView

fun TextView.strikeThroughText() {
    this.paintFlags = this.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
}
