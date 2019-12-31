package com.plugable.mcommerceapp.cpmvp1.ui.fragments

import ServiceGenerator
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.callbacks.EventListener
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.Banners
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.Categories
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.GetCartResponse
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.Notifications
import com.plugable.mcommerceapp.cpmvp1.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.cpmvp1.registration.LoginActivity
import com.plugable.mcommerceapp.cpmvp1.ui.activities.CartActivity
import com.plugable.mcommerceapp.cpmvp1.ui.activities.NotificationActivity
import com.plugable.mcommerceapp.cpmvp1.ui.activities.ProductsListActivity
import com.plugable.mcommerceapp.cpmvp1.ui.activities.SearchActivity
import com.plugable.mcommerceapp.cpmvp1.ui.adapters.HelpSliderAdapter
import com.plugable.mcommerceapp.cpmvp1.ui.adapters.LimitedCategoryListAdapter
import com.plugable.mcommerceapp.cpmvp1.utils.application.App
import com.plugable.mcommerceapp.cpmvp1.utils.constants.IntentFlags
import com.plugable.mcommerceapp.cpmvp1.utils.constants.SharedPreferences.SHARED_PREFERENCES_LAST_NOTIFICATION_TIME
import com.plugable.mcommerceapp.cpmvp1.utils.constants.SharedPreferences.SHARED_PREFERENCES_NOTIFICATION_COUNT
import com.plugable.mcommerceapp.cpmvp1.utils.constants.SharedPreferences.cartItemList
import com.plugable.mcommerceapp.cpmvp1.utils.extension.hide
import com.plugable.mcommerceapp.cpmvp1.utils.extension.invisible
import com.plugable.mcommerceapp.cpmvp1.utils.extension.show
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.cpmvp1.utils.util.CountDrawable
import com.plugable.mcommerceapp.cpmvp1.utils.util.isNetworkAccessible
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.layout_network_condition.*
import kotlinx.android.synthetic.main.layout_no_data_condition.*
import kotlinx.android.synthetic.main.layout_server_error_condition.*
import org.jetbrains.anko.support.v4.startActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.concurrent.fixedRateTimer


/**
 * Use the newInstance factory method to
 * create an instance of this fragment.
 *
 */

/**
 * [Home Fragment] is used to load help data as well as limited categories
 *
 */
/**
 * mHandler use to post delay the scroll of view pager
 */
private var mHandler: Handler? = null
/**
 * mRunnable used for threading the scroll of view pager
 */
private lateinit var mRunnable: Runnable
/**
 * Timer use for fix rate time span between view pager scroll
 */
private lateinit var fixedRateTimer: java.util.Timer


