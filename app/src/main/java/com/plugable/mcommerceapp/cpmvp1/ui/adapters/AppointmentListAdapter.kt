package com.plugable.mcommerceapp.cpmvp1.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.callbacks.EventListener
import com.plugable.mcommerceapp.cpmvp1.network.model.AppointmentData
import com.plugable.mcommerceapp.cpmvp1.utils.constants.IntentFlags
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences
import kotlinx.android.synthetic.main.row_appointment_list.view.*

class AppointmentListAdapter(
    private var appointmentList : ArrayList<AppointmentData>,
    var context: Context,
    var itemClickListener: EventListener
) : RecyclerView.Adapter<AppointmentListAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.row_appointment_list,
                viewGroup,
                false
            )
        )
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        override fun onClick(v: View?) {
            val position = adapterPosition
            itemClickListener.onItemClickListener(position)
        }
        init {
            itemView.setOnClickListener(this)
        }

    }

    override fun getItemCount(): Int {
        return appointmentList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.itemView.tvAppointmentNo.text="#"+appointmentList[position].appointmentNumber
        holder.itemView.tvAppointmentDate.text=appointmentList[position].appointmentDate
        holder.itemView.tvAppointmentTime.text=appointmentList[position].fromTime
        holder.itemView.tvAppointmentStatus.text="Status : "+appointmentList[position].status
    }
}