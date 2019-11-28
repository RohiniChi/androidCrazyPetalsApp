package com.plugable.mcommerceapp.cpmvp1.ui.fragments


import ServiceGenerator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.cpmvp1.mcommerce.db.AppDatabase
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.DeliveryLocation
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.DeliveryScheduleAddress
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.ProductBooking
import com.plugable.mcommerceapp.cpmvp1.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.cpmvp1.ui.activities.SuccessProductBookingActivity
import com.plugable.mcommerceapp.cpmvp1.utils.application.App
import com.plugable.mcommerceapp.cpmvp1.utils.constants.IntentFlags
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi
import com.plugable.mcommerceapp.cpmvp1.utils.extension.hide
import com.plugable.mcommerceapp.cpmvp1.utils.extension.hideKeyboard
import com.plugable.mcommerceapp.cpmvp1.utils.extension.invisible
import com.plugable.mcommerceapp.cpmvp1.utils.extension.show
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.cpmvp1.utils.util.isNetworkAccessible
import com.plugable.mcommerceapp.cpmvp1.utils.validation.*
import com.plugable.mcommerceapp.cpmvp1.utils.validation.EditTextValidations.MAX_NAME_LENGTH
import kotlinx.android.synthetic.main.fragment_product_booking.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.noButton
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.textColor
import org.jetbrains.anko.yesButton
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * A simple [Fragment] subclass.
 * Use the [RequestProductBookingFragment.new Instance] factory method to
 * create an instance of this fragment.
 * * [RequestProductBookingFragment] is used to request booking for products

 */
class RequestProductBookingFragment : BaseFragment(), View.OnTouchListener {

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
        context?.hideKeyboard(view!!)
        if (motionEvent != null) {
            when (motionEvent.action) {
                MotionEvent.ACTION_UP -> {
                    if (activity!!.isNetworkAccessible()) {
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
    private lateinit var mixPanel: MixpanelAPI

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_booking, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializeTheme()
        initializeViews()

        textChangeListeners()
        checkboxCheckListeners()

        val tempList: List<DeliveryLocation> = emptyList()
        addressAdapter(tempList)

        spinnerAreaPinCode.setOnTouchListener(this)

    }

    private fun checkboxCheckListeners() {

        val wishList =
            AppDatabase.getDatabase(context!!).productListDao().getAllWishListedProducts()

        checkBoxWishlist.setOnClickListener {
            if (checkBoxWishlist.isChecked) {

                if (textInputEditTextOrderDetails.text!!.isEmpty()) {
                    if (wishList.isEmpty()) {
                        checkBoxWishlist.isChecked = false

                        toast(getString(R.string.empty_wishlist_message))

                    } else {
                        wishlistValidation()
                    }

                } else {

                    if (wishList.isEmpty()) {

                        checkBoxWishlist.isChecked = false

                        toast(getString(R.string.empty_wishlist_message))
                    } else {
                        if (textInputEditTextOrderDetails.text!!.isEmpty()) {
                            wishlistValidation()
                        } else {
                            alert(getString(R.string.alert_order_detail_clear_message)) {
                                this.title = getString(R.string.alert_title)
                                yesButton { wishlistValidation() }
                                noButton { checkBoxWishlist.isChecked = false }
                            }.show().apply {

                                getButton(AlertDialog.BUTTON_POSITIVE)?.let {

                                    it.allCaps = false
                                    it.background = ContextCompat.getDrawable(
                                        context,
                                        android.R.color.transparent
                                    )
                                    it.textColor = Color.BLUE
                                }
                                getButton(AlertDialog.BUTTON_NEGATIVE)?.let {

                                    it.allCaps = false
                                    it.typeface = Typeface.DEFAULT_BOLD
                                    it.background = ContextCompat.getDrawable(
                                        context,
                                        android.R.color.transparent
                                    )
                                    it.textColor = Color.BLUE
                                }

                            }
                        }

                    }

                }
            } else {
                alert(getString(R.string.alert_order_detail_clear_message)) {
                    this.title = getString(R.string.alert_title)
                    yesButton { textInputEditTextOrderDetails.text?.clear() }
                    noButton { checkBoxWishlist.isChecked = true }
                }.show().apply {

                    getButton(AlertDialog.BUTTON_POSITIVE)?.let {

                        it.allCaps = false
                        it.background =
                            ContextCompat.getDrawable(context, android.R.color.transparent)
                        it.textColor = Color.BLUE
                    }
                    getButton(AlertDialog.BUTTON_NEGATIVE)?.let {

                        it.allCaps = false
                        it.typeface = Typeface.DEFAULT_BOLD
                        it.background =
                            ContextCompat.getDrawable(context, android.R.color.transparent)
                        it.textColor = Color.BLUE
                    }

                }
            }
        }
    }

    private fun wishlistValidation() {
        val wishListProductNames = StringBuilder()
        val wishList =
            AppDatabase.getDatabase(activity!!).productListDao().getAllWishListedProducts()


        wishList.forEachIndexed { index, productDetails ->
            val productName =
                index.plus(1).toString().plus(". ").plus(productDetails.name).plus(" - ")
//                    .plus(productDetails.quantity).plus(productDetails.unit).plus("\n")
            wishListProductNames.append(productName)
        }
        textInputEditTextOrderDetails.text =
            Editable.Factory.getInstance().newEditable(wishListProductNames)
        textInputEditTextOrderDetails.placeCursorToEnd()
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
        editTextName.backgroundTintList = ColorStateList.valueOf(Color.BLACK)
        editTextPhone.backgroundTintList = ColorStateList.valueOf(Color.BLACK)
        editTextEmail.backgroundTintList = ColorStateList.valueOf(Color.BLACK)
        textInputLayoutOrderDetails.boxStrokeColor = Color.BLACK
        submitButton.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        checkBoxWishlist.buttonTintList = ColorStateList.valueOf(Color.BLACK)
        requestBookingProgressBar.indeterminateDrawable.setColorFilter(
            Color.BLACK,
            PorterDuff.Mode.MULTIPLY
        )
    }


    private fun textChangeListeners() {

        editTextName.setOnFocusChangeListener { view, isFocused ->

            if (!isFocused) {
                nameValidation()
                context?.hideKeyboard(view)

            }

        }


        editTextPhone.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused) {
                contactValidation()
                context?.hideKeyboard(view)
            }
        }

        editTextEmail.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused) {
                emailValidation()
                context?.hideKeyboard(view)
            }
        }



