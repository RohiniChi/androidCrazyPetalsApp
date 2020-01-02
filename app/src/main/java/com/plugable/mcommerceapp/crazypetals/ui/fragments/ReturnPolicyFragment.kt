package com.plugable.mcommerceapp.crazypetals.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient

import com.plugable.mcommerceapp.crazypetals.R
import kotlinx.android.synthetic.main.fragment_about_us.*
import kotlinx.android.synthetic.main.fragment_return_policy.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ReturnPolicyFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ReturnPolicyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReturnPolicyFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_return_policy, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializeViews()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeViews() {
        webViewReturnPolicy.settings.javaScriptEnabled = true
        webViewReturnPolicy.loadUrl(getString(R.string.return_policy_web_url))

        progressBarReturnPolicy.indeterminateDrawable.setColorFilter(
            Color.BLACK,
            PorterDuff.Mode.MULTIPLY
        )
//        mixPanel = MixpanelAPI.getInstance(context, resources.getString(R.string.mix_panel_token))
//        sendMixPanelEvent()
        if (activity!!.isFinishing){
            return
        }
        webViewReturnPolicy.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBarReturnPolicy.show()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBarReturnPolicy.hide()
            }
        }
    }
}
