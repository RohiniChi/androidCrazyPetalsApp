package com.plugable.mcommerceapp.crazypetals.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.plugable.mcommerceapp.crazypetals.R
import kotlinx.android.synthetic.main.fragment_faqs.*

class FAQsFragment:Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

//    private lateinit var mixPanel: MixpanelAPI

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_faqs, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initializeViews()
    }
    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeViews() {
        webViewFAQs.settings.javaScriptEnabled=true
        webViewFAQs.loadUrl(getString(R.string.faqs_web_url))

      /*  mixPanel = MixpanelAPI.getInstance(context, resources.getString(R.string.mix_panel_token))
        sendMixPanelEvent()
*/
    }


 /*   private fun sendMixPanelEvent() {
        val productObject = JSONObject()
        productObject.put(IntentFlags.MIXPANEL_PRODUCT_ID,getString(R.string.mixpanel_faqs))
        mixPanel.track(IntentFlags.MIXPANEL_VISITED_FAQS_SCREEN, productObject)
    }

    override fun onDestroy() {
        mixPanel.flush()
        super.onDestroy()
    }
*/
}