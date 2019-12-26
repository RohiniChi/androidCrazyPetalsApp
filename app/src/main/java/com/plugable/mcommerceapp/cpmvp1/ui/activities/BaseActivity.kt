package com.plugable.mcommerceapp.cpmvp1.ui.activities


import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences

/**
 * [BaseActivity] is an abstract class to load multiple common methods to be called in every activity.
 *
 */
abstract class BaseActivity : AppCompatActivity(), View.OnClickListener {
    open fun setToolBar(name: String, shouldShowTitle: Boolean = true) {}
    open fun setToolBar(name: String) {}
    open fun showNetworkCondition() {}

    open fun hideNetworkCondition() {}

    open fun showRecyclerViewData() {}

    open fun showServerErrorMessage() {}

    open fun hideServerErrorMessage() {}
    open fun showNoDataAvailableScreen() {}
    open fun hideNoDataAvailableScreen() {}
    open fun startShimmerView() {}
    open fun stopShimmerView() {}


    fun incrementCartCount() {
        var count = SharedPreferences.getInstance(this)
            .getCartCountString()!!.toInt()

        SharedPreferences.getInstance(this).setCartCountString((count + 1).toString())

    }

    fun decrementCartCount() {
        var count = SharedPreferences.getInstance(this)
            .getCartCountString()!!.toInt()

        SharedPreferences.getInstance(this).setCartCountString((count - 1).toString())

    }


}
