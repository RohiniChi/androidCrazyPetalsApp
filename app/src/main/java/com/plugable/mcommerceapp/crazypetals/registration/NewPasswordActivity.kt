package com.plugable.mcommerceapp.crazypetals.registration

import ServiceGenerator
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.NewPassword
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.ResetPassword
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
import kotlinx.android.synthetic.main.activity_new_password.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewPasswordActivity : AppCompatActivity(), View.OnClickListener {

    private var validationError: String = ""
    private var resetPasswordError: String = ""
    private lateinit var resetPasswordApi: Call<NewPassword>
    private lateinit var mixPanel: MixpanelAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_password)

        initializeViews()
        initializeTheme()
    }

    private fun initializeViews() {
        textChangeListeners()
        buttonSavePassword.setOnClickListener(this)
        imageButtonBackArrow.setOnClickListener(this)
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
        buttonSavePassword.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        progressBar.indeterminateDrawable.setColorFilter(
            Color.BLACK,
            PorterDuff.Mode.MULTIPLY
        )

    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.imageButtonBackArrow -> {
                onBackPressed()
            }

            R.id.buttonSavePassword -> {
                this.hideKeyboard(v)
                confirmPasswordValidation()
                enterPasswordValidation()

                if (enterPasswordValidation() && confirmPasswordValidation()) {
                    buttonSavePassword.isClickable = false
                    attemptApiCall()
                }
            }
        }
    }

    override fun onResume() {
        buttonSavePassword.isClickable = true
        progressBar.hide()
        super.onResume()
    }

    private fun attemptApiCall() {
        if (isNetworkAccessible()) {
            progressBar.show()
            newPasswordApi()
        } else {
            toast(getString(R.string.oops_no_internet_connection))
        }
    }

    private fun newPasswordApi() {
        val phoneNumber =
            intent.getStringExtra(IntentFlags.VERIFY_PHONE_NUMBER)
        val newPassword = ResetPassword(
            phoneNumber = phoneNumber!!,
            password = textInputEditTextConfirmPassword.text.toString()
        )
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!

        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)

        resetPasswordApi = clientInstance.newPasswordApi(newPassword)

        resetPasswordApi.enqueue(object : Callback<NewPassword> {
            override fun onFailure(call: Call<NewPassword>, t: Throwable) {
                toast(getString(R.string.message_something_went_wrong))

            }

            override fun onResponse(call: Call<NewPassword>, response: Response<NewPassword>) {
                if (response.isSuccessful) {
                    if (response.body()!!.statusCode == "10") {
                        toast(response.body()!!.message)
                        startActivity<LoginActivity>()
                        textInputEditTextEnterPassword.text?.clear()
                        textInputEditTextConfirmPassword.text?.clear()

                        textInputLayoutEnterPassword.error = null
                        textInputLayoutConfirmPassword.error = null
                    } else {
                        buttonSavePassword.isClickable = true
                        progressBar.hide()
                        resetPasswordError = if (isNetworkAccessible()) {
                            toast(response.body()!!.message)
                            response.body()!!.message
                        } else {
                            toast(getString(R.string.check_internet_connection))
                            getString(R.string.check_internet_connection)
                        }
                        sendMixPanelEvent("resetPasswordError")
                    }
                }
            }


        })
    }


    override fun onBackPressed() {
        startActivity<LoginActivity>()
        ActivityCompat.finishAffinity(this)
    }

    private fun sendMixPanelEvent(mixPanelTitle: String) {
        val productObject = JSONObject()

        when {
            mixPanelTitle.equals("visitedScreen", true) -> {
                mixPanel.track(IntentFlags.MIXPANEL_VISITED_RESET_PASSWORD_SCREEN, productObject)
            }
            mixPanelTitle.equals("resetPasswordError", true) -> {
                productObject.put(IntentFlags.MIXPANEL_RESET_PASSWORD_ERROR, resetPasswordError)
                mixPanel.track(IntentFlags.MIXPANEL_RESET_PASSWORD_ERROR, productObject)

            }
            mixPanelTitle.equals("validationError", true) -> {
                productObject.put(IntentFlags.MIXPANEL_RESET_PASSWORD_VALIDATION_ERROR, validationError)
                mixPanel.track(IntentFlags.MIXPANEL_RESET_PASSWORD_VALIDATION_ERROR, productObject)

            }
        }
    }

    private fun textChangeListeners() {

        textInputEditTextEnterPassword.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused) {
                this.hideKeyboard(view)
                enterPasswordValidation()
            }
        }

        textInputEditTextConfirmPassword.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused) {
                this.hideKeyboard(view)
                confirmPasswordValidation()
            }
        }

    }

    private fun enterPasswordValidation(): Boolean {

        when {
            textInputEditTextEnterPassword.isEmpty() -> {
                textViewEnterPasswordError.show()
                textViewEnterPasswordError.text = getString(R.string.validation_new_password)
                validationError = getString(R.string.validation_new_password)
                sendMixPanelEvent("validationError")
                return false
            }
            textInputEditTextEnterPassword.text.toString().startsWith(" ") -> {
                textViewEnterPasswordError.show()
                textViewEnterPasswordError.text = getString(R.string.validation_empty_space)
                validationError = getString(R.string.validation_empty_space)
                sendMixPanelEvent("validationError")
                return false
            }
            textInputEditTextEnterPassword.length() < 6 -> {
                textViewEnterPasswordError.show()
                textViewEnterPasswordError.text = getString(R.string.validation_password_length)
                validationError = getString(R.string.validation_password_length)
                sendMixPanelEvent("validationError")
                return false
            }
        }
        textViewEnterPasswordError.invisible()
        validationError = ""
        return true
    }

    private fun confirmPasswordValidation(): Boolean {
        when {

            textInputEditTextConfirmPassword.isEmpty() -> {
                textViewConfirmPasswordError.show()
                textViewConfirmPasswordError.text = getString(R.string.validation_confirm_password)
                validationError = getString(R.string.validation_confirm_password)
                sendMixPanelEvent("validationError")
                return false
            }
            textInputEditTextConfirmPassword.text.toString().startsWith(" ") -> {
                textViewConfirmPasswordError.show()
                textViewConfirmPasswordError.text = getString(R.string.validation_empty_space)
                validationError = getString(R.string.validation_empty_space)
                sendMixPanelEvent("validationError")
                return false
            }
            textInputEditTextConfirmPassword.text.toString() != textInputEditTextEnterPassword.text.toString() -> {
                textViewConfirmPasswordError.show()
                textViewConfirmPasswordError.text =
                    getString(R.string.validation_wrong_confirmed_password)
                validationError = getString(R.string.validation_wrong_confirmed_password)
                sendMixPanelEvent("validationError")
                return false
            }

        }
        textViewConfirmPasswordError.invisible()
        validationError = ""
        return true
    }

    override fun onStop() {
        super.onStop()
        cancelTasks()
    }

    private fun cancelTasks() {
        if (::resetPasswordApi.isInitialized && resetPasswordApi != null) resetPasswordApi.cancel()
    }

    override fun onDestroy() {
        mixPanel.flush()
        super.onDestroy()
    }

}
