package com.plugable.mcommerceapp.cpmvp1.ui.activities


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
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.GetMyProfile
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.NotificationToken
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.NotificationTokenRequest
import com.plugable.mcommerceapp.cpmvp1.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.cpmvp1.registration.LoginActivity
import com.plugable.mcommerceapp.cpmvp1.ui.fragments.*
import com.plugable.mcommerceapp.cpmvp1.utils.application.App
import com.plugable.mcommerceapp.cpmvp1.utils.constants.IntentFlags
import com.plugable.mcommerceapp.cpmvp1.utils.constants.IntentFlags.FRAGMENT_TO_BE_LOADED
import com.plugable.mcommerceapp.cpmvp1.utils.extension.hide
import com.plugable.mcommerceapp.cpmvp1.utils.extension.setStatusBarColor
import com.plugable.mcommerceapp.cpmvp1.utils.extension.setToolBarColor
import com.plugable.mcommerceapp.cpmvp1.utils.extension.show
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.cpmvp1.utils.util.isNetworkAccessible
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*
import kotlinx.android.synthetic.main.nav_header_dashboard.*
import kotlinx.android.synthetic.main.nav_header_dashboard.view.*
import org.jetbrains.anko.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * [DashboardActivity] is used to load Navigation drawer as well as Fragments
 *
 */
class DashboardActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {


    private var backPressed: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        setSupportActionBar(toolBar)

        initializeTheme()
        initializeViews()
        attemptApiCall()
        fetchFCMToken()
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
        val call = clientInstance.getProfileApi(applicationUserId.toString())

        call.enqueue(
            object : Callback<GetMyProfile> {
                override fun onFailure(call: Call<GetMyProfile>, t: Throwable) {
                    showNetworkCondition()
                }

                override fun onResponse(
                    call: Call<GetMyProfile>,
                    response: Response<GetMyProfile>
                ) {
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

        val call = clientInstance.addToken(notificationTokenRequest)
        call.enqueue(object : Callback<NotificationToken> {
            override fun onFailure(call: Call<NotificationToken>, t: Throwable) {
            }

            override fun onResponse(
                call: Call<NotificationToken>,
                response: Response<NotificationToken>
            ) {
            }
        })
    }


    override fun onResume() {
        initializeViews()
        attemptApiCall()
        fetchFCMToken()
        super.onResume()
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
            getHeaderView(0)
                .textViewNavigationHeader.setBackgroundColor(Color.GRAY) //
            getHeaderView(0).textViewNavigationHeader.textColor = Color.WHITE
            if (customerName != null && SharedPreferences.getInstance(this@DashboardActivity).isUserLoggedIn) {
                getHeaderView(0).textViewNavigationHeader.text =
                    getString(R.string.title_hello).plus(" ").plus(customerName.capitalize())
                        .plus(" ")
//                        .plus(getString(R.string.title_emoji))

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
            } else {
                menu.findItem(R.id.nav_login_signUp).isVisible = true
                menu.findItem(R.id.nav_log_out).isVisible = false
                menu.findItem(R.id.nav_my_order).isVisible = false
            }
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
                    setToolBar(ApplicationThemeUtils.APP_NAME)
                }
            }


            R.id.nav_wishList -> {
                if (currentFragment !is WishListFragment) {
                    fragment = WishListFragment()
                    setToolBar(getString(R.string.title_wish_list))
                }
            }

            R.id.nav_my_order -> {
                if (currentFragment !is MyOrderFragment) {
                    fragment = MyOrderFragment()
                    setToolBar(getString(R.string.title_my_order))
                }
            }

            R.id.nav_about_us -> {
                if (currentFragment !is AboutUsFragment) {
                    fragment = AboutUsFragment()
                    setToolBar(getString(R.string.menu_about_us))
                }
            }
            R.id.nav_faqs -> {
                if (currentFragment !is FAQsFragment) {
                    fragment = FAQsFragment()
                    setToolBar(getString(R.string.title_faqs))
                }
            }
            R.id.nav_contact_us -> {
                if (currentFragment !is ContactUsFragment) {
                    fragment = ContactUsFragment()
                    setToolBar(getString(R.string.title_contact_us))
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

        if (fragment != null)
            supportFragmentManager.beginTransaction().apply {
                replace(
                    R.id.fragmentContainer,
                    fragment,
                    getString(R.string.fragment_tag)
                ).commit()
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
        textViewNavigationHeader.text = ""
        SharedPreferences.getInstance(this).setStringValue(IntentFlags.APPLICATION_USER_ID, "0")
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
}
