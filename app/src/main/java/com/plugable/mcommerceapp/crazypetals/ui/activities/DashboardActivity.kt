package com.plugable.mcommerceapp.crazypetals.ui.activities


import ServiceGenerator
import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.navigation.NavigationView
import com.google.firebase.iid.FirebaseInstanceId
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.*
import com.plugable.mcommerceapp.crazypetals.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.crazypetals.registration.LoginActivity
import com.plugable.mcommerceapp.crazypetals.ui.fragments.*
import com.plugable.mcommerceapp.crazypetals.utils.application.App
import com.plugable.mcommerceapp.crazypetals.utils.constants.IntentFlags
import com.plugable.mcommerceapp.crazypetals.utils.constants.IntentFlags.FRAGMENT_TO_BE_LOADED
import com.plugable.mcommerceapp.crazypetals.utils.extension.hide
import com.plugable.mcommerceapp.crazypetals.utils.extension.setStatusBarColor
import com.plugable.mcommerceapp.crazypetals.utils.extension.setToolBarColor
import com.plugable.mcommerceapp.crazypetals.utils.extension.show
import com.plugable.mcommerceapp.crazypetals.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.crazypetals.utils.util.isNetworkAccessible
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_order_summary.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*
import kotlinx.android.synthetic.main.nav_header_dashboard.view.*
import org.jetbrains.anko.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * [DashboardActivity] is used to load Navigation drawer as well as Fragments
 *
 */
class DashboardActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {


        private lateinit var updatePaymentStatusApi: Call<UpdatePaymentResponse>
    private lateinit var addTokenApi: Call<NotificationToken>
    private lateinit var getMyProfileApi: Call<GetMyProfile>
    private var backPressed: Long = 0
    private lateinit var mixPanel: MixpanelAPI


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        setSupportActionBar(toolBar)

