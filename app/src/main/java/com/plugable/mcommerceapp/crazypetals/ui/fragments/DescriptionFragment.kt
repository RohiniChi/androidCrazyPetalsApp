package com.plugable.mcommerceapp.ui.fragments

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.ui.fragments.BaseFragment
import com.plugable.mcommerceapp.crazypetals.utils.extension.hide
import kotlinx.android.synthetic.main.fragment_description.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [DescriptionFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [DescriptionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DescriptionFragment : BaseFragment() {

    private var includedAccessories: String? = null
    private var description: String? = null

    override fun onClick(p0: View?) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_description, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        readArguments()
        setData()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setData() {
        /*val webSettings = webViewDescription.settings
        webSettings.javaScriptEnabled = true
        webViewDescription.isVerticalScrollBarEnabled = false
        webViewDescription.isHorizontalScrollBarEnabled = false*/
        if (TextUtils.isEmpty(DESCRIPTION)) {
            webViewDescription.hide()
        } else {

            //description=description.plus(description).plus(description).plus(description).plus(description)
        //    webViewDescription.loadData(description, "text/html", "UTF-8")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                webViewDescription.setText(Html.fromHtml(description, Html.FROM_HTML_MODE_COMPACT));
            } else {
                webViewDescription.setText(Html.fromHtml(description));
            }

        }

        if (TextUtils.isEmpty(includedAccessories)) {
            textviewAccessories.hide()
            textViewValueAccessories.hide()
        } else {
            textViewValueAccessories.text = includedAccessories

        }
    }

    private fun readArguments() {
        description = arguments?.getString(DESCRIPTION)
        includedAccessories = arguments?.getString(INCLUDED_ACCESSORIES)
    }

    companion object {
        const val DESCRIPTION = "description"
        const val INCLUDED_ACCESSORIES = "included_accessories"

    }

    fun newInstance(
        desc: String? = null,
        includedAccessories: String? = null
    ): DescriptionFragment {
        val f = DescriptionFragment()
        val bundle = Bundle()
        bundle.putString(DESCRIPTION, desc)
        bundle.putString(INCLUDED_ACCESSORIES, includedAccessories)
        f.arguments = bundle
        return f
    }
}
