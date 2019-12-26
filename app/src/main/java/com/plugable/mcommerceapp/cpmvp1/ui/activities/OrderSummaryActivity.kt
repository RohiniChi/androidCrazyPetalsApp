package com.plugable.mcommerceapp.cpmvp1.ui.activities

import ServiceGenerator
import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.payumoney.core.PayUmoneyConstants
import com.payumoney.core.PayUmoneySdkInitializer
import com.payumoney.core.entity.TransactionResponse
import com.payumoney.sdkui.ui.utils.PayUmoneyFlowManager
import com.payumoney.sdkui.ui.utils.ResultModel
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.callbacks.EventListener
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.*
import com.plugable.mcommerceapp.cpmvp1.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.cpmvp1.ui.adapters.OrdersAdapter
import com.plugable.mcommerceapp.cpmvp1.utils.application.App
import com.plugable.mcommerceapp.cpmvp1.utils.constants.IntentFlags
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi
import com.plugable.mcommerceapp.cpmvp1.utils.extension.hide
import com.plugable.mcommerceapp.cpmvp1.utils.extension.setStatusBarColor
import com.plugable.mcommerceapp.cpmvp1.utils.extension.setToolBarColor
import com.plugable.mcommerceapp.cpmvp1.utils.extension.show
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.cpmvp1.utils.util.HashGenerator.hashCal
import com.plugable.mcommerceapp.cpmvp1.utils.util.capitalizeAll
import com.plugable.mcommerceapp.cpmvp1.utils.util.isNetworkAccessible
import kotlinx.android.synthetic.main.activity_order_summary.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*
import kotlinx.android.synthetic.main.layout_sub_total_amount.*
import org.jetbrains.anko.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * [OrderSummaryActivity] is used to show summary of its orders
 *
 */
class OrderSummaryActivity : AppCompatActivity(), View.OnClickListener, EventListener {