        initializeTheme()
        initializeViews()
        attemptApiCall()
        fetchFCMToken()
    }

      private fun updatePaymentStatus(
          orderId: String?,
          paymentStatusId: String
      ) {
          if (isNetworkAccessible()) {
              val updateStatusRequest = UpdatePaymentRequest(
                  orderId.toString(),
                  paymentStatusId
              )
              App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
              val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
              updatePaymentStatusApi = clientInstance.updatePaymentStatus(updateStatusRequest)
              updatePaymentStatusApi.enqueue(object : Callback<UpdatePaymentResponse> {
                  override fun onFailure(call: Call<UpdatePaymentResponse>, t: Throwable) {
                      toast(getString(R.string.message_something_went_wrong))
                  }

                  override fun onResponse(
                      call: Call<UpdatePaymentResponse>,
                      response: Response<UpdatePaymentResponse>
                  ) {
                      if (response.body()!!.statusCode.equals("10")) {
//                          toast("Payment status updated successfully")
                          SharedPreferences.getInstance(this@DashboardActivity).setStringValue(IntentFlags.ORDER_ID,"")
                          sendMixPanelEvent()
                      } else {
                          toast(response.body()!!.message)
                      }
                  }

              })
          } else {
              toast(getString(R.string.oops_no_internet_connection))
          }
      }

      private fun sendMixPanelEvent() {
          val productObject = JSONObject()
          productObject.put(IntentFlags.MIXPANEL_APPLICATION_KILLED,IntentFlags.MIXPANEL_PAYMENT_FAIL_DUE_TO_APP_KILL)
          mixPanel.track(IntentFlags.MIXPANEL_PAYMENT_STATUS_UPDATE,productObject)
      }
    //Fetch firebase cloud messaging token
    private fun fetchFCMToken() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(getString(R.string.app_name), "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token
                val applicationUserId = SharedPreferences.getInstance(this)
                    .getStringValue(IntentFlags.APPLICATION_USER_ID)
                val notificationTokenRequest =
                    NotificationTokenRequest(Token = token, ApplicationUserId = applicationUserId)
                if (isNetworkAccessible()) {
                    callAddTokenApi(notificationTokenRequest)
                }
            })

    }

    private fun attemptApiCall() {
        if (isNetworkAccessible()) {
            callGetProfileApi()
        } else {
//            toast(getString(R.string.check_internet_connection))
        }
    }

    private fun callGetProfileApi() {
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        val applicationUserId =
            SharedPreferences.getInstance(this).getStringValue(IntentFlags.APPLICATION_USER_ID)
        getMyProfileApi = clientInstance.getProfileApi(applicationUserId.toString())

        getMyProfileApi.enqueue(
            object : Callback<GetMyProfile> {
                override fun onFailure(call: Call<GetMyProfile>, t: Throwable) {
                    showNetworkCondition()
                }

                override fun onResponse(
                    call: Call<GetMyProfile>,
                    response: Response<GetMyProfile>
                ) {
                    if (isDestroyed) {
                        return
                    }
                    if (response.isSuccessful) {
                        if (response.body()?.statusCode.equals("10")) {
                            SharedPreferences.getInstance(this@DashboardActivity)
                                .setProfile(response.body()!!.data)
                            val userImage = App.HostUrl.plus(response.body()!!.data.profilePicture)
                            userImage.let {
                                SharedPreferences.getInstance(this@DashboardActivity)
                                    .setStringValue("UserImage", it)
                            }

                            val userName = response.body()!!.data.name
                            SharedPreferences.getInstance(this@DashboardActivity)
                                .setStringValue("UserName", userName)

                            with(nav_view) {
                                setNavigationItemSelectedListener(this@DashboardActivity)
                                getHeaderView(0).setBackgroundColor(
                                    Color.parseColor(
                                        ApplicationThemeUtils.TOOL_BAR_COLOR
                                    )
                                )
                                getHeaderView(0).textViewNavigationHeader.textColor = Color.WHITE
                                getHeaderView(0).setBackgroundColor(Color.GRAY)
                                getHeaderView(0)
                                    .textViewNavigationHeader.setBackgroundColor(Color.GRAY)
                                if (userName != null && SharedPreferences.getInstance(this@DashboardActivity).isUserLoggedIn) {
                                    getHeaderView(0).textViewNavigationHeader.text =
                                        getString(R.string.title_hello).plus(" ")
                                            .plus(userName.capitalize())
                                            .plus(" ")
//                        .plus(getString(R.string.title_emoji))

                                } else {
                                    if (textViewNavigationHeader != null) textViewNavigationHeader.hide()
                                }

                                if (SharedPreferences.getInstance(this@DashboardActivity).isUserLoggedIn) {
                                    Glide.with(this)
                                        .load(userImage)
                                        .apply(
                                            RequestOptions.circleCropTransform()
                                                .placeholder(R.drawable.ic_fill_9)
                                        )
                                        .into(getHeaderView(0).imageViewNavigationHeader)
                                } else {
                                    getHeaderView(0).imageViewNavigationHeader.setImageResource(R.drawable.ic_fill_9)
                                }
                                if (intent.hasExtra(FRAGMENT_TO_BE_LOADED)) {

                                    with(
                                        menu.findItem(
                                            intent.getIntExtra(
                                                FRAGMENT_TO_BE_LOADED,
                                                R.id.nav_home
                                            )
                                        )
                                    ) {
                                        isChecked = true
                                        onNavigationItemSelected(this)
                                    }

                                } else {
                                    with(menu.findItem(R.id.nav_home)) {
                                        isChecked = true
                                        onNavigationItemSelected(this)
                                    }
                                }
                            }
                        }
                    }
                }

            })
    }


    //Update notification token to the server
    private fun callAddTokenApi(notificationTokenRequest: NotificationTokenRequest) {
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)

        addTokenApi = clientInstance.addToken(notificationTokenRequest)
        addTokenApi.enqueue(object : Callback<NotificationToken> {
            override fun onFailure(call: Call<NotificationToken>, t: Throwable) {
            }

            override fun onResponse(
                call: Call<NotificationToken>,
                response: Response<NotificationToken>
            ) {
            }
        })
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
        setToolBar(ApplicationThemeUtils.APP_NAME, false)
    }

    private fun initializeViews() {
        val customerName = SharedPreferences.getInstance(this).getStringValue("UserName")
        val customerImage =
            App.HostUrl.plus(SharedPreferences.getInstance(this).getStringValue("UserImage"))

        imgToolbarHomeLayout.setOnClickListener {
            hideKeyboard()
            drawer_layout.openDrawer(GravityCompat.START)
        }

        val mDrawerToggle = object : ActionBarDrawerToggle(
            this, drawer_layout,
            R.string.drawer_open, R.string.drawer_close
        ) {
            override fun onDrawerOpened(drawerView: View) {
                hideKeyboard()
                super.onDrawerOpened(drawerView)
            }
        }
        drawer_layout.setDrawerListener(mDrawerToggle)


        with(nav_view) {
            setNavigationItemSelectedListener(this@DashboardActivity)
            getHeaderView(0).setBackgroundColor(Color.parseColor(ApplicationThemeUtils.TOOL_BAR_COLOR))
            getHeaderView(0).setBackgroundColor(Color.GRAY)
            getHeaderView(0).textViewNavigationHeader.setBackgroundColor(Color.GRAY) //
            getHeaderView(0).textViewNavigationHeader.textColor = Color.WHITE
            getHeaderView(0).textViewNavigationHeader.text = ""
            if (customerName != null && SharedPreferences.getInstance(this@DashboardActivity).isUserLoggedIn) {
                getHeaderView(0).textViewNavigationHeader.text =
                    getString(R.string.title_hello).plus(" ").plus(customerName.capitalize())
                        .plus(" ")
//                        .plus(getString(R.string.title_emoji))

            } else {
                if (textViewNavigationHeader != null) textViewNavigationHeader.hide()
            } /*else {
                getHeaderView(0).textViewNavigationHeader.text =
                    getString(R.string.title_hello).plus(" User ")
//                        .plus(getString(R.string.title_emoji))

            }*/

            getHeaderView(0).setOnClickListener {
                if (SharedPreferences.getInstance(this@DashboardActivity).isUserLoggedIn) {
                    startActivity<ProfileActivity>()
                } else {
                    toast(context.getString(R.string.please_login_to_continue))
                }
            }
            if (SharedPreferences.getInstance(this@DashboardActivity).isUserLoggedIn) {
                Glide.with(this)
                    .load(customerImage)
                    .apply(
                        RequestOptions.circleCropTransform()
                            .placeholder(R.drawable.ic_fill_9)
                    )
                    .into(getHeaderView(0).imageViewNavigationHeader)
            } else {
                getHeaderView(0).imageViewNavigationHeader.setImageResource(R.drawable.ic_fill_9)

            }
            if (intent.hasExtra(FRAGMENT_TO_BE_LOADED)) {

                with(menu.findItem(intent.getIntExtra(FRAGMENT_TO_BE_LOADED, R.id.nav_home))) {
                    isChecked = true
                    onNavigationItemSelected(this)
                }
            } else {
                with(menu.findItem(R.id.nav_home)) {
                    isChecked = true
                    onNavigationItemSelected(this)
                }
            }

            if (SharedPreferences.getInstance(this@DashboardActivity).isUserLoggedIn) {
                menu.findItem(R.id.nav_login_signUp).isVisible = false
                menu.findItem(R.id.nav_log_out).isVisible = true
                menu.findItem(R.id.nav_my_order).isVisible = true
                menu.findItem(R.id.nav_appointmentList).isVisible = true
            } else {
                menu.findItem(R.id.nav_login_signUp).isVisible = true
                menu.findItem(R.id.nav_log_out).isVisible = false
                menu.findItem(R.id.nav_my_order).isVisible = false
                menu.findItem(R.id.nav_appointmentList).isVisible = false
            }
        }
           mixPanel = MixpanelAPI.getInstance(this, resources.getString(R.string.mix_panel_token))
           if (SharedPreferences.getInstance(this).getStringValue(IntentFlags.ORDER_ID)!!.isNotEmpty()) {
               val orderId = SharedPreferences.getInstance(this).getStringValue(IntentFlags.ORDER_ID)
               updatePaymentStatus(orderId, "3")
           }
    }

    fun hideKeyboard() {
        try {
            val inputMethodManager =
                getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                currentFocus!!.windowToken, 0
            )

        } catch (e: Exception) {

        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawer_layout.closeDrawer(GravityCompat.START)
        var fragment: Fragment? = null
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        when (item.itemId) {

            R.id.nav_login_signUp -> {
                redirectToLogin()
            }

            R.id.nav_home -> {
                if (currentFragment !is HomeFragment) {
                    fragment = HomeFragment()
                    setToolBar(ApplicationThemeUtils.APP_NAME, false)
                }
            }


            R.id.nav_wishList -> {
                if (currentFragment !is WishListFragment) {
                    fragment = WishListFragment()
                    setToolBar(getString(R.string.title_wish_list), true)
                }
            }

            R.id.nav_my_order -> {
                if (currentFragment !is MyOrderFragment) {
                    fragment = MyOrderFragment()
                    setToolBar(getString(R.string.title_my_order), true)
                }
            }
            R.id.nav_appointmentList -> {
                if (currentFragment !is AppointmentListFragment) {
                    fragment = AppointmentListFragment()
                    setToolBar(getString(R.string.title_appointments), true)
                }
            }

            R.id.nav_privacy_policy -> {
                if (currentFragment !is PrivacyPolicyFragment) {
                    fragment = PrivacyPolicyFragment()
                    setToolBar(getString(R.string.menu_privacy_policy), true)
                }
            }

            R.id.nav_return_policy -> {
                if (currentFragment !is ReturnPolicyFragment) {
                    fragment = ReturnPolicyFragment()
                    setToolBar(getString(R.string.menu_return_policy), true)
                }
            }

            R.id.nav_about_us -> {
                if (currentFragment !is AboutUsFragment) {
                    fragment = AboutUsFragment()
                    setToolBar(getString(R.string.menu_about_us), true)
                }
            }
            /*   R.id.nav_faqs -> {
                   if (currentFragment !is FAQsFragment) {
                       fragment = FAQsFragment()
                       setToolBar(getString(R.string.title_faqs), true)
                   }
               }*/
            R.id.nav_contact_us -> {
                if (currentFragment !is ContactUsFragment) {
                    fragment = ContactUsFragment()
                    setToolBar(getString(R.string.title_contact_us), true)
                }
            }

            R.id.nav_log_out -> {
                alert(getString(R.string.message_logout)) {
                    isCancelable = false
                    yesButton {
                        toast(getString(R.string.message_logged_out))
                        logoutUser()
                    }
                    noButton {

                    }
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

        if (!isFinishing) {
            if (fragment != null)
                supportFragmentManager.beginTransaction().apply {
                    replace(
                        R.id.fragmentContainer,
                        fragment,
                        getString(R.string.fragment_tag)
                    ).commit()
                }
        }
        return true
    }

    private fun redirectToLogin() {
        startActivity<LoginActivity>()
        ActivityCompat.finishAffinity(this)
    }

    private fun logoutUser() {
        SharedPreferences.getInstance(this).isUserLoggedIn = false
        SharedPreferences.getInstance(this).isUserSkippedLogin = false
        SharedPreferences.getInstance(this).setStringValue(IntentFlags.APPLICATION_USER_ID, "0")
        SharedPreferences.getInstance(this).isTermsConditionRememberMe = false
        initializeViews()
    }

    override fun onBackPressed() {
        when {
            drawer_layout.isDrawerOpen(GravityCompat.START) -> drawer_layout.closeDrawer(
                GravityCompat.START
            )
            backPressed + 2000 > System.currentTimeMillis() -> {
                super.onBackPressed()
                overridePendingTransition(0, 0)
            }
            else -> {
                toast(getString(R.string.message_press_back_button))
            }
        }
        backPressed = System.currentTimeMillis()
    }

    override fun setToolBar(name: String, shouldShowTitle: Boolean) {
        toolBar.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.TOOL_BAR_COLOR))
        setSupportActionBar(toolBar)
        setStatusBarColor()
        if (shouldShowTitle) {
            txtToolbarTitle.show()
            txtToolbarTitle.text = name
            cp_Logo.hide()
        } else {
            txtToolbarTitle.hide()
            cp_Logo.show()
        }
        supportActionBar?.setDisplayShowTitleEnabled(false)
        imgToolbarHome.setImageResource(R.drawable.ic_hamburger)
        setToolBarColor(imgToolbarHome, txtToolbarTitle, toolbar = toolBar)
    }


    override fun onClick(view: View?) {

    }

    private fun cancelTasks() {
        if (::addTokenApi.isInitialized && addTokenApi != null) addTokenApi.cancel()
        if (::getMyProfileApi.isInitialized && getMyProfileApi != null) getMyProfileApi.cancel()
    }

    override fun onDestroy() {
        cancelTasks()
        super.onDestroy()
    }
}
