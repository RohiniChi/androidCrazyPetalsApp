package com.plugable.mcommerceapp.crazypetals.ui.activities

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.callbacks.OnButtonCheckedListner
import com.plugable.mcommerceapp.crazypetals.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.crazypetals.utils.sharedpreferences.SharedPreferences
import kotlinx.android.synthetic.main.custom_dialog_layout.*

class CustomDialogApptType(
    var activity: Activity,
    private val onButtonCheckedListner: OnButtonCheckedListner,
    internal var adapter: RecyclerView.Adapter<*>
) : Dialog(activity), View.OnClickListener {

    private var mLayoutManager: RecyclerView.LayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.custom_dialog_layout)


        mLayoutManager = LinearLayoutManager(activity)
        recyclerViewAppointmentType?.layoutManager = mLayoutManager
        recyclerViewAppointmentType.adapter = adapter

        initializeTheme()
        button_ok.setOnClickListener(this)
    }

    private fun initializeTheme() {
        val configDetail = SharedPreferences.getInstance(activity).themeDataPreference
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
        button_ok.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_ok -> {
                onButtonCheckedListner.onButtonClicked()
                dismiss()
            }
        }
        dismiss()
    }

}