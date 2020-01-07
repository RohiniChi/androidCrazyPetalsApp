/*
 * Author : Chetan Patil.
 * Module : Notification module
 * Version : V 1.0
 * Sprint : 13
 * Date of Development : 07/10/2019
 * Date of Modified : 07/10/2019
 * Comments : This is activity which provides all the list of notifications
 * Output : List all the notification
 */
package com.plugable.mcommerceapp.crazypetals.ui.activities

import ServiceGenerator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.Notifications
import com.plugable.mcommerceapp.crazypetals.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.crazypetals.ui.adapters.NotificationListAdapter
import com.plugable.mcommerceapp.crazypetals.utils.application.App
import com.plugable.mcommerceapp.crazypetals.utils.constants.IntentFlags
import com.plugable.mcommerceapp.crazypetals.utils.constants.SharedPreferences.SHARED_PREFERENCES_LAST_NOTIFICATION_TIME
import com.plugable.mcommerceapp.crazypetals.utils.constants.SharedPreferences.SHARED_PREFERENCES_NOTIFICATION_COUNT
import com.plugable.mcommerceapp.crazypetals.utils.extension.hide
import com.plugable.mcommerceapp.crazypetals.utils.extension.setStatusBarColor
import com.plugable.mcommerceapp.crazypetals.utils.extension.setToolBarColor
import com.plugable.mcommerceapp.crazypetals.utils.extension.show
import com.plugable.mcommerceapp.crazypetals.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.crazypetals.utils.util.isNetworkAccessible
import kotlinx.android.synthetic.main.activity_notification.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*
import kotlinx.android.synthetic.main.layout_network_condition.*
import kotlinx.android.synthetic.main.layout_no_data_condition.*
import kotlinx.android.synthetic.main.layout_server_error_condition.*
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.toast
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class NotificationActivity : BaseActivity() {

    private lateinit var notificationListApi: Call<Notifications>
    private lateinit var notificationListAdapter: NotificationListAdapter
    private var notificationList = ArrayList<Notifications.Data.NotificationListItem?>()
    private var pageIndex = 1
    private var pageSize = 15
    private lateinit var mixPanel: MixpanelAPI
    var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        initializeViews()
        initializeTheme()

        getNotifications()
    }

    private fun getNotifications() {
        if (isNetworkAccessible()) {
            callNotificationsListAPI()
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
        btnTryAgain.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        btnNoData.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        btnServerError.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
    }

    private fun initializeViews() {
        imgToolbarHomeLayout.setOnClickListener(this)
        btnServerError.setOnClickListener(this)
        btnTryAgain.setOnClickListener(this)
        btnNoData.setOnClickListener(this)

        mixPanel = MixpanelAPI.getInstance(this, resources.getString(R.string.mix_panel_token))
        sendMixPanelEvent()

        notificationListAdapter = NotificationListAdapter(this, notificationList)
        recyclerViewNotifications.itemAnimator = DefaultItemAnimator()
        recyclerViewNotifications.adapter = notificationListAdapter

        recyclerViewNotifications.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?

                if (!isLoading) {
                    if (linearLayoutManager != null && notificationList.size > 12 && linearLayoutManager.findLastCompletelyVisibleItemPosition() == notificationList.size - 1) {
                        //bottom of list!
                        loadMore()
                        isLoading = true
                    }
                }
            }
        })

    }

    override fun setToolBar(name: String) {
        setSupportActionBar(toolBar)
        setStatusBarColor()
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.title = name
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_shape_backarrow_white)
        cp_Logo.hide()
        txtToolbarTitle.show()
        txtToolbarTitle.allCaps = false
        txtToolbarTitle.text = getString(R.string.notification)
        imgToolbarHome.hide()
        setToolBarColor(imgToolbarHome, txtToolbarTitle, toolbar = toolBar)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgToolbarHomeLayout -> {
                onBackPressed()
            }
            R.id.btnNoData -> {
                getNotifications()
            }
            R.id.btnTryAgain -> {
                getNotifications()
            }
        }
    }

    private fun sendMixPanelEvent() {
        val productObject = JSONObject()
        mixPanel.track(IntentFlags.MIXPANEL_VISITED_NOTIFICATIONS_SCREEN, productObject)
    }

    override fun showNetworkCondition() {
        layoutNetworkCondition.show()
        layoutNoDataScreen.hide()
        layoutServerError.hide()
        recyclerViewNotifications.hide()
        stopShimmerView()
    }


    override fun showNoDataAvailableScreen() {
        layoutNetworkCondition.hide()
        layoutNoDataScreen.show()
        layoutServerError.hide()
        recyclerViewNotifications.hide()
        stopShimmerView()
    }

    override fun showRecyclerViewData() {
        layoutNetworkCondition.hide()
        layoutNoDataScreen.hide()
        layoutServerError.hide()
        recyclerViewNotifications.show()
        stopShimmerView()
    }


    override fun stopShimmerView() {
        shimmerViewContainerProductList.hide()
        shimmerViewContainerProductList.stopShimmer()
    }

    private fun loadMore() {
        notificationList.add(null)
        notificationListAdapter.notifyItemInserted(notificationList.size)

        pageIndex += 1

        callNotificationsListAPI()


    }

    private fun callNotificationsListAPI() {
        val applicationUserId =
            SharedPreferences.getInstance(this).getStringValue(IntentFlags.APPLICATION_USER_ID)
        App.HostUrl = SharedPreferences.getInstance(this@NotificationActivity).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        notificationListApi =
            clientInstance.getAllNotificationApi(pageIndex, pageSize, applicationUserId!!)

        notificationListApi.enqueue(object : Callback<Notifications> {
            override fun onFailure(call: Call<Notifications>?, t: Throwable?) {

                showServerErrorMessage()
            }

            override fun onResponse(
                call: Call<Notifications>?,
                response: Response<Notifications>?
            ) {
                if (isNetworkAccessible()) {
                    showRecyclerViewData()

                    if (response?.body()?.statusCode.equals("10")) {
                        if (isLoading) {
                            notificationList.removeAt(notificationList.size - 1)
                            val scrollPosition = notificationList.size
                            notificationListAdapter.notifyItemRemoved(scrollPosition)

                            if (response!!.body()!!.data!!.notificationList.isEmpty()) {
                                pageIndex -= 1
                                isLoading = false
                            }

                        } else if (!isLoading && response!!.body()!!.data!!.notificationList.isEmpty()) {
                            showNoDataAvailableScreen()
                        }

                        notificationList.addAll(response!!.body()!!.data!!.notificationList)

                        notificationListAdapter.notifyDataSetChanged()
                        isLoading = false

                        if (notificationList.isNotEmpty()) {
                            val timeNotification = notificationList[0]!!.timeStamp
                            SharedPreferences.getInstance(this@NotificationActivity).setStringValue(
                                SHARED_PREFERENCES_LAST_NOTIFICATION_TIME,
                                timeNotification!!
                            )
                            SharedPreferences.getInstance(this@NotificationActivity).setStringValue(
                                SHARED_PREFERENCES_NOTIFICATION_COUNT,
                                "0"
                            )
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

    override fun onStop() {
        super.onStop()
        cancelTasks()
    }

    private fun cancelTasks() {
        if (::notificationListApi.isInitialized && notificationListApi != null) notificationListApi.cancel()
    }

    override fun onDestroy() {
        mixPanel.flush()
        super.onDestroy()
    }

}
