package com.plugable.mcommerceapp.crazypetals.ui.activities

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.widget.ImageViewCompat
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.PlaceOrderResponse
import com.plugable.mcommerceapp.crazypetals.utils.constants.IntentFlags
import com.plugable.mcommerceapp.crazypetals.utils.extension.hide
import com.plugable.mcommerceapp.crazypetals.utils.extension.setStatusBarColor
import com.plugable.mcommerceapp.crazypetals.utils.extension.setToolBarColor
import com.plugable.mcommerceapp.crazypetals.utils.sharedpreferences.SharedPreferences
import kotlinx.android.synthetic.main.activity_success_order_status.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*
import org.jetbrains.anko.startActivity
import org.json.JSONObject

/**
 * [SuccessOrderStatusActivity] is used to show order status to user
 *
 */
class SuccessOrderStatusActivity : AppCompatActivity(), View.OnClickListener {
    companion object {
        const val PLACE_ORDER_RESPONSE = "place.order.response"
    }
    private lateinit var mixPanel: MixpanelAPI

    private var transactionStatus: String = ""
    private var orderNumber: String = ""
    private var deliveryDay: String = ""
    private var paymentStatus: String = ""
    var placeOrderResponse: PlaceOrderResponse? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success_order_status)

        readIntent()
        initializeTheme()
        initializeViews()
    }

    private fun setPlaceOrderResponseData(
    ) {
        if (transactionStatus.equals("Unsuccessful", true)) {
            textViewOrderStatusOrderNumberDescription.text =
                String.format("%s", placeOrderResponse?.orderNumber)
            textViewOrderStatusOrderAmountDescription.hide()
            textViewOrderStatusOrderAmountTitle.hide()
            textViewOrderStatusOrderPaymentDescription.text = String.format("%s", "Online payment")
        } else {
            textViewOrderStatusOrderNumberDescription.text =
                String.format("%s", placeOrderResponse?.orderNumber)
            textViewOrderStatusOrderAmountDescription.text =
                String.format("%s", placeOrderResponse?.deliveryDay)
            textViewOrderStatusOrderPaymentDescription.text = String.format("%s", "Online payment")

        }
    }

    private fun readIntent() {


        if (intent.hasExtra(IntentFlags.REDIRECT_FROM)) {
            when (intent.getStringExtra(IntentFlags.REDIRECT_FROM)) {
                "OrderDetailActivity" -> {
                    paymentStatus = intent.getStringExtra("TransactionStatus")
                    deliveryDay = intent.getStringExtra("DeliveryDay")
                    orderNumber = intent.getStringExtra("orderNumber")
                    if (paymentStatus.equals("Successful", true)) {
                        setOrderDetailData()
                        ImageViewCompat.setImageTintList(
                            imageView,
                            ColorStateList.valueOf(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
                        )
                    } else if (paymentStatus.equals("Unsuccessful", true)) {
                        imageView.setImageResource(R.drawable.ic_transaction_failed)
                        tvLabelThankYou.text = getString(R.string.text_transaction_failed)
                        tVOrderConfirmed.text = getString(R.string.message_transaction_failed)
                        tVOrderConfirmed.gravity = Gravity.CENTER_HORIZONTAL
                        setOrderDetailData()
                    }
                }
            }
        } else {
            placeOrderResponse = intent.getParcelableExtra(PLACE_ORDER_RESPONSE)
            transactionStatus = intent.getStringExtra("TransactionStatus")
            if (transactionStatus.equals("Successful", true)) {
                setPlaceOrderResponseData()
                ImageViewCompat.setImageTintList(
                    imageView,
                    ColorStateList.valueOf(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
                )
            } else if (transactionStatus.equals("Unsuccessful", true)) {
                imageView.setImageResource(R.drawable.ic_transaction_failed)
                tvLabelThankYou.text = getString(R.string.text_transaction_failed)
                tVOrderConfirmed.text = getString(R.string.message_transaction_failed)
                tVOrderConfirmed.gravity = Gravity.CENTER_HORIZONTAL
                setPlaceOrderResponseData()
            }
        }
    }

    private fun setOrderDetailData() {
        if (paymentStatus.equals("Unsuccessful", true)) {
            textViewOrderStatusOrderNumberDescription.text = orderNumber
//        textViewOrderStatusOrderAmountDescription.text = deliveryDay
            textViewOrderStatusOrderAmountDescription.hide()
            textViewOrderStatusOrderAmountTitle.hide()
            textViewOrderStatusOrderPaymentDescription.text = String.format("%s", "Online payment")
        } else {
            textViewOrderStatusOrderNumberDescription.text = orderNumber
            textViewOrderStatusOrderAmountDescription.text = deliveryDay
            textViewOrderStatusOrderPaymentDescription.text = String.format("%s", "Online payment")
        }
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

    }


    private fun initializeViews() {
        imgToolbarHome.setOnClickListener(this)
        browseMoreButton.setOnClickListener(this)
        mixPanel = MixpanelAPI.getInstance(this, resources.getString(R.string.mix_panel_token))
        sendMixPanelEvent()
    }


    fun setToolBar(name: String) {
        setSupportActionBar(toolBar)
        setStatusBarColor()
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title =  getString(R.string.title_order_status)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_shape_backarrow_white)
        cp_Logo.hide()
        txtToolbarTitle.hide()
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
    private fun sendMixPanelEvent() {
        val productObject = JSONObject()
        if (intent.hasExtra(IntentFlags.REDIRECT_FROM)) {
            when (intent.getStringExtra(IntentFlags.REDIRECT_FROM)) {
                "OrderDetailActivity" -> {
                    productObject.put(IntentFlags.MIXPANEL_ORDER_NUMBER, orderNumber)
                    mixPanel.track(IntentFlags.MIXPANEL_VISITED_SUCCESS_ORDER_STATUS_SCREEN, productObject)
                }
            }
        }else{
            productObject.put(IntentFlags.MIXPANEL_ORDER_NUMBER, placeOrderResponse!!.orderNumber)
            mixPanel.track(IntentFlags.MIXPANEL_VISITED_SUCCESS_ORDER_STATUS_SCREEN, productObject)
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
    override fun onDestroy() {
        mixPanel.flush()
        super.onDestroy()
    }


}
