package com.plugable.mcommerceapp.cpmvp1.ui.activities

import ServiceGenerator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.callbacks.EventListener
import com.plugable.mcommerceapp.cpmvp1.callbacks.OnButtonClickListener
import com.plugable.mcommerceapp.cpmvp1.callbacks.OnFavoriteListener
import com.plugable.mcommerceapp.cpmvp1.callbacks.SetOnBottomReachedListener
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.cpmvp1.mcommerce.db.AppDatabase
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.ProductDetail
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.Products
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.RequestAddToCart
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.ResponseAddToCart
import com.plugable.mcommerceapp.cpmvp1.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.cpmvp1.registration.LoginActivity
import com.plugable.mcommerceapp.cpmvp1.ui.activities.ProductDetailActivity.Companion.productDetailActivity
import com.plugable.mcommerceapp.cpmvp1.ui.adapters.ColorAdapterMVP1
import com.plugable.mcommerceapp.cpmvp1.ui.adapters.ImageSliderAdapter
import com.plugable.mcommerceapp.cpmvp1.ui.adapters.ProductListAdapter
import com.plugable.mcommerceapp.cpmvp1.ui.adapters.SizeAdapter
import com.plugable.mcommerceapp.cpmvp1.utils.application.App
import com.plugable.mcommerceapp.cpmvp1.utils.constants.IntentFlags
import com.plugable.mcommerceapp.cpmvp1.utils.extension.hide
import com.plugable.mcommerceapp.cpmvp1.utils.extension.show
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.cpmvp1.utils.util.isNetworkAccessible
import com.plugable.mcommerceapp.cpmvp1.utils.validation.strikeThroughText
import com.plugable.mcommerceapp.ui.fragments.DescriptionFragment
import com.plugable.mcommerceapp.ui.fragments.InstructionFragment
import com.plugable.mcommerceapp.ui.fragments.SpecificationFragment
import kotlinx.android.synthetic.main.activity_product_detail.*
import kotlinx.android.synthetic.main.layout_network_condition.*
import kotlinx.android.synthetic.main.layout_product_details.*
import org.jetbrains.anko.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.nikartm.support.BadgePosition

/**
 * [productDetailActivity] is used to show detail of a particular product
 *
 */
