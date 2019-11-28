package com.plugable.mcommerceapp.cpmvp1.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.utils.extension.hide
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
        val webSettings = webViewDescription.settings
        webSettings.javaScriptEnabled = true
        webViewDescription.isVerticalScrollBarEnabled = false
        webViewDescription.isHorizontalScrollBarEnabled = false

        if (TextUtils.isEmpty(DESCRIPTION)) {
            webViewDescription.hide()
        } else {
            webViewDescription.loadData(description, "text/html", "UTF-8")

        }

    }

    private fun readArguments() {
        description = arguments?.getString(DESCRIPTION)
    }

    companion object {
        const val DESCRIPTION = "description"
    }

    fun newInstance(
        desc: String? = null
    ): DescriptionFragment {
        val f = DescriptionFragment()
        val bundle = Bundle()
        bundle.putString(DESCRIPTION, desc)
        f.arguments = bundle
        return f
    }
}
