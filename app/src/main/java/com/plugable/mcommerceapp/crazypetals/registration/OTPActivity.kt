package com.plugable.mcommerceapp.crazypetals.registration

import ServiceGenerator
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.auth.api.phone.SmsRetrieverClient
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.mukesh.OnOtpCompletionListener
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.OTPVerification
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.SendOTPResponse
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.VerifyOTPRequest
import com.plugable.mcommerceapp.crazypetals.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.crazypetals.ui.activities.DashboardActivity
import com.plugable.mcommerceapp.crazypetals.utils.application.App
import com.plugable.mcommerceapp.crazypetals.utils.constants.IntentFlags
import com.plugable.mcommerceapp.crazypetals.utils.extension.*
import com.plugable.mcommerceapp.crazypetals.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.crazypetals.utils.util.isNetworkAccessible
import com.plugable.mcommerceapp.crazypetals.utils.validation.clear
import com.plugable.mcommerceapp.crazypetals.utils.validation.onTextChanged
import kotlinx.android.synthetic.main.activity_otp.*
import org.jetbrains.anko.sdk27.coroutines.onFocusChange
import org.jetbrains.anko.toast
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OTPActivity : AppCompatActivity(), View.OnClickListener, OnOtpCompletionListener,
    SMSReceiver.OTPReceiveListener {


    private var sendOTPError: String = ""
    private var verifyOTPError: String = ""
    private lateinit var sendOTPApi: Call<SendOTPResponse>
    private lateinit var verifyOTPApi: Call<OTPVerification>
    private lateinit var timer: CountDownTimer
    private val TAG: String? = OTPActivity::class.java.simpleName
    private var smsReceiver: SMSReceiver? = null
    private lateinit var mixPanel: MixpanelAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        initializeViews()
        initializeTheme()
        textChangeListener()

        progressBarOTPActivity.hide()
        buttonVerify.isClickable = true

        val appSignatureHashHelper = AppSignatureHashHelper(this)

        // This code requires one time to get Hash keys do comment and share key
        Log.i(TAG, "HashKey: " + appSignatureHashHelper.appSignatures[0])
        startSMSListener()

        timer = object : CountDownTimer(15000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                textViewResendOTP.hide()
                textViewResendCode.show()
                textViewResendCode.text = "Auto-detecting code: " + millisUntilFinished / 1000

            }

            override fun onFinish() {
//                tvTimer.setText("done!")
                textViewResendCode.hide()
                textViewResendOTP.show()
                textViewResendOTP.text = getString(R.string.resend_otp)
                progressBarOTPActivity.hide()
            }
        }
        timer.start()
    }

    private fun textChangeListener() {
        otp_view.onFocusChange { view, isFocused ->
            if (!isFocused) {
                hideKeyboard(view)
                textViewOTPVerificationError.hide()
            }
        }
        otp_view.onTextChanged {
            textViewOTPVerificationError.hide()
        }
    }


    private fun initializeViews() {
        textViewResendOTP.setOnClickListener(this)
        imageButtonBackArrow.setOnClickListener(this)
        buttonVerify.setOnClickListener(this)
        buttonVerify.isClickable = true

        otp_view.setOtpCompletionListener(this)
        mixPanel = MixpanelAPI.getInstance(this, resources.getString(R.string.mix_panel_token))
        sendMixPanelEvent("visitedScreen")
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
        setStatusBarColor()
        textViewResendCode.setTextColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        textViewResendOTP.setTextColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        otp_view.cursorColor = Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR)
        otp_view.setLineColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))

        buttonVerify.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))

        progressBarOTPActivity.hide()
        progressBarOTPActivity.indeterminateDrawable.setColorFilter(
            Color.BLACK,
            PorterDuff.Mode.MULTIPLY
        )
    }

    private fun startSMSListener() {
        try {
            smsReceiver = SMSReceiver()
            smsReceiver!!.setOTPListener(this)

            val intentFilter = IntentFilter()
            intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION)

            this.registerReceiver(smsReceiver, intentFilter)

            val client: SmsRetrieverClient =
                SmsRetriever.getClient(this)

            val task: Task<Void> = client.startSmsRetriever()

            task.addOnSuccessListener {
                // API successfully started
            }
            task.addOnFailureListener { }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onOTPReceived(sms: String) {
//        toast("OTP Received: $sms")
//                    toast("$sms.substring(30,33)")

        timer.cancel()
        textViewOTPVerificationError.hide()
        buttonVerify.isClickable = true

        val otp = sms.substring(30, 34)
        otp_view.setText(otp)
        if (smsReceiver != null) {
            unregisterReceiver(smsReceiver!!)
            smsReceiver = null

        }
    }

    override fun onOTPTimeOut() {
//        toast("OTP Time out")
        timer.cancel()

        textViewResendCode.hide()
        textViewResendOTP.show()
        textViewResendOTP.text = getString(R.string.resend_otp)
        buttonVerify.isClickable = true
        progressBarOTPActivity.hide()
    }

    override fun onOTPReceivedError(error: String) {
//        toast("Error")
        buttonVerify.isClickable = true
        progressBarOTPActivity.hide()
    }

    override fun onOtpCompleted(otp: String?) {

    }

    private fun attemptApiCall() {
        if (isNetworkAccessible()) {
            buttonVerify.isClickable = false
            progressBarOTPActivity.show()
            otpVerificationApi()
        } else {
            timer.cancel()
            buttonVerify.isClickable = true
            progressBarOTPActivity.hide()
            toast(getString(R.string.oops_no_internet_connection))

            textViewResendCode.hide()
            textViewResendOTP.show()
            textViewResendOTP.text = getString(R.string.resend_otp)
        }
    }

    private fun otpVerificationApi() {
        val phoneNumberToVerify = intent.getStringExtra(IntentFlags.VERIFICATION_PHONE_NUMBER)
        val phoneNumber = VerifyOTPRequest(
            phoneNumber = phoneNumberToVerify!!,
            otp = otp_view.text.toString()
        )

        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!

        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)

        verifyOTPApi = clientInstance.otpVerificationApi(phoneNumber)
        verifyOTPApi.enqueue(object : Callback<OTPVerification> {
            override fun onFailure(call: Call<OTPVerification>, t: Throwable) {
                progressBarOTPActivity.hide()
                buttonVerify.isClickable = true

                toast(getString(R.string.message_something_went_wrong))
            }

            override fun onResponse(
                call: Call<OTPVerification>,
                response: Response<OTPVerification>
            ) {
                if (response.isSuccessful) {
                    if (response.body()?.statusCode.equals("10")) {
                        buttonVerify.isClickable = true

                        progressBarOTPActivity.hide()
                        if (intent.hasExtra(IntentFlags.REDIRECT_FROM)) {
                            when (intent.getStringExtra(IntentFlags.REDIRECT_FROM)) {
                                IntentFlags.FROM_REGISTER -> {

                                    textViewOTPVerificationError.invisible()
                                    val intent =
                                        Intent(this@OTPActivity, DashboardActivity::class.java)
                                    startActivity(intent)
                                    ActivityCompat.finishAffinity(this@OTPActivity)
                                    SharedPreferences.getInstance(this@OTPActivity)
                                        .isUserLoggedIn =
                                        true
                                }
                                IntentFlags.FROM_FORGOT_PASSWORD -> {
                                    textViewOTPVerificationError.invisible()
                                    val intent = intent
                                    val numberToVerify =
                                        intent.getStringExtra(IntentFlags.VERIFICATION_PHONE_NUMBER)
                                    val intentToActivity =
                                        Intent(
                                            this@OTPActivity,
                                            NewPasswordActivity::class.java
                                        )
                                    intentToActivity.putExtra(
                                        IntentFlags.VERIFY_PHONE_NUMBER,
                                        numberToVerify
                                    )
                                    startActivity(intentToActivity)
                                }
                            }
                        }
                    } else {
                        buttonVerify.isClickable = true

                        progressBarOTPActivity.hide()
                        textViewOTPVerificationError.show()
                        textViewOTPVerificationError.text =
                            getString(R.string.otp_validation_message)

                        if (isNetworkAccessible()) {
                            textViewOTPVerificationError.text =
                                getString(R.string.otp_validation_message)
                            verifyOTPError = getString(R.string.otp_validation_message)
                        } else {
                            toast(getString(R.string.check_internet_connection))
                            verifyOTPError = getString(R.string.check_internet_connection)
                        }
                        sendMixPanelEvent("verifyOTPError")

                    }
                }
            }


        })

    }

    override fun onResume() {
        progressBarOTPActivity.hide()
        buttonVerify.isClickable = true

        super.onResume()
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.imageButtonBackArrow -> {
                onBackPressed()
            }
            R.id.textViewResendOTP -> {

                hideKeyboard(v)
                textViewOTPVerificationError.invisible()
                otp_view.clear()
                if (isNetworkAccessible()) {
                    progressBarOTPActivity.show()
                    mobileVerificationApi()
                } else {
                    buttonVerify.isClickable = true
                    progressBarOTPActivity.hide()
                    toast(getString(R.string.oops_no_internet_connection))
                }
            }
            R.id.buttonVerify -> {
                val view = View(this)
                hideKeyboard(view)
                attemptApiCall()
            }
        }
    }

    private fun mobileVerificationApi() {
        val phoneNumberToVerify =
            intent.getStringExtra(IntentFlags.VERIFICATION_PHONE_NUMBER)
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!

        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)

        if (intent.hasExtra(IntentFlags.REDIRECT_FROM)) {
            when (intent.getStringExtra(IntentFlags.REDIRECT_FROM)) {
                IntentFlags.FROM_REGISTER -> {
                    sendOTPApi =
                        clientInstance.sendOTPApi(
                            phoneNumberToVerify!!,
                            getString(R.string.send_otp_register_subject)
                        )
                    sendOTPApi.enqueue(object : Callback<SendOTPResponse> {
                        override fun onFailure(call: Call<SendOTPResponse>, t: Throwable) {
                            buttonVerify.isClickable = true

                            progressBarOTPActivity.hide()
                            toast(getString(R.string.message_something_went_wrong))
                        }

                        override fun onResponse(
                            call: Call<SendOTPResponse>,
                            response: Response<SendOTPResponse>
                        ) {
                            if (response.isSuccessful) {
                                if (response.body()!!.statusCode == "10") {
                                    buttonVerify.isClickable = true

                                    progressBarOTPActivity.hide()
                                    toast(getString(R.string.message_otp_code_sent))
                                    startSMSListener()

                                    timer = object : CountDownTimer(15000, 1000) {
                                        override fun onTick(millisUntilFinished: Long) {
                                            textViewResendOTP.hide()
                                            textViewResendCode.show()
                                            textViewResendCode.text =
                                                "Auto-detecting code: " + millisUntilFinished / 1000
                                        }

                                        override fun onFinish() {
//                tvTimer.setText("done!")
                                            textViewResendCode.hide()
                                            textViewResendOTP.show()
                                            textViewResendOTP.text = getString(R.string.resend_otp)

                                        }
                                    }
                                    timer.start()
                                } else {
                                    progressBarOTPActivity.hide()
                                    buttonVerify.isClickable = true

                                    toast(getString(R.string.message_something_went_wrong))
                                }
                            } else {
                                progressBarOTPActivity.hide()
                                buttonVerify.isClickable = true

                                toast(getString(R.string.message_something_went_wrong))
                            }
                        }

                    })
                }
                IntentFlags.FROM_FORGOT_PASSWORD -> {
                    val call =
                        clientInstance.sendOTPApi(
                            phoneNumberToVerify!!,
                            getString(R.string.send_otp_reset_password_subject)
                        )
                    call.enqueue(object : Callback<SendOTPResponse> {
                        override fun onFailure(call: Call<SendOTPResponse>, t: Throwable) {
                            progressBarOTPActivity.hide()
                            buttonVerify.isClickable = true

                            toast(getString(R.string.message_something_went_wrong))
                        }

                        override fun onResponse(
                            call: Call<SendOTPResponse>,
                            response: Response<SendOTPResponse>
                        ) {
                            if (response.isSuccessful) {
                                if (response.body()!!.statusCode == "10") {
                                    progressBarOTPActivity.hide()
                                    buttonVerify.isClickable = true

                                    toast(getString(R.string.message_otp_code_sent))
                                    startSMSListener()

                                    timer = object : CountDownTimer(15000, 1000) {
                                        override fun onTick(millisUntilFinished: Long) {
                                            textViewResendOTP.hide()
                                            textViewResendCode.show()
                                            textViewResendCode.text =
                                                "Auto-detecting code: " + millisUntilFinished / 1000
                                        }

                                        override fun onFinish() {
//                tvTimer.setText("done!")
                                            textViewResendCode.hide()
                                            textViewResendOTP.show()
                                            textViewResendOTP.text = getString(R.string.resend_otp)

                                        }
                                    }
                                    timer.start()
                                } else {
                                    buttonVerify.isClickable = true
                                    progressBarOTPActivity.hide()
                                    textViewResendCode.hide()
                                    textViewResendOTP.show()
                                    textViewResendOTP.text = getString(R.string.resend_otp)

                                    sendOTPError = if (isNetworkAccessible()) {
                                        toast(response.body()!!.message)
                                        response.body()!!.message
                                    } else {
                                        toast(getString(R.string.check_internet_connection))
                                        getString(R.string.check_internet_connection)
                                    }
                                    sendMixPanelEvent("sendOTPError")


                                }
                            } else {
                                progressBarOTPActivity.hide()
                                buttonVerify.isClickable = true
                                textViewResendCode.hide()
                                textViewResendOTP.show()
                                textViewResendOTP.text = getString(R.string.resend_otp)

                                toast(getString(R.string.message_something_went_wrong))
                            }
                        }

                    })
                }
            }

        }

    }


    private fun sendMixPanelEvent(mixPanelTitle: String) {
        val productObject = JSONObject()
        when {
            mixPanelTitle.equals("visitedScreen", true) -> {
                mixPanel.track(IntentFlags.MIXPANEL_VISITED_OTP_SCREEN, productObject)
            }
            mixPanelTitle.equals("sendOTPError", true) -> {
                productObject.put(IntentFlags.MIXPANEL_OTP_SCREEN_SEND_OTP_ERROR, sendOTPError)
                mixPanel.track(IntentFlags.MIXPANEL_OTP_SCREEN_SEND_OTP_ERROR, productObject)
            }
            mixPanelTitle.equals("verifyOTPError", true) -> {
                productObject.put(IntentFlags.MIXPANEL_VERIFY_OTP_ERROR, verifyOTPError)
                mixPanel.track(IntentFlags.MIXPANEL_VERIFY_OTP_ERROR, productObject)
            }
        }
    }

    private fun cancelTasks() {
        if (::verifyOTPApi.isInitialized && verifyOTPApi != null) verifyOTPApi.cancel()
        if (::sendOTPApi.isInitialized && sendOTPApi != null) sendOTPApi.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelTasks()
        if (smsReceiver != null) {
            unregisterReceiver(smsReceiver!!)
//            smsReceiver=null
        }
    }


}

