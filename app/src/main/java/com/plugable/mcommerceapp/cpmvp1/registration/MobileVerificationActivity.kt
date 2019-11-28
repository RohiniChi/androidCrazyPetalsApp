package com.plugable.mcommerceapp.cpmvp1.registration

import ServiceGenerator
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.SendOTPResponse
import com.plugable.mcommerceapp.cpmvp1.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.cpmvp1.utils.application.App
import com.plugable.mcommerceapp.cpmvp1.utils.constants.IntentFlags
import com.plugable.mcommerceapp.cpmvp1.utils.extension.hideKeyboard
import com.plugable.mcommerceapp.cpmvp1.utils.extension.invisible
import com.plugable.mcommerceapp.cpmvp1.utils.extension.setStatusBarColor
import com.plugable.mcommerceapp.cpmvp1.utils.extension.show
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.cpmvp1.utils.util.isNetworkAccessible
import com.plugable.mcommerceapp.cpmvp1.utils.validation.isEmpty
import com.plugable.mcommerceapp.cpmvp1.utils.validation.isValidMobileNumber
import com.plugable.mcommerceapp.cpmvp1.utils.validation.onTextChanged
import kotlinx.android.synthetic.main.activity_mobile_verification.*
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MobileVerificationActivity : AppCompatActivity(), View.OnClickListener {

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
        progressBar.indeterminateDrawable.setColorFilter(
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
        progressBar.hide()
        super.onResume()
    }

    private fun attemptApiCall() {
        if (isNetworkAccessible()) {
            progressBar.show()
            mobileVerificationApi()
        } else {
            toast(getString(R.string.oops_no_internet_connection))
        }
    }

    private fun mobileVerificationApi() {
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!

        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        val call =
            clientInstance.sendOTPApi(textInputEditTextPhoneNo.text.toString(),getString(R.string.send_otp_reset_password_subject))

        call.enqueue(object : Callback<SendOTPResponse> {
            override fun onFailure(call: Call<SendOTPResponse>, t: Throwable) {
                toast(getString(R.string.message_something_went_wrong))
            }

            override fun onResponse(
                call: Call<SendOTPResponse>,
                response: Response<SendOTPResponse>
            ) {
                if (response.isSuccessful) {
                    if (response.body()!!.statusCode == "10") {

                        val intent = Intent(this@MobileVerificationActivity, OTPActivity::class.java)
                        intent.putExtra(IntentFlags.REDIRECT_FROM, IntentFlags.FROM_FORGOT_PASSWORD)
                        intent.putExtra(
                            IntentFlags.VERIFICATION_PHONE_NUMBER,
                            textInputEditTextPhoneNo.text.toString()
                        )
                        startActivity(intent)

                        textInputEditTextPhoneNo.text?.clear()
                        textInputLayoutPhoneNo.error = null
                    } else {
                        buttonGetOtp.isClickable = true
                        progressBar.hide()
                        textViewPhoneNoError.show()
                        textViewPhoneNoError.text =
                            getString(R.string.mobileVerification_validation_meassage)
                    }
                }
            }

        })
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
                return false
            }
            !textInputEditTextPhoneNo.isValidMobileNumber() -> {
                textViewPhoneNoError.show()
                textViewPhoneNoError.text = getString(R.string.validation_number)
                return false
            }
        }
        textViewPhoneNoError.invisible()
        return true
    }
}
