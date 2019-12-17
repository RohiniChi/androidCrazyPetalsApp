package com.plugable.mcommerceapp.cpmvp1.ui.activities

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.widget.ImageViewCompat
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.PlaceOrderResponse
import com.plugable.mcommerceapp.cpmvp1.utils.extension.hide
import com.plugable.mcommerceapp.cpmvp1.utils.extension.setStatusBarColor
import com.plugable.mcommerceapp.cpmvp1.utils.extension.setToolBarColor
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences
import kotlinx.android.synthetic.main.activity_success_order_status.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*
import org.jetbrains.anko.startActivity

/**
 * [SuccessOrderStatusActivity] is used to show order status to user
 *
 */
class SuccessOrderStatusActivity : AppCompatActivity(), View.OnClickListener {
    companion object {
        const val PLACE_ORDER_RESPONSE = "place.order.response"
    }

    var placeOrderResponse: PlaceOrderResponse? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success_order_status)

        readIntent()
        initializeTheme()
        initializeViews()
        setPlaceOrderResponseData()
    }

    private fun setPlaceOrderResponseData() {
        textViewOrderStatusOrderNumberDescription.text =
            String.format("%s", placeOrderResponse!!.orderNumber)
        textViewOrderStatusOrderAmountDescription.text =
            String.format("%s", placeOrderResponse!!.deliveryDay)
        textViewOrderStatusOrderPaymentDescription.text = String.format("%s", "Cash on delivery")
    }

    private fun readIntent() {
        placeOrderResponse = intent.getParcelableExtra(PLACE_ORDER_RESPONSE)
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
        setToolBar(ApplicationThemeUtils.APP_NAME)
        setThemeToComponent()
    }

    private fun setThemeToComponent() {
        browseMoreButton.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        browseMoreButton.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        ImageViewCompat.setImageTintList(
            imageView,
            ColorStateList.valueOf(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        )
    }


    private fun initializeViews() {
        imgToolbarHome.setOnClickListener(this)
        browseMoreButton.setOnClickListener(this)
    }


    fun setToolBar(name: String) {
        setSupportActionBar(toolBar)
        setStatusBarColor()
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = name
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_shape_backarrow_white)
        cp_Logo.hide()
        txtToolbarTitle.text = getString(R.string.title_order_status)
        imgToolbarHome.hide()
        setToolBarColor(imgToolbarHome, txtToolbarTitle, toolbar = toolBar)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            android.R.id.home -> onBackPressed()
            R.id.imgToolbarHome -> onBackPressed()
            R.id.browseMoreButton -> {
                browseMoreButton.isClickable = false

                startActivity<DashboardActivity>()
                ActivityCompat.finishAffinity(this)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        browseMoreButton.isClickable = false

        startActivity<DashboardActivity>()
        ActivityCompat.finishAffinity(this)
    }
}
