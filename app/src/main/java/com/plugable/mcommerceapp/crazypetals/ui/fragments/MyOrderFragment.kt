/*
 * Author : Chetan Patil.
 * Module : My Order module
 * Version : V 1.0
 * Sprint : 13
 * Date of Development : 07/10/2019
 * Date of Modified : 07/10/2019
 * Comments : My order screen
 * Output : List all the Order
 */
package com.plugable.mcommerceapp.crazypetals.ui.fragments


import ServiceGenerator
import android.graphics.Color
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.callbacks.EventListener
import com.plugable.mcommerceapp.crazypetals.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.GetCartResponse
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.MyOrder
import com.plugable.mcommerceapp.crazypetals.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.crazypetals.registration.LoginActivity
import com.plugable.mcommerceapp.crazypetals.ui.activities.CartActivity
import com.plugable.mcommerceapp.crazypetals.ui.activities.DashboardActivity
import com.plugable.mcommerceapp.crazypetals.ui.activities.OrderDetailActivity
import com.plugable.mcommerceapp.crazypetals.ui.adapters.MyOrderAdapter
import com.plugable.mcommerceapp.crazypetals.utils.application.App
import com.plugable.mcommerceapp.crazypetals.utils.constants.IntentFlags
import com.plugable.mcommerceapp.crazypetals.utils.extension.hide
import com.plugable.mcommerceapp.crazypetals.utils.extension.show
import com.plugable.mcommerceapp.crazypetals.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.crazypetals.utils.util.CountDrawable
import com.plugable.mcommerceapp.crazypetals.utils.util.isNetworkAccessible
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.fragment_my_order.*
import kotlinx.android.synthetic.main.layout_network_condition.*
import kotlinx.android.synthetic.main.layout_no_data_condition.*
import kotlinx.android.synthetic.main.layout_no_order_list.*
import kotlinx.android.synthetic.main.layout_server_error_condition.*
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [MyOrderFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class MyOrderFragment : BaseFragment(), EventListener {

    private lateinit var myOrderAdapter: MyOrderAdapter
    private var orderArrayList = ArrayList<MyOrder.DataItem?>()
    private var searchOrderArrayList = ArrayList<MyOrder.DataItem?>()
    private lateinit var mixPanel: MixpanelAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_order, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializeTheme()
        initializeViews()
        getMyOrders()
    }

    private fun initializeTheme() {
        val configDetail = SharedPreferences.getInstance(activity!!).themeDataPreference
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
        buttonBrowseMore.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
    }

    private fun initializeViews() {
        btnServerError.setOnClickListener(this)
        btnTryAgain.setOnClickListener(this)
        btnNoData.setOnClickListener(this)
        buttonBrowseMore.setOnClickListener(this)

        myOrderAdapter = MyOrderAdapter(activity!!, orderArrayList, this)
        recyclerViewOrders.itemAnimator = DefaultItemAnimator()
        recyclerViewOrders.adapter = myOrderAdapter

        mixPanel = MixpanelAPI.getInstance(context, resources.getString(R.string.mix_panel_token))
        sendMixPanelEvent()


        etSearch.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if (s.isNotEmpty()) {
                    orderArrayList.clear()
                    for (order in searchOrderArrayList) {
                        if (order!!.orderNumber.toString().contains(s)) {
                            orderArrayList.add(order)

                        }
                    }

                } else {
                    orderArrayList.clear()
                    orderArrayList.addAll(searchOrderArrayList)

                }
                recyclerViewOrders.adapter = myOrderAdapter

                if (orderArrayList.isEmpty()) {
                    showNoDataAvailableScreen()
                } else {
                    showRecyclerViewData()
                }
            }
        })

    }

    private fun sendMixPanelEvent() {
        val productObject = JSONObject()
        mixPanel.track(IntentFlags.MIXPANEL_VISITED_MY_ORDER_SCREEN, productObject)
    }


    override fun showNetworkCondition() {
        layoutNetworkCondition.show()
        layoutServerError.hide()
        recyclerViewOrders.hide()
        layoutNoDataScreen.hide()
        searchLayout.hide()
        stopShimmerView()

    }

    override fun hideNetworkCondition() {
        layoutNetworkCondition.hide()
        layoutNoDataScreen.hide()
        recyclerViewOrders.hide()
        searchLayout.show()

        stopShimmerView()

    }

    override fun showServerErrorMessage() {
        layoutNetworkCondition.hide()
        layoutServerError.show()
        recyclerViewOrders.hide()
        layoutNoDataScreen.hide()
        searchLayout.hide()
        stopShimmerView()
    }

    override fun hideServerErrorMessage() {
        layoutNetworkCondition.hide()
        layoutServerError.hide()
        recyclerViewOrders.hide()
        layoutNoDataScreen.hide()


        stopShimmerView()

    }

    override fun showRecyclerViewData() {
        layoutNetworkCondition.hide()
        layoutServerError.hide()
        recyclerViewOrders.show()
        layoutNoDataScreen.hide()
        searchLayout.show()
        stopShimmerView()
    }


    fun showEmptyScreen() {
        layoutNetworkCondition.hide()
        layoutServerError.hide()
        recyclerViewOrders.hide()
        layoutNoDataScreen.hide()
        searchLayout.hide()
        layout_no_order_list.show()


        stopShimmerView()

    }

    override fun showNoDataAvailableScreen() {
        layoutNetworkCondition.hide()
        layoutServerError.hide()
        recyclerViewOrders.hide()
        layoutNoDataScreen.show()

        stopShimmerView()
    }

    override fun hideNoDataAvailableScreen() {
        layoutNetworkCondition.hide()
        layoutNoDataScreen.hide()
        layoutServerError.hide()

        recyclerViewOrders.hide()
        stopShimmerView()
    }

    override fun startShimmerView() {
        shimmerViewContainerProductList.show()
        shimmerViewContainerProductList.startShimmer()

        layoutNetworkCondition.hide()
        layoutServerError.hide()
        recyclerViewOrders.hide()
        layoutNoDataScreen.hide()
    }

    override fun stopShimmerView() {
        shimmerViewContainerProductList.hide()
        shimmerViewContainerProductList.stopShimmer()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.buttonBrowseMore -> {
                fragmentManager?.beginTransaction()?.replace(R.id.fragmentContainer, HomeFragment())
                    ?.commit()
                (activity as DashboardActivity).nav_view?.menu?.getItem(1)?.isChecked = true
                (activity as DashboardActivity).setToolBar(ApplicationThemeUtils.APP_NAME)
            }
            R.id.btnNoData -> {
                if (etSearch.text.isNullOrEmpty()) {
                    getMyOrders()
                } else {
                    etSearch.text.clear()
                    orderArrayList.clear()
                    orderArrayList.addAll(searchOrderArrayList)
                    recyclerViewOrders.adapter = myOrderAdapter
                }
            }
            R.id.btnTryAgain -> {
                getMyOrders()
            }
            R.id.buttonBrowseMore -> {
                fragmentManager?.beginTransaction()?.replace(R.id.fragmentContainer, HomeFragment())
                    ?.commit()
                (activity as DashboardActivity).nav_view?.menu?.getItem(0)?.isChecked = true
                (activity as DashboardActivity).setToolBar(ApplicationThemeUtils.APP_NAME)
            }

        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.common_menu, menu)

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

        if (cartCountText == "10") {
            cartCountText = "9+"
        }
        cartBadge.setCount(cartCountText)
        cartIcon.mutate()
        cartIcon.setDrawableByLayerId(R.id.ic_group_count_cart, cartBadge)

        super.onCreateOptionsMenu(menu, inflater)

    }

    private lateinit var cartListApi: Call<GetCartResponse>

    override fun onPause() {
        super.onPause()
        if (::cartListApi.isInitialized && cartListApi != null) cartListApi.cancel()
    }


    object LastClickTimeSingleton {
        var lastClickTime: Long = 0
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_cart -> {

                if (SharedPreferences.getInstance(context!!).isUserLoggedIn) {
                    if (SystemClock.elapsedRealtime() - LastClickTimeSingleton.lastClickTime < 500L) return true
                    else {

                        startActivity<CartActivity>(IntentFlags.REDIRECT_FROM to IntentFlags.ORDER_DETAIL)
                    }

                    LastClickTimeSingleton.lastClickTime = SystemClock.elapsedRealtime()

                } else {
                    startActivity<LoginActivity>(
                        IntentFlags.SHOULD_GO_TO_DASHBOARD to false,
                        IntentFlags.FRAGMENT_TO_BE_LOADED to R.id.nav_my_order
                    )
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        activity!!.invalidateOptionsMenu()
    }

    override fun onItemClickListener(position: Int) {
        startActivity<OrderDetailActivity>(
            "order_id" to orderArrayList[position]!!.id.toString(),
            IntentFlags.REDIRECT_FROM to IntentFlags.ORDER_DETAIL
        )
    }

    private fun getMyOrders() {
        if (activity?.isNetworkAccessible()!!) {
            callMyOrdersListAPI()
        } else {
            showNetworkCondition()
        }
    }

    var call: Call<MyOrder>? = null
    private fun callMyOrdersListAPI() {
        startShimmerView()
        App.HostUrl = SharedPreferences.getInstance(activity!!).hostUrl!!
        val applicationUserId =
            SharedPreferences.getInstance(activity!!)
                .getStringValue(IntentFlags.APPLICATION_USER_ID)
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        call = clientInstance.getMyOrders(applicationUserId!!)

        call?.enqueue(object : Callback<MyOrder> {
            override fun onFailure(call: Call<MyOrder>?, t: Throwable?) {
                if (!call!!.isCanceled) {
                    showServerErrorMessage()
                }
            }

            override fun onResponse(call: Call<MyOrder>?, response: Response<MyOrder>?) {
                if (!call!!.isCanceled) {
                    stopShimmerView()
                    if (response?.body()?.statusCode.equals("10")) {
                        etSearch.text.clear()
                        orderArrayList.clear()
                        orderArrayList.addAll(response!!.body()!!.data!!)
                        searchOrderArrayList.addAll(response.body()!!.data!!)
                        myOrderAdapter.notifyDataSetChanged()

                        if (orderArrayList.isEmpty()) {
                            showEmptyScreen()
                        } else {
                            showRecyclerViewData()
                        }
                    } else {
                        toast(getString(R.string.message_something_went_wrong))
                    }
                }

            }

        })

    }

    override fun onDestroyView() {
        if (call != null) {
            call!!.cancel()
        }
        mixPanel.flush()
        super.onDestroyView()
    }

}