package com.plugable.mcommerceapp.cpmvp1.ui.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.cpmvp1.utils.extension.*
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.cpmvp1.utils.validation.isEmpty
import com.plugable.mcommerceapp.cpmvp1.utils.validation.isValidMobileNumber
import com.plugable.mcommerceapp.cpmvp1.utils.validation.onTextChanged
import kotlinx.android.synthetic.main.activity_book_appointment.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.toast
import java.time.Month
import java.time.Year
import java.util.*

class BookAppointmentActivity : BaseActivity(), AdapterView.OnItemSelectedListener {

    val spinrListType =
        arrayListOf("Select type of appointment", "Saree", "Jewellery", "Dress", "Kurti", "Blowse")

    /* val spinrListDate= arrayListOf("Select date for appointment","14/12/2019","15/12/2019","16/12/2019","17/12/2019","18/12/2019")
     val spinrListTime= arrayListOf("Select time for appointment ","5pm","6pm","4pm","3pm","7pm")
 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_appointment)
        initializeTheme()
        initializeViews()
    }

    private fun initializeViews() {
        imgToolbarHomeLayout.setOnClickListener(this)
        buttonBookAppointment.setOnClickListener(this)
        imageViewDate.setOnClickListener(this)
        imageViewTime.setOnClickListener(this)

        textChangeListeners()

        val arrayAdapterType =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinrListType)
        arrayAdapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerAppointmentType.onItemSelectedListener = this
        spinnerAppointmentType.adapter = arrayAdapterType

        /*    val arrayAdapterDate=ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,spinrListDate)
            arrayAdapterDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            spinnerAppointmentDate.onItemSelectedListener=this
            spinnerAppointmentDate.adapter=arrayAdapterDate
            val arrayAdapterTime =
                ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinrListTime)
            arrayAdapterTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            spinnerAppointmentTime.onItemSelectedListener = this
            spinnerAppointmentTime.adapter = arrayAdapterTime*/
        val contactNumber=SharedPreferences.getInstance(this).getProfile()?.mobileNumber
        editTextphoneNumber.setText(contactNumber)
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
        buttonBookAppointment.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        setStatusBarColor()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgToolbarHomeLayout -> {
                onBackPressed()
            }
            R.id.buttonBookAppointment -> {
                if (contactValidation()) {
                    toast("Appointment booked")
                }
            }
            R.id.imageViewDate -> {
                val calendar=Calendar.getInstance()
                val year=calendar.get(Calendar.YEAR)
                val month=calendar.get(Calendar.MONTH)
                val date=calendar.get(Calendar.DAY_OF_MONTH)
                val datePickerDialog=DatePickerDialog(this,DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                    editTextDate.setText("$dayOfMonth/$month/$year")
                },year,month,date)
                datePickerDialog.show()
            }
            R.id.imageViewTime -> {
                val calendar=Calendar.getInstance()
                val hh=calendar.get(Calendar.HOUR_OF_DAY)
                val mm=calendar.get(Calendar.MINUTE)
                val timePickerDialog=TimePickerDialog(this,TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    editTextTime.setText("$hourOfDay:$minute")
                },hh,mm,false)
                timePickerDialog.show()
            }
        }
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
        textViewMobileNoError.invisible()

        return true
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
    }

}
