package com.plugable.mcommerceapp.cpmvp1.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.utils.constants.IntentFlags
import kotlinx.android.synthetic.main.fragment_about_us.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*
import org.json.JSONObject

class AboutUsFragment:Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about_us, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initializeViews()
    }
//    private lateinit var mixPanel: MixpanelAPI


    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeViews() {
        webViewAboutUS.settings.javaScriptEnabled=true
        webViewAboutUS.loadUrl(getString(R.string.about_us_web_url))

//        mixPanel = MixpanelAPI.getInstance(context, resources.getString(R.string.mix_panel_token))
//        sendMixPanelEvent()

    }

/*

    private fun sendMixPanelEvent() {
        val productObject = JSONObject()
        productObject.put(IntentFlags.MIXPANEL_PRODUCT_ID,getString(R.string.mixpanel_about_us))
        mixPanel.track(IntentFlags.MIXPANEL_VISITED_ABOUT_US_SCREEN, productObject)
    }
*/

    fun setToolBar() {
        txtToolbarTitle.text = ""
        imgToolbarHome.setImageResource(android.R.color.transparent)

    }

   /* override fun onDestroy() {
        mixPanel.flush()
        super.onDestroy()
    }
*/
}