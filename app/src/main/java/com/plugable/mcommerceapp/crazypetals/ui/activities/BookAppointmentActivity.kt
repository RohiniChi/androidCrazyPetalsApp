package com.plugable.mcommerceapp.crazypetals.ui.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.view.MotionEvent
import android.view.View
import android.widget.CalendarView
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.callbacks.OnButtonCheckedListner
import com.plugable.mcommerceapp.crazypetals.callbacks.OnListChekedListner
import com.plugable.mcommerceapp.crazypetals.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.crazypetals.network.error.Error
import com.plugable.mcommerceapp.crazypetals.network.model.BookAppointmentRequest
import com.plugable.mcommerceapp.crazypetals.network.model.BookAppointmentResponse
import com.plugable.mcommerceapp.crazypetals.network.model.GetAppointmentTypeResponse
import com.plugable.mcommerceapp.crazypetals.network.presenter.AppointmentPresenter
import com.plugable.mcommerceapp.crazypetals.network.view.AppointmentView
import com.plugable.mcommerceapp.crazypetals.ui.adapters.SpinnerAdapter
import com.plugable.mcommerceapp.crazypetals.utils.constants.IntentFlags
import com.plugable.mcommerceapp.crazypetals.utils.extension.*
import com.plugable.mcommerceapp.crazypetals.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.crazypetals.utils.util.isNetworkAccessible
import com.plugable.mcommerceapp.crazypetals.utils.validation.isEmpty
import com.plugable.mcommerceapp.crazypetals.utils.validation.isValidMobileNumber
import com.plugable.mcommerceapp.crazypetals.utils.validation.onTextChanged
import kotlinx.android.synthetic.main.activity_book_appointment.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*
import kotlinx.android.synthetic.main.layout_network_condition.*
import kotlinx.android.synthetic.main.layout_no_data_condition.*
import kotlinx.android.synthetic.main.layout_server_error_condition.*
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


