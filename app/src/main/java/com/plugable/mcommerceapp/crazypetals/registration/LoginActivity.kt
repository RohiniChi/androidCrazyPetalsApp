package com.plugable.mcommerceapp.crazypetals.registration

import ServiceGenerator
import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.Login
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.LoginRequest
import com.plugable.mcommerceapp.crazypetals.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.crazypetals.ui.activities.DashboardActivity
import com.plugable.mcommerceapp.crazypetals.ui.activities.ProductDetailActivity
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
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initializeViews()
        initializeTheme()

    }

    override fun onBackPressed() {

    }

    private fun initializeViews() {
        textChangeListeners()
        textViewLoginSkip.setOnClickListener(this)
        buttonLogin.setOnClickListener(this)
        buttonLoginRegister.setOnClickListener(this)
        textViewForgotPassword.setOnClickListener(this)
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

    fun setThemeToComponents() {
        setStatusBarColor()
        buttonLogin.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        buttonLoginRegister.strokeColor =
            ColorStateList.valueOf(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        buttonLoginRegister.setTextColor(
            ColorStateList.valueOf(
                Color.parseColor(
                    ApplicationThemeUtils.SECONDARY_COLOR
                )
            )
        )
        progressBarLogin.indeterminateDrawable.setColorFilter(
            Color.BLACK,
            PorterDuff.Mode.MULTIPLY
        )
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.textViewLoginSkip -> {

                textInputEditTextMobileNumber.text?.clear()
                textInputEditTextPassword.text?.clear()
                textInputLayoutPassword.error = null
                textInputLayoutMobileNumber.error = null
                textInputEditTextPassword.clearFocus()


                if (intent.hasExtra(IntentFlags.SHOULD_GO_TO_DASHBOARD) && intent.getBooleanExtra(
                        IntentFlags.SHOULD_GO_TO_DASHBOARD,
                        true
                    )
                ) {
                    finish()
                } else {
                    startActivity<DashboardActivity>(
                        IntentFlags.FRAGMENT_TO_BE_LOADED to intent.getIntExtra(
                            IntentFlags.FRAGMENT_TO_BE_LOADED,
                            R.id.nav_home
                        )
                    )
                    finish()

                    SharedPreferences.getInstance(this).isUserSkippedLogin = true

                }
            }

            R.id.buttonLogin -> {
                this.hideKeyboard(v)
                passwordValidation()
                phoneValidation()
                if (phoneValidation() && passwordValidation()) {
                    attemptApiCall()
                }
            }

            R.id.buttonLoginRegister -> {

                textInputEditTextMobileNumber.text?.clear()
                textInputEditTextPassword.text?.clear()
                textInputLayoutPassword.error = null
                textInputLayoutMobileNumber.error = null

                startActivity<RegisterActivity>()
            }

            R.id.textViewForgotPassword -> {
                this.hideKeyboard(v)
                textInputEditTextMobileNumber.text?.clear()
                textInputEditTextPassword.text?.clear()
                textInputLayoutPassword.error = null
                textInputLayoutMobileNumber.error = null

                startActivity<MobileVerificationActivity>()

            }
        }
    }

    override fun onResume() {
        buttonLogin.isClickable = true
        progressBarLogin.hide()
        super.onResume()
    }

    private fun attemptApiCall() {
        if (isNetworkAccessible()) {
            progressBarLogin.show()
            loginApi()
        } else {
            toast(getString(R.string.oops_no_internet_connection))
        }
    }

    private fun loginApi() {

        val userLoginInfo = LoginRequest(
            textInputEditTextMobileNumber.text.toString(),
            textInputEditTextPassword.text.toString()
        )

        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!

        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)

        val call = clientInstance.loginApi(userLoginInfo)

        call.enqueue(object : Callback<Login> {
            override fun onFailure(call: Call<Login>, throwable: Throwable) {
                progressBarLogin.hide()
                toast(getString(R.string.message_something_went_wrong))
            }

            override fun onResponse(call: Call<Login>, response: Response<Login>) {
                if (response.isSuccessful) {
                    if (response.body()!!.statusCode == "10") {
                        progressBarLogin.hide()
                        buttonLogin.isClickable = false

                        if (intent.hasExtra(IntentFlags.REDIRECT_FROM)) {
                            when (intent.getStringExtra(IntentFlags.REDIRECT_FROM)) {
                                IntentFlags.PRODUCT_DETAIL -> {
                                    val applicationUserId = response.body()!!.applicationUserId
                                    SharedPreferences.getInstance(this@LoginActivity)
                                        .setStringValue(
                                            IntentFlags.APPLICATION_USER_ID,
                                            applicationUserId.toString()
                                        )
//                                  startActivity<ProductDetailActivity>()


                                    SharedPreferences.getInstance(this@LoginActivity)
                                        .isUserLoggedIn = true
                                    val intent = Intent(
                                        this@LoginActivity,
                                        ProductDetailActivity::class.java
                                    )
                                    setResult(Activity.RESULT_OK, intent)
                                    finish()
                                }

                            }
                        } else {
                            progressBarLogin.hide()
                            val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                            val applicationUserId = response.body()!!.applicationUserId
                            SharedPreferences.getInstance(this@LoginActivity).setStringValue(
                                IntentFlags.APPLICATION_USER_ID,
                                applicationUserId.toString()
                            )
                            intent.putExtra(IntentFlags.REDIRECT_FROM, IntentFlags.FROM_LOGIN)
                            startActivity(intent)

                            textInputEditTextMobileNumber.text?.clear()
                            textInputEditTextPassword.text?.clear()
                            textInputLayoutPassword.error = null
                            textInputLayoutMobileNumber.error = null
                            ActivityCompat.finishAffinity(this@LoginActivity)

                            SharedPreferences.getInstance(this@LoginActivity).isUserLoggedIn = true
                        }


                    } else {
                        progressBarLogin.hide()
                        buttonLogin.isClickable = true
//                        toast(getString(R.string.login_validation_message))
                        toast(response.body()!!.message)
                    }

                }
            }

        })
    }

    private fun textChangeListeners() {

        textInputEditTextMobileNumber.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused) {
                this.hideKeyboard(view)
                phoneValidation()
            }
        }

        textInputEditTextPassword.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused) {
                this.hideKeyboard(view)
                passwordValidation()
            }
        }

        textInputEditTextMobileNumber.onTextChanged {
            textViewPhoneNumberError.invisible()
        }

    }


    private fun phoneValidation(): Boolean {
        when {
            textInputEditTextMobileNumber.isEmpty() -> {
                textViewPhoneNumberError.show()
                textViewPhoneNumberError.text = getString(R.string.validation_enter_number)
                return false
            }
            !textInputEditTextMobileNumber.isValidMobileNumber() -> {
                textViewPhoneNumberError.show()
                textViewPhoneNumberError.text = getString(R.string.validation_number)
                return false
            }
        }
        textViewPhoneNumberError.invisible()
        return true
    }

    private fun passwordValidation(): Boolean {
        when {
            textInputEditTextPassword.isEmpty() -> {
                textViewPasswordError.show()
                textViewPasswordError.text = getString(R.string.validation_enter_password)
                return false
            }
            textInputEditTextPassword.text.toString().startsWith(" ") -> {
                textViewPasswordError.show()
                textViewPasswordError.text = getString(R.string.validation_empty_space)
                return false
            }
            textInputEditTextPassword.length() < 6 -> {
                textViewPasswordError.show()
                textViewPasswordError.text = getString(R.string.validation_password_length)
                return false
            }
        }
        textViewPasswordError.invisible()
        return true
    }
}