        textInputEditTextOrderDetails.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused) {
                orderDetailsValidation()
                context?.hideKeyboard(view)
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

        textInputEditTextOrderDetails.onTextChanged {
            if (it.isEmpty()) {
                checkBoxWishlist.isEnabled = true
                checkBoxWishlist.isChecked = false
            }
            textInputLayoutOrderDetails.error = null

        }
//        textInputEditTextOrderDetails.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(orderDetailText: Editable?) {
//                textInputLayoutOrderDetails.error = null
//            }
//
//            override fun beforeTextChanged(orderDetailText: CharSequence?, start: Int, count: Int, after: Int) {
//            }
//
//            override fun onTextChanged(orderDetailText: CharSequence?, start: Int, before: Int, count: Int) {
//                if (orderDetailText?.length == 0) {
//                    checkBoxWishlist.isEnabled = true
//                    checkBoxWishlist.isChecked = false
//                }
//            }
//
//        })


    }

    private fun initializeViews() {
        mixPanel = MixpanelAPI.getInstance(context, resources.getString(R.string.mix_panel_token))
        submitButton.setOnClickListener(this)
        sendMixPanelEvent()
    }


    private fun deliverAddressApi() {

        val apiInterface = ServiceGenerator.createService(ProjectApi::class.java)
        val call = apiInterface.getDeliveryAddress()
        startShimmerView()
        call.enqueue(object : Callback<DeliveryScheduleAddress> {
            override fun onFailure(call: Call<DeliveryScheduleAddress>, throwable: Throwable) {
                toast(getString(R.string.message_something_went_wrong))
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
        val arrayAdapter =
            ArrayAdapter<String>(activity!!, android.R.layout.simple_spinner_dropdown_item)
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

                when {
                    index == 0 -> {
                        view as TextView?
                        view?.setTextColor(Color.GRAY)
                    }
                    index - 1 >= 0 -> {
                        textViewSpinnerError.invisible()
                        deliveryLocationToPass = deliveryLocationList[index - 1]
                        selectedPincode = deliveryLocationList[index - 1].pinCode
                    }
                    else -> {
                        textViewSpinnerError.invisible()
                        selectedPincode = getString(R.string.spinner_select)
                    }
                }
            }

        }
    }


    private fun attemptApiCall() {
        if (activity?.isNetworkAccessible()!!) {
            bookingApi()
        } else
            toast(R.string.check_internet_connection)

    }

    private fun bookingApi() {
        val bookingObject = ProductBooking.Data(
            name = editTextName.text.toString(), mobileNumber = editTextPhone.text.toString(),
            email = editTextEmail.text.toString(), area = deliveryLocationToPass.area,
            pinCode = deliveryLocationToPass.pinCode,
            appId = WebApi.APP_ID,
            orderNumber = "",
            description = textInputEditTextOrderDetails.text.toString()
        )

        App.HostUrl = SharedPreferences.getInstance(activity!!).hostUrl!!
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

                    startSuccessFragment(response.body()!!.data.orderNumber)
                    context?.hideKeyboard(view!!)
                } else {
                    toast(response.body()?.message.toString())
                }
            }

        })
    }


    private fun sendMixPanelEvent() {
        val productObject = JSONObject()
        productObject.put(
            IntentFlags.MIXPANEL_PRODUCT_ID,
            getString(R.string.mixpanel_request_booking_from_hamburger_menu)
        )
        mixPanel.track(
            IntentFlags.MIXPANEL_VISITED_REQUEST_BOOKING_SCREEN_FROM_HAMBURGER_MENU,
            productObject
        )
    }

    private fun startSuccessFragment(orderNumber: String) {
        val intent = Intent(activity, SuccessProductBookingActivity::class.java)
        intent.putExtra(
            getString(R.string.delivery_location_to_pass_day),
            deliveryLocationToPass.day
        )
        intent.putExtra(
            getString(R.string.delivery_location_to_pass_time),
            deliveryLocationToPass.time
        )
        intent.putExtra(getString(R.string.order_number_to_pass), orderNumber)
        startActivity(intent)
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


    override fun setToolBar(name: String) {
        toolbar.hide()

        txtToolbarTitle.text = ""
        imgToolbarHome.setImageResource(android.R.color.transparent)

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

        productObject.put(
            IntentFlags.MIXPANEL_FROM,
            IntentFlags.MIXPANEL_HAMBURGER_MENU
        )

        mixPanel.track(IntentFlags.MIXPANEL_SUCCESSFULLY_PLACED_ORDER, productObject)
    }


    override fun onDestroy() {
        super.onDestroy()
        mixPanel.flush()

    }
}

