package com.plugable.mcommerceapp.cpmvp1.ui.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.callbacks.OnItemCheckListener
import com.plugable.mcommerceapp.cpmvp1.network.model.GetAppointmentTypeResponse
import kotlinx.android.synthetic.main.spinner_item.view.*

class SpinnerAdapter(
    context: Context,
    private val onItemCheckListener: OnItemCheckListener,
    private var checkedId: HashSet<Int>,
    appointmentTypeList: List<GetAppointmentTypeResponse.AppointmentTypeData>
) : ArrayAdapter<GetAppointmentTypeResponse.AppointmentTypeData>(context ,0,appointmentTypeList) {

    override fun getView(position: Int, recycledView: View?, parent: ViewGroup): View {
        return this.createView(position, recycledView, parent)
    }
    override fun getDropDownView(position: Int, recycledView: View?, parent: ViewGroup): View {
        return this.createView(position, recycledView, parent)
    }
    private fun createView(position: Int, recycledView: View?, parent: ViewGroup): View {
        val typeList = getItem(position)
        val view = recycledView ?: LayoutInflater.from(context).inflate(
            R.layout.spinner_item,
            parent,
            false
        )
        view.checkBoxSpinner.text= typeList?.name

        view.checkBoxSpinner.isChecked=typeList!!.isSelected
        view.checkBoxSpinner.buttonTintList= ColorStateList.valueOf(Color.BLACK)
        view.checkBoxSpinner.isChecked=checkedId.contains(typeList.appointmentTypeId)

        view.checkBoxSpinner.setOnClickListener {
            if (view.checkBoxSpinner.isChecked){
                onItemCheckListener.onItemCheck(typeList.appointmentTypeId,true)
            }
            else{
                onItemCheckListener.onItemCheck(typeList.appointmentTypeId,false)
            }
        }

        return view
    }

}