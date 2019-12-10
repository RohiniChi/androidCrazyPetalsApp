package com.plugable.mcommerceapp.cpmvp1.ui.activities

import ServiceGenerator
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.plugable.mcommerceapp.cpmvp1.BuildConfig
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.ApplicationTheme
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.Host
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.VersionInfo
import com.plugable.mcommerceapp.cpmvp1.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.cpmvp1.registration.LoginActivity
import com.plugable.mcommerceapp.cpmvp1.utils.application.App
import com.plugable.mcommerceapp.cpmvp1.utils.constants.IntentFlags
import com.plugable.mcommerceapp.cpmvp1.utils.extension.setStatusBarColor
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferencesForBaseUrl
import com.plugable.mcommerceapp.cpmvp1.utils.util.isNetworkAccessible
import org.jetbrains.anko.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * [SplashScreenActivity] is used to load application image as well as dynamic theme using api
 *
 */
class SplashScreenActivity : AppCompatActivity() {
    private val hostUrlIndex = 2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        checkInternetConnection()

    }

    private fun checkInternetConnection() {
        if (isNetworkAccessible()) {
            callHostName()
        } else {
            try {
                getDataFromSharedPreferenceForHostUrl()
            } catch (e: Exception) {
                alert(getString(R.string.check_internet_connection)) {
                    positiveButton(getString(R.string.try_again)) { checkInternetConnection() }
                    isCancelable = false
                }.show().apply {
                    getButton(AlertDialog.BUTTON_POSITIVE)?.let {
                        it.textColor = Color.BLUE
                        it.background =
                            ContextCompat.getDrawable(
                                this@SplashScreenActivity,
                                android.R.color.transparent
                            )
                    }
                }

            }
        }
    }

    private fun callHostName() {
        val clientInstance = ServiceGenerator.createHostnameService(ProjectApi::class.java)
        val call = clientInstance.getHostname()

        call.enqueue(object : Callback<Host> {
            override fun onFailure(call: Call<Host>?, throwable: Throwable?) {
                //dialog.cancel()
                alert(getString(R.string.check_internet_connection)) {
                    yesButton { goToDashboardActivity() }
                    isCancelable = false
                }.show().apply {
                    getButton(AlertDialog.BUTTON_POSITIVE)?.let {
                        it.textColor = Color.BLUE
                        it.background =
                            ContextCompat.getDrawable(
                                this@SplashScreenActivity,
                                android.R.color.transparent
                            )
                    }
                }
            }

            override fun onResponse(call: Call<Host>?, response: Response<Host>?) {

                if (response!!.isSuccessful) {

                    if (response.body()!!.apiConfig.isNotEmpty()) {
                        App.HostUrl = response.body()?.apiConfig!![hostUrlIndex].devBaseUrl
                        SharedPreferences.getInstance(this@SplashScreenActivity).hostUrl =
                            App.HostUrl
                        updatePreferenceOfHostname(response.body()!!.apiConfig[hostUrlIndex])
                        callApplicationThemeApi()
                        Log.e("Success", response.body()?.apiConfig!![1].devBaseUrl)
                    } else {
                        alert(getString(R.string.message_contact_admin)) {
                            yesButton { finish() }
                            isCancelable = false
                        }.show().apply {
                            getButton(AlertDialog.BUTTON_POSITIVE)?.let {
                                it.textColor = Color.BLUE
                                it.background =
                                    ContextCompat.getDrawable(
                                        this@SplashScreenActivity,
                                        android.R.color.transparent
                                    )
                            }
                        }
                    }

                } else {

                    alert(getString(R.string.message_contact_admin)) {
                        yesButton { finish() }
                        isCancelable = false
                    }.show().apply {
                        getButton(AlertDialog.BUTTON_POSITIVE)?.let {
                            it.textColor = Color.BLUE
                            it.background =
                                ContextCompat.getDrawable(
                                    this@SplashScreenActivity,
                                    android.R.color.transparent
                                )
                        }
                    }

                }
            }
        })
    }


    private fun getDataFromSharedPreference() {

        if (SharedPreferences.getInstance(this).themeDataPreference != null) {
            val sharedPreferences = SharedPreferences.getInstance(this).themeDataPreference
            ApplicationThemeUtils.PRIMARY_COLOR = sharedPreferences!!.primaryColor
            ApplicationThemeUtils.STATUS_BAR_COLOR = sharedPreferences.statusBarColor
            ApplicationThemeUtils.SECONDARY_COLOR = sharedPreferences.secondryColor
            ApplicationThemeUtils.TEXT_COLOR = sharedPreferences.textColor
            ApplicationThemeUtils.CURRENCY_SYMBOL = sharedPreferences.currencySymbol
            ApplicationThemeUtils.APP_NAME = sharedPreferences.appName
            ApplicationThemeUtils.App_Logo = sharedPreferences.appLogoURL
            ApplicationThemeUtils.TOOL_BAR_COLOR = sharedPreferences.tertiaryColor
            setActivityTheme()

        }
    }

    private fun getDataFromSharedPreferenceForHostUrl() {
        if (SharedPreferencesForBaseUrl.getInstance(this).tokenDataPreference?.devBaseUrl != null) {
            val sharedPreferences =
                SharedPreferencesForBaseUrl.getInstance(this).tokenDataPreference
            App.HostUrl = sharedPreferences!!.devBaseUrl

            try {
                getDataFromSharedPreference()
            } catch (e: Exception) {
                setActivityTheme()
            }
            goToDashboardActivity()
        }
    }

    /**
     * This API is used for checking latest version of Application
     */
    private fun checkAppVersion() {
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        val call = clientInstance.getVersionInfo()

        call.enqueue(object : Callback<VersionInfo> {
            override fun onResponse(call: Call<VersionInfo>, response: Response<VersionInfo>) {

                if (response.isSuccessful) {

                    if (response.body()?.statusCode?.equals("10")!!) {

                        SharedPreferences.getInstance(this@SplashScreenActivity).setStringValue(
                            IntentFlags.VERSION_NUMBER,
                            BuildConfig.VERSION_NAME
                        )
                        if (response.body()!!.data.versionNumber > BuildConfig.VERSION_NAME) {
//                            showPopUpForLatestUpdate()//remove thisline for force update
//                            ("Uncomment following code while going live. for testing purpose it is keep force update commented")
                            if (response.body()!!.data.updateType.equals(getString(R.string.forceful_update))) {
                                showForceUpdatePopUp()
                            } else {
                                showPopUpForLatestUpdate()
                            }
                        } else {
                            goToDashboardActivity()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<VersionInfo>, throwable: Throwable) {
                //Not use because of this API is called in background in activity
            }
        })
    }

    /**
     * This method is used for showing Force update app pop up message.
     */
    private fun showForceUpdatePopUp() {
        // build alert dialog
        alert(getString(R.string.app_update_message)) {
            isCancelable = false
            this.title = getString(R.string.app_update_info)
            positiveButton(getString(R.string.app_update)) {
                val url = Uri.parse(getString(R.string.playstore_url))
                val intents = Intent(Intent.ACTION_VIEW, url)
                startActivity(intents)
                finish()
            }
        }.show().apply {

            getButton(AlertDialog.BUTTON_POSITIVE)?.let {

                it.allCaps = false
                it.typeface = Typeface.DEFAULT_BOLD
                it.background = ContextCompat.getDrawable(context, android.R.color.transparent)
                it.textColor = Color.BLUE
            }
        }

    }

    /**
     * This method is used for showing latest version of app pop up message.
     */
    private fun showPopUpForLatestUpdate() {
        // build alert dialog
        alert(getString(R.string.app_update_message)) {
            isCancelable = false
            this.title = getString(R.string.app_update_info)

            positiveButton(getString(R.string.app_update)) {
                val url = Uri.parse(getString(R.string.playstore_url))
                val intents = Intent(Intent.ACTION_VIEW, url)
                startActivity(intents)
                finish()
            }
            negativeButton(getString(R.string.app_update_later)) {
                goToDashboardActivity()
                finish()
            }
        }.show().apply {

            getButton(AlertDialog.BUTTON_POSITIVE)?.let {

                it.allCaps = false
                it.typeface = Typeface.DEFAULT_BOLD
                it.background = ContextCompat.getDrawable(context, android.R.color.transparent)
                it.textColor = Color.BLUE
            }
            getButton(AlertDialog.BUTTON_NEGATIVE)?.let {

                it.allCaps = false
                it.background = ContextCompat.getDrawable(context, android.R.color.transparent)
                it.textColor = Color.BLUE
            }

        }

    }


    private fun setActivityTheme() {
        setStatusBarColor()
        setSplashScreenImage()
    }

    private fun setSplashScreenImage() {
        /* GlideApp.with(this)
             .load(ApplicationThemeUtils.App_Logo)
             .placeholder(R.drawable.ic_placeholder_category)
             .diskCacheStrategy(DiskCacheStrategy.ALL)
             .fallback(R.drawable.ic_placeholder_category)
             .apply(RequestOptions())
             .error(R.drawable.ic_placeholder_category)
             .into(imgSplashLogo);*/
    }

    private fun callApplicationThemeApi() {
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        val call = clientInstance.myJSON
        call.enqueue(object : Callback<ApplicationTheme> {
            override fun onFailure(call: Call<ApplicationTheme>?, t: Throwable?) {
                toast(getString(R.string.oops_something_went_wrong))
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<ApplicationTheme>?,
                response: Response<ApplicationTheme>?
            ) {
                if (isNetworkAccessible()) {
                    if (response?.body()?.statusCode.equals("10")) {
                        updatePreference(response?.body()!!.data)
                        ApplicationThemeUtils.PRIMARY_COLOR =
                            response.body()?.data?.primaryColor.toString()
                        ApplicationThemeUtils.STATUS_BAR_COLOR =
                            response.body()?.data?.statusBarColor.toString()
                        ApplicationThemeUtils.SECONDARY_COLOR =
                            response.body()?.data?.secondryColor.toString()
                        ApplicationThemeUtils.TEXT_COLOR =
                            response.body()?.data?.textColor.toString()
                        ApplicationThemeUtils.CURRENCY_SYMBOL =
                            response.body()?.data?.currencySymbol.toString()
                        ApplicationThemeUtils.APP_NAME = response.body()?.data?.appName.toString()
                        ApplicationThemeUtils.App_Logo =
                            response.body()?.data?.appLogoURL.toString()
                        ApplicationThemeUtils.App_Logo =
                            response.body()?.data?.tertiaryColor.toString()
                        setActivityTheme()
                        checkAppVersion()
                    }

                } else {
                    toast(getString(R.string.check_internet_connection))
                }
            }
        })
    }

    private fun goToDashboardActivity() {
        if (SharedPreferences.getInstance(this).isUserSkippedLogin || SharedPreferences.getInstance(
                this
            ).isUserLoggedIn
        ) {
            startActivity<DashboardActivity>()
            finish()
        } else {
            startActivity<LoginActivity>()
        }
    }


    private fun updatePreferenceOfHostname(apiConfigData: Host.ApiConfig) {
        val apiConfig: Host.ApiConfig = apiConfigData
        SharedPreferencesForBaseUrl.getInstance(this).tokenDataPreference = apiConfig
    }

    private fun updatePreference(themeDataValue: ApplicationTheme.Data) {
        val themeData: ApplicationTheme.Data = themeDataValue
        SharedPreferences.getInstance(this).themeDataPreference = themeData
    }

    //This is permanent so no change
    /*   if (isNetworkAccessible()) {
           callApplicationThemeApi()
       } else {
           try {
               getDataFromSharedPreference()
           }catch (e:Exception){
               setActivityTheme()
           }
       }*/
}

