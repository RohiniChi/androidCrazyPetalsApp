package com.plugable.mcommerceapp.cpmvp1.ui.activities

import ServiceGenerator
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.callbacks.EventListener
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.DeliveryChartResponse
import com.plugable.mcommerceapp.cpmvp1.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.cpmvp1.ui.adapters.DeliveryScheduleAdapter
import com.plugable.mcommerceapp.cpmvp1.utils.application.App
import com.plugable.mcommerceapp.cpmvp1.utils.extension.hide
import com.plugable.mcommerceapp.cpmvp1.utils.extension.setStatusBarColor
import com.plugable.mcommerceapp.cpmvp1.utils.extension.setToolBarColor
import com.plugable.mcommerceapp.cpmvp1.utils.extension.show
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.cpmvp1.utils.util.isNetworkAccessible
import kotlinx.android.synthetic.main.activity_delivery_schedule.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

/**
 * [DeliveryScheduleActivity] is used to show schedules for delivery
 *
 */
class DeliveryScheduleActivity : AppCompatActivity(), EventListener, View.OnClickListener {


    var deliveryScheduleList = ArrayList<DeliveryChartResponse.Data>()
    lateinit var deliveryScheduleAdapter: DeliveryScheduleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery_schedule)

        initializeTheme()
        initializeViews()
        header.hide()
    }

    private fun initializeViews() {
        deliveryScheduleAdapter = DeliveryScheduleAdapter(deliveryScheduleList, this, this)
        recyclerViewDeliverySchedule.adapter = deliveryScheduleAdapter
        imgToolbarHomeLayout.setOnClickListener(this)
        fetchDeliveryScheduleData()
    }

    private fun fetchDeliveryScheduleData() {
        if (!isNetworkAccessible()) {
            toast(getString(R.string.check_internet_connection))
            return
        }
        callAPI()
    }

    private fun callAPI() {
        progressBar.show()
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        val callback = clientInstance.getDeliveryChart()

        callback.enqueue(object : Callback<DeliveryChartResponse> {
            override fun onFailure(call: Call<DeliveryChartResponse>, t: Throwable) {
                progressBar.hide()
                toast(getString(R.string.message_something_went_wrong))
            }

            override fun onResponse(
                call: Call<DeliveryChartResponse>,
                response: Response<DeliveryChartResponse>
            ) {
                if (response.body()?.statusCode.equals("10")) {
                    progressBar.hide()
                    header.show()
                    if (response.body()!!.data.isNotEmpty()) {
                        deliveryScheduleList.clear()
                        deliveryScheduleList.addAll(response.body()!!.data)
                        deliveryScheduleAdapter.notifyDataSetChanged()
                    }
                } else {
                    progressBar.hide()
                    toast(getString(R.string.message_something_went_wrong))
                }
            }
        })
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
        textViewDeliveryScheduleAreaTitle.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        textViewDeliveryScheduleTimingTitle.setBackgroundColor(
            Color.parseColor(
                ApplicationThemeUtils.SECONDARY_COLOR
            )
        )
        progressBar.indeterminateDrawable.setColorFilter(
            Color.BLACK,
            PorterDuff.Mode.MULTIPLY
        )
    }

    fun setToolBar(name: String) {
        setSupportActionBar(toolBar)
        setStatusBarColor()
        supportActionBar?.setDisplayShowTitleEnabled(false)
        txtToolbarTitle.text = getString(R.string.title_delivery_schedule)
        imgToolbarHome.setImageResource(R.drawable.ic_shape_backarrow)
        setToolBarColor(imgToolbarHome, txtToolbarTitle, toolbar = toolBar)
    }

    override fun onItemClickListener(position: Int) {
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgToolbarHomeLayout -> onBackPressed()
        }
    }
}
