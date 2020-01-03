package com.plugable.mcommerceapp.crazypetals.ui.activities

import ServiceGenerator
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.callbacks.EventListener
import com.plugable.mcommerceapp.crazypetals.callbacks.OnButtonClickListener
import com.plugable.mcommerceapp.crazypetals.callbacks.OnFavoriteListener
import com.plugable.mcommerceapp.crazypetals.callbacks.SetOnBottomReachedListener
import com.plugable.mcommerceapp.crazypetals.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.crazypetals.mcommerce.db.AppDatabase
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.Products
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.RequestAddToCart
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.ResponseAddToCart
import com.plugable.mcommerceapp.crazypetals.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.crazypetals.registration.LoginActivity
import com.plugable.mcommerceapp.crazypetals.ui.adapters.ProductListAdapter
import com.plugable.mcommerceapp.crazypetals.ui.fragments.WishListFragment
import com.plugable.mcommerceapp.crazypetals.utils.application.App
import com.plugable.mcommerceapp.crazypetals.utils.constants.IntentFlags
import com.plugable.mcommerceapp.crazypetals.utils.extension.*
import com.plugable.mcommerceapp.crazypetals.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.crazypetals.utils.util.isNetworkAccessible
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.layout_network_condition.*
import kotlinx.android.synthetic.main.layout_no_data_condition.*
import kotlinx.android.synthetic.main.layout_search_toolbar.*
import kotlinx.android.synthetic.main.layout_server_error_condition.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
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

    private lateinit var searchApi: Call<Products>
    //    private lateinit var mixPanel: MixpanelAPI
    var productList = ArrayList<Products.Data.ProductDetails>()
    private lateinit var productListAdapter: ProductListAdapter
//    private lateinit var onBottomReachedListener: SetOnBottomReachedListener

    private var skipCount = 0
    private var takeCount = 10
    private var keywordGlobal = ""
    var isLoading = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initializeTheme()
        initializeViews()

        if (isNetworkAccessible()) {
            if (intent.getStringExtra(getString(R.string.intent_banner_search)) != null) {
                val bannerSearch = intent.getStringExtra(getString(R.string.intent_banner_search))!!

                if (bannerSearch.isNotEmpty()) {
                    keywordGlobal = bannerSearch
                    if (isNetworkAccessible())
                        searchViewProducts.setQuery(bannerSearch, true)
                    searchViewProducts.clearFocus()
                    keywordGlobal = bannerSearch

                }
            } else
                if (searchViewProducts.query.isNotEmpty() && searchViewProducts.query.length >= 3) {
                    callSearchApi(keywordGlobal)
                    showProgress()
                } else {
                    if (searchViewProducts.query.length < 3) toast(getString(R.string.search_min_characters_to_search))
                }

        } else
            showNetworkCondition()

    }

    override fun onResume() {
        super.onResume()

        invalidateOptionsMenu()

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
        progressBarSearchLoadMore.indeterminateDrawable.setColorFilter(
            Color.BLACK,
            PorterDuff.Mode.MULTIPLY
        )

    }

    private fun initializeViews() {
//        mixPanel = MixpanelAPI.getInstance(this, resources.getString(R.string.mix_panel_token))

//        onBottomReachedListener=this
        productListAdapter = ProductListAdapter(productList, this, this, this, this)
        val gridLayoutManager = GridLayoutManager(this, 2)
        gridLayoutManager.orientation =
            RecyclerView.VERTICAL // set Horizontal Orientation
        recyclerViewSearchProduct.layoutManager = gridLayoutManager
        recyclerViewSearchProduct.itemAnimator = DefaultItemAnimator()
        recyclerViewSearchProduct.adapter = productListAdapter

        recyclerViewSearchProduct.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?

                if (!isLoading) {
                    if (linearLayoutManager != null && productList.size > 8 &&
                        linearLayoutManager.findLastCompletelyVisibleItemPosition() == productList.size - 1
                    ) {
                        //bottom of list!

                        skipCount = skipCount + takeCount

                        isLoading = true
                        progressBarSearchLoadMore.show()
                        callSearchApi(keywordGlobal)
                        showProgress()
                    }
                }
            }
        })

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
                    if (searchText!!.trim().length >= 3) {
                        keywordGlobal = searchText.trim()
                        productList.clear()
                        skipCount = 0
                        callSearchApi(keywordGlobal)
                        showProgress()
                    }
                    productList.clear()
                    productListAdapter.notifyDataSetChanged()
                    hideNoDataAvailableScreen()
                    if (searchText.trim().length < 3) toast(getString(R.string.search_min_characters_to_search))
                } else showNetworkCondition()

                return false
            }

        })


    }

    private fun callSearchApi(keyword: String) {
        val apiInterface = ServiceGenerator.createService(ProjectApi::class.java)
        searchApi = apiInterface.globalSearchApi(takeCount, keyword, skipCount)

        if (productList.isEmpty())
            startShimmerView()

        searchApi.enqueue(object : Callback<Products> {
            override fun onFailure(call: Call<Products>, throwable: Throwable) {
                showServerErrorMessage()
                hideProgress()
            }

            override fun onResponse(call: Call<Products>, response: Response<Products>) {

                hideProgress()
                if (response.body()?.statusCode.equals("10")) {
                    if (response.body()?.data?.productList?.isNotEmpty()!!) {
                        if (skipCount == 0) {
                            productList.clear()
                            recyclerViewSearchProduct.scrollToPosition(0)
                        }
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

//                        sendMixPanelEvent()

                    } else if (skipCount == 0 && response.body()?.data?.productList?.isEmpty()!!) {
                        hideProgress()
                        if (keywordGlobal.length >= 3)
                            showNoDataAvailableScreen()
                    } else if (isLoading && skipCount != 0) {
                        hideProgress()
                        skipCount -= takeCount
                    } else {
                        hideProgress()
                    }
                } else {
                    hideProgress()
                    if (keywordGlobal.length >= 3)
                        toast(getString(R.string.message_something_went_wrong))
                }

                progressBarSearchLoadMore.hide()
                isLoading = false
            }

        })
    }

/*

    private fun sendMixPanelEvent() {
        val productObject = JSONObject()
        productObject.put(IntentFlags.MIXPANEL_KEYWORD, keywordGlobal)
        mixPanel.track(IntentFlags.MIXPANEL_SEARCHED_KEYWORD, productObject)
    }
*/

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
        val textView =
            searchViewProducts.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
        textView.setTextColor(Color.BLACK)
    }

    fun showProgress() {
        recyclerViewSearchProduct.hide()
        progressBarSearchLoadMore.show()
        this.disableWindowClicks()
    }

    fun hideProgress() {
        showRecyclerViewData()

        progressBarSearchLoadMore.hide()
        this.enableWindowClicks()
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
/*

    override fun onDestroy() {
        mixPanel.flush()
        super.onDestroy()
    }
*/

    override fun onStop() {
        super.onStop()
        cancelTasks()
    }

    private fun cancelTasks() {
        if (::searchApi.isInitialized && searchApi != null) searchApi.cancel()
    }
}
