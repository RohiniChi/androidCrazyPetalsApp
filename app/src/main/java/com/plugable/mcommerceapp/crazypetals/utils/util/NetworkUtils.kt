package com.plugable.mcommerceapp.crazypetals.utils.util

import android.content.Context
import android.net.ConnectivityManager

/**
 * [isNetworkAccessible] is used to check internet connectivity
 *
 * @return
 */
fun Context.isNetworkAccessible(): Boolean {
    val connectivityManager: ConnectivityManager =
        this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return if (connectivityManager.activeNetworkInfo != null) {
        (connectivityManager.activeNetworkInfo?.isConnected as Boolean)
    } else {
        false
    }


}

