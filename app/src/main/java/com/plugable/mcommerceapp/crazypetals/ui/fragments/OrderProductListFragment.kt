/*
 * Author : Chetan Patil.
 * Module : My Order module
 * Version : V 1.0
 * Sprint : 13
 * Date of Development : 07/10/2019
 * Date of Modified : 07/10/2019
 * Comments : My order screen
 * Output : List all the Order
 */
package com.plugable.mcommerceapp.crazypetals.ui.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.callbacks.EventListener
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.OrderDetailResponse
import com.plugable.mcommerceapp.crazypetals.ui.adapters.OrderProductAdapter
import kotlinx.android.synthetic.main.fragment_order_product_list.*
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [OrderProductListFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class OrderProductListFragment : BaseFragment(), EventListener {
    override fun onItemClickListener(position: Int) {

    }

    lateinit var orderDetail: OrderDetailResponse.Data.OrderDetails
    var productArrayList = ArrayList<OrderDetailResponse.Data.ProductListItem>()
    private lateinit var orderProductAdapter: OrderProductAdapter
    override fun onClick(v: View?) {

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        orderDetail = arguments!!.getParcelable("details")!!
        productArrayList = arguments!!.getParcelableArrayList("products")!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_product_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        orderProductAdapter = OrderProductAdapter(activity!!, productArrayList, this)
        recyclerViewProduct.adapter = orderProductAdapter
    }


}