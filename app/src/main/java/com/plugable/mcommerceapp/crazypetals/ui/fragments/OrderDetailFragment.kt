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


import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.payumoney.core.PayUmoneyConstants
import com.payumoney.core.PayUmoneySdkInitializer
import com.payumoney.sdkui.ui.utils.PayUmoneyFlowManager
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.OrderDetailResponse
import com.plugable.mcommerceapp.crazypetals.utils.extension.hide
import com.plugable.mcommerceapp.crazypetals.utils.extension.show
import com.plugable.mcommerceapp.crazypetals.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.crazypetals.utils.util.HashGenerator
import com.plugable.mcommerceapp.crazypetals.utils.util.isNetworkAccessible
import kotlinx.android.synthetic.main.fragment_order_detail.*
import org.jetbrains.anko.support.v4.toast


/**
 * A simple [Fragment] subclass.
 * Use the [OrderDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class OrderDetailFragment : BaseFragment() {

    private var orderId: String=""
    private var isTestMode: Boolean = false
    private lateinit var orderDetail: OrderDetailResponse.Data.OrderDetails
    private var mPaymentParams: PayUmoneySdkInitializer.PaymentParam? = null

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.buttonPayment -> {
                if (activity!!.isNetworkAccessible()){
                    buttonPayment.isClickable=false
                val emailId =
                    SharedPreferences.getInstance(activity!!).getProfile()
                        ?.emailId
                val phoneNumber =
                    SharedPreferences.getInstance(activity!!).getProfile()
                        ?.mobileNumber
                val name = SharedPreferences.getInstance(activity!!)
                    .getProfile()?.name

                initiatePayment(
                    name,
                    emailId!!,
                    phoneNumber!!,
                    orderDetail.orderTotal.toString(),
                    orderDetail.orderNumber.toString()
                )
            }
                else{
                    toast(getString(R.string.oops_no_internet_connection))
                    buttonPayment.isClickable=true

                }
        }
        }
    }

    private fun initiatePayment(
        nameData: String?,
        emailData: String,
        phoneData: String,
        amountData: String?,
        orderNumber: String
    ) {

        val transactionId = orderNumber
        val builder = PayUmoneySdkInitializer.PaymentParam.Builder()
        builder.setAmount(amountData)                          // Payment amount
            .setTxnId(transactionId)                                             // Transaction ID
            .setPhone(phoneData)                                           // User Phone number
            .setProductName("Android test")                   // Product Name or description
            .setFirstName(nameData)                              // User First name
            .setEmail(emailData)                                            // User Email ID
            .setsUrl(getString(R.string.pay_u_success_url))                    // Success URL (surl)
            .setfUrl(getString(R.string.pay_u_failed_url))
            .setUdf1("")
            .setUdf2("")
            .setUdf3("")
            .setUdf4("")
            .setUdf5("")
            .setUdf6("")
            .setUdf7("")
            .setUdf8("")
            .setUdf9("")
            .setUdf10("")
            .setIsDebug(isTestMode)                              // Integration environment - true (Debug)/ false(Production)
            .setKey(getString(R.string.pay_u_merchant_key))                        // Merchant key
            .setMerchantId(getString(R.string.pay_u_merchant_id))

        mPaymentParams = builder.build()
        mPaymentParams = calculateServerSideHashAndInitiatePayment1(mPaymentParams!!)

        PayUmoneyFlowManager.startPayUMoneyFlow(
            mPaymentParams,
            activity,
            R.style.AppTheme_Purple,
            false
        )

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        orderDetail = arguments!!.getParcelable("details")!!
        orderId=arguments!!.getString("order_id")!!
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
        buttonPayment.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        buttonPayment.setOnClickListener(this)
        buttonPayment.isClickable=true
    }

    private fun setData() {
        tvOrderedDate.text = orderDetail.orderedDate

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

        if (orderDetail.paymentStatus.equals("Failed", true)) {
            tvPaymentStatus.text = orderDetail.paymentStatus
            tvPaymentStatus.setTextColor(Color.RED)
            buttonPayment.show()
            tvDeliveryDateLabel.hide()
            tvDeliveryDate.hide()
        }
        else{
            tvPaymentStatus.text = orderDetail.paymentStatus
            buttonPayment.hide()
        }

        tvShippingAddress.text = orderDetail.address
    }

       private fun calculateServerSideHashAndInitiatePayment1(paymentParam: PayUmoneySdkInitializer.PaymentParam): PayUmoneySdkInitializer.PaymentParam {

        val stringBuilder = StringBuilder()
        val params = paymentParam.params
        stringBuilder.append(params[PayUmoneyConstants.KEY]!! + "|")
        stringBuilder.append(params[PayUmoneyConstants.TXNID]!! + "|")
        stringBuilder.append(params[PayUmoneyConstants.AMOUNT]!! + "|")
        stringBuilder.append(params[PayUmoneyConstants.PRODUCT_INFO]!! + "|")
        stringBuilder.append(params[PayUmoneyConstants.FIRSTNAME]!! + "|")
        stringBuilder.append(params[PayUmoneyConstants.EMAIL]!! + "|")
        stringBuilder.append(params[PayUmoneyConstants.UDF1]!! + "|")
        stringBuilder.append(params[PayUmoneyConstants.UDF2]!! + "|")
        stringBuilder.append(params[PayUmoneyConstants.UDF3]!! + "|")
        stringBuilder.append(params[PayUmoneyConstants.UDF4]!! + "|")
        stringBuilder.append(params[PayUmoneyConstants.UDF5]!! + "||||||")

        stringBuilder.append(getString(R.string.pay_u_merchant_salt))

        val hash = HashGenerator.hashCal(stringBuilder.toString())
        paymentParam.setMerchantHash(hash)

        return paymentParam
    }
}