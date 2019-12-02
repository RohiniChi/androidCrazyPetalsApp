package com.plugable.mcommerceapp.cpmvp1.ui.activities

import ServiceGenerator
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.callbacks.EventListener
import com.plugable.mcommerceapp.cpmvp1.callbacks.OnButtonClickListener
import com.plugable.mcommerceapp.cpmvp1.callbacks.OnFavoriteListener
import com.plugable.mcommerceapp.cpmvp1.callbacks.SetOnBottomReachedListener
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.cpmvp1.mcommerce.db.AppDatabase
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.GetCartResponse
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.Products
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.RequestAddToCart
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.ResponseAddToCart
import com.plugable.mcommerceapp.cpmvp1.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.cpmvp1.registration.LoginActivity
import com.plugable.mcommerceapp.cpmvp1.ui.adapters.ProductListAdapter
import com.plugable.mcommerceapp.cpmvp1.ui.fragments.WishListFragment
import com.plugable.mcommerceapp.cpmvp1.utils.application.App
import com.plugable.mcommerceapp.cpmvp1.utils.constants.IntentFlags
import com.plugable.mcommerceapp.cpmvp1.utils.extension.hide
import com.plugable.mcommerceapp.cpmvp1.utils.extension.setStatusBarColor
import com.plugable.mcommerceapp.cpmvp1.utils.extension.show
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.cpmvp1.utils.util.isNetworkAccessible
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.layout_network_condition.*
import kotlinx.android.synthetic.main.layout_no_data_condition.*
import kotlinx.android.synthetic.main.layout_search_toolbar.*
import kotlinx.android.synthetic.main.layout_server_error_condition.*
import org.jetbrains.anko.editText
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


/**
 * [SearchActivity] is used to load list of products on basis of keywords
 *
 */
class SearchActivity : BaseActivity(), EventListener, OnFavoriteListener,
    SetOnBottomReachedListener, OnButtonClickListener {

    override fun onButtonClicked(productId: Int) {

        if (isNetworkAccessible()) {
            if (SharedPreferences.getInstance(this).isUserLoggedIn) {
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
                            toast(getString(R.string.message_product_added_to_cart))
                            attemptCartApiCall()
                            productListAdapter.notifyDataSetChanged()
                        } else if (response.body()?.statusCode.equals("30")) {
                            toast(response.body()!!.message)
                            productListAdapter.notifyDataSetChanged()

                        }

                    }
                })

            } else {
                if (SystemClock.elapsedRealtime() - WishListFragment.LastClickTimeSingleton.lastClickTime < 500L) return
                else {

                    startActivity<LoginActivity>(
                        IntentFlags.SHOULD_GO_TO_DASHBOARD to true
                    )
                }

                WishListFragment.LastClickTimeSingleton.lastClickTime =
                    SystemClock.elapsedRealtime()

            }

        }
    }

    override fun onBottomReached(position: Int) {

    }

    private lateinit var mixPanel: MixpanelAPI
    var productList = ArrayList<Products.Data.ProductDetails>()
    private lateinit var productListAdapter: ProductListAdapter
