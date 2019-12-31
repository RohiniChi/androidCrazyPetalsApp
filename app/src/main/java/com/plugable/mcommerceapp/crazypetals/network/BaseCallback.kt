package com.plugable.mcommerceapp.crazypetals.network

import com.plugable.mcommerceapp.crazypetals.network.error.Error


interface BaseCallback {
    fun failed(error: Error) {}
}
