package com.plugable.mcommerceapp.cpmvp1.utils.extension

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils


/**
 * Utils is used to set common extension functions
 *
 */
fun Activity.setStatusBarColor() {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val statusBarColor = Color.parseColor(ApplicationThemeUtils.STATUS_BAR_COLOR)
            if (statusBarColor == Color.BLACK && window.navigationBarColor == Color.BLACK) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            }
            window.statusBarColor = statusBarColor
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

}

fun Activity.disableWindowClicks() {
    window.setFlags(
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
    )
}

fun Activity.enableWindowClicks() {
    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
}

fun setToolBarColor(
    imageView: ImageView,
    toolbarTitle: TextView,
    toolbar: Toolbar
) {
    try {
        /*imageView.setColorFilter(Color.parseColor(ApplicationThemeUtils.TEXT_COLOR))
        imageView.invalidate()*/
        toolbar.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.TOOL_BAR_COLOR))
        toolbarTitle.setTextColor(Color.parseColor(ApplicationThemeUtils.TEXT_COLOR))

    } catch (e: Exception) {
        e.printStackTrace()
    }
}