//    private lateinit var onBottomReachedListener: SetOnBottomReachedListener

    private var skipCount = 0
    private var takeCount = 100
    private var keywordGlobal = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initializeTheme()
        initializeViews()
    }

    override fun onResume() {
        super.onResume()
        attemptCartApiCall()
        if (isNetworkAccessible()) {
            if (intent.getStringExtra(getString(R.string.intent_banner_search)) != null) {
                val bannerSearch = intent.getStringExtra(getString(R.string.intent_banner_search))!!

                if (bannerSearch.isNotEmpty()) {
                    keywordGlobal = bannerSearch
                    if (isNetworkAccessible())
                        searchViewProducts.setQuery(bannerSearch, true)
                    searchViewProducts.clearFocus()
                    keywordGlobal = bannerSearch
//                callSearchApi(skipCount, takeCount, bannerSearch)

                }
            } else
                if (searchViewProducts.query.isNotEmpty() && searchViewProducts.query.length >= 3) {
                    callSearchApi(
                        takeCount,
                        keywordGlobal
                    )
                } else {
                    if (searchViewProducts.query.length < 3) toast(getString(R.string.search_min_characters_to_search))
                }

        } else
            showNetworkCondition()

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
    }

    private fun initializeViews() {
        mixPanel = MixpanelAPI.getInstance(this, resources.getString(R.string.mix_panel_token))

//        onBottomReachedListener=this
        productListAdapter = ProductListAdapter(productList, this, this, this, this)
        val gridLayoutManager = GridLayoutManager(this, 2)
        gridLayoutManager.orientation =
            RecyclerView.VERTICAL // set Horizontal Orientation
        recyclerViewSearchProduct.layoutManager = gridLayoutManager
        recyclerViewSearchProduct.itemAnimator = DefaultItemAnimator()
        recyclerViewSearchProduct.adapter = productListAdapter

        imgToolbarHomeLayout.setOnClickListener(this)
        btnNoData.setOnClickListener(this)
        btnServerError.setOnClickListener(this)
        btnTryAgain.setOnClickListener(this)

        /*  searchViewProducts.requestFocus()
          (searchViewProducts as TextView).setOnEditorActionListener { v, actionId, event ->
              if (searchViewProducts.query.length < 3) toast(getString(R.string.search_min_characters_to_search))
              true
          }*/
        //To get intent extras for banner


        searchViewProducts.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(searchText: String?): Boolean {
                if (isNetworkAccessible()) {
                    if (searchText!!.length < 3) {
                        toast(getString(R.string.search_min_characters_to_search))
                    }
                }
                return false
            }

            override fun onQueryTextChange(searchText: String?): Boolean {

                if (isNetworkAccessible()) {
                    if (searchText!!.length >= 3) {
                        keywordGlobal = searchText
                        productList.clear()
                        callSearchApi(takeCount, keywordGlobal)
//                searchView.clearFocus()
                    } /*else {
                        keywordGlobal = searchText
                        callSearchApi(takeCount, keywordGlobal)
                        productList.clear()
                        productListAdapter.notifyDataSetChanged()
                        hideNoDataAvailableScreen()
                    }*/
                    productList.clear()
                    productListAdapter.notifyDataSetChanged()
                    hideNoDataAvailableScreen()
                    if (searchText.length < 3) toast(getString(R.string.search_min_characters_to_search))
                } else showNetworkCondition()

                return false
            }

        })


    }

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

                            com.plugable.mcommerceapp.cpmvp1.utils.constants.SharedPreferences.cartItemList.clear()
                            for (item in response.body()!!.data) {
                                com.plugable.mcommerceapp.cpmvp1.utils.constants.SharedPreferences.cartItemList.add(
                                    item.productId.toString()
                                )
                            }

                            SharedPreferences.getInstance(this@SearchActivity).setStringValue(
                                com.plugable.mcommerceapp.cpmvp1.utils.constants.SharedPreferences.SHARED_PREFERENCES_CART_COUNT,
                                response.body()!!.data.size.toString()

                            )

                            invalidateOptionsMenu()


                            SharedPreferences.getInstance(this@SearchActivity)
                                .setAddToCartData(
                                    response.body()!!
                                )

                            productListAdapter.notifyDataSetChanged()

                        } else {
                            SharedPreferences.getInstance(this@SearchActivity).setStringValue(
                                com.plugable.mcommerceapp.cpmvp1.utils.constants.SharedPreferences.SHARED_PREFERENCES_CART_COUNT,
                                response.body()!!.data.size.toString()
                            )
                            invalidateOptionsMenu()


                            com.plugable.mcommerceapp.cpmvp1.utils.constants.SharedPreferences.cartItemList.clear()
                            for (item in response.body()!!.data) {
                                com.plugable.mcommerceapp.cpmvp1.utils.constants.SharedPreferences.cartItemList.add(
                                    item.productId.toString()
                                )
                            }

                            SharedPreferences.getInstance(this@SearchActivity)
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
                com.plugable.mcommerceapp.cpmvp1.utils.constants.SharedPreferences.SHARED_PREFERENCES_CART_COUNT,
                "0"
            )
        }
    }

    private fun callSearchApi(take: Int, keyword: String) {
        val apiInterface = ServiceGenerator.createService(ProjectApi::class.java)
        val call = apiInterface.globalSearchApi(take, keyword, skipCount)
        startShimmerView()

        call.enqueue(object : Callback<Products> {
            override fun onFailure(call: Call<Products>, throwable: Throwable) {
                showServerErrorMessage()

            }

            override fun onResponse(call: Call<Products>, response: Response<Products>) {


                showRecyclerViewData()
                if (response.body()?.statusCode.equals("10")) {
                    if (response.body()?.data?.productList?.isNotEmpty()!!) {
                        productList.clear()
                        response.body()?.data!!.productList.forEach {
                            if (AppDatabase.getDatabase(this@SearchActivity).productListDao().getSingleWishListProduct(
                                    it.id
                                ) != null
                            ) {
                                it.isFavorite =
                                    AppDatabase.getDatabase(this@SearchActivity).productListDao()
                                        .getSingleWishListProduct(it.id).isFavorite
                            }
                            if (keywordGlobal.length >= 3) {
                                productList.add(it)
                            } else {
                                productList.clear()
                            }
                        }
                        productListAdapter.notifyDataSetChanged()
                        if (productList.isNotEmpty()) {
                            recyclerViewSearchProduct.scrollToPosition(0)
                        }
                        sendMixPanelEvent()

                    } else {
                        if (keywordGlobal.length >= 3)
                            showNoDataAvailableScreen()
                    }
                } else {
                    if (keywordGlobal.length >= 3)
                        toast(getString(R.string.message_something_went_wrong))
                }


            }

        })
    }


    private fun sendMixPanelEvent() {
           val productObject = JSONObject()
           productObject.put(IntentFlags.MIXPANEL_KEYWORD, keywordGlobal)
           mixPanel.track(IntentFlags.MIXPANEL_SEARCHED_KEYWORD, productObject)
    }

    object LastClickTimeSingleton {
        var lastClickTime: Long = 0
    }

    override fun onItemClickListener(position: Int) {
        if (SystemClock.elapsedRealtime() - LastClickTimeSingleton.lastClickTime < 500L) return
        else {
            startActivity<ProductDetailActivity>(
                IntentFlags.PRODUCT_MODEL to productList[position]
            )
        }
        ProductsListActivity.LastClickTimeSingleton.lastClickTime = SystemClock.elapsedRealtime()
    }

    override fun onFavoriteClicked(clickedIndex: Int, isFavorite: Boolean) {
        if (isFavorite) {
            toast(getString(R.string.message_item_added))
            AppDatabase.getDatabase(this).productListDao()
                .insertAllProducts(productList[clickedIndex])

        } else {
            toast(getString(R.string.message_item_removed))
            AppDatabase.getDatabase(this).productListDao().deleteProduct(productList[clickedIndex])
        }
    }

    override fun setToolBar(name: String) {
        setSupportActionBar(toolBar)
        setStatusBarColor()
        supportActionBar?.setDisplayShowTitleEnabled(false)
        /*imgToolbarHome.setColorFilter(Color.parseColor(ApplicationThemeUtils.TEXT_COLOR))
        imgToolbarHome.invalidate()*/
        toolBar.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.TOOL_BAR_COLOR))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_shape_backarrow_white)
        imgToolbarHome.hide()
        val textView = searchViewProducts.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
        textView.setTextColor(Color.BLACK)
    }

    override fun showNetworkCondition() {

        layoutNetworkCondition.show()
        layoutNoDataScreen.hide()
        layoutServerError.hide()
        recyclerViewSearchProduct.hide()


        stopShimmerView()

    }

    override fun hideNetworkCondition() {
        layoutNetworkCondition.hide()
        layoutNoDataScreen.hide()
        layoutServerError.hide()
        recyclerViewSearchProduct.hide()

        stopShimmerView()

    }

    override fun showServerErrorMessage() {
        layoutNetworkCondition.hide()
        layoutNoDataScreen.hide()
        layoutServerError.show()
        recyclerViewSearchProduct.hide()


        stopShimmerView()

    }

    override fun hideServerErrorMessage() {
        layoutNetworkCondition.hide()
        layoutNoDataScreen.hide()
        layoutServerError.hide()
        recyclerViewSearchProduct.hide()

        stopShimmerView()

    }

    override fun showRecyclerViewData() {
        layoutNetworkCondition.hide()
        layoutNoDataScreen.hide()
        layoutServerError.hide()
        recyclerViewSearchProduct.show()

        stopShimmerView()
    }


    override fun showNoDataAvailableScreen() {
        layoutNetworkCondition.hide()
        layoutNoDataScreen.show()
        layoutServerError.hide()
        recyclerViewSearchProduct.hide()

        stopShimmerView()

    }

    override fun hideNoDataAvailableScreen() {
        layoutNetworkCondition.hide()
        layoutNoDataScreen.hide()
        layoutServerError.hide()
        recyclerViewSearchProduct.hide()
        stopShimmerView()
    }

    override fun startShimmerView() {
        shimmerViewContainerSearchProductList.show()
        shimmerViewContainerSearchProductList.startShimmer()

        layoutNetworkCondition.hide()
        layoutServerError.hide()
        layoutNoDataScreen.hide()
        recyclerViewSearchProduct.hide()
    }

    override fun stopShimmerView() {
        shimmerViewContainerSearchProductList.hide()
        shimmerViewContainerSearchProductList.stopShimmer()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgToolbarHomeLayout -> onBackPressed()
            R.id.btnTryAgain -> {
                hideNetworkCondition()
                searchViewProducts.setQuery("", false)
                searchViewProducts.requestFocus()
            }
            R.id.btnNoData -> {
                hideNoDataAvailableScreen()
                searchViewProducts.setQuery("", false)
                searchViewProducts.requestFocus()

            }
            R.id.btnServerError -> {
                hideServerErrorMessage()
                searchViewProducts.setQuery("", false)
                searchViewProducts.requestFocus()

            }
        }
    }

    override fun onDestroy() {
        mixPanel.flush()
        super.onDestroy()
    }


}
