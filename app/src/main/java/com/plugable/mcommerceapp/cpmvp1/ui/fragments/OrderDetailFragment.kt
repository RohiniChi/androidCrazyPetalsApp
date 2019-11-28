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
package com.plugable.mcommerceapp.cpmvp1.ui.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.OrderDetailResponse
import kotlinx.android.synthetic.main.fragment_order_detail.*


/**
 * A simple [Fragment] subclass.
 * Use the [OrderDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class OrderDetailFragment : BaseFragment() {

    private lateinit var orderDetail: OrderDetailResponse.Data.OrderDetails
    override fun onClick(v: View?) {

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        orderDetail = arguments!!.getParcelable("details")!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_detail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setData()

    }

    private fun setData() {
        tvOrderedDate.text = orderDetail.orderedDate

        /*    if (orderDetail.deliveryDay==null) {
                tvDeliveryDateLabel.text = getString(R.string.label_delivered_date)
                tvDeliveryDate.text = orderDetail.deliveredDate
            } else {
                tvDeliveryDateLabel.text = getString(R.string.label_arriving_on)
                tvDeliveryDate.text = orderDetail.deliveryDay
            }*/

        // here order status are as following
//	DeliveryStatus :  1. Awaiting Processing, 2. Processing, 4. Delivered. 5. Will Not delivered

        if (orderDetail.deliveryStatus == 1 || orderDetail.deliveryStatus == 2) {
            tvDeliveryDateLabel.text = getString(R.string.label_arriving_on)
            tvDeliveryDate.text = orderDetail.deliveryDay

        } else if (orderDetail.deliveryStatus == 4) {
            tvDeliveryDateLabel.text = getString(R.string.label_delivered_date)
            tvDeliveryDate.text = orderDetail.deliveredDate
        } else if (orderDetail.deliveryStatus == 5) {
            tvDeliveryDateLabel.text = "Order status:"
            tvDeliveryDate.text = "Cancelled "
        }

        tvOrderId.text = orderDetail.orderNumber
        tvOrderTotal.text = "₹".plus(orderDetail.orderTotal.toString())

        tvShippingAddress.text = orderDetail.address
    }


}