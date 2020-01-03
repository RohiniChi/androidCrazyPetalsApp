package com.plugable.mcommerceapp.crazypetals.ui.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.plugable.mcommerceapp.crazypetals.R
import kotlinx.android.synthetic.main.fragment_privacy_policy.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [PrivacyPolicyFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [PrivacyPolicyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PrivacyPolicyFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_privacy_policy, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializeViews()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeViews() {
        webViewPrivacyPolicy.settings.javaScriptEnabled = true
        webViewPrivacyPolicy.loadUrl(getString(R.string.privacy_policy__web_url))

        progressBarPrivacyPolicy.indeterminateDrawable.setColorFilter(
            Color.BLACK,
            PorterDuff.Mode.MULTIPLY
        )
//        mixPanel = MixpanelAPI.getInstance(context, resources.getString(R.string.mix_panel_token))
//        sendMixPanelEvent()

        if (activity!!.isFinishing) {
            return
        }
        webViewPrivacyPolicy.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBarPrivacyPolicy.show()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBarPrivacyPolicy.hide()
            }
        }
    }

    fun setToolBar() {
        txtToolbarTitle.text = ""
        imgToolbarHome.setImageResource(android.R.color.transparent)

    }

    override fun onDestroy() {
        if (webViewPrivacyPolicy != null) {
            webViewPrivacyPolicy.removeAllViews()
            webViewPrivacyPolicy.destroy()
        }
        super.onDestroy()
    }
}
