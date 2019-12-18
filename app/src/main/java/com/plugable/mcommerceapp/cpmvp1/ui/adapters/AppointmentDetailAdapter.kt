package com.plugable.mcommerceapp.cpmvp1.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.plugable.mcommerceapp.cpmvp1.R


class AppointmentDetailAdapter(
    private val context: Context,
    private val appointmentTypeList: List<String>
) : RecyclerView.Adapter<AppointmentDetailAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.row_appointment_detail, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return appointmentTypeList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.tag=appointmentTypeList[position]
     holder.appointmentType.text=(position+1).toString().plus(")").plus(appointmentTypeList[position])

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var appointmentType=itemView.findViewById(R.id.tvAppointmentType) as TextView
    }

}