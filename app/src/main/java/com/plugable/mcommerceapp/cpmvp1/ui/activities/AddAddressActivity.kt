package com.plugable.mcommerceapp.cpmvp1.ui.activities

import ServiceGenerator
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.View
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

class AddAddressActivity : AppCompatActivity(),
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

        readIntent()
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

        editTextLocality.setText(addressRequest!!.Landmark)

        /*
            editTextCity.setText("Pune")
            editTextArea.isEnabled = false
            editTextCity.isEnabled = false
        */

        editTextPhoneNo.setText(addressRequest!!.MobileNumber)
        editTextFlatNo.setText(addressRequest!!.Address)
        editTextArea.setText(addressRequest!!.Locality)
        editTextCity.setText(addressRequest!!.city)
        etPinCode.setText(addressRequest!!.PinCode)

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
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = name
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_shape_backarrow_white)
        cp_Logo.hide()
        txtToolbarTitle.text = "New Address"
        imgToolbarHome.hide()
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
                pinCodeValidation()
                areaValidation()
                cityValidation()

                if (phoneNoValidation() && flatNoValidation() && localityValidation() && pinCodeValidation() && areaValidation() && cityValidation()) {
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
                Locality = editTextArea.text.toString().capitalize(),
                ApplicationUserId = SharedPreferences.getInstance(this).getStringValue(IntentFlags.APPLICATION_USER_ID)!!,
                MobileNumber = editTextPhoneNo.text.toString(),
                city = editTextCity.text.toString().capitalize(),
                PinCode = etPinCode.text.toString(),
                PinCodeId = 0
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
                textViewFlatNoError.text = "Please enter flat no/house no/floor/building."

                return false
            }
            /*editTextFlatNo.text.toString().length > 6 -> {
                textViewFlatNoError.show()
                textViewFlatNoError.text = "Please enter your address details "

                return false
            }*/
            else -> textViewFlatNoError.invisible()
        }
        return true
    }


    private fun localityValidation(): Boolean {

        when {
            editTextLocality.text.toString().isEmpty() -> {
                textViewLocalityError.show()
                textViewLocalityError.text = "Please enter colony/street/landmark."
                return false
            }
            else -> textViewLocalityError.invisible()
        }
        return true
    }

    private fun areaValidation(): Boolean {

        when {
            editTextArea.text.toString().isEmpty() -> {
                textViewAreaError.show()
                textViewAreaError.text = "Please enter area/locality."
                return false
            }
            else -> textViewAreaError.invisible()
        }
        return true
    }

    private fun cityValidation(): Boolean {

        when {
            editTextCity.text.toString().isEmpty() -> {
                textViewCityError.show()
                textViewCityError.text = "Please enter city."
                return false
            }
            else -> textViewCityError.invisible()
        }
        return true
    }


    private fun pinCodeValidation(): Boolean {

        when {
            editTextLocality.text.toString().isEmpty() -> {
                textViewSpinnerError.show()
                textViewSpinnerError.text = "Please enter a pin code."
                return false
            }
            else -> textViewSpinnerError.invisible()
        }
        return true
    }


}
