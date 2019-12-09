package com.plugable.mcommerceapp.cpmvp1.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.utils.constants.IntentFlags
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences
import kotlinx.android.synthetic.main.activity_contact_us.*
import org.json.JSONObject

class ContactUsFragment:Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_contact_us, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initializeViews()
    }

//    private lateinit var mixPanel: MixpanelAPI

    private fun initializeViews() {
    /*    mixPanel = MixpanelAPI.getInstance(context, resources.getString(R.string.mix_panel_token))
        sendMixPanelEvent()
*/
        val versionNumber= SharedPreferences.getInstance(activity!!).getStringValue(IntentFlags.VERSION_NUMBER)
        textViewVersionNumber.text=getString(R.string.text_version).plus(" ").plus(versionNumber)

    }

    /*private fun sendMixPanelEvent() {
        val productObject = JSONObject()
        productObject.put(IntentFlags.MIXPANEL_PRODUCT_ID,getString(R.string.mixpanel_contact_us))
        mixPanel.track(IntentFlags.MIXPANEL_VISITED_CONTACT_US_SCREEN, productObject)
    }

    override fun onDestroy() {
        mixPanel.flush()
        super.onDestroy()
    }*/
}