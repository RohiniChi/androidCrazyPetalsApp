package com.plugable.mcommerceapp.crazypetals.registration

import ServiceGenerator
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.SendOTPResponse
import com.plugable.mcommerceapp.crazypetals.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.crazypetals.utils.application.App
import com.plugable.mcommerceapp.crazypetals.utils.constants.IntentFlags
import com.plugable.mcommerceapp.crazypetals.utils.extension.hideKeyboard
import com.plugable.mcommerceapp.crazypetals.utils.extension.invisible
import com.plugable.mcommerceapp.crazypetals.utils.extension.setStatusBarColor
import com.plugable.mcommerceapp.crazypetals.utils.extension.show
import com.plugable.mcommerceapp.crazypetals.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.crazypetals.utils.util.isNetworkAccessible
import com.plugable.mcommerceapp.crazypetals.utils.validation.isEmpty
import com.plugable.mcommerceapp.crazypetals.utils.validation.isValidMobileNumber
import com.plugable.mcommerceapp.crazypetals.utils.validation.onTextChanged
import kotlinx.android.synthetic.main.activity_mobile_verification.*
import org.jetbrains.anko.toast
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MobileVerificationActivity : AppCompatActivity(), View.OnClickListener {

    private var validationError: String = ""
    private var sendOTPError: String = ""
    private lateinit var sendOTPAPi: Call<SendOTPResponse>
    private lateinit var mixPanel: MixpanelAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mobile_verification)

        initializeViews()
        initializeTheme()
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

        setThemeToComponents()
    }

    private fun setThemeToComponents() {
        progressBarMobileVerification.indeterminateDrawable.setColorFilter(
            Color.BLACK,
            PorterDuff.Mode.MULTIPLY
        )
        setStatusBarColor()
        buttonGetOtp.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
    }

    private fun initializeViews() {
        imageButtonBackArrow.setOnClickListener(this)
        buttonGetOtp.setOnClickListener(this)
        textChangeListeners()
        buttonGetOtp.isClickable = true
        mixPanel = MixpanelAPI.getInstance(this, resources.getString(R.string.mix_panel_token))
        sendMixPanelEvent("visitedScreen")
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.imageButtonBackArrow -> {
                onBackPressed()

            }
            R.id.buttonGetOtp -> {
                this.hideKeyboard(v)
                if (mobileValidation()) {
                    buttonGetOtp.isClickable = false
                    attemptApiCall()

                }
            }
        }
    }

    override fun onResume() {
        buttonGetOtp.isClickable = true
        progressBarMobileVerification.hide()
        super.onResume()
    }

    private fun attemptApiCall() {
        if (isNetworkAccessible()) {
            progressBarMobileVerification.show()
            mobileVerificationApi()
        } else {
            progressBarMobileVerification.hide()
            buttonGetOtp.isClickable = true
            toast(getString(R.string.oops_no_internet_connection))
        }
    }

    private fun mobileVerificationApi() {
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!

        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        sendOTPAPi =
            clientInstance.sendOTPApi(
                textInputEditTextPhoneNo.text.toString(),
                getString(R.string.send_otp_reset_password_subject)
            )

        sendOTPAPi.enqueue(object : Callback<SendOTPResponse> {
            override fun onFailure(call: Call<SendOTPResponse>, t: Throwable) {
                toast(getString(R.string.message_something_went_wrong))
                buttonGetOtp.isClickable = true
            }

            override fun onResponse(
                call: Call<SendOTPResponse>,
                response: Response<SendOTPResponse>
            ) {
                if (response.isSuccessful) {
                    if (response.body()!!.statusCode == "10") {

                        val intent =
                            Intent(this@MobileVerificationActivity, OTPActivity::class.java)
                        intent.putExtra(IntentFlags.REDIRECT_FROM, IntentFlags.FROM_FORGOT_PASSWORD)
                        intent.putExtra(
                            IntentFlags.VERIFICATION_PHONE_NUMBER,
                            textInputEditTextPhoneNo.text.toString()
                        )
                        startActivity(intent)

                        buttonGetOtp.isClickable = true
                        textInputEditTextPhoneNo.text?.clear()
                        textInputLayoutPhoneNo.error = null
                    } else {
                        buttonGetOtp.isClickable = true
                        progressBarMobileVerification.hide()
                        textViewPhoneNoError.show()
                        textViewPhoneNoError.text =
                            getString(R.string.mobileVerification_validation_meassage)
                        if (isNetworkAccessible()) {
                            textViewPhoneNoError.text =
                                getString(R.string.mobileVerification_validation_meassage)
                            sendOTPError =
                                getString(R.string.mobileVerification_validation_meassage)
                        } else {
                            toast(getString(R.string.check_internet_connection))
                            sendOTPError = getString(R.string.check_internet_connection)
                        }
                        sendMixPanelEvent("sendOTPError")
                    }
                }
                buttonGetOtp.isClickable = true
                progressBarMobileVerification.hide()
            }

        })
    }


    private fun sendMixPanelEvent(mixPanelTitle: String) {
        val productObject = JSONObject()
        when {
            mixPanelTitle.equals("visitedScreen", true) -> {
                mixPanel.track(IntentFlags.MIXPANEL_VISITED_MOBILE_VERIFICATION, productObject)
            }
            mixPanelTitle.equals("sendOTPError", true) -> {
                productObject.put(IntentFlags.MIXPANEL_MOBILE_VERIFICATION_SEND_OTP_ERROR, sendOTPError)
                mixPanel.track(IntentFlags.MIXPANEL_MOBILE_VERIFICATION_SEND_OTP_ERROR, productObject)
            }
            mixPanelTitle.equals("validationError", true) -> {
                productObject.put(IntentFlags.MIXPANEL_SEND_OTP_VALIDATION_ERROR, validationError)
                mixPanel.track(IntentFlags.MIXPANEL_SEND_OTP_VALIDATION_ERROR, productObject)
            }
        }
    }


    private fun textChangeListeners() {

        textInputEditTextPhoneNo.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused) {
                this.hideKeyboard(view)
                mobileValidation()
            }
        }

        textInputEditTextPhoneNo.onTextChanged {
            textViewPhoneNoError.invisible()
        }

    }

    private fun mobileValidation(): Boolean {
        when {
            textInputEditTextPhoneNo.isEmpty() -> {
                textViewPhoneNoError.show()
                textViewPhoneNoError.text = getString(R.string.validation_enter_number)
                validationError = getString(R.string.validation_enter_number)
                sendMixPanelEvent("validationError")
                return false
            }
            !textInputEditTextPhoneNo.isValidMobileNumber() -> {
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

    override fun onStop() {
        super.onStop()
        cancelTasks()
    }

    private fun cancelTasks() {
        if (::sendOTPAPi.isInitialized && sendOTPAPi != null) sendOTPAPi.cancel()
    }

    override fun onDestroy() {
        mixPanel.flush()
        super.onDestroy()
    }

}