    private lateinit var placeOrderResponse: PlaceOrderResponse
    private var orderId = 0
    private val productList = ArrayList<GetCartResponse.Data>()
    var ordersList = ArrayList<OrderItems.Data.Items>()
    var address: AddressListResponse.Data? = null
    var addressId: Int? = null
    lateinit var ordersAdapter: OrdersAdapter
    var data: GetTotalPrice.Data? = null
    private val tag = "OrderSummaryActivity : "
    private var mPaymentParams: PayUmoneySdkInitializer.PaymentParam? = null
    private var isTestMode: Boolean = true
    private var totalPrice = "0"
    var transactionId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_summary)


        initializeTheme()
        initializeViews()
    }

    private fun initializeViews() {
        ordersAdapter = OrdersAdapter(ordersList, this, this)
        recyclerViewOrderSummaryOrderDetails.adapter = ordersAdapter
        imgToolbarHomeLayout.setOnClickListener(this)
        textViewTermsAndConditions.setOnClickListener(this)
        materialButtonOrderSummaryPlaceOrder.setOnClickListener(this)

        imageViewDeliveryAddressDelete.hide()
        imageViewSubTotalCollapse.hide()
        progressBar.indeterminateDrawable.setColorFilter(
            Color.BLACK,
            PorterDuff.Mode.MULTIPLY
        )
        progressBar.hide()

        textViewTermsAndConditions.isClickable = true
        checkboxInstructions.isClickable = true
        materialButtonOrderSummaryPlaceOrder.isClickable = true

        checkBoxCheckListener()
        loadData()
    }

    private fun checkBoxCheckListener() {
        checkboxInstructions.setOnClickListener {
            if (checkboxInstructions.isChecked) {
                materialButtonOrderSummaryPlaceOrder.setBackgroundColor(
                    Color.parseColor(
                        ApplicationThemeUtils.SECONDARY_COLOR
                    )
                )
                materialButtonOrderSummaryPlaceOrder.isClickable = true
            } else {
                toast("Please agree to terms and conditions")
                materialButtonOrderSummaryPlaceOrder.setBackgroundColor(
                    Color.GRAY
                )
                materialButtonOrderSummaryPlaceOrder.isClickable = true
            }
        }

    }

    private fun loadData() {
        showProgress()
        fetchAddressList()
        callTotalPriceApi()
        callCartApi()
    }

    private fun showAddress() {
        textViewDeliveryAddressPersonName.text = String.format("%s", address?.name).capitalizeAll()
        textViewDeliveryAddressCompleteAddress.text = String.format(
            "%s, %s, %s, %s - %s",
            address?.address,
            address?.landmark,
            address?.locality,
            address?.city,
            address?.pinCode
        ).capitalizeAll()
        textViewDeliveryAddressContactNumber.text =
            String.format("Contact No. %s", address?.mobileNumber)
    }

    private fun readAddressFromIntent() {
        addressId = intent.getIntExtra(ADDRESS, -1)
    }

    private fun callTotalPriceApi() {
        val applicationUserId =
            SharedPreferences.getInstance(this).getStringValue(IntentFlags.APPLICATION_USER_ID)
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        val call = clientInstance.getTotalPriceApi(applicationUserId!!.toInt(), WebApi.APP_ID)
        call.enqueue(object : Callback<GetTotalPrice> {
            override fun onFailure(call: Call<GetTotalPrice>, t: Throwable) {
                hideProgress()
                toast(getString(R.string.message_something_went_wrong))
            }

            override fun onResponse(call: Call<GetTotalPrice>, response: Response<GetTotalPrice>) {
                data = response.body()!!.data
                if (response.body()?.statusCode.equals("10")) {
                    totalPrice = data!!.subTotal.toString()
                    textViewSubTotalDescription.text =
                        "₹".plus(data!!.subTotal.toString())
                    textViewSubTotalDiscountDescription.text =
                        "₹".plus(data!!.productDiscounts.toString())
                    if (data!!.deliveryCharges.toString().equals("0")) {
                        textViewSubTotalDeliveryChargesDescription.text = "Free"
                    } else {
                        textViewSubTotalDeliveryChargesDescription.text =
                            "₹".plus(data!!.deliveryCharges.toString())
                    }
                    textViewSubTotalMrpDescription.text =
                        "₹".plus(data!!.mrp.toString())
//                    textViewCheckoutButtonSubtotal.text =
//                        "Total".plus(" ").plus("₹").plus(response.body()!!.data.subTotal.toString())
                }
            }

        })
    }

    private fun initializeTheme() {
        val configDetail = SharedPreferences.getInstance(this).themeDataPreference
        ApplicationThemeUtils.APP_NAME = configDetail!!.appName
        ApplicationThemeUtils.PRIMARY_COLOR = configDetail.primaryColor
        ApplicationThemeUtils.SECONDARY_COLOR = configDetail.secondryColor
        ApplicationThemeUtils.STATUS_BAR_COLOR = configDetail.statusBarColor
        ApplicationThemeUtils.TEXT_COLOR = configDetail.textColor
        ApplicationThemeUtils.CURRENCY_SYMBOL = configDetail.currencySymbol
        ApplicationThemeUtils.TOOL_BAR_COLOR = configDetail.tertiaryColor
        setToolBar(ApplicationThemeUtils.APP_NAME)
        setThemeToComponents()
    }

    private fun setThemeToComponents() {
        materialButtonOrderSummaryPlaceOrder.setBackgroundColor(
            Color.GRAY
        )

        checkboxInstructions.buttonTintList = ColorStateList.valueOf(Color.BLACK)

    }

    fun setToolBar(name: String) {
        setSupportActionBar(toolBar)
        setStatusBarColor()
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = name
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_shape_backarrow_white)
        cp_Logo.hide()
        txtToolbarTitle.text = "Order Summary"
        imgToolbarHome.hide()
        setToolBarColor(imgToolbarHome, txtToolbarTitle, toolbar = toolBar)
    }


    private fun callCartApi() {
        val applicationUserId =
            SharedPreferences.getInstance(this).getStringValue(IntentFlags.APPLICATION_USER_ID)
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        val callback = clientInstance.getCartApi(applicationUserId!!.toInt())

        callback.enqueue(object : Callback<GetCartResponse> {
            override fun onFailure(call: Call<GetCartResponse>, t: Throwable) {
                hideProgress()
                toast(getString(R.string.message_something_went_wrong))
            }

            override fun onResponse(
                call: Call<GetCartResponse>,
                response: Response<GetCartResponse>
            ) {
                if (response.body()?.statusCode.equals("10")) {
                    hideProgress()
                    if (response.body()!!.data.isNotEmpty()) {
                        productList.clear()
                        productList.addAll(response.body()!!.data)
                        response.body()!!.data.forEach {
                            ordersList.add(
                                OrderItems.Data.Items(
                                    it.productName,
                                    it.id,
                                    it.discountedPrice.toString(),
                                    it.quantity
                                )
                            )
                        }
                        ordersAdapter.notifyDataSetChanged()
                    }
                } else {
                    hideProgress()
                    toast(getString(R.string.message_something_went_wrong))
                }
            }
        })

    }

    override fun onResume() {
        textViewTermsAndConditions.isClickable = true
        materialButtonOrderSummaryPlaceOrder.isClickable = true
        checkboxInstructions.isClickable = true
        super.onResume()
    }

    override fun onItemClickListener(position: Int) {
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            android.R.id.home -> onBackPressed()
            R.id.imgToolbarHomeLayout -> onBackPressed()
            R.id.materialButtonOrderSummaryPlaceOrder -> {
                if (checkboxInstructions.isChecked) {
                    materialButtonOrderSummaryPlaceOrder.isClickable = false

                    materialButtonOrderSummaryPlaceOrder.setBackgroundColor(
                        Color.parseColor(
                            ApplicationThemeUtils.SECONDARY_COLOR
                        )
                    )
                    placeOrder()
                } else {
                    materialButtonOrderSummaryPlaceOrder.isClickable = true
                    toast("Please agree to terms and conditions")

                    materialButtonOrderSummaryPlaceOrder.setBackgroundColor(
                        Color.GRAY
                    )
                }
            }
            R.id.textViewTermsAndConditions -> {
                textViewTermsAndConditions.isClickable = false
                startActivity<InstructionActivity>()

            }

        }
    }

    object LastClickTimeSingleton {
        var lastClickTime: Long = 0
    }

    private fun placeOrder() {
        checkboxInstructions.isClickable = false
        materialButtonOrderSummaryPlaceOrder.isClickable = true
        if (!isNetworkAccessible()) {
            toast(R.string.oops_no_internet_connection)
            return
        }
        val productDetails = ArrayList<PlaceOrderRequest.OrderDetail>()
        productList.forEach {
            productDetails.add(
                PlaceOrderRequest.OrderDetail(
//                    if (it.colorId==null) it.colorId else it.colorId,
                    if (it.colorId == null) 0.toString() else it.colorId,
                    it.productId.toString(),
                    it.quantity.toString(),
                    if (it.size == null) 0.toString() else it.sizeId,
                    if (it.originalPrice == null) it.discountedPrice.toString() else it.originalPrice.toString()
                )
            )
        }

        val placeOrderRequest = PlaceOrderRequest(
            MRP = data!!.subTotal.toString(),
            AddressId = address!!.id.toString(),
            ApplicationUserId = SharedPreferences.getInstance(this).getStringValue(IntentFlags.APPLICATION_USER_ID)!!,
            Comments = if (TextUtils.isEmpty(etComments.text.toString())) "" else etComments.text.toString(),
            DeliveryCharges = data!!.deliveryCharges.toString(),
            OrderDetails = productDetails,
            ProductsDiscount = data!!.productDiscounts.toString(),
            SubTotal = data!!.mrp.toString()
        )


        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        val callback = clientInstance.placeOrder(placeOrderRequest)
        progressBar.show()
        callback.enqueue(object : Callback<PlaceOrderResponse> {
            override fun onFailure(call: Call<PlaceOrderResponse>, t: Throwable) {
                materialButtonOrderSummaryPlaceOrder.isEnabled = true
                materialButtonOrderSummaryPlaceOrder.isClickable = false
                progressBar.hide()
                toast(getString(R.string.message_something_went_wrong))
            }

            override fun onResponse(
                call: Call<PlaceOrderResponse>,
                response: Response<PlaceOrderResponse>
            ) {
                when {
                    response.body()?.statusCode.equals("10") -> {
                        progressBar.hide()
                        materialButtonOrderSummaryPlaceOrder.isClickable = false

                        orderId = response.body()!!.orderId

                        SharedPreferences.getInstance(this@OrderSummaryActivity)
                            .setCartCountString(("0").toString())

                        val emailId =
                            SharedPreferences.getInstance(this@OrderSummaryActivity).getProfile()
                                ?.emailId
                        initiatePayment(
                            address!!.name,
                            emailId!!,
                            address!!.mobileNumber,
                            totalPrice,
                            response.body()!!.orderNumber
                        )
                        //startActivity<SuccessOrderStatusActivity>(SuccessOrderStatusActivity.PLACE_ORDER_RESPONSE to response.body())
                        placeOrderResponse = response.body()!!
                    }
                    response.body()?.statusCode.equals("30") -> {
                        progressBar.hide()
                        //                    toast(response.body()!!.message)
                        showAlert(response.body()!!.message)
                        materialButtonOrderSummaryPlaceOrder.isClickable = true
                    }
                    else -> {
                        materialButtonOrderSummaryPlaceOrder.isClickable = true
                        progressBar.hide()
                        toast(getString(R.string.message_something_went_wrong))
                    }
                }
            }
        })
    }

    private fun showAlert(message: String) {
        alert(message) {
            yesButton { }
            isCancelable = false
        }.show().apply {

            getButton(AlertDialog.BUTTON_POSITIVE)?.let {
                it.textColor = Color.BLUE
                it.background =
                    ContextCompat.getDrawable(
                        this@OrderSummaryActivity,
                        android.R.color.transparent
                    )
            }
        }
    }

    companion object {
        const val ADDRESS = "address"
    }

    private fun showProgress() {
        progressBar.show()
        content.hide()
        materialButtonOrderSummaryPlaceOrder.hide()
    }

    private fun hideProgress() {
        progressBar.hide()
        content.show()
        materialButtonOrderSummaryPlaceOrder.show()
    }

    private fun fetchAddressList() {
        readAddressFromIntent()
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        val callback = clientInstance.getAddressList(
            SharedPreferences.getInstance(this).getStringValue(
                IntentFlags.APPLICATION_USER_ID
            )!!
        )

        callback.enqueue(object : Callback<AddressListResponse> {
            override fun onFailure(call: Call<AddressListResponse>, t: Throwable) {
                hideProgress()
                toast(getString(R.string.message_something_went_wrong))
            }

            override fun onResponse(
                call: Call<AddressListResponse>,
                response: Response<AddressListResponse>
            ) {
                if (response.body()?.statusCode.equals("10")) {
                    if (response.body()!!.data.isNotEmpty()) {

                        response.body()!!.data.forEach {
                            if (it.id == addressId) {
                                address = it
                            }
                        }
                        showAddress()
                    }
                } else {
                    hideProgress()
                    toast(getString(R.string.message_something_went_wrong))
                }
            }
        })
    }


    fun initiatePayment(
        nameData: String,
        emailData: String,
        phoneData: String,
        amountData: String,
        orderNumber: String
    ) {
        transactionId = orderNumber
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
            this@OrderSummaryActivity,
            R.style.AppTheme_Purple,
            false
        )

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result Code is -1 send from Payumoney activity
        Log.d("MainActivity", "request code $requestCode resultcode $resultCode")
        materialButtonOrderSummaryPlaceOrder.isClickable = true
        hideProgress()
        if (isNetworkAccessible()) {
            updateTransactionStatus(requestCode, resultCode, data)
        } else {
            showNetworkAlert(requestCode, resultCode, data)
        }
    }

    private fun showNetworkAlert(requestCode: Int, resultCode: Int, data: Intent?) {
        alert(getString(R.string.oops_no_internet_connection)) {
            isCancelable = false
            positiveButton(getString(R.string.try_again)) {
                if (isNetworkAccessible()) {
                    updateTransactionStatus(requestCode, resultCode, data)
                } else {
                    showNetworkAlert(requestCode, resultCode, data)
                }
            }
        }.show().apply {
            getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.let {
                it.allCaps = false
                it.textColor = Color.BLUE
                it.background =
                    ContextCompat.getDrawable(
                        this@OrderSummaryActivity,
                        android.R.color.transparent
                    )
            }
        }
    }


    private fun updateTransactionStatus(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == PayUmoneyFlowManager.REQUEST_CODE_PAYMENT && resultCode == RESULT_OK && data != null) {
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
                    updatePaymentStatus(orderId, "2")
                } else {
                    toast("Transaction unsuccessful")
                    showProgress()
                    updatePaymentStatus(orderId, "5")

                    //Failure Transaction
                }

            } else if (resultModel != null && resultModel.error != null) {
                Log.d(tag, "Error response : " + resultModel.error.transactionResponse)
            } else {
                Log.d(tag, "Both objects are null!")
            }
        }

    }

    private fun updatePaymentStatus(orderId: Int, paymentStatusId: String) {
        if (isNetworkAccessible()) {
            val updateStatusRequest = UpdatePaymentRequest(
                orderId.toString(),
                paymentStatusId
            )
            App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
            val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
            val call = clientInstance.updatePaymentStatus(updateStatusRequest)
            call.enqueue(object : Callback<UpdatePaymentResponse> {
                override fun onFailure(call: Call<UpdatePaymentResponse>, t: Throwable) {
                    materialButtonOrderSummaryPlaceOrder.isClickable = true
                    hideProgress()
                    toast(getString(R.string.message_something_went_wrong))
                }

                override fun onResponse(
                    call: Call<UpdatePaymentResponse>,
                    response: Response<UpdatePaymentResponse>
                ) {
                    if (response.body()!!.statusCode.equals("10")) {
                        hideProgress()
                        materialButtonOrderSummaryPlaceOrder.isClickable = true
                        startActivity<SuccessOrderStatusActivity>(SuccessOrderStatusActivity.PLACE_ORDER_RESPONSE to placeOrderResponse)
                    } else {
                        hideProgress()
                        materialButtonOrderSummaryPlaceOrder.isClickable = true
                        toast(response.body()!!.message)
                    }
                }

            })
        } else {
            hideProgress()
            materialButtonOrderSummaryPlaceOrder.isClickable = true
            toast(getString(R.string.oops_no_internet_connection))
        }
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

        val hash = hashCal(stringBuilder.toString())
        paymentParam.setMerchantHash(hash)

        return paymentParam
    }
}
