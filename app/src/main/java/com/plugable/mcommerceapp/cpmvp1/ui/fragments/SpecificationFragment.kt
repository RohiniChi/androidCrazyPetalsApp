package com.plugable.mcommerceapp.cpmvp1.ui.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.utils.extension.hide
import kotlinx.android.synthetic.main.fragment_specification.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SpecificationFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [SpecificationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SpecificationFragment : BaseFragment() {


    private var brand: String? = null
    private var quantity: String? = null
    private var unitName: String? = null

    override fun onClick(p0: View?) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
//        productDetail=arguments!!.getParcelable("productDetails")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_specification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        readArguments()
        setData()
    }

    private fun readArguments() {
        brand = arguments?.getString(BRAND)
        quantity = arguments?.getString(QUANTITY)
        unitName = arguments?.getString(UNIT_NAME)
    }

    companion object {
                const val BRAND = "brand"
        const val QUANTITY = "quantity"
        const val UNIT_NAME = "unitName"
    }

    fun newInstance(
        brand: String? = null,
        quantity: String? = null,
        unitName: String? = null
    ): SpecificationFragment {
        val f = SpecificationFragment()
        val bundle = Bundle()
        bundle.putString(BRAND, brand)
        bundle.putString(QUANTITY, quantity)
        bundle.putString(UNIT_NAME, unitName)
        f.arguments = bundle
        return f
    }

    private fun setData() {
        if (TextUtils.isEmpty(brand)) {
            textViewBrand.hide()
            textViewBrandColon.hide()
            valueBrand.hide()
        } else {
            valueBrand.text = brand
        }

        if (TextUtils.isEmpty(quantity)) {
            textViewQuantity.hide()
            textViewQuantityColon.hide()
            valueQuantity.hide()
        } else {
            valueQuantity.text = quantity.plus(unitName)
        }

    }
}
