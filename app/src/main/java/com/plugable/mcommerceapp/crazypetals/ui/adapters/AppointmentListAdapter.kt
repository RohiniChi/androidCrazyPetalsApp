package com.plugable.mcommerceapp.crazypetals.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.callbacks.EventListener
import com.plugable.mcommerceapp.crazypetals.network.model.AppointmentData
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
        holder.itemView.tvAppointmentTime.text=appointmentList[position].appointmentTime
        holder.itemView.tvAppointmentStatus.text="Status : "+appointmentList[position].status
    }
}