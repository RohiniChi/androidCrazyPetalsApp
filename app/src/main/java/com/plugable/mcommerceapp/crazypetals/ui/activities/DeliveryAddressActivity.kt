package com.plugable.mcommerceapp.crazypetals.ui.activities

import ServiceGenerator
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.callbacks.EventListener
import com.plugable.mcommerceapp.crazypetals.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.*
import com.plugable.mcommerceapp.crazypetals.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.crazypetals.ui.adapters.AddressListActionListener
import com.plugable.mcommerceapp.crazypetals.ui.adapters.DeliveryAddressAdapter
import com.plugable.mcommerceapp.crazypetals.utils.application.App
import com.plugable.mcommerceapp.crazypetals.utils.constants.IntentFlags
import com.plugable.mcommerceapp.crazypetals.utils.extension.hide
import com.plugable.mcommerceapp.crazypetals.utils.extension.setStatusBarColor
import com.plugable.mcommerceapp.crazypetals.utils.extension.setToolBarColor
import com.plugable.mcommerceapp.crazypetals.utils.extension.show
import com.plugable.mcommerceapp.crazypetals.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.crazypetals.utils.util.isNetworkAccessible
import kotlinx.android.synthetic.main.activity_delivery_address.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DeliveryAddressActivity : AppCompatActivity(), View.OnClickListener, EventListener,
    AddressListActionListener {

    private lateinit var deleteAddressApi: Call<AddressAddResponse>
    private lateinit var fetchAddressListApi: Call<AddressListResponse>
    private lateinit var getDeliveryDayApi: Call<DeliveryDayResponse>
    private var addressList = ArrayList<AddressListResponse.Data>()
    private lateinit var deliveryAddressAdapter: DeliveryAddressAdapter
    private var isAddressSelected = false
    //private var paymentMode = "cod"
    private var selectedAddress: AddressListResponse.Data? = null

    override fun onAddressSelected(address: AddressListResponse.Data) {

        if (!isNetworkAccessible()) {
            toast(R.string.oops_no_internet_connection)
            return
        }

        getDeliveryDay(address.id.toString())
        isAddressSelected = address.isSelected
        selectedAddress = address

        deliveryDateLayout.show()
        tvDeliveryAddressDate.text=address.deliveryDay

        /*  if (isAddressSelected) {
              materialButtonDeliveryAddressReviewOrder.isEnabled = true
  //        }*/
        recyclerViewDeliveryAddresses.smoothScrollToPosition(addressList.indexOf(address))
    }

    override fun onAddAddressClicked() {

        if (!isNetworkAccessible()) {
            toast(R.string.oops_no_internet_connection)
            return
        }

        startActivityForResult<AddAddressActivity>(
            AddAddressActivity.ADD_REQUEST,
            AddAddressActivity.REQUEST_CODE to AddAddressActivity.ADD_REQUEST
        )
    }

    override fun onEditAddressClicked(address: AddressListResponse.Data) {
        if (!isNetworkAccessible()) {
            toast(R.string.oops_no_internet_connection)
            return
        }

        startActivityForResult<AddAddressActivity>(
            AddAddressActivity.EDIT_REQUEST,
            AddAddressActivity.REQUEST_CODE to AddAddressActivity.EDIT_REQUEST,
            AddAddressActivity.ADDRESS to AddressRequest(
                Address = address.address,
                ApplicationUserId = address.applicationUserId.toString(),
                Locality = address.locality,
                Landmark = address.landmark,
                city = address.city,
                state = address.state,
                country= address.country,
                ID = address.id.toString(),
                MobileNumber = address.mobileNumber,
                PinCode = address.pinCode,
                PinCodeId = address.pinCodeId
            )
        )
    }

    override fun onDeleteAddressClicked(address: AddressListResponse.Data) {
        if (!isNetworkAccessible()) {
            toast(R.string.oops_no_internet_connection)
            return
        }

        with(recyclerViewDeliveryAddresses.adapter!!) {
            val index = addressList.indexOf(address)
            if (index > -1) {
                deleteAddress(address.id.toString())
                addressList.remove(address)
                notifyItemRemoved(index)
                toast("Address removed")


            } else {
                toast("Item not found")
            }

            if(addressList.isEmpty()){
                deliveryDateLayout.hide()
            }
        }

        if(addressList.isEmpty()){
            deliveryDateLayout.hide()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                AddAddressActivity.ADD_REQUEST -> {
                    fetchAddressList()
                }
                AddAddressActivity.EDIT_REQUEST -> {
                    fetchAddressList()
                }
            }
        } else {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery_address)
        initializeTheme()
        initializeViews()
        fetchAddressList()
    }

    private fun getDeliveryDay(addressId: String) {
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        getDeliveryDayApi = clientInstance.getDeliveryDay(addressId)

        getDeliveryDayApi.enqueue(object : Callback<DeliveryDayResponse> {
            override fun onFailure(call: Call<DeliveryDayResponse>, t: Throwable) {
                toast(getString(R.string.message_something_went_wrong))
            }

            override fun onResponse(
                call: Call<DeliveryDayResponse>,
                response: Response<DeliveryDayResponse>
            ) {
                if (response.body()?.statusCode.equals("10")) {
                    setColorToDeliveryDate(response.body()!!.deliveryDay)
                } else {
//                    toast(getString(R.string.message_something_went_wrong))
                }
            }

        })
    }

    override fun onResume() {
        materialButtonDeliveryAddressReviewOrder.isClickable = true
        materialButtonDeliveryAddressReviewOrder.isEnabled = true

        super.onResume()
    }

    private fun showProgress() {
        progressBar.show()
        content.hide()
    }

    private fun hideProgress() {
        progressBar.hide()
        content.show()
    }

    private fun fetchAddressList() {
        if (!isNetworkAccessible()) {
            toast(R.string.oops_no_internet_connection)
            return
        }
        showProgress()
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        fetchAddressListApi = clientInstance.getAddressList(
            SharedPreferences.getInstance(this).getStringValue(
                IntentFlags.APPLICATION_USER_ID
            )!!
        )

        fetchAddressListApi.enqueue(object : Callback<AddressListResponse> {
            override fun onFailure(call: Call<AddressListResponse>, t: Throwable) {
                hideProgress()
                toast(getString(R.string.message_something_went_wrong))
            }

            override fun onResponse(
                call: Call<AddressListResponse>,
                response: Response<AddressListResponse>
            ) {
                if (response.body()?.statusCode.equals("10")) {
                    hideProgress()
                    if (response.body()!!.data.isNotEmpty()) {
                        addressList.clear()
                        addressList.addAll(response.body()!!.data)
                        if (selectedAddress != null) {
                            val index = addressList.indexOf(selectedAddress!!)
                            if (index > -1) addressList[index].isSelected = true
                        }
                        if (addressList.size == 1) {
                            deliveryAddressAdapter.setlectFirstitem()

                            deliveryDateLayout.show()
                            tvDeliveryAddressDate.text=addressList[0].deliveryDay
                        }
                        deliveryAddressAdapter.notifyDataSetChanged()
                    } else {
                        addressList.clear()
                        deliveryAddressAdapter.notifyDataSetChanged()
                    }
                } else {
                    hideProgress()
                    toast(getString(R.string.message_something_went_wrong))
                }
            }
        })
    }

    private fun initializeViews() {
        deliveryAddressAdapter = DeliveryAddressAdapter(addressList, this, this, this)
        recyclerViewDeliveryAddresses.adapter = deliveryAddressAdapter
        imgToolbarHomeLayout.setOnClickListener(this)

        materialButtonDeliveryAddressReviewOrder.isClickable = true
        materialButtonDeliveryAddressReviewOrder.isEnabled = true

        materialButtonDeliveryAddressReviewOrder.setOnClickListener(this)

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
        setThemeToComponents()
    }

    private fun setThemeToComponents() {

        viewSeparator.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        dividerDeliveryAddressFirst.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        dividerDeliveryAddressSecond.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        /*  dividerDeliveryAddressThird.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
          radioButtonDeliveryAddressPaymentMethodCashOnDelivery.buttonTintList =
              ColorStateList.valueOf(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
          rdbOnline.buttonTintList =
              ColorStateList.valueOf(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))*/
        materialButtonDeliveryAddressReviewOrder.setBackgroundColor(
            Color.parseColor(
                ApplicationThemeUtils.SECONDARY_COLOR
            )
        )

        tvDeliveryAddressDate.setTextColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        // setColorToDeliveryDate()
        progressBar.indeterminateDrawable.setColorFilter(
            Color.BLACK,
            PorterDuff.Mode.MULTIPLY
        )
    }

    private fun setColorToDeliveryDate(deliveryDay: String) {
        val text =
            String.format(
                getString(R.string.label_your_order_will_be_delivered_on_s),
                deliveryDay
            )
        val splitText = text.split(":")
        val spannedText = SpannableString(text)
        spannedText.setSpan(
            ForegroundColorSpan(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR)),
            splitText[0].length.plus(1),
            splitText[0].length.plus(1).plus(splitText[1].length),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
//        textViewDeliveryAddressDeliverDate.setText("${splitText[0]} - $spannedText", TextView.BufferType.SPANNABLE)
        textViewDeliveryAddressDeliverDate.show()
        textViewDeliveryAddressDeliverDate.setText(spannedText, TextView.BufferType.SPANNABLE)
    }

    fun setToolBar(name: String) {
        setSupportActionBar(toolBar)
        setStatusBarColor()
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = getString(R.string.message_delivery_address)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_shape_backarrow_white)
        cp_Logo.hide()
        txtToolbarTitle.hide()
        imgToolbarHome.hide()
        setToolBarColor(imgToolbarHome, txtToolbarTitle, toolbar = toolBar)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgToolbarHomeLayout -> onBackPressed()
            R.id.materialButtonDeliveryAddressReviewOrder -> {
                materialButtonDeliveryAddressReviewOrder.isClickable = false


                if (!isNetworkAccessible()) {
                    materialButtonDeliveryAddressReviewOrder.isClickable = true

                    toast(R.string.oops_no_internet_connection)
                    return
                }
                if (!isAddressSelected || !addressList.contains(selectedAddress)) {
                    materialButtonDeliveryAddressReviewOrder.isClickable = true
                    if (addressList.size > 1) {
                        toast("Please select preferable address")

                    } else {
                        toast("Please select an address")
                    }
                    return
                }
                /*if (!radioButtonDeliveryAddressPaymentMethodCashOnDelivery.isChecked && !rdbOnline.isChecked) {
                    materialButtonDeliveryAddressReviewOrder.isClickable = true

                    toast("Please select payment method")
                    return
                }

                if (radioButtonDeliveryAddressPaymentMethodCashOnDelivery.isChecked) {
                    paymentMode = "cod"
                } else if (rdbOnline.isChecked) {
                    paymentMode = "online"
                }
*/
                materialButtonDeliveryAddressReviewOrder.isClickable = false
                val intent = Intent(this@DeliveryAddressActivity, OrderSummaryActivity::class.java)
                intent.putExtra(OrderSummaryActivity.ADDRESS, selectedAddress!!.id)
                startActivity(intent)
                //startActivity<OrderSummaryActivity>(OrderSummaryActivity.ADDRESS to selectedAddress)
            }
        }
    }

    private fun deleteAddress(addressId: String) {
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!
        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        deleteAddressApi = clientInstance.deleteAddress(
            DeleteAddressRequest((addressId))
        )

        deleteAddressApi.enqueue(object : Callback<AddressAddResponse> {
            override fun onFailure(call: Call<AddressAddResponse>, t: Throwable) {
                toast(getString(R.string.message_something_went_wrong))
            }

            override fun onResponse(
                call: Call<AddressAddResponse>,
                response: Response<AddressAddResponse>
            ) {
                if (response.body()?.statusCode.equals("10")) {
                    fetchAddressList()
                } else {
                    toast(getString(R.string.message_something_went_wrong))
                }
            }
        })
    }

    override fun onItemClickListener(position: Int) {

    }

    override fun onStop() {
        super.onStop()
        cancelTasks()
    }

    private fun cancelTasks() {
        if (::deleteAddressApi.isInitialized && deleteAddressApi != null) deleteAddressApi.cancel()
        if (::fetchAddressListApi.isInitialized && fetchAddressListApi != null) fetchAddressListApi.cancel()
        if (::getDeliveryDayApi.isInitialized && getDeliveryDayApi != null) getDeliveryDayApi.cancel()
    }

}