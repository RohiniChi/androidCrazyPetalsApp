package com.plugable.mcommerceapp.cpmvp1.ui.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.callbacks.OnItemCheckListener
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.cpmvp1.network.error.Error
import com.plugable.mcommerceapp.cpmvp1.network.model.BookAppointmentRequest
import com.plugable.mcommerceapp.cpmvp1.network.model.BookAppointmentResponse
import com.plugable.mcommerceapp.cpmvp1.network.model.GetAppointmentTypeResponse
import com.plugable.mcommerceapp.cpmvp1.network.presenter.AppointmentPresenter
import com.plugable.mcommerceapp.cpmvp1.network.view.AppointmentView
import com.plugable.mcommerceapp.cpmvp1.ui.adapters.SpinnerAdapter
import com.plugable.mcommerceapp.cpmvp1.utils.constants.IntentFlags
import com.plugable.mcommerceapp.cpmvp1.utils.extension.*
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.cpmvp1.utils.util.isNetworkAccessible
import com.plugable.mcommerceapp.cpmvp1.utils.validation.isEmpty
import com.plugable.mcommerceapp.cpmvp1.utils.validation.isValidMobileNumber
import com.plugable.mcommerceapp.cpmvp1.utils.validation.onTextChanged
import kotlinx.android.synthetic.main.activity_book_appointment.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashSet


class BookAppointmentActivity : BaseActivity(),
    AppointmentView, AdapterView.OnItemSelectedListener, OnItemCheckListener {

    private lateinit var appointmentName: String
    private lateinit var appointmentType: List<GetAppointmentTypeResponse.AppointmentTypeData>
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
//        textViewAppointmentType.setOnClickListener(this)
//        editTextDate.setOnClickListener(this)
//        editTextTime.setOnClickListener(this)

        editTextDate.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                this.hideKeyboard(v)
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val date = calendar.get(Calendar.DAY_OF_MONTH)
                val datePickerDialog = DatePickerDialog(
                    this,
                    DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                        editTextDate.setText("$dayOfMonth/$month/$year")
                    },
                    year,
                    month,
                    date
                )

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
                        editTextTime.setText("$hourOfDay:$minute")
                    },
                    hh,
                    mm,
                    false
                )

                timePickerDialog.show()
            }
            return@setOnTouchListener false
        }
        textChangeListeners()
        buttonAddAppointment.isClickable = true
        hideProgress()
        /*   val arrayAdapterType =
               ArrayAdapter<String>(
                   this,
                   android.R.layout.simple_spinner_item,
                   appointmentType
               )
           arrayAdapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

           spinnerAppointmentType.onItemSelectedListener = this
           spinnerAppointmentType.adapter = arrayAdapterType

   */

        val contactNumber = SharedPreferences.getInstance(this).getProfile()?.mobileNumber
        editTextphoneNumber.setText(contactNumber)
        if (isNetworkAccessible()) {
            appointmentPresenter.getAppointmentType()

        } else {
            showNetworkCondition()
        }

//        spinnerAppointmentType.adapter=SpinnerAdapter(this,appointmentType)
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
        setStatusBarColor()
        supportActionBar?.setDisplayShowTitleEnabled(true)
        cp_Logo.hide()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        txtToolbarTitle.show()
        txtToolbarTitle.allCaps = false
        txtToolbarTitle.text = "Book Appointment"
        imgToolbarHome.setImageResource(R.drawable.ic_shape_backarrow)
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

                if (contactValidation() && dateValidation() && timeValidation()) {
                    buttonAddAppointment.isClickable = false
                    attemptApiCall()
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
                editTextphoneNumber.text.toString(), editTextDescription.text.toString(),
                timeToTimeStamp.toString(),
                checkedId
            )
            appointmentPresenter.bookAppointment(bookData)

        } else {
            buttonAddAppointment.isClickable = true
            hideProgress()
            showNetworkCondition()
        }
    }

    override fun onGetAppointmentTypeSuccess(response: GetAppointmentTypeResponse) {
        appointmentType = response.data


        appointmentType.forEachIndexed { index, appointmentTypeData ->
            //            appointmentType[0].name="Select  appointment type"
            appointmentName = appointmentType[index].name
        }
        val spinnerAdapter = SpinnerAdapter(this, this, checkedId, appointmentType)
        spinnerAppointmentType.adapter = spinnerAdapter
    }

    override fun onBookAppointmentSuccess(response: BookAppointmentResponse) {
        toast(response.message)
        onBackPressed()
        finish()
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


    override fun onDestroy() {
        appointmentPresenter.onStop()
        super.onDestroy()
    }


    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        spinnerAppointmentType.prompt = checkedId.toString()
    }

    override fun onItemCheck(clickedId: Int, isChecked: Boolean) {
        if (isChecked)
            checkedId.add(clickedId)
        else
            checkedId.remove(clickedId)
    }
}
