package com.plugable.mcommerceapp.crazypetals.ui.fragments

import android.view.View
import androidx.fragment.app.Fragment

/**
 * [BaseFragment] is an abstract class to load multiple common methods to be called in every Fragment.
 *
 */
abstract class BaseFragment : Fragment(), View.OnClickListener {

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