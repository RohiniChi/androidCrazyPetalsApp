package com.plugable.mcommerceapp.cpmvp1.ui.activities
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.cpmvp1.utils.extension.setStatusBarColor
import com.plugable.mcommerceapp.cpmvp1.utils.extension.setToolBarColor
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences
import kotlinx.android.synthetic.main.activity_instruction.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*
import org.jetbrains.anko.allCaps

class InstructionActivity : AppCompatActivity(), View.OnClickListener {
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgToolbarHomeLayout->{
                onBackPressed()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instruction)
        initializeViews()
        initializeTheme()
    }
    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeViews() {
        webViewInstructions.settings.javaScriptEnabled = true
        webViewInstructions.loadUrl(getString(R.string.instructions_web_url))
        imgToolbarHomeLayout.setOnClickListener(this)
//        mixPanel = MixpanelAPI.getInstance(context, resources.getString(R.string.mix_panel_token))
//        sendMixPanelEvent()
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
        setToolBar(ApplicationThemeUtils.APP_NAME)
    }
    /*private fun sendMixPanelEvent() {
        val productObject = JSONObject()
        productObject.put(IntentFlags.MIXPANEL_PRODUCT_ID,getString(R.string.mixpanel_about_us))
        mixPanel.track(IntentFlags.MIXPANEL_VISITED_ABOUT_US_SCREEN, productObject)
    }*/
    fun setToolBar(name: String) {
        setSupportActionBar(toolBar)
        setStatusBarColor()
        supportActionBar?.setDisplayShowTitleEnabled(false)
        /*supportActionBar?.setDisplayHomeAsUpEnabled(true)
        txtToolbarTitle.show()*/
        txtToolbarTitle.allCaps = false
        txtToolbarTitle.text ="Terms of use"
        imgToolbarHome.setImageResource(R.drawable.ic_shape_backarrow)
        setToolBarColor(imgToolbarHome, txtToolbarTitle, toolbar = toolBar)
    }
}
