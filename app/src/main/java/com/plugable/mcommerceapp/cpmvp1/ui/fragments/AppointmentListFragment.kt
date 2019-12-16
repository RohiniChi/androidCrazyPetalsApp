package com.plugable.mcommerceapp.cpmvp1.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.cpmvp1.network.error.Error
import com.plugable.mcommerceapp.cpmvp1.network.model.AppointmentData
import com.plugable.mcommerceapp.cpmvp1.network.model.AppointmentListResponse
import com.plugable.mcommerceapp.cpmvp1.network.presenter.AppointmentPresenter
import com.plugable.mcommerceapp.cpmvp1.network.view.AppointmentView
import com.plugable.mcommerceapp.cpmvp1.ui.activities.AppointmentDetailActivity
import com.plugable.mcommerceapp.cpmvp1.ui.activities.BookAppointmentActivity
import com.plugable.mcommerceapp.cpmvp1.ui.adapters.AppointmentListAdapter
import com.plugable.mcommerceapp.cpmvp1.utils.constants.IntentFlags
import com.plugable.mcommerceapp.cpmvp1.utils.extension.hide
import com.plugable.mcommerceapp.cpmvp1.utils.extension.show
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences
import kotlinx.android.synthetic.main.fragment_appointment_list.*
import kotlinx.android.synthetic.main.fragment_my_order.*
import kotlinx.android.synthetic.main.layout_network_condition.*
import kotlinx.android.synthetic.main.layout_no_data_condition.*
import kotlinx.android.synthetic.main.layout_server_error_condition.*
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [AppointmentListFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [AppointmentListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AppointmentListFragment : BaseFragment(), View.OnClickListener,AppointmentView,
    com.plugable.mcommerceapp.cpmvp1.callbacks.EventListener {

    private lateinit var appointmentListAdapter: AppointmentListAdapter
    private var appointmentArrayList=ArrayList<AppointmentData>()
    private val appointmentPresenter= AppointmentPresenter(this)

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

        appointmentListAdapter = AppointmentListAdapter(appointmentArrayList,activity!!,this)
        recyclerViewAppointments.itemAnimator = DefaultItemAnimator()
        recyclerViewAppointments.adapter = appointmentListAdapter

        val applicationUserId=SharedPreferences.getInstance(activity!!).getStringValue(IntentFlags.APPLICATION_USER_ID)
        appointmentPresenter.getAppointment(applicationUserId!!,0,25)
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
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.appointment_list_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_appointment -> {
                startActivity<BookAppointmentActivity>()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View?) {

    }

    override fun showNetworkCondition() {
        layoutNetworkCondition.show()
        layoutServerError.hide()
        recyclerViewOrders.hide()
        layoutNoDataScreen.hide()
        stopShimmerView()
    }

    override fun hideNetworkCondition() {
        layoutNetworkCondition.hide()
        layoutNoDataScreen.hide()
        recyclerViewOrders.hide()
        stopShimmerView()
    }

    override fun showServerErrorMessage() {
        layoutNetworkCondition.hide()
        layoutServerError.show()
        recyclerViewOrders.hide()
        layoutNoDataScreen.hide()
        stopShimmerView()
    }

    override fun hideServerErrorMessage() {
        layoutNetworkCondition.hide()
        layoutServerError.hide()
        recyclerViewAppointments.hide()
        layoutNoDataScreen.hide()
        stopShimmerView()

    }

    override fun showRecyclerViewData() {
        layoutNetworkCondition.hide()
        layoutServerError.hide()
        recyclerViewAppointments.show()
        layoutNoDataScreen.hide()
        stopShimmerView()
    }

    override fun startShimmerView() {
        shimmerViewContainerAppointmentList.show()
        shimmerViewContainerAppointmentList.startShimmer()

        layoutNetworkCondition.hide()
        layoutServerError.hide()
        recyclerViewAppointments.hide()
        layoutNoDataScreen.hide()
    }

    override fun stopShimmerView() {
        shimmerViewContainerAppointmentList.hide()
        shimmerViewContainerAppointmentList.stopShimmer()
    }

    override fun onItemClickListener(position: Int) {
        startActivity<AppointmentDetailActivity>()
    }

    override fun onAppointmentListSuccess(response: AppointmentListResponse) {
        appointmentArrayList.addAll(response.data)
        appointmentListAdapter.notifyDataSetChanged()
    }


    override fun showProgress() {

    }

    override fun hideProgress() {

    }

    override fun failed(error:Error) {
        toast(error.getErrorMessage())
    }
}
