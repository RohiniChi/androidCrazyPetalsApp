package com.plugable.mcommerceapp.cpmvp1.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.cpmvp1.utils.extension.setStatusBarColor
import com.plugable.mcommerceapp.cpmvp1.utils.extension.setToolBarColor
import kotlinx.android.synthetic.main.activity_privacy_policy.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*


/**
 * [PrivacyPolicyActivity] is used to show privacy policies
 *
 */
class PrivacyPolicyActivity : AppCompatActivity() {

    private val privacyPolicyUrl:String="https://www.payumoney.com/privacypolicy.html?utm_source=google&utm_medium=dynamic_ads&utm_campaign=adgroup1&utm_content=ad1&gclid=CjwKCAiA2fjjBRAjEiwAuewS_aOEZdjJf1uZ23awPdmMctycI-6k45T1f8823VUvfd5yPOcmpQkQ9hoCGsMQAvD_BwE"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)


        initializeTheme()
        initializeViews()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeViews() {
        webViewPrivacyPolicy.settings.javaScriptEnabled=true
        webViewPrivacyPolicy.loadUrl(privacyPolicyUrl)
        imgToolbarHomeLayout.setOnClickListener {
            onBackPressed()
        }
    }

    private fun initializeTheme() {
        val configDetail = SharedPreferences.getInstance(this).themeDataPreference
        ApplicationThemeUtils.APP_NAME = configDetail!!.appName
        ApplicationThemeUtils.PRIMARY_COLOR = configDetail.primaryColor
        ApplicationThemeUtils.SECONDARY_COLOR = configDetail.secondryColor
        ApplicationThemeUtils.STATUS_BAR_COLOR = configDetail.statusBarColor
        ApplicationThemeUtils.TEXT_COLOR = configDetail.textColor
        ApplicationThemeUtils.CURRENCY_SYMBOL = configDetail.currencySymbol
        ApplicationThemeUtils.TOOL_BAR_COLOR = configDetail.tertiaryColor
        setToolBar()
    }

    fun setToolBar() {
        setSupportActionBar(toolBar)
        setStatusBarColor()
        supportActionBar?.setDisplayShowTitleEnabled(false)
        txtToolbarTitle.text = getString(R.string.title_privacy_policy)
        imgToolbarHome.setImageResource(R.drawable.ic_arrow_back_white_24dp)
        setToolBarColor(imgToolbarHome, txtToolbarTitle, toolbar = toolBar)

    }
}
