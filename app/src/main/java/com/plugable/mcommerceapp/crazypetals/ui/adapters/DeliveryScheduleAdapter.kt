package com.plugable.mcommerceapp.crazypetals.ui.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.callbacks.EventListener
import com.plugable.mcommerceapp.crazypetals.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.DeliveryChartResponse
import kotlinx.android.synthetic.main.layout_delivery_schedule.view.*
import java.util.*

/**
 * [DeliveryScheduleAdapter] is an adapter used to load list of delivery schedules in recycler view
 *
 * @property deliverySchedulesList
 * @property context
 * @property itemClickListener
 */
class DeliveryScheduleAdapter(
    private var deliverySchedulesList: ArrayList<DeliveryChartResponse.Data>,
    var context: Context,
    var itemClickListener: EventListener
) : androidx.recyclerview.widget.RecyclerView.Adapter<DeliveryScheduleAdapter.MyViewHolder>() {
    override fun onBindViewHolder(viewHolder: MyViewHolder, position: Int) {

        val productItems = deliverySchedulesList[position]
        viewHolder.itemView.textViewDeliveryScheduleLocality.text = productItems.area
        viewHolder.itemView.textViewDeliverySchedulePinCode.text = productItems.pinCode
        viewHolder.itemView.textViewDeliveryScheduleDay.text = productItems.day
        viewHolder.itemView.textViewDeliveryScheduleTimings.text = productItems.time
        with(viewHolder.itemView) {
            dividerDeliveryScheduleItem.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        }
    }

    inner class MyViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        override fun onClick(v: View?) {
            val position = adapterPosition
            itemClickListener.onItemClickListener(position)
        }

        init {
            itemView.setOnClickListener(this)
        }

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.layout_delivery_schedule,
                viewGroup,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return deliverySchedulesList.size
    }
}