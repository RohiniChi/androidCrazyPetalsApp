package com.plugable.mcommerceapp.crazypetals.ui.activities

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
import androidx.core.view.isVisible
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.payumoney.core.PayUmoneyConstants
import com.payumoney.core.PayUmoneySdkInitializer
import com.payumoney.core.entity.TransactionResponse
import com.payumoney.sdkui.ui.utils.PayUmoneyFlowManager
import com.payumoney.sdkui.ui.utils.ResultModel
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.callbacks.EventListener
import com.plugable.mcommerceapp.crazypetals.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.*
import com.plugable.mcommerceapp.crazypetals.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.crazypetals.ui.adapters.OrdersAdapter
import com.plugable.mcommerceapp.crazypetals.utils.application.App
import com.plugable.mcommerceapp.crazypetals.utils.constants.IntentFlags
import com.plugable.mcommerceapp.crazypetals.utils.constants.WebApi
import com.plugable.mcommerceapp.crazypetals.utils.extension.*
import com.plugable.mcommerceapp.crazypetals.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.crazypetals.utils.util.HashGenerator.hashCal
import com.plugable.mcommerceapp.crazypetals.utils.util.capitalizeAll
import com.plugable.mcommerceapp.crazypetals.utils.util.isNetworkAccessible
import kotlinx.android.synthetic.main.activity_order_summary.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*
import kotlinx.android.synthetic.main.layout_sub_total_amount.*
import org.jetbrains.anko.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * [OrderSummaryActivity] is used to show summary of its orders
 *
 */
class OrderSummaryActivity : AppCompatActivity(), View.OnClickListener, EventListener {


    private var paymentStatus: String = ""
    private var termsOfUseError: String = ""
    private lateinit var updatePaymentStatusApi: Call<UpdatePaymentResponse>
    private lateinit var fetchAddressListApi: Call<AddressListResponse>
    private lateinit var placeOrderApi: Call<PlaceOrderResponse>
    private lateinit var cartListApi: Call<GetCartResponse>
    private lateinit var totalPriceApi: Call<GetTotalPrice>
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
    private var isTestMode: Boolean = false
    private var totalPrice = "0"
    var transactionId = ""
    private lateinit var mixPanel: MixpanelAPI

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


        textViewTermsAndConditions.isClickable = true
        checkboxInstructions.isClickable = true
        materialButtonOrderSummaryPlaceOrder.isClickable = true

