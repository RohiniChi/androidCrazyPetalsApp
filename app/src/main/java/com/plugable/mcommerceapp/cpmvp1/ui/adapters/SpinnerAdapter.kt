package com.plugable.mcommerceapp.cpmvp1.ui.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.callbacks.OnButtonClickListener
import com.plugable.mcommerceapp.cpmvp1.callbacks.OnItemCheckListener
import com.plugable.mcommerceapp.cpmvp1.callbacks.OnListChekedListner
import com.plugable.mcommerceapp.cpmvp1.network.model.GetAppointmentTypeResponse

class SpinnerAdapter(
    private val context: Context,
    private val onItemCheckListener: OnListChekedListner,
    private var checkedId: HashSet<Int>,
    private val appointmentTypeList: ArrayList<GetAppointmentTypeResponse.AppointmentTypeData>
) : RecyclerView.Adapter<SpinnerAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpinnerAdapter.MyViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return appointmentTypeList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.itemView.tag = appointmentTypeList[position]
        holder.checklistSpinner.text = appointmentTypeList[position].name
        holder.checklistSpinner.isChecked=appointmentTypeList[position].isSelected
        holder.checklistSpinner.buttonTintList= ColorStateList.valueOf(Color.BLACK)
        holder.checklistSpinner.isChecked=checkedId.contains(appointmentTypeList[position].appointmentTypeId)

        holder.checklistSpinner.setOnClickListener {
            if (holder.checklistSpinner.isChecked){
                onItemCheckListener.onListCheck(appointmentTypeList[position].appointmentTypeId,appointmentTypeList[position].name,true)
            }
            else{
                onItemCheckListener.onListCheck(appointmentTypeList[position].appointmentTypeId,appointmentTypeList[position].name,false)
            }
        }

    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var checklistSpinner = itemView.findViewById(R.id.checkBoxSpinner) as CheckBox

    }

}