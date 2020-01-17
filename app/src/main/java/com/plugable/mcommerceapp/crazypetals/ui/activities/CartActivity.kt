package com.plugable.mcommerceapp.crazypetals.ui.activities

import ServiceGenerator
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.callbacks.EventListener
import com.plugable.mcommerceapp.crazypetals.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.*
import com.plugable.mcommerceapp.crazypetals.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.crazypetals.ui.adapters.CartAdapter
import com.plugable.mcommerceapp.crazypetals.ui.adapters.CartItemActionListener
import com.plugable.mcommerceapp.crazypetals.utils.application.App
import com.plugable.mcommerceapp.crazypetals.utils.constants.IntentFlags
import com.plugable.mcommerceapp.crazypetals.utils.constants.WebApi
import com.plugable.mcommerceapp.crazypetals.utils.extension.hide
import com.plugable.mcommerceapp.crazypetals.utils.extension.setStatusBarColor
import com.plugable.mcommerceapp.crazypetals.utils.extension.setToolBarColor
import com.plugable.mcommerceapp.crazypetals.utils.extension.show
import com.plugable.mcommerceapp.crazypetals.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.crazypetals.utils.util.isNetworkAccessible
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.layout_checkout_button.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*
import kotlinx.android.synthetic.main.layout_empty_cart.*
import kotlinx.android.synthetic.main.layout_network_condition.*
import kotlinx.android.synthetic.main.layout_sub_total_amount.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * [CartActivity] is used to show items available to cart.
 *
 */
