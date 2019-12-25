package com.plugable.mcommerceapp.cpmvp1.ui.activities

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.cpmvp1.network.error.Error
import com.plugable.mcommerceapp.cpmvp1.network.model.AppointmentDetailResponse
import com.plugable.mcommerceapp.cpmvp1.network.presenter.AppointmentPresenter
import com.plugable.mcommerceapp.cpmvp1.network.view.AppointmentView
import com.plugable.mcommerceapp.cpmvp1.ui.adapters.AppointmentDetailAdapter
import com.plugable.mcommerceapp.cpmvp1.utils.constants.IntentFlags
import com.plugable.mcommerceapp.cpmvp1.utils.extension.*
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.cpmvp1.utils.util.isNetworkAccessible
import kotlinx.android.synthetic.main.activity_appointment_detail.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast

class AppointmentDetailActivity : BaseActivity(), AppointmentView {

    private val appointmentPresenter = AppointmentPresenter(this)
    private lateinit var appointmentDetailAdapter: AppointmentDetailAdapter
    private lateinit var appointTypeList:ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment_detail)

        initializeTheme()
        initializeViews()
    }

    private fun initializeViews() {
        imgToolbarHomeLayout.setOnClickListener(this)


        val appointmentId = intent.getIntExtra(IntentFlags.APPOINTMENT_ID, 0)
        if (isNetworkAccessible()) {
            if (appointmentId != 0) {
                if (isFinishing){
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
        }
    }

    override fun onGetAppointmentDetailSuccess(response: AppointmentDetailResponse) {
        if (response.statusCode.equals("10")) {
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
        }
        else{
            toast(getString(R.string.message_something_went_wrong))
        }
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
