package com.plugable.mcommerceapp.cpmvp1.ui.activities

import ServiceGenerator
import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import com.plugable.mcommerceapp.cpmvp1.utils.util.capitalizeAll
import com.plugable.mcommerceapp.cpmvp1.utils.util.isNetworkAccessible
import kotlinx.android.synthetic.main.layout_sub_total_amount.*
import kotlinx.android.synthetic.main.activity_order_summary.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*
import org.jetbrains.anko.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * [OrderSummaryActivity] is used to show summary of its orders
 *
 */
class OrderSummaryActivity : AppCompatActivity(), View.OnClickListener, EventListener {


    private val productList = ArrayList<GetCartResponse.Data>()
    var ordersList = ArrayList<OrderItems.Data.Items>()
    var address: AddressListResponse.Data? = null
    var addressId: Int? = null
    lateinit var ordersAdapter: OrdersAdapter
    var data: GetTotalPrice.Data? = null


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
        materialButtonOrderSummaryPlaceOrder.isClickable = false

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
//                materialButtonOrderSummaryPlaceOrder.isClickable = false

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
        textViewDeliveryAddressPersonName.text = String.format("%s", address?.name)
        textViewDeliveryAddressCompleteAddress.text = String.format(
            "%s,%s, %s - %s",
            address?.address,
            address?.landmark,
            address?.locality,
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
                    textViewSubTotalDescription.text =
                        "₹".plus(data!!.subTotal.toString())
                    textViewSubTotalDiscountDescription.text =
                        "₹".plus(data!!.productDiscounts.toString())
                    if (data!!.deliveryCharges.toString().equals("0")){
                        textViewSubTotalDeliveryChargesDescription.text ="Free"
                    }else{
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
        supportActionBar?.setDisplayShowTitleEnabled(false)
        txtToolbarTitle.text = "Order Summary"
        imgToolbarHome.setImageResource(R.drawable.ic_shape_backarrow)
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
        materialButtonOrderSummaryPlaceOrder.isClickable = false
        if (!isNetworkAccessible()) {
            toast(R.string.oops_no_internet_connection)
            return
        }
        val productDetails = ArrayList<PlaceOrderRequest.OrderDetail>()
        productList.forEach {
            productDetails.add(
                PlaceOrderRequest.OrderDetail(
                    "1",
                    it.productId.toString(),
                    it.quantity.toString(),
                    "1",
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
                progressBar.hide()
                toast(getString(R.string.message_something_went_wrong))
            }

            override fun onResponse(
                call: Call<PlaceOrderResponse>,
                response: Response<PlaceOrderResponse>
            ) {
                if (response.body()?.statusCode.equals("10")) {
                    progressBar.hide()
                    materialButtonOrderSummaryPlaceOrder.isClickable = false

                    startActivity<SuccessOrderStatusActivity>(SuccessOrderStatusActivity.PLACE_ORDER_RESPONSE to response.body())
                } else if (response.body()?.statusCode.equals("30")){
                    progressBar.hide()
//                    toast(response.body()!!.message)
                    showAlert()
                    materialButtonOrderSummaryPlaceOrder.isClickable = true
                }
                else{
                    materialButtonOrderSummaryPlaceOrder.isClickable = true
                    progressBar.hide()
                    toast(getString(R.string.message_something_went_wrong))
                }
            }
        })
    }

    private fun showAlert() {
        alert(getString(R.string.alert_message_place_order)) {
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
}
