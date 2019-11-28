package com.plugable.mcommerceapp.cpmvp1.ui.fragments

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.utils.extension.hide
import kotlinx.android.synthetic.main.fragment_instrucation.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [InstrucationFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [InstrucationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class InstrucationFragment : BaseFragment() {

    private var precautions: String? = null
    private var delivery_time: String? = null

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
        return inflater.inflate(R.layout.fragment_instrucation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        readArguments()
        setData()
    }

    private fun setData() {
        if (TextUtils.isEmpty(delivery_time)) {
            textViewDeliveryTime.hide()
            valueDeliveryTime.hide()
        } else {
            valueDeliveryTime.text = delivery_time
        }



        if (TextUtils.isEmpty(precautions)) {
            textViewPrecautions.hide()
            valuePrecautions.hide()
        } else {
            valuePrecautions.text = precautions
        }
    }


    private fun readArguments() {
        delivery_time = arguments?.getString(DELIVERY_TIME)
        precautions = arguments?.getString(PRECAUTIONS)
    }
    companion object {
        const val DELIVERY_TIME = "delivery_time"
        const val PRECAUTIONS = "precautions"

    }


    fun newInstance(
        delivery_time: String? = null,
        precautions: String? = null
    ): InstrucationFragment {
        val f = InstrucationFragment()
        val bundle = Bundle()
        bundle.putString(DELIVERY_TIME, delivery_time)
        bundle.putString(PRECAUTIONS, precautions)
        f.arguments = bundle
        return f
    }
}
