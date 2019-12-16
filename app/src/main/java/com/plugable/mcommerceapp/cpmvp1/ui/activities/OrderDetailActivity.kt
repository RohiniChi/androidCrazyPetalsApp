package com.plugable.mcommerceapp.cpmvp1.ui.activities

import ServiceGenerator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.OrderDetailResponse
import com.plugable.mcommerceapp.cpmvp1.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.cpmvp1.ui.fragments.OrderDetailFragment
import com.plugable.mcommerceapp.cpmvp1.ui.fragments.OrderProductListFragment
import com.plugable.mcommerceapp.cpmvp1.utils.application.App
import com.plugable.mcommerceapp.cpmvp1.utils.constants.IntentFlags
import com.plugable.mcommerceapp.cpmvp1.utils.extension.hide
import com.plugable.mcommerceapp.cpmvp1.utils.extension.setStatusBarColor
import com.plugable.mcommerceapp.cpmvp1.utils.extension.setToolBarColor
import com.plugable.mcommerceapp.cpmvp1.utils.extension.show
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.cpmvp1.utils.util.isNetworkAccessible
import kotlinx.android.synthetic.main.activity_order_detail.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*
import kotlinx.android.synthetic.main.layout_network_condition.*
import kotlinx.android.synthetic.main.layout_no_data_condition.*
import kotlinx.android.synthetic.main.layout_server_error_condition.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrderDetailActivity : BaseActivity() {

    private lateinit var orderId: String
    lateinit var orderDetail: OrderDetailResponse.Data.OrderDetails
    var productArrayList = ArrayList<OrderDetailResponse.Data.ProductListItem?>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)


        orderId = intent.getStringExtra("order_id")

        initializeViews()
        initializeTheme()

        getOrderDetails()
    }

    private fun initializeViews() {
        btnTryAgain.setOnClickListener(this)
        imgToolbarHomeLayout.setOnClickListener(this)
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

    override fun setToolBar(name: String) {
        setSupportActionBar(toolBar)
        setStatusBarColor()
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = name
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_shape_backarrow_white)
        cp_Logo.hide()
        txtToolbarTitle.text = getString(R.string.title_order_detail)
        imgToolbarHome.hide()
        setToolBarColor(imgToolbarHome, txtToolbarTitle, toolbar = toolBar)
    }

    override fun onBackPressed() {
        if (intent.hasExtra(IntentFlags.REDIRECT_FROM) && intent.getStringExtra(IntentFlags.REDIRECT_FROM) == IntentFlags.ORDER_DETAIL) {
            startActivity<DashboardActivity>(IntentFlags.FRAGMENT_TO_BE_LOADED to R.id.nav_my_order)
            finish()
        } else {
            finish()
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgToolbarHomeLayout -> {
                onBackPressed()
            }

            R.id.btnTryAgain -> {
                getOrderDetails()
            }
        }
    }

    private fun getOrderDetails() {
        if (isNetworkAccessible()) {
            callOrderDetailAPI()
        } else {
            showNetworkCondition()
        }
    }


    override fun showNetworkCondition() {
        layoutNetworkCondition.show()
        layoutServerError.hide()
        tabLayout.hide()
        viewPager.hide()
        stopShimmerView()
    }

    override fun showRecyclerViewData() {
        tabLayout.show()
        viewPager.show()
        layoutNetworkCondition.hide()
        layoutNoDataScreen.hide()
        layoutServerError.hide()
        stopShimmerView()
    }


    override fun startShimmerView() {
        shimmerViewContainerProductList.show()
        shimmerViewContainerProductList.startShimmer()

        tabLayout.hide()
        viewPager.hide()
        layoutNetworkCondition.hide()
        layoutNoDataScreen.hide()
        layoutServerError.hide()

    }

    override fun stopShimmerView() {
        shimmerViewContainerProductList.hide()
        shimmerViewContainerProductList.stopShimmer()
    }


    private fun callOrderDetailAPI() {
        startShimmerView()
        App.HostUrl = SharedPreferences.getInstance(this@OrderDetailActivity).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)

        val callback = clientInstance.getOrderDetails(orderId)

        callback.enqueue(object : Callback<OrderDetailResponse> {
            override fun onFailure(call: Call<OrderDetailResponse>?, t: Throwable?) {

                showServerErrorMessage()
            }

            override fun onResponse(
                call: Call<OrderDetailResponse>?,
                response: Response<OrderDetailResponse>?
            ) {
                if (isNetworkAccessible()) {
                    showRecyclerViewData()

                    if (response?.body()?.statusCode.equals("10")) {
                        orderDetail = response?.body()?.data!!.orderDetails!!
                        productArrayList.clear()
                        productArrayList.addAll(response.body()?.data!!.productList)

                        viewPager.adapter = ViewPagerAdapter(supportFragmentManager)
                        tabLayout.setupWithViewPager(viewPager)
                    } else {
                        toast(getString(R.string.message_something_went_wrong))
                    }
                } else {
                    showNetworkCondition()
                }
            }

        })

    }


    inner class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {

        override fun getItem(position: Int): Fragment {

            var fragment: Fragment? = null
            var bundle = Bundle()
            if (position == 0) {
                fragment = OrderDetailFragment()
                bundle.putParcelable("details", orderDetail)
            } else if (position == 1) {
                fragment = OrderProductListFragment()
                bundle.putParcelable("details", orderDetail)
                bundle.putParcelableArrayList("products", productArrayList)
            }

            fragment!!.arguments = bundle
            return fragment
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {

            var title: String? = null
            if (position == 0) {
                title = getString(R.string.tab_order_detail)
            } else {
                title = getString(R.string.tab_product)
            }
            return title
        }
    }

}