class CartActivity : BaseActivity(), View.OnClickListener, EventListener,
    CartItemActionListener {
    override fun onIncremented(getCartResponseData: GetCartResponse.Data) {
        if (!isNetworkAccessible()) {
            toast(R.string.oops_no_internet_connection)
            return
        }

        getCartResponseData.quantity += 1
        with(recyclerViewCart.adapter!!) {
            val index = productList.indexOf(getCartResponseData)
            if (index > -1) {
                callProductQuantityApi(
                    getCartResponseData.quantity.toString(),
                    productList[index].productId
                )
                notifyItemChanged(index)
            }
        }
        updateData()
    }

    override fun onDecremented(getCartResponseData: GetCartResponse.Data) {

        if (!isNetworkAccessible()) {

            toast(R.string.oops_no_internet_connection)
            return
        }
        getCartResponseData.quantity -= 1

        with(recyclerViewCart.adapter!!) {
            val index = productList.indexOf(getCartResponseData)
            if (index > -1) {
                callProductQuantityApi(
                    getCartResponseData.quantity.toString(),
                    productList[index].productId
                )
                notifyItemChanged(index)
            }
        }

        updateData()
    }

    override fun onItemRemoved(getCartResponseData: GetCartResponse.Data) {
        if (!isNetworkAccessible()) {

            toast(R.string.oops_no_internet_connection)
            return
        }

        with(recyclerViewCart.adapter!!) {
            removeItemFromCart(getCartResponseData.productId)

            val index = productList.indexOf(getCartResponseData)
            if (index > -1) {
                productList.remove(getCartResponseData)
                notifyItemRemoved(index)
            }
            decrementCartCount()
            toast("Item removed successfully")
        }
        updateData()
    }

    private lateinit var removeItemFromCartApi: Call<ResponseAddToCart>
    private lateinit var productQuantityApi: Call<ResponseUpdateQuantity>
    private lateinit var cartListApi: Call<GetCartResponse>
    private lateinit var totalPriceApi: Call<GetTotalPrice>
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    var productList = ArrayList<GetCartResponse.Data>()
    lateinit var cartAdapter: CartAdapter
    private lateinit var mixPanel: MixpanelAPI


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        initializeTheme()
        initializeViews()
        attemptApiCall()
        attemptTotalPriceApi()
    }

    private fun attemptTotalPriceApi() {
        if (isNetworkAccessible()) {
            callTotalPriceApi()
        } else {
//            toast(getString(R.string.check_internet_connection))
//            showNetworkCondition()
        }
    }

    private fun callTotalPriceApi() {
        val applicationUserId =
            SharedPreferences.getInstance(this).getStringValue(IntentFlags.APPLICATION_USER_ID)
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        totalPriceApi = clientInstance.getTotalPriceApi(applicationUserId!!.toInt(), WebApi.APP_ID)
        totalPriceApi.enqueue(object : Callback<GetTotalPrice> {
            override fun onFailure(call: Call<GetTotalPrice>, t: Throwable) {
                toast(getString(R.string.message_something_went_wrong))
            }

            override fun onResponse(call: Call<GetTotalPrice>, response: Response<GetTotalPrice>) {
                if (response.body()?.statusCode.equals("10")) {
                    textViewSubTotalDescription.text =
                        "₹".plus(response.body()!!.data.subTotal.toString())
                    textViewSubTotalDiscountDescription.text =
                        "₹".plus(response.body()!!.data.productDiscounts.toString())

                    if (response.body()!!.data.shippingCharges.toString().equals("0")) {
                        textViewSubTotalDeliveryChargesDescription.text = "Free"
                    } else {
                        textViewSubTotalDeliveryChargesDescription.text =
                            "₹".plus(response.body()!!.data.shippingCharges.toString())
                    }

                    textViewSubTotalMrpDescription.text =
                        "₹".plus(response.body()!!.data.mrp.toString())
                    textViewCheckoutButtonSubtotal.text =
                        "Total".plus(" ").plus("₹").plus(response.body()!!.data.subTotal.toString())
                }
            }

        })
    }

    private fun attemptApiCall() {
        if (isNetworkAccessible()) {
            callCartApi()
        } else {
//            toast(getString(R.string.check_internet_connection))
            showNetworkCondition()
        }
    }

    private fun callCartApi() {
        showProgressBar()
        //cartItemsShimmerLayout.showShimmer(true)
        recyclerViewCart.hide()
        constraintLayoutBottomSheet.hide()
        val applicationUserId =
            SharedPreferences.getInstance(this).getStringValue(IntentFlags.APPLICATION_USER_ID)
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        cartListApi = clientInstance.getCartApi(applicationUserId!!.toInt())

        cartListApi.enqueue(object : Callback<GetCartResponse> {
            override fun onFailure(call: Call<GetCartResponse>, t: Throwable) {
                cartItemsShimmerLayout.hideShimmer()
                recyclerViewCart.show()
                constraintLayoutBottomSheet.show()

                toast(getString(R.string.message_something_went_wrong))
            }

            override fun onResponse(
                call: Call<GetCartResponse>,
                response: Response<GetCartResponse>
            ) {
                hideProgressBar()
                if (response.body()?.statusCode.equals("10")) {
                    cartItemsShimmerLayout.hideShimmer()
                    recyclerViewCart.show()
                    constraintLayoutBottomSheet.show()

                    if (response.body()!!.data.isNotEmpty()) {
                        productList.clear()
                        response.body()!!.data.forEach {
                            productList.add(it)
                        }
                        cartAdapter.notifyDataSetChanged()
                        SharedPreferences.getInstance(this@CartActivity)
                            .setCartCountString(productList.size.toString())

                    } else {
                        SharedPreferences.getInstance(this@CartActivity)
                            .setCartCountString("0")
                    }
                    updateData()
                } else {
                    recyclerViewCart.show()
                    constraintLayoutBottomSheet.show()

                    cartItemsShimmerLayout.hideShimmer()
//                    toast(getString(R.string.message_something_went_wrong))
                }

            }

        })

    }

    private fun hideData() {
        layout_cart_list.hide()
        constraintLayoutBottomSheet.hide()
        layout_empty_cart.show()
    }

    override fun onResume() {
        materialButtonCheckout.isClickable = true

        super.onResume()
    }

    private fun showData() {
        layout_cart_list.show()
        constraintLayoutBottomSheet.show()
        layout_empty_cart.hide()
    }

    override fun showNetworkCondition() {
        layout_cart_list.hide()
        constraintLayoutBottomSheet.hide()
        layout_empty_cart.hide()

        layoutNetworkCondition.show()
        stopShimmerView()
    }


    override fun hideNetworkCondition() {
        attemptApiCall()
        attemptTotalPriceApi()
        layout_cart_list.show()
        constraintLayoutBottomSheet.show()
        layout_empty_cart.hide()
        layoutNetworkCondition.hide()

    }

    private fun initializeViews() {
        mixPanel = MixpanelAPI.getInstance(this, resources.getString(R.string.mix_panel_token))

        cartAdapter = CartAdapter(productList, this, this, this)
        recyclerViewCart.adapter = cartAdapter

        textViewCheckoutButtonSubtotal.setOnClickListener(this)
        textViewViewPriceDetails.setOnClickListener(this)
        imgToolbarHomeLayout.setOnClickListener(this)
        materialButtonCheckout.setOnClickListener(this)
        imageViewSubTotalCollapse.setOnClickListener(this)

        btnTryAgain.setOnClickListener(this)
        buttonBrowseMore.setOnClickListener(this)
        materialButtonCheckout.isClickable = true

        bottomSheetBehavior = BottomSheetBehavior.from(constraintLayoutBottomSheet)
        bottomSheetBehavior.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {

            }

            override fun onStateChanged(p0: View, p1: Int) {
                if (p1 == BottomSheetBehavior.STATE_DRAGGING) {
                    collapseBottomSheet()
                }
            }
        })
        sendMixPanelEvent()
    }

    private fun sendMixPanelEvent() {
        val productObject = JSONObject()
        mixPanel.track(IntentFlags.MIXPANEL_VISITED_CART_LIST_SCREEN, productObject)
    }

    private fun updateData() {
        if (productList.size == 0) {
            textViewTotalItemsTitle.hide()
            hideData()
        } else {
            showData()
            textViewTotalItemsTitle.show()
            textViewTotalItemsTitle.text =
                String.format(getString(R.string.title_total_items), productList.size)
        }
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
        materialButtonCheckout.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        btnTryAgain.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        buttonBrowseMore.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        textViewViewPriceDetails.setTextColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))

        progressBarCartList.indeterminateDrawable.setColorFilter(
            Color.BLACK,
            PorterDuff.Mode.MULTIPLY
        )

    }

    override fun setToolBar(name: String) {
        setSupportActionBar(toolBar)
        setStatusBarColor()
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = "Cart"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_shape_backarrow_white)
        cp_Logo.hide()
        txtToolbarTitle.hide()
        imgToolbarHome.hide()
        setToolBarColor(imgToolbarHome, txtToolbarTitle, toolbar = toolBar)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgToolbarHomeLayout -> onBackPressed()

            R.id.textViewCheckoutButtonSubtotal -> showHideBootomSheet()
            R.id.textViewViewPriceDetails -> showHideBootomSheet()
            R.id.materialButtonCheckout -> {
                if (isNetworkAccessible()) {
                    materialButtonCheckout.isClickable = false

                    var isOutOfStock = false
                    for (product in productList) {
                        if (!product.isAvailable) {
                            isOutOfStock = true
                        }
                    }

                    if (isOutOfStock) {
                        materialButtonCheckout.isClickable = true

                        Toast.makeText(
                            this@CartActivity,
                            "Please remove out of stock product",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        materialButtonCheckout.isClickable = false
                        startActivity<DeliveryAddressActivity>()
                    }
                } else {
                    materialButtonCheckout.isClickable = true
                    toast(getString(R.string.oops_no_internet_connection))
                }
            }

            R.id.buttonBrowseMore -> {
                startActivity<DashboardActivity>()
                ActivityCompat.finishAffinity(this)
            }

            R.id.imageViewSubTotalCollapse -> {
                collapseBottomSheet()
            }

            R.id.btnTryAgain -> {
                if (isNetworkAccessible()) {
                    hideNetworkCondition()
                } else {
                    showNetworkCondition()
                }
            }
        }
    }

    fun showHideBootomSheet() {
        if (!layout_subtotal.isVisible) {
            expandBottomSheet()
        } else {
            collapseBottomSheet()
        }
    }

    private fun expandBottomSheet() {
        layout_subtotal.show()
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onItemClickListener(position: Int) {
    }


    private fun callProductQuantityApi(subItemType: String, productId: Int) {

        val applicationUserId =
            SharedPreferences.getInstance(this).getStringValue(IntentFlags.APPLICATION_USER_ID)

        val productQuantity = RequestUpdateQuantity(
            WebApi.APP_ID,
            applicationUserId!!.toInt(),
            productId,
            subItemType
        )
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        productQuantityApi = clientInstance.getUpdatedQuantity(productQuantity)
        productQuantityApi.enqueue(object : Callback<ResponseUpdateQuantity> {
            override fun onFailure(call: Call<ResponseUpdateQuantity>, t: Throwable) {
                toast(getString(R.string.message_something_went_wrong))
            }

            override fun onResponse(
                call: Call<ResponseUpdateQuantity>,
                response: Response<ResponseUpdateQuantity>
            ) {
                if (response.isSuccessful) {
                    if (response.body()?.statusCode.equals("10")) {
                        callTotalPriceApi()
//                        toast(response.body()!!.message)
                    } else if (response.body()?.statusCode.equals("30")) {
                        toast(response.body()!!.message)
                    }
                }
            }

        })
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {

                val outRect = Rect()
                constraintLayoutBottomSheet.getGlobalVisibleRect(outRect)

                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    collapseBottomSheet()
                }

            }
        }

        return super.dispatchTouchEvent(event)
    }

    private fun collapseBottomSheet() {
        if (layout_subtotal.isVisible) layout_subtotal.hide()
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun removeItemFromCart(productId: Int) {
        showProgressBar()
        val applicationUserId =
            SharedPreferences.getInstance(this).getStringValue(IntentFlags.APPLICATION_USER_ID)
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        removeItemFromCartApi = clientInstance.removeFromCart(
            RemoveFromCartRequest(
                applicationUserId!!,
                productId.toString()
            )
        )

        removeItemFromCartApi.enqueue(object : Callback<ResponseAddToCart> {
            override fun onFailure(call: Call<ResponseAddToCart>, t: Throwable) {
                toast(getString(R.string.message_something_went_wrong))
                hideProgressBar()

            }

            override fun onResponse(
                call: Call<ResponseAddToCart>,
                response: Response<ResponseAddToCart>
            ) {
                if (response.body()?.statusCode.equals("10")) {
                    toast(response.body()!!.message)
                    callTotalPriceApi()
                    callCartApi()
                } else {
//                    toast(getString(R.string.message_something_went_wrong))
                    hideProgressBar()

                }

            }
        })
    }


    fun showProgressBar() {
        progressBarCartList.show()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    fun hideProgressBar() {
        progressBarCartList.hide()
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun cancelTasks() {
        if (::productQuantityApi.isInitialized && productQuantityApi != null) productQuantityApi.cancel()
        if (::totalPriceApi.isInitialized && totalPriceApi != null) totalPriceApi.cancel()
        if (::cartListApi.isInitialized && cartListApi != null) cartListApi.cancel()
        if (::removeItemFromCartApi.isInitialized && removeItemFromCartApi != null) removeItemFromCartApi.cancel()
    }

    override fun onDestroy() {
        mixPanel.flush()
        cancelTasks()
        super.onDestroy()
    }

}