package com.plugable.mcommerceapp.cpmvp1.ui.activities

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.core.app.ActivityCompat
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.cpmvp1.utils.constants.IntentFlags
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.cpmvp1.utils.extension.setStatusBarColor
import com.plugable.mcommerceapp.cpmvp1.utils.extension.setToolBarColor
import kotlinx.android.synthetic.main.activity_success_booking.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*
import org.jetbrains.anko.startActivity
import org.json.JSONObject

/**
 * [SuccessProductBookingActivity] is used to show user fields to be filled and sent back to server
 *
 */
class SuccessProductBookingActivity : BaseActivity() {

//    private lateinit var mixPanel: MixpanelAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success_booking)

        val intent=intent
       val day:String?=intent.getStringExtra(getString(R.string.delivery_location_to_pass_day))

       val time:String?= intent.getStringExtra(getString(R.string.delivery_location_to_pass_time))

       val orderId:String?=intent.getStringExtra(getString(R.string.order_number_to_pass))
        textViewOrderId.text=orderId

        textViewDateAndTime.text=day?.plus("(").plus(time).plus(")")
        initializeTheme()
        initializeViews()
    }

    override fun onBackPressed() {

    }
    private fun initializeTheme() {
        val configDetail = SharedPreferences.getInstance(this).themeDataPreference
        ApplicationThemeUtils.APP_NAME = configDetail!!.appName
        ApplicationThemeUtils.PRIMARY_COLOR = configDetail.primaryColor
        ApplicationThemeUtils.SECONDARY_COLOR = configDetail.secondryColor
        ApplicationThemeUtils.STATUS_BAR_COLOR = configDetail.statusBarColor
        ApplicationThemeUtils.TEXT_COLOR = configDetail.textColor
        ApplicationThemeUtils.CURRENCY_SYMBOL = configDetail.currencySymbol
        setToolBar(ApplicationThemeUtils.APP_NAME)
        setThemeToComponent()
    }

    private fun setThemeToComponent() {
        browseMoreButton.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
    }


    private fun initializeViews() {
//        mixPanel = MixpanelAPI.getInstance(this, resources.getString(R.string.mix_panel_token))

        imgToolbarHomeLayout.setOnClickListener(this)
        browseMoreButton.setOnClickListener(this)
//        sendMixPanelEvent()
    }


/*

    private fun sendMixPanelEvent() {
        val productObject = JSONObject()
        productObject.put(IntentFlags.MIXPANEL_PRODUCT_ID,getString(R.string.mixpanel_booking_successful) )
        mixPanel.track(IntentFlags.MIXPANEL_VISITED_BOOKING_SUCCESSFUL_SCREEN, productObject)
    }
*/

    override fun setToolBar(name: String) {
        setSupportActionBar(toolBar)
        setStatusBarColor()
        supportActionBar?.setDisplayShowTitleEnabled(false)
        txtToolbarTitle.text = getString(R.string.title_booking_successful)
        txtToolbarTitle.gravity = Gravity.CENTER_HORIZONTAL
        imgToolbarHome.setImageResource(android.R.color.transparent)
        setToolBarColor(imgToolbarHome, txtToolbarTitle, toolbar = toolBar)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgToolbarHomeLayout -> onBackPressed()
            R.id.browseMoreButton -> {
                startActivity<DashboardActivity>()
                ActivityCompat.finishAffinity(this)
            }
        }
    }
/*
    override fun onDestroy() {
        mixPanel.flush()
        super.onDestroy()
    }*/
}