class HomeFragment : BaseFragment(), EventListener, View.OnClickListener,
    HelpSliderAdapter.OnBannerClick {
    private lateinit var bannerListApi: Call<Banners>
    override fun onClick(view: View?) {

        when (view?.id) {
            R.id.btnTryAgain -> {
                attemptNotificationApiCall()
                attemptBannerApiCall()
                attemptApiCall()
            }

            R.id.btnNoData -> {
                attemptNotificationApiCall()
                attemptBannerApiCall()
                attemptApiCall()
            }

            R.id.btnServerError -> {
                attemptNotificationApiCall()
                attemptBannerApiCall()
                attemptApiCall()
            }

            R.id.searchLayout -> {
                if (context?.isNetworkAccessible()!!) {
                    startActivity<SearchActivity>()
                } else {
                    showNetworkCondition()
                }
            }
        }
    }

    private var bannerImages = ArrayList<Banners.Data.Banner>()
    //    private lateinit var mixPanel: MixpanelAPI
    //    private var position: Int = 0
    var category: Categories.Data.Category? = null
    private var banner: Banners.Data.Banner? = null
    lateinit var categoryListAdapter: LimitedCategoryListAdapter
    var categoryList = ArrayList<Categories.Data.Category>()
    private lateinit var eventClickListener: EventListener
    private lateinit var notificationListApi: Call<Notifications>
    private lateinit var cartListApi: Call<GetCartResponse>
    private lateinit var categoriesListApi: Call<Categories>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews()
        attemptApiCall()
        attemptBannerApiCall()
        attemptCartApiCall()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment


        return inflater.inflate(R.layout.fragment_home, container, false)

    }

    override fun onStop() {
        super.onStop()
        cancelTasks()
    }

    private fun cancelTasks() {
        if (::cartListApi.isInitialized && cartListApi != null) cartListApi.cancel()
        if (::bannerListApi.isInitialized && bannerListApi != null) bannerListApi.cancel()
        if (::notificationListApi.isInitialized && notificationListApi != null) notificationListApi.cancel()
        if (::categoriesListApi.isInitialized && categoriesListApi != null) categoriesListApi.cancel()
    }

    private fun attemptCartApiCall() {
        if (SharedPreferences.getInstance(activity!!).isUserLoggedIn) {

            val applicationUserId =
                SharedPreferences.getInstance(activity!!)
                    .getStringValue(IntentFlags.APPLICATION_USER_ID)
            App.HostUrl = SharedPreferences.getInstance(context!!).hostUrl!!
            val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
            cartListApi = clientInstance.getCartApi(applicationUserId!!.toInt())

            cartListApi.enqueue(object : Callback<GetCartResponse> {
                override fun onFailure(call: Call<GetCartResponse>, t: Throwable) {
//                    toast(getString(R.string.message_something_went_wrong))
                }

                override fun onResponse(
                    call: Call<GetCartResponse>,
                    response: Response<GetCartResponse>
                ) {
                    if (!isVisible) {
                        return
                    }
                    if (response.body()?.statusCode.equals("10")) {
                        if (response.body()!!.data.isNotEmpty()) {
                            cartItemList.clear()
                            for (item in response.body()!!.data) {
                                cartItemList.add(item.productId.toString())
                            }

                            SharedPreferences.getInstance(activity!!)
                                .setCartCountString(response.body()!!.count.toString())

                        } else {
                            SharedPreferences.getInstance(activity!!)
                                .setCartCountString("0")
                        }
                    } else {
                        SharedPreferences.getInstance(activity!!)
                            .setCartCountString("0")
                    }

                    activity!!.invalidateOptionsMenu()

                }

            })
        } else {
            SharedPreferences.getInstance(activity!!)
                .setCartCountString("0")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dashboard, menu)

        //Custom notification badge count
        val menuItem = menu.findItem(R.id.action_notification)
        val icon = menuItem.icon as LayerDrawable

        val badge: CountDrawable

        // Reuse drawable if possible
        val reuse = icon.findDrawableByLayerId(R.id.ic_group_count)
        if (reuse != null && reuse is CountDrawable) {
            badge = reuse
        } else {
            badge = CountDrawable(activity!!)
        }
        var notificationCountText = SharedPreferences.getInstance(activity!!)
            .getStringValue(SHARED_PREFERENCES_NOTIFICATION_COUNT)!!


        if (notificationCountText.isEmpty()) {
            notificationCountText = "0"
        } else if (notificationCountText == "10") {
            notificationCountText = "9+"
        }
        badge.setCount(notificationCountText)
        icon.mutate()
        icon.setDrawableByLayerId(R.id.ic_group_count, badge)

        //Custom cart badge count
        val cartItem = menu.findItem(R.id.action_cart)
        val cartIcon = cartItem.icon as LayerDrawable

        val cartBadge: CountDrawable

        // Reuse drawable if possible
        val reuseIcon = cartIcon.findDrawableByLayerId(R.id.ic_group_count_cart)
        if (reuseIcon != null && reuseIcon is CountDrawable) {
            cartBadge = reuseIcon
        } else {
            cartBadge = CountDrawable(activity!!)
        }

        var cartCountText = SharedPreferences.getInstance(activity!!).getCartCountString()!!

        if (cartCountText == "15") {
            cartCountText = "9+"
        }
        cartBadge.setCount(cartCountText)
        cartIcon.mutate()
        cartIcon.setDrawableByLayerId(R.id.ic_group_count_cart, cartBadge)


        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_cart -> {
                if (SharedPreferences.getInstance(context!!).isUserLoggedIn) {
                    if (SystemClock.elapsedRealtime() - LastClickTimeSingleton.lastClickTime < 500L) return true
                    else {

                        startActivity<CartActivity>(
                            IntentFlags.REDIRECT_FROM to IntentFlags.HOME_FRAGMENT
                        )
                    }

                    LastClickTimeSingleton.lastClickTime = SystemClock.elapsedRealtime()

                } else {
                    if (SystemClock.elapsedRealtime() - LastClickTimeSingleton.lastClickTime < 500L) return true
                    else {

                        startActivity<LoginActivity>(
                            IntentFlags.SHOULD_GO_TO_DASHBOARD to false,
                            IntentFlags.FRAGMENT_TO_BE_LOADED to R.id.nav_home
                        )
                    }

                    LastClickTimeSingleton.lastClickTime = SystemClock.elapsedRealtime()

                }
            }

            R.id.action_notification -> {
                startActivity<NotificationActivity>()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun attemptApiCall() {
        if (activity?.isNetworkAccessible()!!) {
            callCategoryListApi()
        } else showNetworkCondition()
    }

    private fun attemptBannerApiCall() {
        if (activity?.isNetworkAccessible()!!) {
            callBannerListApi()
        } else showNetworkCondition()
    }

    private fun attemptNotificationApiCall() {
        if (activity?.isNetworkAccessible()!!) {
            callNotificationsListAPI()
        } else showNetworkCondition()
    }

    //fetch notifications from notification api
    private fun callNotificationsListAPI() {
        val pageIndex = 1
        val pageSize = 15

        val applicationUserId = SharedPreferences.getInstance(activity!!)
            .getStringValue(IntentFlags.APPLICATION_USER_ID)
        App.HostUrl = SharedPreferences.getInstance(activity!!).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        notificationListApi =
            clientInstance.getAllNotificationApi(pageIndex, pageSize, applicationUserId!!)

        notificationListApi.enqueue(object : Callback<Notifications> {
            override fun onFailure(call: Call<Notifications>?, t: Throwable?) {

            }

            override fun onResponse(
                call: Call<Notifications>?,
                response: Response<Notifications>?
            ) {
                if (response?.body()?.statusCode.equals("10")) {

                    val notificationTime = SharedPreferences.getInstance(activity!!)
                        .getStringValue(SHARED_PREFERENCES_LAST_NOTIFICATION_TIME)

                    var notificationCount = 0
                    if (notificationTime.isNullOrEmpty()) {
                        notificationCount = response!!.body()!!.data!!.notificationList.size
                    } else {

                        //Calculate notification unread count based on last notification read
                        for (notification in response!!.body()!!.data!!.notificationList) {
                            val compareTimeStamp =
                                java.sql.Timestamp(notification!!.timeStamp!!.toLong())
                            if (java.sql.Timestamp(notificationTime.toLong()).before(
                                    compareTimeStamp
                                )
                            ) {
                                notificationCount += 1
                            }
                        }

                    }

                    //Update notification count into the SharedPreferences
                    SharedPreferences.getInstance(activity!!).setStringValue(
                        SHARED_PREFERENCES_NOTIFICATION_COUNT,
                        "" + notificationCount
                    )

                    //invalidate option menu for refreshing notification icon highlight
                    activity!!.invalidateOptionsMenu()
                }

            }

        })

    }

    private fun callBannerListApi() {
        startShimmerView()
        App.HostUrl = SharedPreferences.getInstance(activity!!).hostUrl!!
//        Log.e("host url=", App.HostUrl)
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        bannerListApi = clientInstance.getBanners(100, 0)
        bannerListApi.enqueue(object : Callback<Banners> {
            override fun onFailure(call: Call<Banners>, t: Throwable) {
//                toast(getString(R.string.message_something_went_wrong))
            }

            override fun onResponse(call: Call<Banners>, response: Response<Banners>) {
                if (response.isSuccessful) {
                    if (response.body()?.statusCode.equals("10")) {
                        showAllViews()
                        if (response.body()?.data?.bannerList?.isNotEmpty()!!) {
                            bannerImages.clear()
                            bannerImages.addAll(response.body()!!.data.bannerList)
                            NUM_PAGES = bannerImages.size
                            if (NUM_PAGES == 1) {
                                if (dotsIndicatorDashboard != null) dotsIndicatorDashboard.hide()
                            } else {
                                if (dotsIndicatorDashboard != null) dotsIndicatorDashboard.show()
                            }
                        }

                        if (viewPagerDashboard != null) viewPagerDashboard.adapter?.notifyDataSetChanged()


                    }
                }
            }

        })

    }

    companion object {
        var NUM_PAGES = 0
    }

    private fun initializeViews() {
        btnNoData.setOnClickListener(this)
        btnServerError.setOnClickListener(this)
        btnTryAgain.setOnClickListener(this)

        searchLayout.setOnClickListener(this)

//        mixPanel = MixpanelAPI.getInstance(context, resources.getString(R.string.mix_panel_token))

        categoryList = arrayListOf()


        dotsIndicatorDashboard.setDotIndicatorColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        dotsIndicatorDashboard.setStrokeDotsIndicatorColor(Color.parseColor(ApplicationThemeUtils.TOOL_BAR_COLOR))
        viewPagerDashboard.adapter = HelpSliderAdapter(context!!, bannerImages, this)
        dotsIndicatorDashboard.setViewPager(viewPagerDashboard)
        eventClickListener = this

        categoryListAdapter =
            LimitedCategoryListAdapter(categoryList, context!!, eventClickListener)
        val gridLayoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)

        recyclerViewLimitedCategory.layoutManager =
            gridLayoutManager // set LayoutManager to RecyclerView
        recyclerViewLimitedCategory.adapter = categoryListAdapter

        setThemeToComponents()

    }

    private fun setThemeToComponents() {

        btnTryAgain.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        btnNoData.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        btnServerError.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
    }

    override fun onStart() {
        super.onStart()

        //Fix Rate timer use to perform scroll action after every 3000 millis
        fixedRateTimer = fixedRateTimer(
            name = getString(R.string.hello_timer),
            initialDelay = 3000, period = 3000
        ) {
            mHandler?.postDelayed(mRunnable, 3000)
        }

        dotsIndicatorDashboard.setDotIndicatorColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        dotsIndicatorDashboard.setStrokeDotsIndicatorColor(Color.parseColor(ApplicationThemeUtils.TOOL_BAR_COLOR))

        dotsIndicatorDashboard.setViewPager(viewPagerDashboard)
    }

    override fun onResume() {
        super.onResume()
        //invalidate option menu for refreshing notification icon highlight
        activity!!.invalidateOptionsMenu()

        //Set logical no for current item is one
        var currentItem = 1

        mHandler = Handler()
        //check if current item is reach list size other wise increase it
        //set view pager current item to position derived from current item
        mRunnable = Runnable {
            if (currentItem == bannerImages.size) {
                currentItem = 1
            } else {
                currentItem++
            }
            viewPagerDashboard?.setCurrentItem(currentItem - 1, true)
        }


    }
    /*

    private fun sendMixPanelEvent() {
            val productObject = JSONObject()
            productObject.put(IntentFlags.MIXPANEL_PRODUCT_ID, category?.id)
            mixPanel.track(IntentFlags.MIXPANEL_VISITED_DASHBOARD, productObject)
    }
*/

    override fun onPause() {
        super.onPause()
        //To stop timer
        fixedRateTimer.cancel()
    }

    override fun onItemClickListener(position: Int) {
        if (SystemClock.elapsedRealtime() - LastClickTimeSingleton.lastClickTime < 500L) return
        else {
            val categoryItems = categoryList[position]
            val intent = Intent(activity, ProductsListActivity::class.java)
            intent.putExtra(IntentFlags.CATEGORY_ID, categoryList[position].id)
            intent.putExtra(IntentFlags.CATEGORY_NAME, categoryItems.name)
            startActivity(intent)
        }
        LastClickTimeSingleton.lastClickTime = SystemClock.elapsedRealtime()

    }

    object LastClickTimeSingleton {
        var lastClickTime: Long = 0
    }

    override fun onBannerClick(position: Int) {

        if (SystemClock.elapsedRealtime() - LastClickTimeSingleton.lastClickTime < 500L) return
        else {

            val bannerTitle = bannerImages[position].title
            val intent = Intent(context, SearchActivity::class.java)
            intent.putExtra(getString(R.string.intent_banner_search), bannerTitle)
            startActivity(intent)
        }
        LastClickTimeSingleton.lastClickTime = SystemClock.elapsedRealtime()

    }

    private fun callCategoryListApi() {
        startShimmerView()
        App.HostUrl = SharedPreferences.getInstance(activity!!).hostUrl!!
//        Log.e("host url=", App.HostUrl)
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        categoriesListApi = clientInstance.getCategories(20, 0)
        categoriesListApi.enqueue(object : Callback<Categories> {
            override fun onFailure(call: Call<Categories>?, t: Throwable?) {
                if (isFragmentAlive()) showServerErrorMessage()
            }

            override fun onResponse(call: Call<Categories>?, response: Response<Categories>?) {
                if (isFragmentAlive()) {
                    showRecyclerViewData()
                    if (response?.isSuccessful!!) {
                        if (response.body()?.statusCode?.equals("10")!!) {
                            if (response.body()?.data!!.categoryList.isNotEmpty()) {

                                categoryList.clear()
                                categoryList.addAll(response.body()?.data?.categoryList!!)
                                Log.e("priceDetailFields size", categoryList.size.toString())
                                categoryListAdapter.notifyDataSetChanged()
//                                sendMixPanelEvent()
                                nestedScrollView.scrollTo(0,0)

                            } else {
                                showNoDataAvailableScreen()
                            }
                        } else {
                            showServerErrorMessage()
                        }
                    } else {
                        showServerErrorMessage()
                    }
                }

            }

        })
    }

    override fun showNetworkCondition() {
        layoutNetworkCondition.show()
        layoutNoDataScreen.hide()
        layoutServerError.hide()

        hideAllViews()


        stopShimmerView()
    }

    override fun hideNetworkCondition() {
        layoutNetworkCondition.hide()
        layoutNoDataScreen.hide()
        layoutServerError.hide()
        hideAllViews()

        stopShimmerView()
    }

    override fun showRecyclerViewData() {
        layoutNetworkCondition.hide()
        layoutNoDataScreen.hide()
        layoutServerError.hide()
        showAllViews()

    }

    override fun showServerErrorMessage() {
        layoutNetworkCondition.hide()
        layoutNoDataScreen.hide()
        layoutServerError.show()
        hideAllViews()


        stopShimmerView()
    }

    override fun hideServerErrorMessage() {
        layoutNetworkCondition.hide()
        layoutNoDataScreen.hide()
        layoutServerError.hide()
        hideAllViews()
        stopShimmerView()

    }

    override fun showNoDataAvailableScreen() {
        layoutNetworkCondition.hide()
        layoutNoDataScreen.show()
        layoutServerError.hide()
        hideAllViews()
        stopShimmerView()

    }

    override fun hideNoDataAvailableScreen() {
        layoutNetworkCondition.hide()
        layoutNoDataScreen.hide()
        layoutServerError.hide()
        hideAllViews()
        stopShimmerView()
    }

    override fun startShimmerView() {
        shimmerViewContainerCategoryList.show()
        shimmerViewContainerCategoryList.startShimmer()

        layoutNetworkCondition.hide()
        layoutServerError.hide()
        layoutNoDataScreen.hide()
        hideAllViews()
    }

    override fun stopShimmerView() {
        shimmerViewContainerCategoryList.hide()
        shimmerViewContainerCategoryList.stopShimmer()
    }

    private fun hideAllViews() {
        viewPagerDashboard.hide()
        dotsIndicatorDashboard.hide()
        searchLayout.invisible()
        recyclerViewLimitedCategory.invisible()
    }

    private fun showAllViews() {
        if (isVisible) {
            viewPagerDashboard.show()
            dotsIndicatorDashboard.show()
            searchLayout.show()
            recyclerViewLimitedCategory.show()
            stopShimmerView()
        }
    }

    private fun isFragmentAlive(): Boolean {
        return activity?.supportFragmentManager?.findFragmentById(R.id.fragmentContainer) is HomeFragment

    }

    /*  override fun onDestroy() {
          mixPanel.flush()
          super.onDestroy()
      }
  */
}
