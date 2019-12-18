package com.plugable.mcommerceapp.cpmvp1.ui.activities

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
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
        setStatusBarColor()
        supportActionBar?.setDisplayShowTitleEnabled(true)
        cp_Logo.hide()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        txtToolbarTitle.show()
        txtToolbarTitle.allCaps = false
        txtToolbarTitle.text = "Appointment Detail"
        imgToolbarHome.setImageResource(R.drawable.ic_shape_backarrow)
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
         appointTypeList= response.data.appointmentType
        listViewAppointmentType.setHasFixedSize(true)
        listViewAppointmentType.layoutManager=LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        appointmentDetailAdapter=AppointmentDetailAdapter(this,appointTypeList)
        listViewAppointmentType.adapter=appointmentDetailAdapter
    }

    override fun showProgress() {
        progressBarAppointmentDetail.show()
        this.disableWindowClicks()
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
