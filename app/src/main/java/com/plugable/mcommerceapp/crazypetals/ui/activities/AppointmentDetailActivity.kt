package com.plugable.mcommerceapp.crazypetals.ui.activities

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.crazypetals.network.error.Error
import com.plugable.mcommerceapp.crazypetals.network.model.AppointmentDetailResponse
import com.plugable.mcommerceapp.crazypetals.network.presenter.AppointmentPresenter
import com.plugable.mcommerceapp.crazypetals.network.view.AppointmentView
import com.plugable.mcommerceapp.crazypetals.ui.adapters.AppointmentDetailAdapter
import com.plugable.mcommerceapp.crazypetals.utils.constants.IntentFlags
import com.plugable.mcommerceapp.crazypetals.utils.extension.*
import com.plugable.mcommerceapp.crazypetals.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.crazypetals.utils.util.isNetworkAccessible
import kotlinx.android.synthetic.main.activity_appointment_detail.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*
import kotlinx.android.synthetic.main.layout_network_condition.*
import kotlinx.android.synthetic.main.layout_no_data_condition.*
import kotlinx.android.synthetic.main.layout_server_error_condition.*
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.toast

class AppointmentDetailActivity : BaseActivity(), AppointmentView {

    private val appointmentPresenter = AppointmentPresenter(this)
    private lateinit var appointmentDetailAdapter: AppointmentDetailAdapter
    private lateinit var appointTypeList: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment_detail)

        initializeTheme()
        initializeViews()
    }

    private fun initializeViews() {
        imgToolbarHomeLayout.setOnClickListener(this)
        btnServerError.setOnClickListener(this)
        btnTryAgain.setOnClickListener(this)
        btnNoData.setOnClickListener(this)

        val appointmentId = intent.getIntExtra(IntentFlags.APPOINTMENT_ID, 0)
        if (isNetworkAccessible()) {
            if (appointmentId != 0) {
                if (isFinishing) {
                    return
                }
                appointmentPresenter.getAppointmentDetail(appointmentId)
            }
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
        progressBarAppointmentDetail.indeterminateDrawable.setColorFilter(
            Color.BLACK,
            PorterDuff.Mode.MULTIPLY
        )
        setStatusBarColor()
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
        txtToolbarTitle.show()
        txtToolbarTitle.allCaps = false
        txtToolbarTitle.text = "Appointment Detail"
        imgToolbarHome.hide()
        setToolBarColor(imgToolbarHome, txtToolbarTitle, toolbar = toolBar)
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgToolbarHomeLayout -> {
                onBackPressed()
            }
            R.id.btnTryAgain -> {
                initializeViews()
            }

            R.id.btnNoData -> {
                initializeViews()
            }

            R.id.btnServerError -> {
                initializeViews()
            }

        }
    }

    override fun onGetAppointmentDetailSuccess(response: AppointmentDetailResponse) {
        if (response.statusCode.equals("10")) {
            showAppointmentListDetail()
            textViewAppointmentNo.text = "#".plus(response.data.appointmentNumber)
            textViewAppointmentDate.text = response.data.appointmentDate
            textViewAppointmentTime.text = response.data.appointmentTime
            textViewAppointmentContactNo.text = response.data.contactNumber
            if (response.data.description.isEmpty()) {
                textViewDetail.hide()
                textViewAppointmentDetail.hide()
            } else {
                textViewAppointmentDetail.text = response.data.description
            }
            appointTypeList = response.data.appointmentType
            listViewAppointmentType.setHasFixedSize(true)
            listViewAppointmentType.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            appointmentDetailAdapter = AppointmentDetailAdapter(this, appointTypeList)
            listViewAppointmentType.adapter = appointmentDetailAdapter
        } else {
            showServerErrorMessage()
        }
    }

    override fun showNetworkCondition() {
        layoutNetworkCondition.show()
        layoutServerError.hide()
        layoutNoDataScreen.hide()
        layoutDescription.hide()
    }

    override fun hideNetworkCondition() {
        layoutNetworkCondition.hide()
        layoutNoDataScreen.hide()
        layoutDescription.hide()
    }

    override fun showServerErrorMessage() {
        layoutNetworkCondition.hide()
        layoutServerError.show()
        layoutDescription.hide()
        layoutNoDataScreen.hide()
    }

    override fun hideServerErrorMessage() {
        layoutNetworkCondition.hide()
        layoutServerError.hide()
        layoutDescription.hide()
        layoutNoDataScreen.hide()

    }

    fun showAppointmentListDetail() {
        layoutNetworkCondition.hide()
        layoutServerError.hide()
        layoutDescription.show()
        layoutNoDataScreen.hide()
    }

    override fun showProgress() {
        progressBarAppointmentDetail.show()
        this.disableWindowClicks()
    }

    override fun failed(error: Error) {
        toast(error.getErrorMessage())

    }

    override fun hideProgress() {
        progressBarAppointmentDetail.hide()
        this.enableWindowClicks()
    }

    override fun onDestroy() {
        appointmentPresenter.onStop()
        super.onDestroy()
    }
}
