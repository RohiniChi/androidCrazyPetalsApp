package com.plugable.mcommerceapp.cpmvp1.ui.activities

import ServiceGenerator
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.os.SystemClock
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.callbacks.EventListener
import com.plugable.mcommerceapp.cpmvp1.callbacks.OnButtonClickListener
import com.plugable.mcommerceapp.cpmvp1.callbacks.OnFavoriteListener
import com.plugable.mcommerceapp.cpmvp1.callbacks.OnItemCheckListener
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.cpmvp1.mcommerce.db.AppDatabase
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.*
import com.plugable.mcommerceapp.cpmvp1.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.cpmvp1.registration.LoginActivity
import com.plugable.mcommerceapp.cpmvp1.ui.adapters.FilterAdapter
import com.plugable.mcommerceapp.cpmvp1.ui.adapters.ProductListAdapter
import com.plugable.mcommerceapp.cpmvp1.utils.application.App
import com.plugable.mcommerceapp.cpmvp1.utils.constants.IntentFlags
import com.plugable.mcommerceapp.cpmvp1.utils.constants.SharedPreferences.SHARED_PREFERENCES_CART_COUNT
import com.plugable.mcommerceapp.cpmvp1.utils.constants.SharedPreferences.cartItemList
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi
import com.plugable.mcommerceapp.cpmvp1.utils.extension.hide
import com.plugable.mcommerceapp.cpmvp1.utils.extension.setStatusBarColor
import com.plugable.mcommerceapp.cpmvp1.utils.extension.setToolBarColor
import com.plugable.mcommerceapp.cpmvp1.utils.extension.show
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.cpmvp1.utils.util.CountDrawable
import com.plugable.mcommerceapp.cpmvp1.utils.util.isNetworkAccessible
import kotlinx.android.synthetic.main.activity_products.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*
import kotlinx.android.synthetic.main.layout_network_condition.*
import kotlinx.android.synthetic.main.layout_no_data_condition.*
import kotlinx.android.synthetic.main.layout_server_error_condition.*
import kotlinx.android.synthetic.main.recycler_filter_item.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * [ProductsListActivity] is used to show list of products based on category and subcategory id
 *
 */
