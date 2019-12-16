package com.plugable.mcommerceapp.cpmvp1.network

import com.plugable.mcommerceapp.cpmvp1.network.error.Error


interface BaseCallback {
    fun failed(error: Error) {}
}
