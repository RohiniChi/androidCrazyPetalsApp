package com.plugable.mcommerceapp.cpmvp1.ui.activities

import ServiceGenerator
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.DeliveryLocation
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.DeliveryScheduleAddress
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.ProductBooking
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.Products
import com.plugable.mcommerceapp.cpmvp1.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.cpmvp1.utils.application.App
import com.plugable.mcommerceapp.cpmvp1.utils.constants.IntentFlags
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi
import com.plugable.mcommerceapp.cpmvp1.utils.extension.*
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.cpmvp1.utils.util.isNetworkAccessible
import com.plugable.mcommerceapp.cpmvp1.utils.validation.EditTextValidations.MAX_NAME_LENGTH
import com.plugable.mcommerceapp.cpmvp1.utils.validation.isEmpty
import com.plugable.mcommerceapp.cpmvp1.utils.validation.isValidEmail
import com.plugable.mcommerceapp.cpmvp1.utils.validation.isValidMobileNumber
import com.plugable.mcommerceapp.cpmvp1.utils.validation.onTextChanged
import kotlinx.android.synthetic.main.fragment_product_booking.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*
import org.jetbrains.anko.toast
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * [RequestProductBookingActivity] is used to request booking for products
 *
 */
class RequestProductBookingActivity : AppCompatActivity(), View.OnClickListener,
    View.OnTouchListener {

    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
        this.hideKeyboard(view!!)
        if (motionEvent != null) {
            when (motionEvent.action) {
                MotionEvent.ACTION_UP -> {
                    if (isNetworkAccessible()) {
                        deliverAddressApi()
                    } else {
                        val tempList: List<DeliveryLocation> = emptyList()
                        addressAdapter(tempList)
                        toast(R.string.check_internet_connection)
                    }

                }
            }
        }
        return true
    }

    private lateinit var selectedPincode: String

    private lateinit var deliveryLocationToPass: DeliveryLocation

    private lateinit var product: Products.Data.ProductDetails

    private lateinit var mixPanel: MixpanelAPI


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_product_booking)
        initializeTheme()
        initializeViews()

        textChangeListeners()
