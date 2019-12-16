package com.plugable.mcommerceapp.cpmvp1.network.view

import com.plugable.mcommerceapp.cpmvp1.network.error.Error

interface Base {
    fun showProgress() {}
    fun hideProgress() {}
    fun failed(error: Error) {}
}