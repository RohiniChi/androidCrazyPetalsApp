package com.plugable.mcommerceapp.cpmvp1.ui.activities


import android.view.View
import androidx.appcompat.app.AppCompatActivity

/**
 * [BaseActivity] is an abstract class to load multiple common methods to be called in every activity.
 *
 */
abstract class BaseActivity : AppCompatActivity(), View.OnClickListener {
    open fun setToolBar(name: String, shouldShowTitle: Boolean=false){}
    open fun setToolBar(name: String){}
    open fun showNetworkCondition(){}

    open fun hideNetworkCondition(){}

    open fun showRecyclerViewData(){}

    open fun showServerErrorMessage(){}

    open fun hideServerErrorMessage(){}
    open fun showNoDataAvailableScreen(){}
    open fun hideNoDataAvailableScreen(){}
    open fun startShimmerView(){}
    open fun stopShimmerView(){}


}