class ProductsListActivity : BaseActivity(), EventListener, OnFavoriteListener,
    OnItemCheckListener, OnButtonClickListener,
    BottomNavigationView.OnNavigationItemSelectedListener {


    private lateinit var gridLayoutManager: GridLayoutManager
    private var totalCount: Int = 0
    private lateinit var callback: Call<Products>
    lateinit var productListAdapter: ProductListAdapter
    var productList = ArrayList<Products.Data.ProductDetails>()
    internal lateinit var filterList: ArrayList<GetFilters.Data.Filter>
    lateinit var filterListAdapter: FilterAdapter
    var products: Products.Data.ProductDetails? = null
    private lateinit var eventClickListener: EventListener
    private lateinit var onButtonClickListener: OnButtonClickListener
    //    private lateinit var onBottomReachedListener: SetOnBottomReachedListener
    private var skipCount = 0
    private var takeCount = 1000
    private var categoryId = 0
    var categoryList = ArrayList<Categories.Data.Category>()
    private lateinit var mixPanel: MixpanelAPI
    var checkedId = HashSet<Int>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)

        initializeViews()
        initializeTheme()
        attemptApiCall()
        attemptGetFilterApi()
        this.invalidateOptionsMenu()
        attemptCartApiCall()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.product_list_menu, menu)

        //Custom cart badge count
        val cartItem = menu?.findItem(R.id.action_cart)
        val cartIcon = cartItem?.icon as LayerDrawable

        val cartBadge: CountDrawable

        // Reuse drawable if possible
        val reuseIcon = cartIcon.findDrawableByLayerId(R.id.ic_group_count_cart)
        if (reuseIcon != null && reuseIcon is CountDrawable) {
            cartBadge = reuseIcon
        } else {
            cartBadge = CountDrawable(this)
        }

        var cartCountText = SharedPreferences.getInstance(this)
            .getStringValue(SHARED_PREFERENCES_CART_COUNT)!!

        if (cartCountText == "10") {
            cartCountText = "9+"
        }
        cartBadge.setCount(cartCountText)
        cartIcon.mutate()
        cartIcon.setDrawableByLayerId(R.id.ic_group_count_cart, cartBadge)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
            R.id.action_cart -> {
                if (SharedPreferences.getInstance(this).isUserLoggedIn) {
                    if (SystemClock.elapsedRealtime() - LastClickTimeSingleton.lastClickTime < 500L) return true
                    else {

                        startActivity<CartActivity>()
                    }

                    LastClickTimeSingleton.lastClickTime = SystemClock.elapsedRealtime()

                } else {
                    if (SystemClock.elapsedRealtime() - LastClickTimeSingleton.lastClickTime < 500L) return true
                    else {

                        startActivity<LoginActivity>(IntentFlags.SHOULD_GO_TO_DASHBOARD to true)
                    }

                    LastClickTimeSingleton.lastClickTime = SystemClock.elapsedRealtime()

                }
            }
            R.id.action_search -> {
                if (SystemClock.elapsedRealtime() - LastClickTimeSingleton.lastClickTime < 500L) return true
                else {

                    startActivity<SearchActivity>()
                }

                LastClickTimeSingleton.lastClickTime = SystemClock.elapsedRealtime()

            }
        }
        return super.onOptionsItemSelected(item!!)
    }

    override fun onResume() {
//        this.invalidateOptionsMenu()

        initializeViews()
        attemptApiCall()
        attemptGetFilterApi()
        attemptCartApiCall()
        super.onResume()

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

        btnTryAgain.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        btnNoData.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        btnServerError.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        button_apply_filter.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))

        progressBarProductList.indeterminateDrawable.setColorFilter(
            Color.BLACK,
            PorterDuff.Mode.MULTIPLY
        )

        button_cancel.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray))


        val iconsColorStates = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
            ), intArrayOf(
                Color.BLACK,
                Color.parseColor(ApplicationThemeUtils.PRIMARY_COLOR)
            )
        )

        val textColorStates = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
            ), intArrayOf(
                Color.BLACK,
                Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR)
            )
        )

        bottom_navigation.itemIconTintList = iconsColorStates
        bottom_navigation.itemTextColor = textColorStates
    }

    private fun initializeViews() {
        imgToolbarHomeLayout.setOnClickListener(this)

        btnNoData.setOnClickListener(this)
        btnServerError.setOnClickListener(this)
        btnTryAgain.setOnClickListener(this)

        bottom_navigation.setOnNavigationItemSelectedListener(this)
        filter_layout.setOnClickListener(this)
        button_apply_filter.setOnClickListener(this)
        button_cancel.setOnClickListener(this)

        mixPanel = MixpanelAPI.getInstance(this, resources.getString(R.string.mix_panel_token))

        /*     swipeRefreshLayoutProductList.setColorSchemeResources(R.color.colorAccent)
             swipeRefreshLayoutProductList.setOnRefreshListener {
                 productList.clear()
                 skipCount=0
                 takeCount=25
                 startShimmerView()
                 callProductsListAPI(skipCount,takeCount,categoryId)
             }*/
        categoryId = intent.getIntExtra(IntentFlags.CATEGORY_ID, 0)

        SharedPreferences.getInstance(this)
            .setStringValue(IntentFlags.CATEGORY_ID, categoryId.toString())

        eventClickListener = this
        onButtonClickListener = this
//            onBottomReachedListener = this

        productListAdapter =
            ProductListAdapter(productList, this, eventClickListener, onButtonClickListener, this)
        gridLayoutManager = GridLayoutManager(applicationContext, 2)
        gridLayoutManager.orientation = LinearLayoutManager.VERTICAL // set Horizontal Orientation
        recyclerViewProducts.layoutManager = gridLayoutManager
        recyclerViewProducts.itemAnimator = DefaultItemAnimator()
        recyclerViewProducts.adapter = productListAdapter

        filterList = arrayListOf()
        filterListAdapter = FilterAdapter(filterList, this, this, checkedId)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        recycler_filter.layoutManager = linearLayoutManager
        recycler_filter.adapter = filterListAdapter


    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_crazypetals -> {
                attemptApiCall()
                attemptGetFilterApi()
            }

            R.id.action_exclusive -> {
                attemptExclusiveProductsApi()
                attemptGetFilterApi()
            }
        }
        return true
    }

    /* private fun updateGridData(gridLayoutManager: GridLayoutManager) {
         val visibleItemCount = gridLayoutManager.childCount
         val totalItemCount = gridLayoutManager.itemCount
         val positionView = gridLayoutManager.findFirstVisibleItemPosition()

         if (positionView + visibleItemCount >= totalItemCount && productList.size < totalCount) {
             skipCount += 25
             takeCount = 25
             callProductsListAPI(skipCount,takeCount,categoryId)
         }
     }

 */
    private fun attemptCartApiCall() {
        if (SharedPreferences.getInstance(this).isUserLoggedIn) {

            val applicationUserId =
                SharedPreferences.getInstance(this)
                    .getStringValue(IntentFlags.APPLICATION_USER_ID)
            App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
            val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
            val callback = clientInstance.getCartApi(applicationUserId!!.toInt())

            callback.enqueue(object : Callback<GetCartResponse> {
                override fun onFailure(call: Call<GetCartResponse>, t: Throwable) {
//                    toast(getString(R.string.message_something_went_wrong))
                }

                override fun onResponse(
                    call: Call<GetCartResponse>,
                    response: Response<GetCartResponse>
                ) {
                    if (response.body()?.statusCode.equals("10")) {


                        if (response.body()!!.data.isNotEmpty()) {

                            cartItemList.clear()
                            for (item in response.body()!!.data) {
                                cartItemList.add(item.productId.toString())
                            }

                            SharedPreferences.getInstance(this@ProductsListActivity).setStringValue(
                                SHARED_PREFERENCES_CART_COUNT,
                                response.body()!!.data.size.toString()

                            )

                            invalidateOptionsMenu()


                            SharedPreferences.getInstance(this@ProductsListActivity)
                                .setAddToCartData(
                                    response.body()!!
                                )

                            productListAdapter.notifyDataSetChanged()

                        } else {
                            SharedPreferences.getInstance(this@ProductsListActivity).setStringValue(
                                SHARED_PREFERENCES_CART_COUNT,
                                response.body()!!.data.size.toString()
                            )
                            invalidateOptionsMenu()


                            cartItemList.clear()
                            for (item in response.body()!!.data) {
                                cartItemList.add(item.productId.toString())
                            }

                            SharedPreferences.getInstance(this@ProductsListActivity)
                                .setAddToCartData(
                                    response.body()!!
                                )
                            productListAdapter.notifyDataSetChanged()


                        }
                    } else {
//                        toast(getString(R.string.message_something_went_wrong))
                        productListAdapter.notifyDataSetChanged()

                    }

                }

            })
        } else {
            SharedPreferences.getInstance(this).setStringValue(
                SHARED_PREFERENCES_CART_COUNT,
                "0"
            )
        }
    }

    private fun attemptExclusiveProductsApi() {
        if (isNetworkAccessible()) {
            callExclusiveProducts()
        } else {
            showNetworkCondition()
        }
    }

    private fun callExclusiveProducts() {
        App.HostUrl = SharedPreferences.getInstance(this@ProductsListActivity).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        val call = clientInstance.exclusiveModel(skipCount, takeCount, categoryId, WebApi.APP_ID)
//        swipeRefreshLayout.isRefreshing = true
        call.enqueue(object : Callback<Products> {
            override fun onFailure(call: Call<Products>?, t: Throwable?) {
//                swipeRefreshLayout.isRefreshing = false
                showServerErrorMessage()
            }

            override fun onResponse(call: Call<Products>?, response: Response<Products>?) {
                if (isNetworkAccessible()) {
                    showRecyclerViewData()
//                    swipeRefreshLayout.isRefreshing = false
                    if (response?.body()?.statusCode.equals("10")) {
//                        totalCount = response?.body()?.data!!.totalCount
                        if (response?.body()?.data?.productList?.isNotEmpty()!!) {
                            productList.clear()
                            response.body()?.data!!.productList.forEach {
                                if (AppDatabase.getDatabase(this@ProductsListActivity).productListDao().getSingleWishListProduct(
                                        it.id
                                    ) != null
                                ) {
                                    it.isFavorite =
                                        AppDatabase.getDatabase(this@ProductsListActivity)
                                            .productListDao()
                                            .getSingleWishListProduct(it.id).isFavorite
                                }
                                productList.add(it)
                            }
//                            productList.addAll(response.body()?.data!!.productDetailsList)
                            productListAdapter.notifyDataSetChanged()
                            sendMixPanelEvent()

                        } else {
                            showNoDataAvailableScreen()
                        }
                    } else {
                        toast(getString(R.string.message_something_went_wrong))
                    }

                } else {
                    showNetworkCondition()

                }
            }

        })

    }

    private fun attemptApiCall() {
        if (isNetworkAccessible()) {
            callProductsListAPI(skipCount, takeCount, categoryId)
        } else {
            showNetworkCondition()
        }
    }

    private fun callProductsListAPI(skipCount: Int, takeCount: Int, categoryId: Int) {
        App.HostUrl = SharedPreferences.getInstance(this@ProductsListActivity).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        callback = clientInstance.productModel(skipCount, takeCount, categoryId)
//        swipeRefreshLayoutProductList.isRefreshing = true
        callback.enqueue(object : Callback<Products> {
            override fun onFailure(call: Call<Products>?, t: Throwable?) {
//                swipeRefreshLayoutProductList.isRefreshing = false
                showServerErrorMessage()
            }

            override fun onResponse(call: Call<Products>?, response: Response<Products>?) {
                if (isNetworkAccessible()) {
                    showRecyclerViewData()
//                    swipeRefreshLayoutProductList.isRefreshing = false
                    if (response?.body()?.statusCode.equals("10")) {
                        totalCount = response?.body()?.data!!.totalCount
                        if (response.body()?.data?.productList?.isNotEmpty()!!) {
                            productList.clear()
                            response.body()?.data!!.productList.forEach {
                                if (AppDatabase.getDatabase(this@ProductsListActivity).productListDao().getSingleWishListProduct(
                                        it.id
                                    ) != null
                                ) {
                                    it.isFavorite =
                                        AppDatabase.getDatabase(this@ProductsListActivity)
                                            .productListDao()
                                            .getSingleWishListProduct(it.id).isFavorite
                                }
                                productList.add(it)

                            }
//                            productList.addAll(response.body()?.data!!.productDetailsList)
                            productListAdapter.notifyDataSetChanged()

                            if (productList.isNotEmpty()) {
                                recyclerViewProducts.scrollToPosition(0)
                            }

                            sendMixPanelEvent()

                        } else {
                            showNoDataAvailableScreen()
                        }
                    } else {
//                        toast(getString(R.string.message_something_went_wrong))
                    }

                } else {
                    showNetworkCondition()

                }
            }

        })

    }

    private fun sendMixPanelEvent() {
        val productObject = JSONObject()
        productObject.put(IntentFlags.MIXPANEL_PRODUCT_ID, products?.id)
        mixPanel.track(IntentFlags.MIXPANEL_VISITED_PRODUCT_LIST, productObject)
    }


    override fun setToolBar(name: String) {
        val categoryName = intent.getStringExtra(IntentFlags.CATEGORY_NAME)
        setSupportActionBar(toolBar)
        setStatusBarColor()
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = categoryName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_shape_backarrow_white)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        txtToolbarTitle.hide()
//        txtToolbarTitle.allCaps = false
//        txtToolbarTitle.text = categoryName
        imgToolbarHome.hide()
