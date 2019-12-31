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


import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.payumoney.core.PayUmoneyConstants
import com.payumoney.core.PayUmoneySdkInitializer
import com.payumoney.core.entity.TransactionResponse
import com.payumoney.sdkui.ui.utils.PayUmoneyFlowManager
import com.payumoney.sdkui.ui.utils.ResultModel
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.OrderDetailResponse
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.UpdatePaymentRequest
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.UpdatePaymentResponse
import com.plugable.mcommerceapp.cpmvp1.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.cpmvp1.ui.activities.DashboardActivity
import com.plugable.mcommerceapp.cpmvp1.ui.activities.SuccessOrderStatusActivity
import com.plugable.mcommerceapp.cpmvp1.utils.application.App
import com.plugable.mcommerceapp.cpmvp1.utils.constants.IntentFlags
import com.plugable.mcommerceapp.cpmvp1.utils.extension.hide
import com.plugable.mcommerceapp.cpmvp1.utils.extension.show
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.cpmvp1.utils.util.HashGenerator
import com.plugable.mcommerceapp.cpmvp1.utils.util.isNetworkAccessible
import kotlinx.android.synthetic.main.fragment_order_detail.*
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.textColor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (activity!!.isNetworkAccessible()) {
            updateTransactionStatus(requestCode, resultCode, data)
        } else {
            showNetworkAlert(requestCode, resultCode, data)
          layoutOrderDetail.hide()
        }
    }

    private fun showNetworkAlert(requestCode: Int, resultCode: Int, data: Intent?) {
        alert(getString(R.string.oops_no_internet_connection)) {
            isCancelable = false
            positiveButton(getString(R.string.try_again)) {
                if (activity!!.isNetworkAccessible()) {
                    updateTransactionStatus(requestCode, resultCode, data)
                } else {
                    showNetworkAlert(requestCode, resultCode, data)

                }
            }
        }.show().apply {
            getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).let {
                it.allCaps = false
                it.textColor = Color.BLUE
                it.background =
                    ContextCompat.getDrawable(
                        activity!!,
                        android.R.color.transparent
                    )
            }
        }
    }
    private fun updateTransactionStatus(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == PayUmoneyFlowManager.REQUEST_CODE_PAYMENT && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val transactionResponse = data.getParcelableExtra<TransactionResponse>(
                PayUmoneyFlowManager
                    .INTENT_EXTRA_TRANSACTION_RESPONSE
            )

            val resultModel =
                data.getParcelableExtra<ResultModel>(PayUmoneyFlowManager.ARG_RESULT)

            // Check which object is non-null
            if (transactionResponse.getPayuResponse() != null) {
                transactionResponse.transactionDetails
                if (transactionResponse.transactionStatus == TransactionResponse.TransactionStatus.SUCCESSFUL) {
                    //Success Transaction
                    // call update order api
                    toast("Transaction successful")
                    showProgress()
                    updatePaymentStatus(orderId.toInt(), "2","Successful")
                } else {
                    toast("Transaction unsuccessful")
                    showProgress()
                    updatePaymentStatus(orderId.toInt(), "5","Unsuccessful")

                    //Failure Transaction
                }

            } else if (resultModel != null && resultModel.error != null) {
                Log.d(tag, "Error response : " + resultModel.error.transactionResponse)
            } else {
                Log.d(tag, "Both objects are null!")
            }
        } else {
            toast("Transaction unsuccessful")
            showProgress()
            updatePaymentStatus(orderId.toInt(), "5","Unsuccessful")
        }

    }
    private fun updatePaymentStatus(orderId: Int, paymentStatusId: String,transactionStatus:String) {
        if (activity!!.isNetworkAccessible()) {
            val updateStatusRequest = UpdatePaymentRequest(
                orderId.toString(),
                paymentStatusId
            )
            App.HostUrl = SharedPreferences.getInstance(activity!!).hostUrl!!
            val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
            val call = clientInstance.updatePaymentStatus(updateStatusRequest)
            call.enqueue(object : Callback<UpdatePaymentResponse> {
                override fun onFailure(call: Call<UpdatePaymentResponse>, t: Throwable) {
                    hideProgress()
                    toast(getString(R.string.message_something_went_wrong))
                }

                override fun onResponse(
                    call: Call<UpdatePaymentResponse>,
                    response: Response<UpdatePaymentResponse>
                ) {
                    if (response.body()!!.statusCode.equals("10")) {
                        /*startActivity<DashboardActivity>(
                            IntentFlags.FRAGMENT_TO_BE_LOADED to activity!!.intent.getIntExtra(
                                IntentFlags.FRAGMENT_TO_BE_LOADED,
                                R.id.nav_home
                            )
                        )
                        activity!!.finish()*/
                        startActivity<SuccessOrderStatusActivity>(
                            IntentFlags.REDIRECT_FROM to "OrderDetailFragment",
                            "DeliveryDay" to orderDetail.deliveryDay,
                            "orderNumber" to orderDetail.orderNumber,
                            "TransactionStatus"  to transactionStatus
                        )


                    } else {
                        toast(response.body()!!.message)
                    }
                }

            })
        } else {
            hideProgress()
            toast(getString(R.string.oops_no_internet_connection))
        }
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
        }
        tvPaymentStatus.text = orderDetail.paymentStatus

        tvShippingAddress.text = orderDetail.address
    }

    fun showProgress(){
      /*  tableLayout.hide()
        delivery_timing_statement.hide()
        layoutShippingAddress.hide()*/
        layoutOrderDetail.hide()
        progressBarOrderDetail.show()
    }

    fun hideProgress(){
      /*  tableLayout.show()
        delivery_timing_statement.show()
        layoutShippingAddress.show()*/
        layoutOrderDetail.show()
        progressBarOrderDetail.hide()
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