package com.plugable.mcommerceapp.crazypetals.ui.activities

import ServiceGenerator
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.payumoney.core.entity.TransactionResponse
import com.payumoney.sdkui.ui.utils.PayUmoneyFlowManager
import com.payumoney.sdkui.ui.utils.ResultModel
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.OrderDetailResponse
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.UpdatePaymentRequest
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.UpdatePaymentResponse
import com.plugable.mcommerceapp.crazypetals.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.crazypetals.ui.fragments.OrderDetailFragment
import com.plugable.mcommerceapp.crazypetals.ui.fragments.OrderProductListFragment
import com.plugable.mcommerceapp.crazypetals.utils.application.App
import com.plugable.mcommerceapp.crazypetals.utils.constants.IntentFlags
import com.plugable.mcommerceapp.crazypetals.utils.extension.hide
import com.plugable.mcommerceapp.crazypetals.utils.extension.setStatusBarColor
import com.plugable.mcommerceapp.crazypetals.utils.extension.setToolBarColor
import com.plugable.mcommerceapp.crazypetals.utils.extension.show
import com.plugable.mcommerceapp.crazypetals.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.crazypetals.utils.util.isNetworkAccessible
import kotlinx.android.synthetic.main.activity_order_detail.*
import kotlinx.android.synthetic.main.activity_order_summary.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*
import kotlinx.android.synthetic.main.layout_network_condition.*
import kotlinx.android.synthetic.main.layout_no_data_condition.*
import kotlinx.android.synthetic.main.layout_server_error_condition.*
import org.jetbrains.anko.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrderDetailActivity : BaseActivity() {

    private lateinit var orderDetailApi: Call<OrderDetailResponse>
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
        progressBarDetailOrder.indeterminateDrawable.setColorFilter(
            Color.BLACK,
            PorterDuff.Mode.MULTIPLY
        )
    }

    override fun setToolBar(name: String) {
        setSupportActionBar(toolBar)
        setStatusBarColor()
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = getString(R.string.title_order_detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_shape_backarrow_white)
        cp_Logo.hide()
        txtToolbarTitle.hide()
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

        orderDetailApi = clientInstance.getOrderDetails(orderId)

        orderDetailApi.enqueue(object : Callback<OrderDetailResponse> {
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
            val bundle = Bundle()
            if (position == 0) {
                fragment = OrderDetailFragment()
                bundle.putParcelable("details", orderDetail)
                bundle.putString("order_id", orderId)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (isNetworkAccessible()) {
            updateTransactionStatus(requestCode, resultCode, data)
        } else {
            showNetworkAlert(requestCode, resultCode, data)
            orderDetailActivity.hide()
        }
    }

    private fun updateTransactionStatus(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == PayUmoneyFlowManager.REQUEST_CODE_PAYMENT && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val transactionResponse = data.getParcelableExtra<TransactionResponse>(
                PayUmoneyFlowManager
                    .INTENT_EXTRA_TRANSACTION_RESPONSE
            )

            val resultModel =
                data.getParcelableExtra<ResultModel>(PayUmoneyFlowManager.ARG_RESULT)

            // Check which object is non-null
            if (transactionResponse.getPayuResponse() != null) {
                transactionResponse.transactionDetails
                if (transactionResponse.transactionStatus == TransactionResponse.TransactionStatus.SUCCESSFUL) {
                    //Success Transaction
                    // call update order api
                    toast("Transaction successful")
                    showProgress()
                    updatePaymentStatus(orderId.toInt(), "2", "Successful")
                } else {
                    toast("Transaction unsuccessful")
                    showProgress()
                    updatePaymentStatus(orderId.toInt(), "5", "Unsuccessful")

                    //Failure Transaction
                }

            } else if (resultModel != null && resultModel.error != null) {
                Log.d("Payment", "Error response : " + resultModel.error.transactionResponse)
            } else {
                Log.d("Payment", "Both objects are null!")
            }
        } else {
            toast("Transaction unsuccessful")
            showProgress()
            updatePaymentStatus(orderId.toInt(), "5", "Unsuccessful")
        }

    }
    private fun showProgress() {
        shimmerViewContainerProductList.hide()
        tabLayout.hide()
        viewPager.hide()
        progressBarDetailOrder.show()
        toolbar.hide()
    }

    private fun hideProgress() {
        shimmerViewContainerProductList.show()
        tabLayout.show()
        viewPager.show()
        progressBarDetailOrder.hide()
        toolbar.show()
    }

    private fun updatePaymentStatus(
        orderId: Int,
        paymentStatusId: String,
        transactionStatus: String
    ) {
        if (isNetworkAccessible()) {
            val updateStatusRequest = UpdatePaymentRequest(
                orderId.toString(),
                paymentStatusId
            )
            App.HostUrl = SharedPreferences.getInstance(this@OrderDetailActivity).hostUrl!!
            val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
            val call = clientInstance.updatePaymentStatus(updateStatusRequest)
            call.enqueue(object : Callback<UpdatePaymentResponse> {
                override fun onFailure(call: Call<UpdatePaymentResponse>, t: Throwable) {
                    hideProgress()
                    toast(getString(R.string.message_something_went_wrong))
                }

                override fun onResponse(
                    call: Call<UpdatePaymentResponse>,
                    response: Response<UpdatePaymentResponse>
                ) {
                    if (response.body()!!.statusCode.equals("10")) {
                        /*startActivity<DashboardActivity>(
                            IntentFlags.FRAGMENT_TO_BE_LOADED to activity!!.intent.getIntExtra(
                                IntentFlags.FRAGMENT_TO_BE_LOADED,
                                R.id.nav_home
                            )
                        )
                        activity!!.finish()*/
                        startActivity<SuccessOrderStatusActivity>(
                            IntentFlags.REDIRECT_FROM to "OrderDetailFragment",
                            "DeliveryDay" to orderDetail.deliveryDay,
                            "orderNumber" to orderDetail.orderNumber,
                            "TransactionStatus" to transactionStatus
                        )
                        showProgress()

                    } else {
                        toast(response.body()!!.message)
                    }
                }

            })
        } else {
            hideProgress()
            toast(getString(R.string.oops_no_internet_connection))
        }
    }

    private fun showNetworkAlert(requestCode: Int, resultCode: Int, data: Intent?) {
        alert(getString(R.string.oops_no_internet_connection)) {
            isCancelable = false
            positiveButton(getString(R.string.try_again)) {
                if (isNetworkAccessible()) {
                    updateTransactionStatus(requestCode, resultCode, data)
                    showProgress()
                } else {
                    showNetworkAlert(requestCode, resultCode, data)
                    orderDetailActivity.hide()
                }
            }
        }.show().apply {
            getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).let {
                it.allCaps = false
                it.textColor = Color.BLUE
                it.background =
                    ContextCompat.getDrawable(
                        this@OrderDetailActivity,
                        android.R.color.transparent
                    )
            }
        }
    }
    override fun onStop() {
        super.onStop()
        cancelTasks()
    }

    private fun cancelTasks() {
        if (::orderDetailApi.isInitialized && orderDetailApi != null) orderDetailApi.cancel()
    }

}
