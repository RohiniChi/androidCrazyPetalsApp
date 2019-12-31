package com.plugable.mcommerceapp.crazypetals.utils.validation

import android.graphics.Paint
import android.widget.TextView

fun TextView.strikeThroughText() {
    this.paintFlags = this.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
}