class BookAppointmentActivity : BaseActivity(), AppointmentView, OnListChekedListner,
    OnButtonCheckedListner {

    private lateinit var bookAppointmentResponse: BookAppointmentResponse
    private var hour = 0
    private val checkedName = HashSet<String>()
    private lateinit var spinnerAdapter: SpinnerAdapter
    private var appointmentType = ArrayList<GetAppointmentTypeResponse.AppointmentTypeData>()
    private var customDialog: CustomDialogApptType? = null
    private var timeToTimeStamp: Long = 0
    private var dateToTimeStamp: Long = 0
    private val appointmentPresenter = AppointmentPresenter(this)
    var checkedId = HashSet<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_appointment)
        initializeTheme()
        initializeViews()
    }

    private fun initializeViews() {

        imgToolbarHomeLayout.setOnClickListener(this)
        buttonAddAppointment.setOnClickListener(this)
        layoutSpinnerAppointmentType.setOnClickListener(this)
        btnServerError.setOnClickListener(this)
        btnTryAgain.setOnClickListener(this)
        btnNoData.setOnClickListener(this)

        editTextDate.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                this.hideKeyboard(v)
                val calendar = Calendar.getInstance()
                val calendarView = CalendarView(this)
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val date = calendar.get(Calendar.DAY_OF_MONTH)
                val datePickerDialog = DatePickerDialog(
                    this,
                    DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                        calendar.set(Calendar.YEAR, year)
                        calendar.set(Calendar.MONTH, month)
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        calendarView.date = calendar.timeInMillis
                        val myFormat = "dd/MM/yyyy"
                        val sdf = SimpleDateFormat(myFormat, Locale.ENGLISH)
                        /*editTextDate.text = Editable.Factory.getInstance()
                        .newEditable("$dayOfMonth ${DateFormatSymbols.getInstance().months[month]} $year")*/
                        editTextDate.setText(sdf.format(calendar.time))
                    },
                    year,
                    month,
                    date
                )
                datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000

                datePickerDialog.show()
            }
            return@setOnTouchListener false

        }

        editTextTime.setOnTouchListener { v, event ->

            if (event.action == MotionEvent.ACTION_UP) {
                this.hideKeyboard(v)
                val calendar = Calendar.getInstance()
                val hh = calendar.get(Calendar.HOUR_OF_DAY)
                val mm = calendar.get(Calendar.MINUTE)
                val timePickerDialog = TimePickerDialog(
                    this,
                    TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                        var isAMorPM = ""
                        hour = when (hourOfDay == 0) {
                            true -> 12
                            false -> hourOfDay
                        }
                        if (view.is24HourView) {
                            isAMorPM = if (hourOfDay > 12) {
                                hour = hourOfDay - 12
                                " PM"
                            } else {
                                " AM"
                            }
                        }
                        editTextTime.text =
                            Editable.Factory.getInstance().newEditable("$hour:$minute$isAMorPM")
                    },
                    hh,
                    mm,
                    true
                )

                timePickerDialog.show()
            }
            return@setOnTouchListener false
        }
        textChangeListeners()
        buttonAddAppointment.isClickable = true
        hideProgress()

        val contactNumber = SharedPreferences.getInstance(this).getProfile()?.mobileNumber
        editTextphoneNumber.setText(contactNumber)

        if (isNetworkAccessible()) {
            if (isFinishing) {
                return
            }
            appointmentPresenter.getAppointmentType()
        }
        spinnerAdapter = SpinnerAdapter(this, this, checkedId, appointmentType)
        customDialog = CustomDialogApptType(this, this, spinnerAdapter)

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

    override fun setToolBar(name: String) {
        setSupportActionBar(toolBar)
        setStatusBarColor()
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = "Book Appointment"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_shape_backarrow_white)
        cp_Logo.hide()
        txtToolbarTitle.hide()
        txtToolbarTitle.allCaps = false
        txtToolbarTitle.text = "Book Appointment"
        imgToolbarHome.hide()
        setToolBarColor(imgToolbarHome, txtToolbarTitle, toolbar = toolBar)
    }

    private fun setThemeToComponents() {
        buttonAddAppointment.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        progressBarBookAppointment.indeterminateDrawable.setColorFilter(
            Color.BLACK,
            PorterDuff.Mode.MULTIPLY
        )
        setStatusBarColor()

    }

    override fun onResume() {
        buttonAddAppointment.isClickable = true
        hideProgress()
        super.onResume()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgToolbarHomeLayout -> {
                onBackPressed()
            }

            R.id.buttonAddAppointment -> {
                spinnerValidation()
                dateValidation()
                timeValidation()
                contactValidation()

                if (spinnerValidation() && dateValidation() && timeValidation() && contactValidation()) {
                    buttonAddAppointment.isClickable = false
                    attemptApiCall()
                }
            }

            R.id.layoutSpinnerAppointmentType -> {
                if (isNetworkAccessible()) {
                    customDialog?.show()
                    customDialog?.setCanceledOnTouchOutside(true)

                } else {
                    toast(getString(R.string.oops_no_internet_connection))
                }
            }
        }
    }


    private fun attemptApiCall() {
        if (isNetworkAccessible()) {
            val dateFormatter = SimpleDateFormat("dd/MM/yyyy")
            val dateToTime = dateFormatter.parse(editTextDate.text.toString())

            dateToTimeStamp = dateToTime!!.time

            val concatedDate =
                editTextDate.text.toString().plus(" ").plus(editTextTime.text.toString())
            val timeFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm")
            val timeToTime = timeFormatter.parse(concatedDate)

            timeToTimeStamp = timeToTime!!.time

            val applicationUserId = SharedPreferences.getInstance(this)
                .getStringValue(IntentFlags.APPLICATION_USER_ID)
            val bookData = BookAppointmentRequest(
                applicationUserId!!, dateToTimeStamp.toString(),
                editTextphoneNumber.text.toString(),
                editTextDescription.text.toString(),
                timeToTimeStamp.toString(),
                checkedId
            )
            if (isFinishing) {
                return
            }
            appointmentPresenter.bookAppointment(bookData)

        } else {
            buttonAddAppointment.isClickable = true
            hideProgress()
            toast(getString(R.string.oops_no_internet_connection))
        }
    }

    override fun onGetAppointmentTypeSuccess(response: GetAppointmentTypeResponse) {
        if (response.statusCode.equals("10")) {
            response.data.forEach {
                appointmentType.add(it)
            }
            spinnerAdapter.notifyDataSetChanged()
        } else {
            toast(getString(R.string.message_something_went_wrong))
        }
    }

    override fun onBookAppointmentSuccess(response: BookAppointmentResponse) {
        if (response.statusCode.equals("10")) {
            toast(response.message)
            bookAppointmentResponse = response
            if (intent.hasExtra(IntentFlags.REDIRECT_FROM) && intent.getStringExtra(IntentFlags.REDIRECT_FROM) == IntentFlags.APPOINTMENT_LIST) {
                if (intent.getStringExtra("ButtonClick").equals("ActionBookAppointment", true)) {
                    val returnIntent = Intent()
                    returnIntent.putExtra(
                        IntentFlags.FRAGMENT_TO_BE_LOADED,
                        R.id.nav_appointmentList
                    )
                    returnIntent.putExtra("response", bookAppointmentResponse.data)
                    setResult(1, returnIntent)
                    finish()
                } else if (intent.getStringExtra("ButtonClick").equals(
                        "ButtonBookAppointment",
                        true
                    )
                ) {
                    val returnIntent = Intent()
                    returnIntent.putExtra(
                        IntentFlags.FRAGMENT_TO_BE_LOADED,
                        R.id.nav_appointmentList
                    )
                    returnIntent.putExtra("response", bookAppointmentResponse.data)
                    setResult(2, returnIntent)
                    finish()
                }
            } else {
                finish()
            }
            finish()
        } else {
            toast(getString(R.string.message_something_went_wrong))
        }
    }


    override fun showProgress() {
        progressBarBookAppointment.show()
        this.disableWindowClicks()
    }

    override fun hideProgress() {
        progressBarBookAppointment.hide()
        this.enableWindowClicks()
    }

    override fun failed(error: Error) {
        error.getErrorMessage()
    }

    private fun textChangeListeners() {
        editTextphoneNumber.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused) {
                this.hideKeyboard(view)
                contactValidation()
            }
        }
        editTextphoneNumber.onTextChanged {
            textViewContactNoError.invisible()
        }

        editTextDate.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused) {
                this.hideKeyboard(view)
                dateValidation()
            }
        }
        editTextDate.onTextChanged {
            textViewDateError.invisible()
        }

        editTextTime.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused) {
                this.hideKeyboard(view)
                timeValidation()
            }
        }
        editTextTime.onTextChanged {
            textViewTimeError.invisible()
        }


    }


    private fun contactValidation(): Boolean {
        when {
            editTextphoneNumber.isEmpty() -> {

                textViewContactNoError.show()
                textViewContactNoError.text = getString(R.string.validation_enter_number)

                return false
            }
            !editTextphoneNumber.isValidMobileNumber() -> {

                textViewContactNoError.show()
                textViewContactNoError.text = getString(R.string.validation_number)


                return false
            }

        }
        textViewContactNoError.invisible()

        return true
    }

    private fun dateValidation(): Boolean {
        when {
            editTextDate.isEmpty() -> {
                textViewDateError.show()
                textViewDateError.text = "Please select date for appointment"

                return false
            }
        }
        textViewDateError.invisible()
        return true
    }

    private fun timeValidation(): Boolean {
        when {
            editTextTime.isEmpty() -> {
                textViewTimeError.show()
                textViewTimeError.text = "Please select time for appointment"

                return false
            }
        }
        textViewTimeError.invisible()
        return true
    }

    private fun spinnerValidation(): Boolean {
        when {
            textViewAppointmentType.text.equals(getString(R.string.text_select_appt_type)) -> {
                textViewSpinnerTypeError.show()
                textViewSpinnerTypeError.text = getString(R.string.text_select_appt_type)

                return false
            }
        }
        textViewSpinnerTypeError.invisible()
        return true
    }


    override fun onListCheck(clickedId: Int, clickedName: String, isChecked: Boolean) {
        if (isChecked) {
            checkedId.add(clickedId)
            checkedName.add(clickedName)

        } else {
            checkedId.remove(clickedId)
            checkedName.remove(clickedName)
        }
    }

    override fun onButtonClicked() {
        when {
            checkedName.isEmpty() -> {
                textViewAppointmentType.text = getString(R.string.text_select_appt_type)
            }
            checkedName.size == 1 -> {
                val selectedTypes = checkedName.toString().replace("[", "").replace("]", "")
                textViewAppointmentType.text = selectedTypes
                textViewAppointmentType.setTextColor(Color.BLACK)
                textViewSpinnerTypeError.invisible()
            }
            else -> {
                val selectedTypes = checkedName.toString().replace("[", "").replace("]", "")
                textViewAppointmentType.text = selectedTypes.plus(",")
                textViewAppointmentType.setTextColor(Color.BLACK)
                textViewSpinnerTypeError.invisible()
            }
        }
    }

    override fun onDestroy() {
        appointmentPresenter.onStop()
        super.onDestroy()
    }


}
