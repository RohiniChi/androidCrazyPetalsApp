package com.plugable.mcommerceapp.cpmvp1.ui.activities

import ServiceGenerator
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.AddressAddResponse
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.AddressRequest
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.DeliveryChartResponse
import com.plugable.mcommerceapp.cpmvp1.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.cpmvp1.utils.application.App
import com.plugable.mcommerceapp.cpmvp1.utils.constants.IntentFlags
import com.plugable.mcommerceapp.cpmvp1.utils.extension.*
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.cpmvp1.utils.util.isNetworkAccessible
import com.plugable.mcommerceapp.cpmvp1.utils.validation.onTextChanged
import kotlinx.android.synthetic.main.activity_add_address.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddAddressActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener,
    View.OnClickListener {
    companion object {
        const val ADD_REQUEST = 1
        const val EDIT_REQUEST = 22
        const val REQUEST_CODE = "request.code"
        const val ADDRESS = "address"
    }

    var pinCode: Array<String?> = arrayOf()
    var addressRequest: AddressRequest? = null
    var addRequest = true
    var data: ArrayList<DeliveryChartResponse.Data?> = ArrayList()
    private var phPattern: String = "[6-9][0-9]{9}"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_address)
        initializeTheme()
        initializeViews()
        fetchDeliveryChart()
        readIntent()
    }

    private fun fetchDeliveryChart() {
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        val callback = clientInstance.getDeliveryChart()

        callback.enqueue(object : Callback<DeliveryChartResponse> {
            override fun onFailure(call: Call<DeliveryChartResponse>, t: Throwable) {
                toast(getString(R.string.message_something_went_wrong))
            }

            override fun onResponse(
                call: Call<DeliveryChartResponse>,
                response: Response<DeliveryChartResponse>
            ) {
                if (response.body()?.statusCode.equals("10")) {
                    data.addAll(response.body()!!.data)
                    pinCode = arrayOfNulls(data.size + 1)
                    pinCode[0] = "Select a pin code"
                    data.forEachIndexed { index, it ->
                        pinCode[index + 1] = String.format(
                            "%s(%s)", it!!.area, it.pinCode
                        )
                    }
                    setUpSpinner()
                    readIntent()
                } else {
                    toast(getString(R.string.message_something_went_wrong))
                }
            }
        })
    }

    private fun readIntent() {
        addRequest = intent.getIntExtra(REQUEST_CODE, ADD_REQUEST) == ADD_REQUEST
        addressRequest = intent.getParcelableExtra(ADDRESS)
        changeTitleAndSubmitButton()
        showCurrentData()
    }

    private fun changeTitleAndSubmitButton() {
        if (addRequest) {
            txtToolbarTitle.text = "New Address"
            buttonAddAddress.text = "Add Address"
        } else {
            txtToolbarTitle.text = "Edit Address"
            buttonAddAddress.text = "Update Address"
        }
    }

    private fun showCurrentData() {
        if (addRequest) {
            return
        }
        textViewCity.show()
        editTextCity.show()
        editTextArea.show()
        textViewArea.show()
        dividerSpinner.show()
        editTextLocality.setText(addressRequest!!.Landmark)
        editTextCity.setText("Pune")
        editTextArea.isEnabled = false
        editTextCity.isEnabled = false
        editTextPhoneNo.setText(addressRequest!!.MobileNumber)
        editTextFlatNo.setText(addressRequest!!.Address)
        pinCode.forEachIndexed { index, s ->
            if (s!!.contains(addressRequest!!.PinCode) && s.contains(addressRequest!!.Locality)) {
                spinnerPinCode.setSelection(index)
            }
        }


    }

    private fun initializeViews() {
        imgToolbarHomeLayout.setOnClickListener(this)
        buttonAddAddress.setOnClickListener(this)
        textChangeListeners()
    }

    private fun setUpSpinner() {
        val arrayAdapter =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, pinCode)

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)


        spinnerPinCode.onItemSelectedListener = this
        spinnerPinCode.adapter = arrayAdapter
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

        buttonAddAddress.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
    }

    fun setToolBar(name: String) {
        setSupportActionBar(toolBar)
        setStatusBarColor()
        supportActionBar?.setDisplayShowTitleEnabled(false)
        txtToolbarTitle.text = "New Address"
        imgToolbarHome.setImageResource(R.drawable.ic_shape_backarrow)
        setToolBarColor(imgToolbarHome, txtToolbarTitle, toolbar = toolBar)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgToolbarHomeLayout -> onBackPressed()
            R.id.buttonAddAddress -> {
                if (!isNetworkAccessible()) {
                    toast(R.string.oops_no_internet_connection)
                    return
                }
                localityValidation()
                flatNoValidation()
                phoneNoValidation()
                spinnerValidation()

                if (phoneNoValidation() && flatNoValidation() && localityValidation() && spinnerValidation()) {
                    buttonAddAddress.isEnabled = false
                    if (addRequest) callAddOrEditAddressApi(makeAddressObject()) else editAddress(
                        makeAddressObject()
                    )
                }
            }

        }
    }

    private fun editAddress(addressRequest: AddressRequest) {
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        val callback = clientInstance.editAddress(addressRequest)

        callback.enqueue(object : Callback<AddressAddResponse> {
            override fun onFailure(call: Call<AddressAddResponse>, t: Throwable) {
                buttonAddAddress.isEnabled = true
                toast(getString(R.string.message_something_went_wrong))
            }

            override fun onResponse(
                call: Call<AddressAddResponse>,
                response: Response<AddressAddResponse>
            ) {
                if (response.body()?.statusCode.equals("10")) {
                    setResult(Activity.RESULT_OK)
                    toast(response.body()!!.message)
                    finish()
                } else {
                    buttonAddAddress.isEnabled = true
                    toast(getString(R.string.message_something_went_wrong))
                }

            }

        })
    }

    @SuppressLint("DefaultLocale")
    private fun makeAddressObject(): AddressRequest {
        addressRequest =
            AddressRequest(
                ID = if (!addRequest) addressRequest!!.ID else null,
                Address = editTextFlatNo.text.toString().capitalize(),
                Landmark = editTextLocality.text.toString().capitalize(),
                Locality = data[spinnerPinCode.selectedItemPosition - 1]!!.area,
                ApplicationUserId = SharedPreferences.getInstance(this).getStringValue(IntentFlags.APPLICATION_USER_ID)!!,
                MobileNumber = editTextPhoneNo.text.toString(),
                PinCode = data[spinnerPinCode.selectedItemPosition - 1]!!.pinCode,
                PinCodeId = data[spinnerPinCode.selectedItemPosition - 1]!!.id
            )
        return addressRequest!!
    }

    private fun callAddOrEditAddressApi(addressRequest: AddressRequest) {
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        val callback = clientInstance.addAddress(addressRequest)

        callback.enqueue(object : Callback<AddressAddResponse> {
            override fun onFailure(call: Call<AddressAddResponse>, t: Throwable) {
                buttonAddAddress.isEnabled = true
                toast(getString(R.string.message_something_went_wrong))
            }

            override fun onResponse(
                call: Call<AddressAddResponse>,
                response: Response<AddressAddResponse>
            ) {
                if (response.body()?.statusCode.equals("10")) {
                    setResult(Activity.RESULT_OK)
                    toast(response.body()!!.message)
                    finish()
                } else {
                    buttonAddAddress.isEnabled = true
                    toast(getString(R.string.message_something_went_wrong))
                }

            }

        })
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
//        Toast.makeText(applicationContext,pinCode[p2],Toast.LENGTH_LONG).show()


        if (p2 != 0) {
            if (textViewSpinnerError.visibility == View.VISIBLE) {
                textViewSpinnerError.invisible()
            }
            textViewCity.show()
            editTextCity.show()
            editTextArea.show()
            textViewArea.show()
            dividerSpinner.show()
            editTextArea.setText(data[p2 - 1]!!.area)
            editTextCity.setText("Pune")
            editTextArea.isEnabled = false
            editTextCity.isEnabled = false

        } else {
            textViewCity.hide()
            editTextCity.hide()
            editTextArea.hide()
            textViewArea.hide()
            dividerSpinner.hide()
        }
    }

    private fun textChangeListeners() {

        editTextPhoneNo.setOnFocusChangeListener { view, isFocused ->

            if (!isFocused) {
                this.hideKeyboard(view)
                phoneNoValidation()
            }

        }

        editTextFlatNo.setOnFocusChangeListener { view, isFocused ->

            if (!isFocused) {
                this.hideKeyboard(view)
                flatNoValidation()
            }

        }

        editTextLocality.setOnFocusChangeListener { view, isFocused ->

            if (!isFocused) {
                this.hideKeyboard(view)
                localityValidation()
            }


        }

        editTextPhoneNo.onTextChanged {
            textViewPhoneNoError.invisible()
        }
        editTextFlatNo.onTextChanged {
            textViewFlatNoError.invisible()
        }
        editTextLocality.onTextChanged {
            textViewLocalityError.invisible()
        }

    }


    private fun phoneNoValidation(): Boolean {

        when {
            editTextPhoneNo.text.toString().isEmpty() -> {
                textViewPhoneNoError.show()
                textViewPhoneNoError.text = "Please enter your mobile number"

                return false
            }
            !editTextPhoneNo.text.toString().matches(phPattern.toRegex()) -> {

                textViewPhoneNoError.show()
                textViewPhoneNoError.text = "Enter a 10 digit mobile number"


                return false
            }
        }
        textViewPhoneNoError.invisible()

        return true
    }

    private fun flatNoValidation(): Boolean {
        when {

            editTextFlatNo.text.toString().isEmpty() -> {
                textViewFlatNoError.show()
                textViewFlatNoError.text = "Please enter your address details "

                return false
            }
            else -> textViewFlatNoError.invisible()
        }
        return true
    }


    private fun localityValidation(): Boolean {

        when {
            editTextLocality.text.toString().isEmpty() -> {
                textViewLocalityError.show()
                textViewLocalityError.text = "Please enter your address details"
                return false
            }
            else -> textViewLocalityError.invisible()
        }
        return true
    }

    private fun spinnerValidation(): Boolean {
        return if (spinnerPinCode.selectedItemPosition > 0) {
            textViewSpinnerError.invisible()
            true
        } else {
            textViewSpinnerError.text = "Please select a pin code"
            textViewSpinnerError.show()
            false
        }
    }
}
