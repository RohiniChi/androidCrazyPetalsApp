package com.plugable.mcommerceapp.cpmvp1.ui.activities

import ServiceGenerator
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.callbacks.EventListener
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.*
import com.plugable.mcommerceapp.cpmvp1.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.cpmvp1.ui.adapters.CartAdapter
import com.plugable.mcommerceapp.cpmvp1.ui.adapters.CartItemActionListener
import com.plugable.mcommerceapp.cpmvp1.utils.application.App
import com.plugable.mcommerceapp.cpmvp1.utils.constants.IntentFlags
import com.plugable.mcommerceapp.cpmvp1.utils.constants.SharedPreferences.SHARED_PREFERENCES_CART_COUNT
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi
import com.plugable.mcommerceapp.cpmvp1.utils.extension.hide
import com.plugable.mcommerceapp.cpmvp1.utils.extension.setStatusBarColor
import com.plugable.mcommerceapp.cpmvp1.utils.extension.setToolBarColor
import com.plugable.mcommerceapp.cpmvp1.utils.extension.show
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.cpmvp1.utils.util.isNetworkAccessible
import kotlinx.android.synthetic.main.layout_checkout_button.*
import kotlinx.android.synthetic.main.layout_empty_cart.*
import kotlinx.android.synthetic.main.layout_sub_total_amount.*
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.activity_products.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*
import kotlinx.android.synthetic.main.layout_network_condition.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
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
        with(recyclerViewCart.adapter!!) {
            removeItemFromCart(getCartResponseData.productId)

            val index = productList.indexOf(getCartResponseData)
            if (index > -1) {
                productList.remove(getCartResponseData)
                notifyItemRemoved(index)
            }
            toast("Item removed successfully")
        }
        updateData()
    }

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    var productList = ArrayList<GetCartResponse.Data>()
    lateinit var cartAdapter: CartAdapter


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
        val call = clientInstance.getTotalPriceApi(applicationUserId!!.toInt(), WebApi.APP_ID)
        call.enqueue(object : Callback<GetTotalPrice> {
            override fun onFailure(call: Call<GetTotalPrice>, t: Throwable) {
                toast(getString(R.string.message_something_went_wrong))
            }

            override fun onResponse(call: Call<GetTotalPrice>, response: Response<GetTotalPrice>) {
                if (response.body()?.statusCode.equals("10")) {
                    textViewSubTotalDescription.text =
                        "₹".plus(response.body()!!.data.subTotal.toString())
                    textViewSubTotalDiscountDescription.text =
                        "₹".plus(response.body()!!.data.productDiscounts.toString())

                    if (response.body()!!.data.deliveryCharges.toString().equals("0")) {
                        textViewSubTotalDeliveryChargesDescription.text = "Free"
                    } else {
                        textViewSubTotalDeliveryChargesDescription.text =
                            "₹".plus(response.body()!!.data.deliveryCharges.toString())
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
        cartItemsShimmerLayout.showShimmer(true)
        recyclerViewCart.hide()
        constraintLayoutBottomSheet.hide()
        val applicationUserId =
            SharedPreferences.getInstance(this).getStringValue(IntentFlags.APPLICATION_USER_ID)
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        val callback = clientInstance.getCartApi(applicationUserId!!.toInt())

        callback.enqueue(object : Callback<GetCartResponse> {
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
                if (response.body()?.statusCode.equals("10")) {
                    cartItemsShimmerLayout.hideShimmer()
                    recyclerViewCart.show()
                    constraintLayoutBottomSheet.show()

                    if (response.body()!!.data.isNotEmpty()) {

                        progressBarCartList.hide()
                        productList.clear()
                        response.body()!!.data.forEach {
                            productList.add(it)
                        }
                        cartAdapter.notifyDataSetChanged()
                           SharedPreferences.getInstance(this@CartActivity).setStringValue(
                               SHARED_PREFERENCES_CART_COUNT,
                               productList.size.toString()
                           )
                        progressBarCartList.hide()

                    }else{
                        SharedPreferences.getInstance(this@CartActivity).setStringValue(
                            SHARED_PREFERENCES_CART_COUNT,
                            "0"
                        )
                    }
                    updateData()
                } else {
                    progressBarCartList.hide()
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
        initializeViews()
        attemptApiCall()
        attemptTotalPriceApi()
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
        cartAdapter = CartAdapter(productList, this, this, this)
        recyclerViewCart.adapter = cartAdapter

        textViewViewPriceDetails.setOnClickListener(this)
        imgToolbarHomeLayout.setOnClickListener(this)
        materialButtonCheckout.setOnClickListener(this)
        imageViewSubTotalCollapse.setOnClickListener(this)

        btnTryAgain.setOnClickListener(this)
        buttonBrowseMore.setOnClickListener(this)
        materialButtonCheckout.isClickable = true

        bottomSheetBehavior = BottomSheetBehavior.from(constraintLayoutBottomSheet)
        bottomSheetBehavior.setBottomSheetCallback( object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {

            }

            override fun onStateChanged(p0: View, p1: Int) {
                if (p1 == BottomSheetBehavior.STATE_DRAGGING) {
                    collapseBottomSheet()
                }
            }
        })

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
        supportActionBar?.setDisplayShowTitleEnabled(false)
        txtToolbarTitle.text = "Cart"
        cp_Logo.hide()
        imgToolbarHome.setImageResource(R.drawable.ic_shape_backarrow)
        setToolBarColor(imgToolbarHome, txtToolbarTitle, toolbar = toolBar)
    }
    override fun onBackPressed() {
        if (intent.hasExtra(IntentFlags.REDIRECT_FROM) && intent.getStringExtra(IntentFlags.REDIRECT_FROM) == IntentFlags.WISHLIST) {
            startActivity<DashboardActivity>(IntentFlags.FRAGMENT_TO_BE_LOADED to R.id.nav_wishList)
            finish()
        }
        else if (intent.hasExtra(IntentFlags.REDIRECT_FROM) && intent.getStringExtra(IntentFlags.REDIRECT_FROM) == IntentFlags.ORDER_DETAIL){
            startActivity<DashboardActivity>(IntentFlags.FRAGMENT_TO_BE_LOADED to R.id.nav_my_order)
            finish()
        }
        else if (intent.hasExtra(IntentFlags.REDIRECT_FROM) && intent.getStringExtra(IntentFlags.REDIRECT_FROM) == IntentFlags.HOME_FRAGMENT){
            startActivity<DashboardActivity>(IntentFlags.FRAGMENT_TO_BE_LOADED to R.id.nav_home)
            finish()
        }
        else {
            finish()
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgToolbarHomeLayout -> onBackPressed()
            R.id.textViewViewPriceDetails -> if (!layout_subtotal.isVisible) {
                expandBottomSheet()
//                nestedScrollViewCart.post { nestedScrollViewCart.fullScroll(View.FOCUS_DOWN) }
            } else {
                collapseBottomSheet()
            }
            R.id.materialButtonCheckout -> {
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
        val call = clientInstance.getUpdatedQuantity(productQuantity)
        call.enqueue(object : Callback<ResponseUpdateQuantity> {
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
                        toast(response.body()!!.message)
                    }
                    else if (response.body()?.statusCode.equals("30")){
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
        progressBarCartList.show()
        val applicationUserId =
            SharedPreferences.getInstance(this).getStringValue(IntentFlags.APPLICATION_USER_ID)
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        val callback = clientInstance.removeFromCart(
            RemoveFromCartRequest(
                applicationUserId!!,
                productId.toString()
            )
        )

        callback.enqueue(object : Callback<ResponseAddToCart> {
            override fun onFailure(call: Call<ResponseAddToCart>, t: Throwable) {
                toast(getString(R.string.message_something_went_wrong))
                progressBarCartList.hide()

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
                    progressBarCartList.hide()

                }

            }

        })

    }
}