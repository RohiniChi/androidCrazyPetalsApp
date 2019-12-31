package com.plugable.mcommerceapp.ui.fragments

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.ui.fragments.BaseFragment
import com.plugable.mcommerceapp.crazypetals.utils.extension.hide
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


    private var length: String? = null
    private var height: String? = null
    private var width: String? = null
    private var weight: String? = null
    private var materialType: String? = null

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
        length = arguments?.getString(LENGTH)
        height = arguments?.getString(HEIGHT)
        width = arguments?.getString(WIDTH)
        weight = arguments?.getString(WEIGHT)
        materialType = arguments?.getString(MATERIAL_TYPE)
    }

    companion object {
        const val LENGTH = "length"
        const val HEIGHT = "height"
        const val WIDTH = "width"
        const val WEIGHT = "weight"
        const val MATERIAL_TYPE = "material_type"

    }

    fun newInstance(
        length: String? = null,
        height: String? = null,
        width: String? = null,
        weight: String? = null,
        material_type: String? = null
    ): SpecificationFragment {
        val f = SpecificationFragment()
        val bundle = Bundle()
        bundle.putString(LENGTH, length)
        bundle.putString(HEIGHT, height)
        bundle.putString(WIDTH, width)
        bundle.putString(WEIGHT, weight)
        bundle.putString(MATERIAL_TYPE, material_type)
        f.arguments = bundle
        return f
    }

    private fun setData() {
        if (TextUtils.isEmpty(height)) {
            textViewHeight.hide()
            textViewHeightColon.hide()
            valueHeight.hide()
        } else {
            valueHeight.text = height
        }

        if (TextUtils.isEmpty(width)) {
            textViewWidth.hide()
            textViewWidthColon.hide()
            valueWidth.hide()
        } else {
            valueWidth.text = width
        }

        if (TextUtils.isEmpty(length)) {
            textViewLength.hide()
            textViewLengthColon.hide()
            valueLength.hide()
        } else {
            valueLength.text = length
        }


        if (TextUtils.isEmpty(materialType)) {
            textViewMaterialType.hide()
            textViewMaterialTypeColon.hide()
            valueMaterialType.hide()
        } else {
            valueMaterialType.text = materialType
        }

        if (TextUtils.isEmpty(weight)) {
            textViewWeight.hide()
            textViewWeightColon.hide()
            valueWeight.hide()
        } else {
            valueWeight.text = weight
        }

    }
}