//        setProductsToOrderDetails()

        val tempList: List<DeliveryLocation> = emptyList()
        addressAdapter(tempList)

        spinnerAreaPinCode.setOnTouchListener(this)

    }


    private fun deliverAddressApi() {

        val apiInterface = ServiceGenerator.createService(ProjectApi::class.java)
        val call = apiInterface.getDeliveryAddress()


        call.enqueue(object : Callback<DeliveryScheduleAddress> {
            override fun onFailure(call: Call<DeliveryScheduleAddress>, throwable: Throwable) {

                toast(R.string.message_something_went_wrong)

            }

            override fun onResponse(
                call: Call<DeliveryScheduleAddress>,
                response: Response<DeliveryScheduleAddress>
            ) {

                if (response.body()!!.statusCode == "10") {

                    addressAdapter(response.body()?.data?.deliveryLocationList!!)
                }
            }

        })
    }

    private fun addressAdapter(deliveryLocationList: List<DeliveryLocation>) {
        val arrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        if (deliveryLocationList.isEmpty()) {
            arrayAdapter.add(getString(R.string.spinner_select))
        } else {
            arrayAdapter.add(getString(R.string.spinner_select))
            deliveryLocationList.forEachIndexed { _, deliveryLocation ->
                arrayAdapter.add(
                    deliveryLocation.area.plus("(").plus(deliveryLocation.pinCode).plus(
                        ")"
                    )
                )
            }
        }
        spinnerAreaPinCode.adapter = arrayAdapter

        if (arrayAdapter.count != 1)
            spinnerAreaPinCode.performClick()

        spinnerAreaPinCode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }

            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                index: Int,
                length: Long
            ) {

                if (index == 0) {
                    view as TextView?
                    view?.setTextColor(Color.GRAY)
                }
                if (index - 1 >= 0) {
                    textViewSpinnerError.invisible()
                    deliveryLocationToPass = deliveryLocationList[index - 1]
                    selectedPincode = deliveryLocationList[index - 1].pinCode
                } else {
                    textViewSpinnerError.invisible()
                    selectedPincode = getString(R.string.spinner_select)
                }
            }

        }

    }

    private fun setProductsToOrderDetails() {
//        if (intent.hasExtra(IntentFlags.PRODUCT_MODEL)) {
//            product = intent.getParcelableExtra(IntentFlags.PRODUCT_MODEL)!!
//            textInputEditTextOrderDetails.setText(
//                String.format(
//                    getString(R.string.order_detail_data).plus("1.").plus(product.name).plus(
//                        " - "
//                    ).plus(product.quantity).plus(
//                        product.unit
//                    )
//                )
//
//            )
//
//            checkBoxWishlist.invisible()
//        }
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
        setToolBar()
        setThemeToComponents()
    }

    private fun setThemeToComponents() {
        editTextName.backgroundTintList = ColorStateList.valueOf(Color.BLACK)
        editTextPhone.backgroundTintList = ColorStateList.valueOf(Color.BLACK)
        editTextEmail.backgroundTintList = ColorStateList.valueOf(Color.BLACK)
        textInputLayoutOrderDetails.boxStrokeColor = Color.BLACK
        submitButton.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        checkBoxWishlist.buttonTintList =
            ColorStateList.valueOf(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        requestBookingProgressBar.indeterminateDrawable.setColorFilter(
            Color.BLACK,
            PorterDuff.Mode.MULTIPLY
        )
    }


    private fun textChangeListeners() {

        editTextName.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused) {
                this.hideKeyboard(view)
                nameValidation()
            }
        }

        editTextPhone.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused) {
                this.hideKeyboard(view)
                contactValidation()
            }
        }

        editTextEmail.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused) {
                this.hideKeyboard(view)
                emailValidation()
            }
        }

        editTextName.onTextChanged {
            textViewNameError.invisible()
        }

        editTextPhone.onTextChanged {
            textViewPhoneError.invisible()
        }

        editTextEmail.onTextChanged {
            textViewEmailError.invisible()
        }


        textInputEditTextOrderDetails.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused) {
                orderDetailsValidation()
                this.hideKeyboard(view)
            }
        }

    }

    private fun initializeViews() {
        mixPanel = MixpanelAPI.getInstance(this, resources.getString(R.string.mix_panel_token))

        submitButton.setOnClickListener(this)
        imgToolbarHomeLayout.setOnClickListener(this)
        sendMixPanelEvent()

    }


    private fun sendMixPanelEvent() {
        val productObject = JSONObject()
        productObject.put(
            IntentFlags.MIXPANEL_PRODUCT_ID,
            getString(R.string.mixpanel_request_booking_from_product_detail)
        )
        mixPanel.track(
            IntentFlags.MIXPANEL_VISITED_REQUEST_BOOKING_SCREEN_FROM_PRODUCT_DETAIL,
            productObject
        )
    }

    private fun attemptApiCall() {
        if (isNetworkAccessible()) {
            bookingApi()
        } else {
            toast(R.string.check_internet_connection)
        }
    }

    private fun bookingApi() {

        val bookingObject = ProductBooking.Data(

            name = editTextName.text.toString(), mobileNumber = editTextPhone.text.toString(),
            email = editTextEmail.text.toString(),
            area = deliveryLocationToPass.area,
            pinCode = deliveryLocationToPass.pinCode,
            appId = WebApi.APP_ID,
            orderNumber = "",
            description = textInputEditTextOrderDetails.text.toString()
        )

        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        val call = clientInstance.saveCustomerData(bookingObject)

        call.enqueue(object : Callback<ProductBooking> {
            override fun onFailure(call: Call<ProductBooking>, throwable: Throwable) {
                toast(R.string.check_internet_connection)
            }

            override fun onResponse(
                call: Call<ProductBooking>,
                response: Response<ProductBooking>
            ) {

                if (response.isSuccessful) {
                    val orderId = response.body()!!.data.orderNumber
                    val intent = Intent(
                        this@RequestProductBookingActivity,
                        SuccessProductBookingActivity::class.java
                    )
                    intent.putExtra(
                        getString(R.string.delivery_location_to_pass_day),
                        deliveryLocationToPass.day
                    )
                    intent.putExtra(
                        getString(R.string.delivery_location_to_pass_time),
                        deliveryLocationToPass.time
                    )
                    intent.putExtra(getString(R.string.order_number_to_pass), orderId)
                    startActivity(intent)

                } else {
                    toast(response.body()?.message.toString())
                }
            }

        })
    }


    private fun nameValidation(): Boolean {

        when {
            editTextName.isEmpty() -> {

                textViewNameError.show()
                textViewNameError.text = getString(R.string.validation_enter_name)
                return false
            }
            editTextName.text.toString().startsWith(" ") -> {

                textViewNameError.show()
                textViewNameError.text = getString(R.string.validation_empty_space)
                return false
            }
            editTextName.length() > MAX_NAME_LENGTH -> {

                textViewNameError.show()
                textViewNameError.text = getString(R.string.validation_name_length)

                return false
            }

        }
        textViewNameError.invisible()
        return true
    }

    private fun contactValidation(): Boolean {

        when {
            editTextPhone.isEmpty() -> {

                textViewPhoneError.show()
                textViewPhoneError.text = getString(R.string.validation_enter_number)
                return false
            }
            !editTextPhone.isValidMobileNumber() -> {

                textViewPhoneError.show()
                textViewPhoneError.text = getString(R.string.validation_number)

                return false
            }

        }
        textViewPhoneError.invisible()

        return true
    }

    private fun emailValidation(): Boolean {

        when {
            editTextEmail.isEmpty() -> {

                textViewEmailError.show()
                textViewEmailError.text = getString(R.string.validation_enter_email)
                return false
            }
            editTextEmail.text.toString().startsWith(" ") -> {

                textViewEmailError.show()
                textViewNameError.text = getString(R.string.validation_empty_space)
                return false
            }
            !editTextEmail.isValidEmail() -> {

                textViewEmailError.show()
                textViewEmailError.text = getString(R.string.validation_email)

                return false
            }

        }
        textViewEmailError.invisible()
        return true

    }


    private fun orderDetailsValidation(): Boolean {

        when {
            textInputEditTextOrderDetails.isEmpty() -> {

                textInputLayoutOrderDetails.error =
                    getString(R.string.validation_enter_order_detail)
                textInputLayoutOrderDetails.requestFocus()

                return false
            }
            textInputEditTextOrderDetails.text.toString()[0].isWhitespace() -> {

                textInputLayoutOrderDetails.error = getString(R.string.validation_empty_space)
                textInputLayoutOrderDetails.requestFocus()


                return false
            }
            else -> textInputLayoutOrderDetails.error = null
        }

        return true
    }

    fun setToolBar() {
        setSupportActionBar(toolBar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        txtToolbarTitle.hide()
        supportActionBar?.title = getString(R.string.title_request_booking)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
        imgToolbarHome.hide()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setToolBarColor(imgToolbarHome, txtToolbarTitle, toolbar = toolBar)
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.submitButton -> {
                orderDetailsValidation()
                emailValidation()
                contactValidation()
                nameValidation()
                if (!textViewNameError.isVisible && !textViewPhoneError.isVisible && !textViewEmailError.isVisible
                    && orderDetailsValidation()
                    && !selectedPincode.equals(getString(R.string.spinner_select))
                ) {
                    submitButton.isClickable = false
                    requestBookingProgressBar.show()
                    attemptApiCall()
                    sendMixPanelPlacedOrder()

                } else {
                    textViewSpinnerError.show()
                    textViewSpinnerError.text = getString(R.string.area_pincode_validation)
                }
            }
        }
    }

    private fun sendMixPanelPlacedOrder() {
        val productObject = JSONObject()

        val mixpanelAreaPincode =
            deliveryLocationToPass.area.plus("(").plus(deliveryLocationToPass.pinCode.plus(")"))
        productObject.put(IntentFlags.MIXPANEL_AREA_PINCODE, mixpanelAreaPincode)

        val mixpanelPhoneNumber = editTextPhone.text.toString()
        productObject.put(IntentFlags.MIXPANEL_MOBILE_NUMBER, mixpanelPhoneNumber)

        val mixpanelEmail = editTextEmail.text.toString()
        productObject.put(IntentFlags.MIXPANEL_EMAIL_ADDRESS, mixpanelEmail)

        val mixpanelOrderedProducts = textInputEditTextOrderDetails.text.toString()
        productObject.put(IntentFlags.MIXPANEL_ORDERED_PRODUCTS, mixpanelOrderedProducts)

        productObject.put(IntentFlags.MIXPANEL_FROM, IntentFlags.MIXPANEL_PRODUCT_DETAIL)

        mixPanel.track(IntentFlags.MIXPANEL_SUCCESSFULLY_PLACED_ORDER, productObject)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onDestroy() {
        mixPanel.flush()
        super.onDestroy()
    }

    /* @TargetApi(Build.VERSION_CODES.M)
     private fun getCurrentLocation() {
         if (ContextCompat.checkSelfPermission(
                 this,
                 Manifest.permission.ACCESS_FINE_LOCATION
             ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                 this, Manifest.permission.ACCESS_COARSE_LOCATION
             ) != PackageManager.PERMISSION_GRANTED
         ) {
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                 requestPermissions(
                     arrayOf(
                         Manifest.permission.ACCESS_FINE_LOCATION,
                         Manifest.permission.ACCESS_COARSE_LOCATION
                     ), 29
                 )
             }
         } else {
             fetchLocation()
         }
     }

     @SuppressLint("MissingPermission")
     private fun fetchLocation() {
         fusedLocationClient.lastLocation
             .addOnSuccessListener { location: Location? ->
                 // Got last known location. In some rare situations this can be null.
                 if (location != null) {
                     val geoCoder = Geocoder(this, Locale.getDefault())
                     val address = geoCoder.getFromLocation(location.latitude, location.longitude, 1)
                     mixPanelInstance.track(
                         IntentFlags.MIXPANEL_LOCATION,
                         JSONObject().put(IntentFlags.MIXPANEL_AREA_NAME, address[0]?.getAddressLine(0)).put(
                             IntentFlags.MIXPANEL_ZIP_CODE,
                             address[0].postalCode
                         )
                     )

                 } else {
                     longToast(getString(R.string.message_enable_location))
 //                    mixPanelInstance.track("Location Service", JSONObject().put("status", false))
                 }
             }
     }

     override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
         super.onRequestPermissionsResult(requestCode, permissions, grantResults)

         if (requestCode == 29) {
             if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                 //App has location permission, now create location request to check location settings

                 fetchLocation()
             } else {
                 longToast(getString(R.string.message_grant_permission))
             }
         }
     }
 */
}
