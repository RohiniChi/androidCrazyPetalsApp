package com.plugable.mcommerceapp.crazypetals.network.view

import com.plugable.mcommerceapp.crazypetals.network.error.Error

interface Base {
    fun showProgress() {}
    fun hideProgress() {}
    fun failed(error: Error) {}
}