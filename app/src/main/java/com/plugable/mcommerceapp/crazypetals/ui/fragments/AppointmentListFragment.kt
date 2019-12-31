package com.plugable.mcommerceapp.crazypetals.ui.fragments

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.callbacks.EventListener
import com.plugable.mcommerceapp.crazypetals.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.crazypetals.network.error.Error
import com.plugable.mcommerceapp.crazypetals.network.model.AppointmentData
import com.plugable.mcommerceapp.crazypetals.network.model.AppointmentListResponse
import com.plugable.mcommerceapp.crazypetals.network.presenter.AppointmentPresenter
import com.plugable.mcommerceapp.crazypetals.network.view.AppointmentView
import com.plugable.mcommerceapp.crazypetals.ui.activities.AppointmentDetailActivity
import com.plugable.mcommerceapp.crazypetals.ui.activities.BookAppointmentActivity
import com.plugable.mcommerceapp.crazypetals.ui.adapters.AppointmentListAdapter
import com.plugable.mcommerceapp.crazypetals.utils.constants.IntentFlags
import com.plugable.mcommerceapp.crazypetals.utils.extension.disableWindowClicks
import com.plugable.mcommerceapp.crazypetals.utils.extension.enableWindowClicks
import com.plugable.mcommerceapp.crazypetals.utils.extension.hide
import com.plugable.mcommerceapp.crazypetals.utils.extension.show
import com.plugable.mcommerceapp.crazypetals.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.crazypetals.utils.util.isNetworkAccessible
import kotlinx.android.synthetic.main.fragment_appointment_list.*
import kotlinx.android.synthetic.main.layout_network_condition.*
import kotlinx.android.synthetic.main.layout_no_appointmentlist.*
import kotlinx.android.synthetic.main.layout_no_data_condition.*
import kotlinx.android.synthetic.main.layout_server_error_condition.*
import org.jetbrains.anko.support.v4.toast


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [AppointmentListFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [AppointmentListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AppointmentListFragment : BaseFragment(), View.OnClickListener, AppointmentView,
    EventListener {


    private lateinit var appointmentListAdapter: AppointmentListAdapter
    private var appointmentArrayList = ArrayList<AppointmentData>()
    private val appointmentPresenter = AppointmentPresenter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_appointment_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializeTheme()
        initializeViews()
    }


    private fun initializeViews() {
        btnServerError.setOnClickListener(this)
        btnTryAgain.setOnClickListener(this)
        btnNoData.setOnClickListener(this)
        buttonBookAppointment.setOnClickListener(this)

        appointmentListAdapter = AppointmentListAdapter(appointmentArrayList, activity!!, this)
        recyclerViewAppointments.itemAnimator = DefaultItemAnimator()
        recyclerViewAppointments.adapter = appointmentListAdapter

        val applicationUserId = SharedPreferences.getInstance(activity!!)
            .getStringValue(IntentFlags.APPLICATION_USER_ID)

        if (activity!!.isNetworkAccessible()) {
            if (activity!!.isFinishing) {
                return
            }
            appointmentPresenter.getAppointment(applicationUserId!!, 0, 1000)
        } else {
            showNetworkCondition()
        }
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
        progressBarAppointmentList.indeterminateDrawable.setColorFilter(
            Color.BLACK,
            PorterDuff.Mode.MULTIPLY
        )
        buttonBookAppointment.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.appointment_list_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_appointment -> {
                if (activity!!.isNetworkAccessible()) {
                    if (SystemClock.elapsedRealtime() - LastClickTimeSingleton.lastClickTime < 500L) return true
                    else {
                        val intent = Intent(activity, BookAppointmentActivity::class.java)
                        intent.putExtra(IntentFlags.REDIRECT_FROM, IntentFlags.APPOINTMENT_LIST)
                        intent.putExtra("ButtonClick", "ActionBookAppointment")
                        startActivityForResult(intent, 1)
                    }
                    LastClickTimeSingleton.lastClickTime = SystemClock.elapsedRealtime()

                } else {
                    toast(getString(R.string.oops_no_internet_connection))
                }
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.buttonBookAppointment -> {
                if (activity!!.isNetworkAccessible()) {
                    if (SystemClock.elapsedRealtime() - LastClickTimeSingleton.lastClickTime < 500L) return
                    else {
                        val intent = Intent(activity, BookAppointmentActivity::class.java)
                        intent.putExtra(IntentFlags.REDIRECT_FROM, IntentFlags.APPOINTMENT_LIST)
                        intent.putExtra("ButtonClick", "ButtonBookAppointment")
                        startActivityForResult(intent, 2)
                    }
                    LastClickTimeSingleton.lastClickTime = SystemClock.elapsedRealtime()
                } else {
                    toast(getString(R.string.oops_no_internet_connection))
                }

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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1) {
            if (resultCode == 1 && data != null) {
                val appointmentData: AppointmentData = data.getParcelableExtra("response")!!
                appointmentArrayList.add(0, appointmentData)
                appointmentListAdapter.notifyDataSetChanged()
            }
        } else {
            if (resultCode == 2 && data != null) {
                val appointmentData: AppointmentData = data.getParcelableExtra("response")!!
                appointmentArrayList.add(0, appointmentData)
                appointmentListAdapter.notifyDataSetChanged()
                if (appointmentArrayList.isEmpty()) {
                    showEmptyScreen()
                } else {
                    showRecyclerViewData()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun showNetworkCondition() {
        layoutNetworkCondition.show()
        layoutServerError.hide()
        recyclerViewAppointments.hide()
        layoutNoDataScreen.hide()
    }

    override fun hideNetworkCondition() {
        layoutNetworkCondition.hide()
        layoutNoDataScreen.hide()
        recyclerViewAppointments.hide()
    }

    override fun showServerErrorMessage() {
        layoutNetworkCondition.hide()
        layoutServerError.show()
        recyclerViewAppointments.hide()
        layoutNoDataScreen.hide()
    }

    override fun hideServerErrorMessage() {
        layoutNetworkCondition.hide()
        layoutServerError.hide()
        recyclerViewAppointments.hide()
        layoutNoDataScreen.hide()

    }

    override fun showRecyclerViewData() {
        layoutNetworkCondition.hide()
        layoutServerError.hide()
        recyclerViewAppointments.show()
        layoutNoDataScreen.hide()
        layout_no_appointment_list.hide()
    }

    fun showEmptyScreen() {
        layoutNetworkCondition.hide()
        layoutServerError.hide()
        recyclerViewAppointments.hide()
        layoutNoDataScreen.hide()
        layout_no_appointment_list.show()
    }

    override fun onItemClickListener(position: Int) {
        if (SystemClock.elapsedRealtime() - LastClickTimeSingleton.lastClickTime < 500L) return
        else {

            val appointmentId = appointmentArrayList[position].appointmentId
            val intent = Intent(activity, AppointmentDetailActivity::class.java)
            intent.putExtra(IntentFlags.APPOINTMENT_ID, appointmentId)
            startActivity(intent)
        }
        LastClickTimeSingleton.lastClickTime = SystemClock.elapsedRealtime()

    }

    override fun onAppointmentListSuccess(response: AppointmentListResponse) {
        if (activity!!.isFinishing) {
            return
        }
        appointmentArrayList.addAll(response.data)
        appointmentListAdapter.notifyDataSetChanged()

        if (appointmentArrayList.isEmpty()) {
            showEmptyScreen()
        } else {
            showRecyclerViewData()
        }
    }

    object LastClickTimeSingleton {
        var lastClickTime: Long = 0
    }


    override fun showProgress() {
        progressBarAppointmentList.show()
        activity?.disableWindowClicks()
    }

    override fun hideProgress() {
        progressBarAppointmentList.hide()
        activity?.enableWindowClicks()
    }

    override fun failed(error: Error) {
        toast(error.getErrorMessage())
    }

    override fun onDestroy() {
        appointmentPresenter.onStop()
        super.onDestroy()
    }
}