class ProductDetailActivity : BaseActivity(), EventListener, OnFavoriteListener,
    SetOnBottomReachedListener, OnButtonClickListener {

    override fun onBottomReached(position: Int) {

    }


    private var productListId: Int = 0
    private var productDetail = ProductDetail.Data()
    private lateinit var callback: Call<Products>
    //    private lateinit var mixPanel: MixpanelAPI
    private var productImages = ArrayList<ProductDetail.Data.Image>()
    private lateinit var product: Products.Data.ProductDetails
    private lateinit var eventClickListener: EventListener
    private lateinit var onButtonClickListener: OnButtonClickListener
    //    private lateinit var onBottomReachedListener: SetOnBottomReachedListener
    private var skipCount = 0
    private var takeCount = 4
    private var categoryId = 0
    private var colorAdapter: ColorAdapterMVP1? = null
    private var sizeAdapter: SizeAdapter? = null
    var productList = ArrayList<Products.Data.ProductDetails>()
    lateinit var productListAdapter: ProductListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        colorSelectedId = 0
        sizeSelectedId = 0

        initializeViews()
        initializeTheme()
        attemptApiCall()
        attemptListApiCall()
    }

    private fun attemptApiCall() {
        if (isNetworkAccessible()) {
            callProductDetailApi(product.id)
        } else {
            showNetworkCondition()
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
        buttonPlaceOrder.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        btnTryAgain.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))

        with(buttonAddToCart) {
            setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
            setTextColor(Color.parseColor(ApplicationThemeUtils.TOOL_BAR_COLOR))
            strokeColor =
                ColorStateList.valueOf(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        }

        dotsIndicator.setDotIndicatorColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        dotsIndicator.setStrokeDotsIndicatorColor(Color.parseColor(ApplicationThemeUtils.PRIMARY_COLOR))

        dotsIndicator.setViewPager(viewPagerImage)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeViews() {

        layoutBackArrow.setOnClickListener(this)
        layoutCartIcon.setOnClickListener(this)

        buttonPlaceOrder.setOnClickListener(this)
        buttonAddToCart.setOnClickListener(this)
        btnTryAgain.setOnClickListener(this)

        imageViewFavorite.setOnClickListener(this)

//        mixPanel = MixpanelAPI.getInstance(this, resources.getString(R.string.mix_panel_token))

        product = intent.getParcelableExtra(IntentFlags.PRODUCT_MODEL)!!

        categoryId = intent.getIntExtra(IntentFlags.CATEGORY_ID, 0)

        productDetailActivity = this

        viewPagerImage.adapter = ImageSliderAdapter(this, productImages)


        eventClickListener = this
        onButtonClickListener = this
//        onBottomReachedListener = this

        productListAdapter =
            ProductListAdapter(productList, this, eventClickListener, onButtonClickListener, this)
        val gridLayoutManager = GridLayoutManager(this, 1)
        gridLayoutManager.orientation =
            RecyclerView.HORIZONTAL // set Horizontal Orientation

        recyclerViewAlsoRecommended.layoutManager = gridLayoutManager
        recyclerViewAlsoRecommended.itemAnimator = DefaultItemAnimator()
        recyclerViewAlsoRecommended.adapter = productListAdapter

        buttonPlaceOrder.isClickable = true
        buttonAddToCart.isClickable = true
        layoutCartIcon.isClickable = true

        hideUiComponents()
    }

    private fun setCartBadge() {
        val cartCount = SharedPreferences.getInstance(this).getCartCountString()!!

        if (cartCount.toInt() == 0) {
            cartIcon.clearBadge()
        } else {
            cartIcon.setBadgeValue(cartCount.toInt())
                .setBadgeBackground(resources.getDrawable(R.drawable.badge_background_circle))
                .setBadgeTextStyle(Typeface.NORMAL)
                .setBadgePosition(BadgePosition.TOP_RIGHT)
                .setFixedBadgeRadius(25.toFloat()).badgeTextSize = 8.toFloat()
        }
//        cartIcon.visibleBadge(cartCount!!.toInt()>0)

    }

    override fun onResume() {
        super.onResume()
        setCartBadge()
        layoutCartIcon.isClickable = true
        buttonPlaceOrder.isClickable = true
    }


    private fun callProductDetailApi(productId: Int) {
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        val call = clientInstance.productDetailModel(productId)
        call.enqueue(object : Callback<ProductDetail> {
            override fun onFailure(call: Call<ProductDetail>?, t: Throwable?) {
                showNetworkCondition()
            }

            override fun onResponse(
                call: Call<ProductDetail>?,
                response: Response<ProductDetail>?
            ) {
                if (response?.body()?.statusCode.equals("10")) {
                    showUiComponents()
                    productImages.addAll(response?.body()!!.data.images!!)
                    viewPagerImage.adapter?.notifyDataSetChanged()





                    NUM_PAGES = productImages.size
                    if (NUM_PAGES == 1) {
                        dotsIndicator.hide()
                    } else {
                        dotsIndicator.show()
                    }



                    if (AppDatabase.getDatabase(this@ProductDetailActivity).productListDao().getSingleWishListProduct(
                            productId
                        ) != null
                    ) {
                        product.isFavorite = true
                        imageViewFavorite.isSelected = true
                    }
                    productDetail = response.body()!!.data
                    loadDataToViews(response.body()!!)


                    product.discountedPrice = productDetail.discountedPrice
                    product.originalPrice = productDetail.originalPrice


                }

            }

        })

    }

    var pageCount = 1
    private fun loadDataToViews(productDetail: ProductDetail) {
        txtProductName.text = productDetail.data.name

        when {
            TextUtils.isEmpty(productDetail.data.brand) -> {
                textViewBrand.hide()
                valueBrand.hide()
            }
            productDetail.data.brand.equals(null) -> {
                textViewBrand.hide()
                valueBrand.hide()
            }
            else -> valueBrand.text = productDetail.data.brand
        }

        when {
            TextUtils.isEmpty(productDetail.data.quantity) -> {
                textViewQuantity.hide()
                textViewTitleQuantity.hide()
            }
            productDetail.data.quantity.equals(null) -> {
                textViewQuantity.hide()
                textViewTitleQuantity.hide()
            }
            else -> textViewQuantity.text =
                productDetail.data.quantity.plus(productDetail.data.unitName)
        }

        textViewDiscountedPrice.text =
            String.format(
                "${ApplicationThemeUtils.CURRENCY_SYMBOL}%d",
                productDetail.data.discountedPrice
            )

        when {
            productDetail.data.originalPrice.equals(null) -> textViewActualPrice.hide()
            productDetail.data.originalPrice == 0 -> textViewActualPrice.hide()
            else -> {
                textViewActualPrice.text =
                    String.format(
                        "${ApplicationThemeUtils.CURRENCY_SYMBOL}%d",
                        productDetail.data.originalPrice
                    )
                textViewActualPrice.strikeThroughText()
            }
        }
        with(textViewAvailability) {
            text = if (productDetail.data.isAvailable!!) {
                buttonPlaceOrder.isClickable = true
                buttonAddToCart.isClickable = true
                buttonPlaceOrder.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
                buttonAddToCart.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
                this.setTextColor(resources.getColor(R.color.offerGreenColor))
                context.getString(R.string.title_in_stock)
            } else {
                buttonPlaceOrder.isClickable = false
                buttonAddToCart.isClickable = false
                buttonPlaceOrder.setBackgroundColor(Color.GRAY)
                buttonAddToCart.setBackgroundColor(Color.GRAY)

                this.setTextColor(resources.getColor(android.R.color.holo_red_light))
                context.getString(R.string.title_out_of_stock)
            }
        }

        when {
            productDetail.data.colorList!!.isEmpty() -> {
                textViewColour.hide()
                recyclerViewColor.hide()
            }
            productDetail.data.colorList!!.equals(null) -> {
                textViewColour.hide()
                recyclerViewColor.hide()
            }
            else -> {
                val colorArray = productDetail.data.colorList
                recyclerViewColor.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true)

                colorAdapter = ColorAdapterMVP1(colorArray!!, this)
                recyclerViewColor.adapter = colorAdapter

            }
        }

        when {
            productDetail.data.sizeList!!.isEmpty() -> {
                textViewSize.hide()
                recyclerViewSizeCount.hide()

            }
            productDetail.data.sizeList!!.equals(null) -> {
                textViewSize.hide()
                recyclerViewSizeCount.hide()
            }
            else -> {
                val sizeArray = productDetail.data.sizeList
                recyclerViewSizeCount.setHasFixedSize(true)
                recyclerViewSizeCount.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true)

                sizeAdapter = SizeAdapter(sizeArray!!, this)
                recyclerViewSizeCount.adapter = sizeAdapter
            }
        }
        // webViewDescription.loadData(productDetail.data.description, "text/html", "UTF-8")

        tabLayoutProductDetail.addTab(tabLayoutProductDetail.newTab().setText(R.string.title_description))

        if (!productDetail.data.length.isNullOrEmpty() ||
            !productDetail.data.height.isNullOrEmpty() ||
            !productDetail.data.width.isNullOrEmpty() ||
            !productDetail.data.weight.isNullOrEmpty() ||
            !productDetail.data.materialType.isNullOrEmpty()

        ) {
            pageCount++
            tabLayoutProductDetail.addTab(tabLayoutProductDetail.newTab().setText(R.string.title_specification))
        }
        if (!productDetail.data.deliveryTime.isNullOrEmpty() ||
            !productDetail.data.precautions.isNullOrEmpty()
        ) {
            pageCount++
            tabLayoutProductDetail.addTab(tabLayoutProductDetail.newTab().setText(R.string.title_instruction))
        }

        viewPagerProductDetail.adapter =
            ViewPagerAdapterProductDetail(supportFragmentManager)
        tabLayoutProductDetail.setupWithViewPager(viewPagerProductDetail)
        //viewPagerProductDetail.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);


        viewPagerProductDetail!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {

                viewPagerProductDetail.reMeasureCurrentPage(viewPagerProductDetail.currentItem)
            }
        })