        checkBoxCheckListener()
        loadData()
        if (!SharedPreferences.getInstance(this@OrderSummaryActivity)
                .isTermsConditionRememberMe
        ) {
            materialButtonOrderSummaryPlaceOrder.setBackgroundColor(
                Color.GRAY
            )
            materialButtonOrderSummaryPlaceOrder.isClickable = true

        } else {
            materialButtonOrderSummaryPlaceOrder.setBackgroundColor(
                Color.parseColor(
                    ApplicationThemeUtils.SECONDARY_COLOR
                )
            )
            materialButtonOrderSummaryPlaceOrder.isClickable = true

        }
        mixPanel = MixpanelAPI.getInstance(this, resources.getString(R.string.mix_panel_token))
        sendMixPanelEvent("visitedScreen")
    }

    private fun checkBoxCheckListener() {
        checkboxInstructions.isChecked =
            SharedPreferences.getInstance(this@OrderSummaryActivity).isTermsConditionRememberMe
        checkboxInstructions.setOnClickListener {
            if (checkboxInstructions.isChecked) {
                if (!SharedPreferences.getInstance(this@OrderSummaryActivity)
                        .isTermsConditionRememberMe
                ) {
                    showRememberMeDialog()
                } else {
                    materialButtonOrderSummaryPlaceOrder.setBackgroundColor(
                        Color.parseColor(
                            ApplicationThemeUtils.SECONDARY_COLOR
                        )
                    )
                    materialButtonOrderSummaryPlaceOrder.isClickable = true

                }


            } else {
                SharedPreferences.getInstance(this@OrderSummaryActivity)
                    .isTermsConditionRememberMe = false
                toast(getString(R.string.message_agree_terms))
                termsOfUseError = getString(R.string.message_agree_terms)
                sendMixPanelEvent("checkBoxUnchecked")

                materialButtonOrderSummaryPlaceOrder.setBackgroundColor(
                    Color.GRAY
                )
                materialButtonOrderSummaryPlaceOrder.isClickable = true
            }
        }

    }

    private fun showRememberMeDialog() {
        alert(getString(R.string.message_remember_me), getString(R.string.label_remember_me)) {
            positiveButton("Yes") {
                SharedPreferences.getInstance(this@OrderSummaryActivity)
                    .isTermsConditionRememberMe = true
                materialButtonOrderSummaryPlaceOrder.setBackgroundColor(
                    Color.parseColor(
                        ApplicationThemeUtils.SECONDARY_COLOR
                    )
                )
            }
            negativeButton("No") {
                SharedPreferences.getInstance(this@OrderSummaryActivity)
                    .isTermsConditionRememberMe = false
                materialButtonOrderSummaryPlaceOrder.setBackgroundColor(
                    Color.parseColor(
                        ApplicationThemeUtils.SECONDARY_COLOR
                    )
                )
            }
            isCancelable = false
        }.show().apply {

            getButton(AlertDialog.BUTTON_POSITIVE)?.let {
                it.textColor = Color.BLUE
                it.allCaps = false
                it.background =
                    ContextCompat.getDrawable(
                        this@OrderSummaryActivity,
                        android.R.color.transparent
                    )
            }
            getButton(AlertDialog.BUTTON_NEGATIVE)?.let {
                it.textColor = Color.BLUE
                it.allCaps = false
                it.background =
                    ContextCompat.getDrawable(
                        this@OrderSummaryActivity,
                        android.R.color.transparent
                    )
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
            "%s, %s, %s, %s, %s, %s - %s",
            address?.address,
            address?.landmark,
            address?.locality,
            address?.city,
            address?.state,
            address?.country,
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
        totalPriceApi = clientInstance.getTotalPriceApi(applicationUserId!!.toInt(), WebApi.APP_ID)
        totalPriceApi.enqueue(object : Callback<GetTotalPrice> {
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
                    if (data!!.shippingCharges.toString().equals("0")) {
                        textViewSubTotalDeliveryChargesDescription.text = "Free"
                    } else {
                        textViewSubTotalDeliveryChargesDescription.text =
                            "₹".plus(data!!.shippingCharges.toString())
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
        progressBarOrderSummary.indeterminateDrawable.setColorFilter(
            Color.BLACK,
            PorterDuff.Mode.MULTIPLY
        )
    }

    fun setToolBar(name: String) {
        setSupportActionBar(toolBar)
        setStatusBarColor()
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = "Order Summary"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_shape_backarrow_white)
        cp_Logo.hide()
        txtToolbarTitle.hide()
        imgToolbarHome.hide()
        setToolBarColor(imgToolbarHome, txtToolbarTitle, toolbar = toolBar)
    }


    private fun callCartApi() {
        val applicationUserId =
            SharedPreferences.getInstance(this).getStringValue(IntentFlags.APPLICATION_USER_ID)
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        cartListApi = clientInstance.getCartApi(applicationUserId!!.toInt())

        cartListApi.enqueue(object : Callback<GetCartResponse> {
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

    override fun onBackPressed() {
        if (!progressBarOrderSummary.isVisible){
            super.onBackPressed()
        }
    }
    override fun onClick(view: View?) {
        when (view?.id) {
            android.R.id.home -> onBackPressed()
            R.id.imgToolbarHomeLayout -> onBackPressed()
            R.id.materialButtonOrderSummaryPlaceOrder -> {
                this.hideKeyboard(view)

                if (checkboxInstructions.isChecked) {
                    materialButtonOrderSummaryPlaceOrder.isClickable = false

                    materialButtonOrderSummaryPlaceOrder.setBackgroundColor(
                        Color.parseColor(
                            ApplicationThemeUtils.SECONDARY_COLOR
                        )
                    )
                    showProgress()
                    placeOrder()
                } else {
                    materialButtonOrderSummaryPlaceOrder.isClickable = true
                    toast(getString(R.string.message_agree_terms))
                    termsOfUseError = getString(R.string.message_agree_terms)
                    sendMixPanelEvent("buttonClickError")

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

        if (!isNetworkAccessible()) {
            materialButtonOrderSummaryPlaceOrder.isClickable = true
            toast(R.string.oops_no_internet_connection)
            hideProgress()
            return
        }
        val productDetails = ArrayList<PlaceOrderRequest.OrderDetail>()
        productList.forEach {
            productDetails.add(
                PlaceOrderRequest.OrderDetail(
                    if (it.colorId == null) "0" else it.colorId,
                    it.productId.toString(),
                    it.quantity.toString(),
                    if (it.sizeId == null) "0" else it.sizeId,
                    if (it.originalPrice == null) it.discountedPrice.toString() else it.originalPrice.toString()
                )
            )
        }

        val placeOrderRequest = PlaceOrderRequest(
            MRP = data!!.subTotal.toString(),
            AddressId = address!!.id.toString(),
            ApplicationUserId = SharedPreferences.getInstance(this).getStringValue(IntentFlags.APPLICATION_USER_ID)!!,
            Comments = if (TextUtils.isEmpty(etComments.text.toString())) "" else etComments.text.toString(),
            DeliveryCharges = data!!.shippingCharges.toString(),
            OrderDetails = productDetails,
            ProductsDiscount = data!!.productDiscounts.toString(),
            SubTotal = data!!.mrp.toString()
        )


        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        placeOrderApi = clientInstance.placeOrder(placeOrderRequest)
        placeOrderApi.enqueue(object : Callback<PlaceOrderResponse> {
            override fun onFailure(call: Call<PlaceOrderResponse>, t: Throwable) {
                materialButtonOrderSummaryPlaceOrder.isEnabled = true
                materialButtonOrderSummaryPlaceOrder.isClickable = false
                hideProgress()
                toast(getString(R.string.message_something_went_wrong))
            }

            override fun onResponse(
                call: Call<PlaceOrderResponse>,
                response: Response<PlaceOrderResponse>
            ) {
                when {
                    response.body()?.statusCode.equals("10") -> {
                        materialButtonOrderSummaryPlaceOrder.isClickable = false
                        orderId = response.body()!!.orderId

                        SharedPreferences.getInstance(this@OrderSummaryActivity)
                            .setCartCountString(("0").toString())

                        val emailId =
                            SharedPreferences.getInstance(this@OrderSummaryActivity).getProfile()
                                ?.emailId
                        val phoneNumber =
                            SharedPreferences.getInstance(this@OrderSummaryActivity).getProfile()
                                ?.mobileNumber

                        SharedPreferences.getInstance(this@OrderSummaryActivity)
                            .setStringValue(IntentFlags.ORDER_ID, orderId.toString())

                        initiatePayment(
                            address!!.name,
                            emailId!!,
                            phoneNumber!!,
                            totalPrice,
                            response.body()!!.orderNumber
                        )
                        //startActivity<SuccessOrderStatusActivity>(SuccessOrderStatusActivity.PLACE_ORDER_RESPONSE to response.body())
                        placeOrderResponse = response.body()!!
                    }
                    response.body()?.statusCode.equals("30") -> {
                        //                    toast(response.body()!!.message)
                        showAlert(response.body()!!.message)
                        materialButtonOrderSummaryPlaceOrder.isClickable = true
                        hideProgress()
                    }
                    else -> {
                        materialButtonOrderSummaryPlaceOrder.isClickable = true
                        toast(getString(R.string.message_something_went_wrong))
                        hideProgress()
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
        progressBarOrderSummary.show()
        content.hide()
        include2.hide()
        materialButtonOrderSummaryPlaceOrder.hide()
        checkboxInstructions.hide()
        textViewTermsAndConditions.hide()
        this.disableWindowClicks()
    }

    private fun hideProgress() {
        progressBarOrderSummary.hide()
        content.show()
        include2.show()
        materialButtonOrderSummaryPlaceOrder.show()
        checkboxInstructions.show()
        textViewTermsAndConditions.show()
        this.enableWindowClicks()
    }

    private fun fetchAddressList() {
        readAddressFromIntent()
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        fetchAddressListApi = clientInstance.getAddressList(
            SharedPreferences.getInstance(this).getStringValue(
                IntentFlags.APPLICATION_USER_ID
            )!!
        )

        fetchAddressListApi.enqueue(object : Callback<AddressListResponse> {
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
        SharedPreferences.getInstance(this).setStringValue(IntentFlags.ORDER_ID, "")
        materialButtonOrderSummaryPlaceOrder.isClickable = true
        hideProgress()
        if (isNetworkAccessible()) {
            updateTransactionStatus(requestCode, resultCode, data)
        } else {
            showNetworkAlert(requestCode, resultCode, data)
            layoutOrderSummary.hide()
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
//                    toast("Transaction successful")
                    showProgress()
                    updatePaymentStatus(orderId, "2", "Successful")
                    paymentStatus = "Payment SuccessFul"
                    sendMixPanelEvent("successFulPayment")
                } else {
//                    toast("Transaction unsuccessful")
                    showProgress()
                    updatePaymentStatus(orderId, "5", "Unsuccessful")
                    paymentStatus = "Payment UnSuccessFul"
                    sendMixPanelEvent("unSuccessFulPayment")

                    //Failure Transaction
                }

            } else if (resultModel != null && resultModel.error != null) {
                Log.d(tag, "Error response : " + resultModel.error.transactionResponse)
            } else {
                Log.d(tag, "Both objects are null!")
            }
        } else {
//            toast("Transaction unsuccessful")
            showProgress()
            updatePaymentStatus(orderId, "5", "Unsuccessful")
            paymentStatus = "Payment UnSuccessFul"
            sendMixPanelEvent("unSuccessFulPayment")

        }

    }

    private fun updatePaymentStatus(
        orderId: Int,
        paymentStatusId: String,
        transactionStatus: String
    ) {
        if (isNetworkAccessible()) {
            val updateStatusRequest = UpdatePaymentRequest(
                orderId.toString(),
                paymentStatusId
            )
            App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
            val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
            updatePaymentStatusApi = clientInstance.updatePaymentStatus(updateStatusRequest)
            updatePaymentStatusApi.enqueue(object : Callback<UpdatePaymentResponse> {
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
                        materialButtonOrderSummaryPlaceOrder.isClickable = true
                        startActivity<SuccessOrderStatusActivity>(
                            SuccessOrderStatusActivity.PLACE_ORDER_RESPONSE to placeOrderResponse,
                            "TransactionStatus" to transactionStatus
                        )
                    } else {
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


    private fun sendMixPanelEvent(mixPanelTitle: String) {
        val productObject = JSONObject()
        when {
            mixPanelTitle.equals("visitedScreen", true) -> {
                mixPanel.track(IntentFlags.MIXPANEL_VISITED_ORDER_SUMMARY_SCREEN, productObject)
            }
            mixPanelTitle.equals("checkBoxUnchecked", true) -> {
                productObject.put(
                    IntentFlags.MIXPANEL_TERMS_OF_USE_SELECTION_ERROR,
                    termsOfUseError
                )
                mixPanel.track(IntentFlags.MIXPANEL_TERMS_OF_USE_SELECTION_ERROR, productObject)
            }
            mixPanelTitle.equals("buttonClickError", true) -> {
                productObject.put(
                    IntentFlags.MIXPANEL_TERMS_OF_USE_SELECTION_ERROR,
                    termsOfUseError
                )
                mixPanel.track(IntentFlags.MIXPANEL_TERMS_OF_USE_SELECTION_ERROR, productObject)
            }
            mixPanelTitle.equals("successFulPayment", true) -> {
                productObject.put(
                    IntentFlags.MIXPANEL_SUCCESSFUL_PAYMENT_ORDER_SUMMARY,
                    paymentStatus
                )
                mixPanel.track(IntentFlags.MIXPANEL_SUCCESSFUL_PAYMENT_ORDER_SUMMARY, productObject)
            }
            mixPanelTitle.equals("unSuccessFulPayment", true) -> {
                productObject.put(
                    IntentFlags.MIXPANEL_UNSUCCESSFUL_PAYMENT_ORDER_SUMMARY,
                    paymentStatus
                )
                mixPanel.track(
                    IntentFlags.MIXPANEL_UNSUCCESSFUL_PAYMENT_ORDER_SUMMARY,
                    productObject
                )
            }
        }

    }

    override fun onStop() {
        super.onStop()
        cancelTasks()
    }

    private fun cancelTasks() {
        if (::placeOrderApi.isInitialized && placeOrderApi != null) placeOrderApi.cancel()
        if (::fetchAddressListApi.isInitialized && fetchAddressListApi != null) fetchAddressListApi.cancel()
        if (::cartListApi.isInitialized && cartListApi != null) cartListApi.cancel()
        if (::totalPriceApi.isInitialized && totalPriceApi != null) totalPriceApi.cancel()
        if (::updatePaymentStatusApi.isInitialized && updatePaymentStatusApi != null) updatePaymentStatusApi.cancel()
    }

    override fun onDestroy() {
        mixPanel.flush()
        super.onDestroy()
    }

}
