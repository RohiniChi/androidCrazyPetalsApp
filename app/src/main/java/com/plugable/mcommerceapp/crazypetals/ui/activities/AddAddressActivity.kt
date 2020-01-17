package com.plugable.mcommerceapp.crazypetals.ui.activities

import ServiceGenerator
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.AddressAddResponse
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.AddressRequest
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.DeliveryChartResponse
import com.plugable.mcommerceapp.crazypetals.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.crazypetals.utils.application.App
import com.plugable.mcommerceapp.crazypetals.utils.constants.IntentFlags
import com.plugable.mcommerceapp.crazypetals.utils.extension.*
import com.plugable.mcommerceapp.crazypetals.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.crazypetals.utils.util.isNetworkAccessible
import com.plugable.mcommerceapp.crazypetals.utils.validation.onTextChanged
import kotlinx.android.synthetic.main.activity_add_address.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*
import org.jetbrains.anko.toast
import org.json.JSONObject
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

    private var validationError: String = ""
    private var addAddressError: String = ""
    private var editAddresError: String = ""
    private lateinit var editAddressApi: Call<AddressAddResponse>
    private lateinit var addAddressApi: Call<AddressAddResponse>
    var pinCode: Array<String?> = arrayOf()
    private var addressRequest: AddressRequest? = null
    private var addRequest = true
    var data: ArrayList<DeliveryChartResponse.Data?> = ArrayList()
    private var phPattern: String = "[6-9][0-9]{9}"
    private var pincodePattern: String = "[1-9][0-9]{5}"
    private lateinit var mixPanel: MixpanelAPI


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
        sendMixPanelEvent("visitedScreen")
    }

    private fun changeTitleAndSubmitButton() {
        if (addRequest) {
//            txtToolbarTitle.text = "New Address"
            supportActionBar?.title = "New Address"
            buttonAddAddress.text = "Add Address"
        } else {
//            txtToolbarTitle.text = "Edit Address"
            supportActionBar?.title = "Edit Address"
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
        editTextState.setText(addressRequest!!.state)
        editTextCountry.setText(addressRequest!!.country)
        etPinCode.setText(addressRequest!!.PinCode)

    }

    private fun initializeViews() {
        imgToolbarHomeLayout.setOnClickListener(this)
        buttonAddAddress.setOnClickListener(this)
        textChangeListeners()
        mixPanel = MixpanelAPI.getInstance(this, resources.getString(R.string.mix_panel_token))
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
        supportActionBar?.title = "New Address"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_shape_backarrow_white)
        cp_Logo.hide()
        txtToolbarTitle.hide()
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
                stateValidation()
                countryValidation()

                if (phoneNoValidation() && flatNoValidation() && localityValidation() && pinCodeValidation() && areaValidation()
                    && cityValidation() && stateValidation() && countryValidation()
                ) {
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
        editAddressApi = clientInstance.editAddress(addressRequest)

        editAddressApi.enqueue(object : Callback<AddressAddResponse> {
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
                    editAddresError = response.body()!!.message
                    sendMixPanelEvent("editAddressError")

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
                state = editTextState.text.toString().capitalize(),
                country = editTextCountry.text.toString().capitalize(),
                PinCode = etPinCode.text.toString(),
                PinCodeId = 0
            )
        return addressRequest!!
    }

    private fun callAddOrEditAddressApi(addressRequest: AddressRequest) {
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        addAddressApi = clientInstance.addAddress(addressRequest)

        addAddressApi.enqueue(object : Callback<AddressAddResponse> {
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
                    addAddressError = response.body()!!.message
                    sendMixPanelEvent("addAddressError")
                }

            }

        })
    }

    private fun sendMixPanelEvent(mixPanelTitle: String) {
        val productObject = JSONObject()
        if (mixPanelTitle.equals("visitedScreen", true)) {
            if (addRequest) {
                mixPanel.track(IntentFlags.MIXPANEL_VISITED_ADD_ADDRESS_SCREEN, productObject)
            } else {
                mixPanel.track(IntentFlags.MIXPANEL_VISITED_EDIT_ADDRESS_SCREEN, productObject)
            }
        } else if (mixPanelTitle.equals("validationError", true)) {
            if (addRequest) {
                productObject.put(
                    IntentFlags.MIXPANEL_ADD_ADDRESS_VALIDATION_ERROR,
                    validationError
                )
                mixPanel.track(IntentFlags.MIXPANEL_ADD_ADDRESS_VALIDATION_ERROR, productObject)
            } else {
                productObject.put(
                    IntentFlags.MIXPANEL_EDIT_ADDRESS_VALIDATION_ERROR,
                    validationError
                )
                mixPanel.track(IntentFlags.MIXPANEL_EDIT_ADDRESS_VALIDATION_ERROR, productObject)
            }
        } else if (mixPanelTitle.equals("addAddressError", true)) {
            productObject.put(IntentFlags.MIXPANEL_ADD_ADDRESS_ERROR, addAddressError)
            mixPanel.track(IntentFlags.MIXPANEL_ADD_ADDRESS_ERROR, productObject)
        } else if (mixPanelTitle.equals("editAddressError", true)) {
            productObject.put(IntentFlags.MIXPANEL_EDIT_ADDRESS_ERROR, editAddresError)
            mixPanel.track(IntentFlags.MIXPANEL_EDIT_ADDRESS_ERROR, productObject)
        }


    }


    private fun textChangeListeners() {

        editTextPhoneNo.setOnFocusChangeListener { _, isFocused ->

            if (!isFocused) {
                //this.hideKeyboard(view)
                phoneNoValidation()
            }

        }

        editTextFlatNo.setOnFocusChangeListener { _, isFocused ->

            if (!isFocused) {
                //this.hideKeyboard(view)
                flatNoValidation()
            }

        }

        editTextLocality.setOnFocusChangeListener { _, isFocused ->

            if (!isFocused) {
                //this.hideKeyboard(view)
                localityValidation()
            }


        }


        editTextArea.setOnFocusChangeListener { _, isFocused ->

            if (!isFocused) {
                //this.hideKeyboard(view)
                areaValidation()
            }


        }
        editTextCity.setOnFocusChangeListener { _, isFocused ->

            if (!isFocused) {
                //this.hideKeyboard(view)
                cityValidation()
            }


        }

        editTextState.setOnFocusChangeListener { _, isFocused ->

            if (!isFocused) {
                //this.hideKeyboard(view)
                stateValidation()
            }


        }

        editTextCountry.setOnFocusChangeListener { _, isFocused ->

            if (!isFocused) {
                //this.hideKeyboard(view)
                countryValidation()
            }


        }

        etPinCode.setOnFocusChangeListener { view, isFocused ->

            if (!isFocused) {
                this.hideKeyboard(view)
                pinCodeValidation()
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
        editTextArea.onTextChanged {
            textViewAreaError.invisible()
        }
        editTextCity.onTextChanged {
            textViewCityError.invisible()
        }

        editTextState.onTextChanged {
            textViewStateError.invisible()
        }

        editTextCountry.onTextChanged {
            textViewCountryError.invisible()
        }
        etPinCode.onTextChanged {
            textViewSpinnerError.invisible()
        }

    }


    private fun phoneNoValidation(): Boolean {

        when {
            editTextPhoneNo.text.toString().isEmpty() -> {
                textViewPhoneNoError.show()
                textViewPhoneNoError.text = getString(R.string.validation_enter_number)
                validationError = getString(R.string.validation_enter_number)
                sendMixPanelEvent("validationError")
                return false
            }
            !editTextPhoneNo.text.toString().matches(phPattern.toRegex()) -> {
                textViewPhoneNoError.show()
                textViewPhoneNoError.text = getString(R.string.validation_number)
                validationError = getString(R.string.validation_number)
                sendMixPanelEvent("validationError")
                return false
            }
        }
        textViewPhoneNoError.invisible()
        validationError = ""
        return true
    }

    private fun flatNoValidation(): Boolean {
        when {

            editTextFlatNo.text.toString().isEmpty() -> {
                textViewFlatNoError.show()
                textViewFlatNoError.text = getString(R.string.validation_flat_no)
                validationError = getString(R.string.validation_flat_no)
                sendMixPanelEvent("validationError")

                return false
            }
            else -> textViewFlatNoError.invisible()
        }
        validationError = ""
        return true
    }


    private fun localityValidation(): Boolean {

        when {
            editTextLocality.text.toString().isEmpty() -> {
                textViewLocalityError.show()
                textViewLocalityError.text = getString(R.string.validation_locality_landmark)
                validationError = getString(R.string.validation_locality_landmark)
                sendMixPanelEvent("validationError")
                return false
            }
            else -> textViewLocalityError.invisible()
        }
        validationError = ""
        return true
    }

    private fun areaValidation(): Boolean {

        when {
            editTextArea.text.toString().isEmpty() -> {
                textViewAreaError.show()
                textViewAreaError.text = getString(R.string.validation_area)
                validationError = getString(R.string.validation_area)
                sendMixPanelEvent("validationError")
                return false
            }
            else -> textViewAreaError.invisible()
        }
        validationError = ""
        return true
    }

    private fun cityValidation(): Boolean {

        when {
            editTextCity.text.toString().isEmpty() -> {
                textViewCityError.show()
                textViewCityError.text = getString(R.string.validation_enter_city)
                validationError = getString(R.string.validation_enter_city)
                sendMixPanelEvent("validationError")
                return false
            }
            else -> textViewCityError.invisible()
        }
        validationError = ""
        return true
    }

    private fun stateValidation(): Boolean {

        when {
            editTextState.text.toString().isEmpty() -> {
                textViewStateError.show()
                textViewStateError.text = getString(R.string.validation_enter_state)
                validationError = getString(R.string.validation_enter_state)
                sendMixPanelEvent("validationError")
                return false
            }
            else -> textViewStateError.invisible()
        }
        validationError = ""
        return true
    }

    private fun countryValidation(): Boolean {

        when {
            editTextCountry.text.toString().isEmpty() -> {
                textViewCountryError.show()
                textViewCountryError.text = getString(R.string.validation_enter_country)
                validationError = getString(R.string.validation_enter_country)
                sendMixPanelEvent("validationError")
                return false
            }
            else -> textViewCountryError.invisible()
        }
        validationError = ""
        return true
    }


    private fun pinCodeValidation(): Boolean {


        when {
            etPinCode.text.toString().isEmpty() -> {
                textViewSpinnerError.show()
                textViewSpinnerError.text = getString(R.string.validation_enter_pincode)
                validationError = getString(R.string.validation_enter_pincode)
                sendMixPanelEvent("validationError")
                return false
            }
            !etPinCode.text.toString().matches(pincodePattern.toRegex()) -> {

                textViewSpinnerError.show()
                textViewSpinnerError.text = getString(R.string.validation_correct_pincode)
                validationError = getString(R.string.validation_correct_pincode)
                sendMixPanelEvent("validationError")
                return false
            }

            else -> textViewSpinnerError.invisible()
        }
        validationError = ""
        return true
    }

    private fun cancelTasks() {
        if (::editAddressApi.isInitialized && editAddressApi != null) editAddressApi.cancel()
        if (::addAddressApi.isInitialized && addAddressApi != null) addAddressApi.cancel()
    }

    override fun onDestroy() {
        mixPanel.flush()
        cancelTasks()
        super.onDestroy()
    }

}