//        sendMixPanelEvent()
    }

    private fun attemptListApiCall() {
        if (isNetworkAccessible()) {
            val categoryId =
                SharedPreferences.getInstance(this).getStringValue(IntentFlags.CATEGORY_ID)?.toInt()
            callRecommendedListApi(skipCount, takeCount, categoryId!!, product.id)
        } else {
            toast(getString(R.string.check_internet_connection))
        }
    }

    private fun callRecommendedListApi(
        skipCount: Int,
        takeCount: Int,
        categoryId: Int,
        productId: Int
    ) {
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        callback = clientInstance.recommendedProducts(
            skipCount,
            takeCount,
            categoryId,
            productId
        )

        callback.enqueue(object : Callback<Products> {
            override fun onFailure(call: Call<Products>?, t: Throwable?) {
//                toast(getString(R.string.message_something_went_wrong))
            }

            override fun onResponse(call: Call<Products>?, response: Response<Products>?) {
                if (isNetworkAccessible()) {
                    recyclerViewAlsoRecommended.show()
                    if (response?.body()?.statusCode.equals("10")) {
                        if (response?.body()?.data?.productList?.isNotEmpty()!!) {
                            productList.clear()
                            response.body()?.data!!.productList.forEach {
                                if (AppDatabase.getDatabase(this@ProductDetailActivity).productListDao().getSingleWishListProduct(
                                        it.id
                                    ) != null
                                ) {
                                    it.isFavorite =
                                        AppDatabase.getDatabase(this@ProductDetailActivity)
                                            .productListDao()
                                            .getSingleWishListProduct(it.id).isFavorite
                                }
                                productList.add(it)
                            }
//                            productList.addAll(response.body()?.data!!.productDetailsList)
                            productListAdapter.notifyDataSetChanged()
//                            sendMixPanelEvent()

                        } else {
                            textViewAlsoRecommended.hide()
                            layoutAlsoRecommended.hide()
                        }
                    } /*else {
//                        toast(getString(R.string.message_something_went_wrong))
                    }*/

                } /*else {
                    showNetworkCondition()

                }*/
            }

        })
    }

    object LastClickTimeSingleton {
        var lastClickTime: Long = 0
    }


    override fun onItemClickListener(position: Int) {
        if (SystemClock.elapsedRealtime() - LastClickTimeSingleton.lastClickTime < 500L) return
        else {
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra(IntentFlags.PRODUCT_MODEL, productList[position])
            intent.putExtra(IntentFlags.CATEGORY_ID, categoryId)
            startActivity(intent)
        }
        LastClickTimeSingleton.lastClickTime = SystemClock.elapsedRealtime()
    }

    override fun onFavoriteClicked(clickedIndex: Int, isFavorite: Boolean) {
        if (isFavorite) {
            AppDatabase.getDatabase(this).productListDao()
                .insertAllProducts(productList[clickedIndex])
            toast(getString(R.string.message_item_added))

        } else {
            AppDatabase.getDatabase(this).productListDao().deleteProduct(productList[clickedIndex])
            toast(getString(R.string.message_item_removed))
        }
    }

    /*  private fun sendMixPanelEvent() {
          val productObject = JSONObject()
          productObject.put(IntentFlags.MIXPANEL_PRODUCT_ID, product.id)
          productObject.put(IntentFlags.MIXPANEL_PRODUCT_NAME, product.name)
          mixPanel.track(IntentFlags.MIXPANEL_VISITED_PRODUCTS, productObject)
      }
  */

    private fun hideUiComponents() {
        nestedScrollProductDetail.hide()
        buttonPlaceOrder.hide()
        buttonAddToCart.hide()
    }

    private fun showUiComponents() {
        nestedScrollProductDetail.show()
        buttonPlaceOrder.show()
        buttonAddToCart.show()
        stopShimmerView()
    }

    companion object {
        var colorSelectedId = 0
        var sizeSelectedId = 0
        var NUM_PAGES = 0
        lateinit var productDetailActivity: ProductDetailActivity
    }


    override fun showNetworkCondition() {
        nestedScrollProductDetail.hide()
        buttonPlaceOrder.hide()
        buttonAddToCart.hide()
        layoutNetworkCondition.show()
        stopShimmerView()
    }


    override fun hideNetworkCondition() {
        nestedScrollProductDetail.show()
        buttonPlaceOrder.show()
        buttonAddToCart.show()
        layoutNetworkCondition.hide()

    }


    override fun startShimmerView() {
        shimmerViewContainerProductDetail.show()
        shimmerViewContainerProductDetail.startShimmer()

        layoutNetworkCondition.hide()

        nestedScrollProductDetail.hide()
        buttonPlaceOrder.hide()
        buttonAddToCart.hide()
    }

    override fun stopShimmerView() {
        shimmerViewContainerProductDetail.hide()
        shimmerViewContainerProductDetail.stopShimmer()

        /*  nestedScrollProductDetail.show()
          buttonPlaceOrder.show()
          buttonAddToCart.show()*/
    }

    override fun onBackPressed() {
        if (intent.hasExtra(IntentFlags.REDIRECT_FROM) && intent.getStringExtra(IntentFlags.REDIRECT_FROM) == IntentFlags.WISHLIST) {
            startActivity<DashboardActivity>(IntentFlags.FRAGMENT_TO_BE_LOADED to R.id.nav_wishList)
            finish()
        } else {
//            startActivity<ProductsListActivity>()
            finish()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.layoutBackArrow -> {
                onBackPressed()
                colorSelectedId = 0
                sizeSelectedId = 0
            }

            R.id.layoutCartIcon -> {
                if (SharedPreferences.getInstance(this).isUserLoggedIn) {
                    layoutCartIcon.isClickable = false
                    startActivity<CartActivity>()
                } else {
                    layoutCartIcon.isClickable = false

                    startActivity<LoginActivity>(IntentFlags.SHOULD_GO_TO_DASHBOARD to true)
                }
            }


            R.id.btnTryAgain -> {
                if (isNetworkAccessible()) {
                    hideNetworkCondition()
                    initializeViews()
                    attemptApiCall()

                } else {
                    showNetworkCondition()
                }
            }

            R.id.buttonPlaceOrder -> {
                if (SharedPreferences.getInstance(this).isUserLoggedIn) {
                    if (colorSelectedId == 0 && recyclerViewColor.isVisible) {
                        alert(getString(R.string.alert_select_color)) {
                            yesButton { }
                            isCancelable = false
                        }.show().apply {

                            getButton(AlertDialog.BUTTON_POSITIVE)?.let {
                                it.textColor = Color.BLUE
                                it.background =
                                    ContextCompat.getDrawable(
                                        this@ProductDetailActivity,
                                        android.R.color.transparent
                                    )
                            }
                        }
                    } else if (sizeSelectedId == 0 && recyclerViewSizeCount.isVisible) {
                        alert(getString(R.string.alert_select_size)) {
                            yesButton { }
                            isCancelable = false
                        }.show().apply {
                            getButton(AlertDialog.BUTTON_POSITIVE)?.let {
                                it.textColor = Color.BLUE
                                it.background =
                                    ContextCompat.getDrawable(
                                        this@ProductDetailActivity,
                                        android.R.color.transparent
                                    )
                            }
                        }
                    } else {

                        buttonPlaceOrder.isClickable = false
                        callAddToCartApi(
                            "BuyNow",
                            product.id,
                            colorSelectedId,
                            sizeSelectedId
                        )
                    }
                } else {
                    buttonPlaceOrder.isClickable = false

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.putExtra(IntentFlags.REDIRECT_FROM, IntentFlags.PRODUCT_DETAIL)
                    intent.putExtra(IntentFlags.SHOULD_GO_TO_DASHBOARD, true)
                    startActivityForResult(intent, 1)
                }

            }

            R.id.buttonAddToCart -> {
                if (SharedPreferences.getInstance(this).isUserLoggedIn) {
                    if (colorSelectedId == 0 && recyclerViewColor.isVisible) {
                        alert(getString(R.string.alert_select_color)) {
                            yesButton { }
                            isCancelable = false
                        }.show().apply {

                            getButton(AlertDialog.BUTTON_POSITIVE)?.let {
                                it.textColor = Color.BLUE
                                it.background =
                                    ContextCompat.getDrawable(
                                        this@ProductDetailActivity,
                                        android.R.color.transparent
                                    )
                            }
                        }
                    } else if (sizeSelectedId == 0 && recyclerViewSizeCount.isVisible) {
                        alert(getString(R.string.alert_select_size)) {
                            yesButton { }
                            isCancelable = false
                        }.show().apply {
                            getButton(AlertDialog.BUTTON_POSITIVE)?.let {
                                it.textColor = Color.BLUE
                                it.background =
                                    ContextCompat.getDrawable(
                                        this@ProductDetailActivity,
                                        android.R.color.transparent
                                    )
                            }
                        }
                    } else {
                        buttonAddToCart.isClickable = false

                        callAddToCartApi(
                            "Add to Cart",
                            product.id,
                            colorSelectedId,
                            sizeSelectedId
                        )
                    }
                } else {
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.putExtra(IntentFlags.REDIRECT_FROM, IntentFlags.PRODUCT_DETAIL)
                    intent.putExtra(IntentFlags.SHOULD_GO_TO_DASHBOARD, true)
                    startActivityForResult(intent, 2)
                }
            }

            R.id.imageViewFavorite -> {
                if (product.isFavorite) {
                    toast(getString(R.string.message_item_removed))
                    product.isFavorite = false
                    AppDatabase.getDatabase(this).productListDao().deleteProduct(product)
                    imageViewFavorite.isSelected = false

                } else {
                    toast(getString(R.string.message_item_added))
                    product.isFavorite = true
                    AppDatabase.getDatabase(this).productListDao().insertAllProducts(product)
                    imageViewFavorite.isSelected = true

                }
            }


        }
    }

    override fun onButtonClicked(productId: Int) {
        productListId = productId
        if (SharedPreferences.getInstance(this).isUserLoggedIn) {

            callAddToCartApi(
                "Add to Cart from list",
                productListId,
                colorSelectedId,
                sizeSelectedId
            )
        } else {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra(IntentFlags.REDIRECT_FROM, IntentFlags.PRODUCT_DETAIL)
            intent.putExtra(IntentFlags.SHOULD_GO_TO_DASHBOARD, true)
            startActivityForResult(intent, 3)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        layoutCartIcon.isClickable = true

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                if (colorSelectedId == 0 && recyclerViewColor.isVisible) {
                    alert(getString(R.string.alert_select_color)) {
                        yesButton { }
                        isCancelable = false
                    }.show().apply {

                        getButton(AlertDialog.BUTTON_POSITIVE)?.let {
                            it.textColor = Color.BLUE
                            it.background =
                                ContextCompat.getDrawable(
                                    this@ProductDetailActivity,
                                    android.R.color.transparent
                                )
                        }
                    }
                } else if (sizeSelectedId == 0 && recyclerViewSizeCount.isVisible) {
                    alert(getString(R.string.alert_select_size)) {
                        yesButton { }
                        isCancelable = false
                    }.show().apply {
                        getButton(AlertDialog.BUTTON_POSITIVE)?.let {
                            it.textColor = Color.BLUE
                            it.background =
                                ContextCompat.getDrawable(
                                    this@ProductDetailActivity,
                                    android.R.color.transparent
                                )
                        }
                    }
                } else {
                    buttonPlaceOrder.isClickable = false

                    callAddToCartApi(
                        "BuyNow",
                        product.id,
                        colorSelectedId,
                        sizeSelectedId
                    )
                }
            }
        } else if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                if (colorSelectedId == 0 && recyclerViewColor.isVisible) {
                    alert(getString(R.string.alert_select_color)) {
                        yesButton { }
                        isCancelable = false
                    }.show().apply {

                        getButton(AlertDialog.BUTTON_POSITIVE)?.let {
                            it.textColor = Color.BLUE
                            it.background =
                                ContextCompat.getDrawable(
                                    this@ProductDetailActivity,
                                    android.R.color.transparent
                                )
                        }
                    }
                } else if (sizeSelectedId == 0 && recyclerViewSizeCount.isVisible) {
                    alert(getString(R.string.alert_select_size)) {
                        yesButton { }
                        isCancelable = false
                    }.show().apply {
                        getButton(AlertDialog.BUTTON_POSITIVE)?.let {
                            it.textColor = Color.BLUE
                            it.background =
                                ContextCompat.getDrawable(
                                    this@ProductDetailActivity,
                                    android.R.color.transparent
                                )
                        }
                    }
                } else {
                    buttonAddToCart.isClickable = false

                    callAddToCartApi(
                        "Add to Cart",
                        product.id,
                        colorSelectedId,
                        sizeSelectedId
                    )
                }
            }
        } else if (requestCode == 3) {
            callAddToCartApi(
                "Add to Cart from list",
                productListId,
                colorSelectedId,
                sizeSelectedId
            )
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun callAddToCartApi(
        clickedButton: String,
        productId: Int,
        colorSelectedId: Int,
        sizeSelectedId: Int
    ) {
        if (isNetworkAccessible()) {
            val applicationUserId =
                SharedPreferences.getInstance(this@ProductDetailActivity)
                    .getStringValue(IntentFlags.APPLICATION_USER_ID)
            val cartListVariables = RequestAddToCart(
                applicationUserId!!.toInt(),
                productId,
                colorSelectedId,
                sizeSelectedId
            )
            App.HostUrl = SharedPreferences.getInstance(this@ProductDetailActivity).hostUrl!!
            val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
            val call = clientInstance.addToCartApi(cartListVariables)
            call.enqueue(object : Callback<ResponseAddToCart> {
                override fun onFailure(call: Call<ResponseAddToCart>, t: Throwable) {
//                    toast(getString(R.string.message_something_went_wrong))
                }

                override fun onResponse(
                    call: Call<ResponseAddToCart>,
                    response: Response<ResponseAddToCart>
                ) {
                    if (response.body()?.statusCode.equals("10")) {
                        buttonPlaceOrder.isClickable = true
                        buttonAddToCart.isClickable = true

                        if (clickedButton.equals(
                                "Add to Cart",
                                true
                            ) || clickedButton.equals("Add to Cart from list", true)
                        ) {
                            incrementCartCount()
                            setCartBadge()
                            toast(getString(R.string.message_product_added_to_cart))
                        } else if (clickedButton == "BuyNow") {
                            startActivity<CartActivity>()
                        }
                    } else {
                        if (clickedButton == "BuyNow" && response.body()!!.statusCode.equals("30")) {
                            buttonPlaceOrder.isClickable = true
                            buttonAddToCart.isClickable = true
//                            setCartBadge()
                            incrementCartCount()
                            setCartBadge()
                            startActivity<CartActivity>()
                        } else if (clickedButton.equals(
                                "Add to Cart",
                                true
                            ) || clickedButton.equals("Add to Cart from list", true)
                        ) {
                            buttonPlaceOrder.isClickable = true
                            buttonAddToCart.isClickable = true

                            toast(response.body()!!.message)
                        }
                    }
                }

            })
        } else {
            toast(getString(R.string.check_internet_connection))
        }
    }
/*
    override fun onDestroy() {
        mixPanel.flush()
        super.onDestroy()
    }*/

    inner class ViewPagerAdapterProductDetail(manager: FragmentManager) :
        FragmentStatePagerAdapter(manager) {

        var mCurrentPosition = -1

        override fun getItem(position: Int): Fragment {

            var fragment: Fragment? = null


            if (getPageTitle(position) == getString(R.string.title_description)) {
                fragment = DescriptionFragment().newInstance(
                    productDetail.description,
                    productDetail.includedAccesories
                )

            } else if (getPageTitle(position) == getString(R.string.title_specification)) {
                fragment = SpecificationFragment().newInstance(
                    productDetail.length,
                    productDetail.height,
                    productDetail.width,
                    productDetail.weight,
                    productDetail.materialType
                )
            } else if (getPageTitle(position) == getString(R.string.title_instruction)) {
                fragment = InstructionFragment().newInstance(
                    productDetail.deliveryTime,
                    productDetail.precautions
                )
            }

            return fragment!!
        }

        override fun getCount(): Int {
            return pageCount
        }

        override fun getPageTitle(position: Int): CharSequence? {

            var title: String? = null
            if (position == 0) {
                title = getString(R.string.title_description)

            } else if (position == 1 && (!productDetail.length.isNullOrEmpty() ||
                        !productDetail.height.isNullOrEmpty() ||
                        !productDetail.width.isNullOrEmpty() ||
                        !productDetail.weight.isNullOrEmpty() ||
                        !productDetail.materialType.isNullOrEmpty()
                        )
            ) {
                title = getString(R.string.title_specification)
            } else {
                title = getString(R.string.title_instruction)
            }
            return title
        }

        /*override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
            super.setPrimaryItem(container, position, `object`)

            if (container !is WrappingViewPager) {
                return
            }
            if (position != mCurrentPosition) {
            val fragment = `object` as Fragment
            if (fragment != null && fragment.view != null) {

                    mCurrentPosition = position
                container.reMeasureCurrentPage(position)
            }}
        }*/


    }

}