//        imgToolbarHome.setImageResource(R.drawable.ic_shape_backarrow)
        setToolBarColor(imgToolbarHome, txtToolbarTitle, toolbar = toolBar)
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgToolbarHomeLayout -> {
                onBackPressed()
            }
            R.id.btnTryAgain -> {
                if (bottom_navigation.selectedItemId == R.id.action_crazypetals) {
                    attemptApiCall()
                } else {
                    attemptExclusiveProductsApi()
                }

                attemptGetFilterApi()

            }
            R.id.btnNoData -> {
                if (bottom_navigation.selectedItemId == R.id.action_crazypetals) {
                    attemptApiCall()
                } else {
                    attemptExclusiveProductsApi()
                }
                attemptGetFilterApi()
            }
            R.id.btnServerError -> {
                attemptGetFilterApi()
                if (bottom_navigation.selectedItemId == R.id.action_crazypetals) {
                    attemptApiCall()
                } else {
                    attemptExclusiveProductsApi()
                }
            }
            R.id.filter_layout -> {
                textViewFilter.hide()
                show_filter.hide()
                recycler_filter.show()
                button_apply_filter.show()
                button_cancel.show()
//                progressBarProductList.show()
                filterListAdapter.notifyDataSetChanged()
            }

            R.id.button_apply_filter -> {
                textViewFilter.show()
                show_filter.show()
                recycler_filter.hide()
                button_apply_filter.hide()
                button_cancel.hide()

                if (bottom_navigation.selectedItemId == R.id.action_crazypetals) {
                    if (checkedId.isNotEmpty()) {
                        attemptApplyFilterApi(false)
                    } else {
                        attemptApiCall()
                    }
                } else {
                    if (checkedId.isNotEmpty()) {
                        attemptApplyFilterApi(true)
                    } else {
                        attemptExclusiveProductsApi()
                    }
                }


            }

            R.id.button_cancel -> {
                checkedId.clear()

                textViewFilter.show()
                show_filter.show()
                recycler_filter.hide()
                button_apply_filter.hide()
                button_cancel.hide()
                checkbox_filter.isChecked = false

                if (bottom_navigation.selectedItemId == R.id.action_crazypetals) {
                    attemptApiCall()
                } else {
                    attemptExclusiveProductsApi()
                }
            }

        }

    }


    private fun attemptGetFilterApi() {
        if (isNetworkAccessible()) {
            callGetFilterApi()
        } else {
            progressBarProductList.hide()
            toast(getString(R.string.check_internet_connection))
        }
    }

    private fun attemptApplyFilterApi(isExclusive: Boolean) {
        if (isNetworkAccessible()) {
            callApplyFilterApi(isExclusive)
        } else {
            toast(getString(R.string.check_internet_connection))
        }
    }

    private fun callGetFilterApi() {
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        val call = clientInstance.getFilters(
            categoryId = categoryId
        )
        call.enqueue(object : Callback<GetFilters> {
            override fun onFailure(call: Call<GetFilters>, t: Throwable) {
//                toast(getString(R.string.message_something_went_wrong))
            }

            override fun onResponse(call: Call<GetFilters>, response: Response<GetFilters>) {
                if (response.isSuccessful) {
                    if (response.body()?.statusCode.equals("10")) {
                        if (response.body()?.data?.filterList?.isNotEmpty()!!) {
                            filter_layout.show()
                            progressBarProductList.hide()
                            filterList.clear()
                            filterList.addAll(response.body()!!.data.filterList)
//                                recycler_filter.adapter = filterListAdapter
                            filterListAdapter.notifyDataSetChanged()
//                                toast(response.body()!!.message)

                            if (filterList.size > 5) {
                                val params = recycler_filter.layoutParams
                                params.height = 700
                                recycler_filter.layoutParams = params
                            }

                        } else {

                            filter_layout.hide()
                            button_apply_filter.hide()
                            button_cancel.hide()
                        }
                    }
                }
            }

        })
    }

    private fun callApplyFilterApi(isExclusive: Boolean) {
        val filterData = FilterData(
            CategoryId = categoryId.toString(),
            skip = skipCount.toString(),
            take = takeCount.toString(),
            Filters = checkedId.toList(),
            isExclusive = "" + isExclusive
        )
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        val call = clientInstance.applyFilters(filterData)
        call.enqueue(object : Callback<Products> {
            override fun onFailure(call: Call<Products>, t: Throwable) {
//                toast(getString(R.string.message_something_went_wrong))
            }

            override fun onResponse(call: Call<Products>, response: Response<Products>) {
                if (isNetworkAccessible()) {
                    showRecyclerViewData()
//                    swipeRefreshLayout.isRefreshing = false
                    if (response.body()?.statusCode.equals("10")) {
//                        totalCount = response?.body()?.data!!.totalCount
                        if (response.body()?.data?.productList?.isNotEmpty()!!) {
                            filter_layout.show()
                            productList.clear()
                            response.body()?.data!!.productList.forEach {
                                if (AppDatabase.getDatabase(this@ProductsListActivity).productListDao().getSingleWishListProduct(
                                        it.id
                                    ) != null
                                ) {
                                    it.isFavorite =
                                        AppDatabase.getDatabase(this@ProductsListActivity)
                                            .productListDao()
                                            .getSingleWishListProduct(it.id).isFavorite
                                }
                                productList.add(it)
                            }
//                            productList.addAll(response.body()?.data!!.productDetailsList)
                            productListAdapter.notifyDataSetChanged()
                            if (productList.isNotEmpty()) {
                                recycler_filter.scrollToPosition(0)
                            }
                            sendMixPanelEvent()

                        } else {
                            showNoDataAvailableScreen()
                        }
                    } else {
//                        toast(getString(R.string.message_something_went_wrong))
                    }

                } else {
                    showNetworkCondition()

                }

            }
        })
    }

    override fun showNetworkCondition() {
        layoutNetworkCondition.show()
        layoutNoDataScreen.hide()
        layoutServerError.hide()
        recyclerViewProducts.hide()
        bottom_navigation.hide()
        filter_layout.hide()

        stopShimmerView()
        /*  skipCount=0
          takeCount=25
          productList.clear()
          swipeRefreshLayoutProductList.hide()
  */
    }

    override fun hideNetworkCondition() {
        layoutNetworkCondition.hide()
        layoutNoDataScreen.hide()
        layoutServerError.hide()


        stopShimmerView()

//        swipeRefreshLayoutProductList.hide()
    }

    override fun showServerErrorMessage() {
        layoutNetworkCondition.hide()
        layoutNoDataScreen.hide()
        layoutServerError.show()
        recyclerViewProducts.hide()
        bottom_navigation.hide()
        filter_layout.hide()

        stopShimmerView()

        /* skipCount=0
         takeCount=25
         productList.clear()
         swipeRefreshLayoutProductList.hide()
 */
    }

    override fun hideServerErrorMessage() {
        layoutNetworkCondition.hide()
        layoutNoDataScreen.hide()
        layoutServerError.hide()

        stopShimmerView()
//swipeRefreshLayoutProductList.hide()
    }

    override fun showRecyclerViewData() {
        layoutNetworkCondition.hide()
        layoutNoDataScreen.hide()
        layoutServerError.hide()
        recyclerViewProducts.show()
        bottom_navigation.show()
//        filter_layout.show()
//        swipeRefreshLayoutProductList.show()

        stopShimmerView()
    }


    override fun showNoDataAvailableScreen() {
        layoutNetworkCondition.hide()
        layoutNoDataScreen.show()
        layoutServerError.hide()
        recyclerViewProducts.hide()
        filter_layout.hide()
        //bottom_navigation.hide()
        stopShimmerView()
/*

        skipCount=0
        takeCount=25
        productList.clear()
//        swipeRefreshLayoutProductList.hide()
*/

    }

    override fun hideNoDataAvailableScreen() {
        layoutNetworkCondition.hide()
        layoutNoDataScreen.hide()
        layoutServerError.hide()

        stopShimmerView()
//        swipeRefreshLayoutProductList.hide()
    }

    override fun startShimmerView() {
        shimmerViewContainerProductList.show()
        shimmerViewContainerProductList.startShimmer()

        layoutNetworkCondition.hide()
        layoutServerError.hide()
        recyclerViewProducts.hide()
        filter_layout.hide()
        button_apply_filter.hide()
        button_cancel.hide()
    }

    override fun stopShimmerView() {
        shimmerViewContainerProductList.hide()
        shimmerViewContainerProductList.stopShimmer()
    }

    object LastClickTimeSingleton {
        var lastClickTime: Long = 0
    }

    override fun onItemClickListener(position: Int) {
        if (SystemClock.elapsedRealtime() - LastClickTimeSingleton.lastClickTime < 500L) return
        else {

            textViewFilter.show()
            show_filter.show()
            recycler_filter.hide()
            button_apply_filter.hide()
            button_cancel.hide()

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
            AppDatabase.getDatabase(this).productListDao()
                .deleteProduct(productList[clickedIndex])
            toast(getString(R.string.message_item_removed))
        }
    }

    /* override fun onBottomReached(position: Int) {
         recyclerViewProducts.addOnScrollListener(object :RecyclerView.OnScrollListener(){
             override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                 super.onScrolled(recyclerView, dx, dy)
                 try {
                     updateGridData(gridLayoutManager)

                 }
                 catch (e:Exception){

                 }

             }

         })
     }
 */
    override fun onItemCheck(clickedId: Int, isChecked: Boolean) {

        if (isChecked)
            checkedId.add(clickedId)
        else
            checkedId.remove(clickedId)
    }

    override fun onButtonClicked(productId: Int) {

        if (isNetworkAccessible()) {
            if (SharedPreferences.getInstance(this).isUserLoggedIn) {
                progressBarProductList.show()
                val applicationUserId = SharedPreferences.getInstance(this)
                    .getStringValue(IntentFlags.APPLICATION_USER_ID)
                val cartListVariables = RequestAddToCart(
                    ApplicationUserId = applicationUserId!!.toInt(),
                    ProductId = productId,
                    ColorsId = 0,
                    SizeId = 0
                )
                App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
                val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
                val call = clientInstance.addToCartApi(cartListVariables)
                call.enqueue(object : Callback<ResponseAddToCart> {
                    override fun onFailure(call: Call<ResponseAddToCart>, t: Throwable) {
                    }

                    override fun onResponse(
                        call: Call<ResponseAddToCart>,
                        response: Response<ResponseAddToCart>
                    ) {
                        if (response.body()?.statusCode.equals("10")) {
//                                toast(getString(R.string.message_product_added_to_cart))
                            attemptCartApiCall()
                            toast(getString(R.string.message_product_added_to_cart))
                            progressBarProductList.hide()

                            productListAdapter.notifyDataSetChanged()
                        } else if (response.body()?.statusCode.equals("30")) {
                            toast(response.body()!!.message)
                            productListAdapter.notifyDataSetChanged()
                            progressBarProductList.hide()
                        }

                    }
                })

            } else {
                if (SystemClock.elapsedRealtime() - LastClickTimeSingleton.lastClickTime < 500L) return
                else {

                    startActivity<LoginActivity>(IntentFlags.SHOULD_GO_TO_DASHBOARD to true)
                }

                LastClickTimeSingleton.lastClickTime =
                    SystemClock.elapsedRealtime()

            }

        }
    }

    override fun onDestroy() {
        mixPanel.flush()
        super.onDestroy()
    }

}

