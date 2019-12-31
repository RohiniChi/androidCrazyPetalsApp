/*
 * Author : Chetan Patil.
 * Module : My Order
 * Version : V 1.0
 * Sprint : 13
 * Date of Development : 10/10/2019
 * Date of Modified : 10/10/2019
 * Comments : This is adapter to set all the order to RecyclerView
 * Output : List all the Orders
 */

package com.plugable.mcommerceapp.crazypetals.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.callbacks.EventListener
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.MyOrder
import com.plugable.mcommerceapp.crazypetals.utils.extension.hide
import com.plugable.mcommerceapp.crazypetals.utils.extension.show
import kotlinx.android.synthetic.main.fragment_order_detail.view.tvOrderId
import kotlinx.android.synthetic.main.row_order.view.*


/**
 * [MyOrderAdapter] is an adapter used to load list of order in recycler view
 *
 * @property context
 * @property orderArrayList
 * @property itemClickListener
 */
class MyOrderAdapter(
    private var context: Context,
    private var orderArrayList: ArrayList<MyOrder.DataItem?>,
    var itemClickListener: EventListener
    ) : RecyclerView.Adapter<MyOrderAdapter.MyViewHolder>() {

    override fun onBindViewHolder(viewHolder: MyViewHolder, position: Int) {

        viewHolder.itemView.tvOrderId.text="#"+orderArrayList[position]!!.orderNumber.toString()

        /*if(orderArrayList[position]!!.deliveredDate==null){
            viewHolder.itemView.tvOrderedOn.hide()
            viewHolder.itemView.tvDeliveredDate.hide()
            viewHolder.itemView.tvArrivingDate.show()

            viewHolder.itemView.tvArrivingDate.text="Arriving on ".plus(orderArrayList[position]!!.deliveryDay)
            viewHolder.itemView.imgIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_cancel_order))
        }else{
            viewHolder.itemView.tvOrderedOn.show()
            viewHolder.itemView.tvDeliveredDate.show()
            viewHolder.itemView.tvArrivingDate.hide()

            viewHolder.itemView.tvOrderedOn.text="Ordered on ".plus(orderArrayList[position]!!.orderedDate)
            viewHolder.itemView.tvDeliveredDate.text="Delivered on ".plus(orderArrayList[position]!!.deliveredDate)
            viewHolder.itemView.imgIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_order_delivered))
        }*/

        if (orderArrayList[position]!!.deliveryStatus==1 || orderArrayList[position]!!.deliveryStatus==2){
            viewHolder.itemView.tvOrderedOn.hide()
            viewHolder.itemView.tvDeliveredDate.hide()
            viewHolder.itemView.tvArrivingDate.show()

            viewHolder.itemView.tvArrivingDate.text="Arriving on ".plus(orderArrayList[position]!!.deliveryDay)
            viewHolder.itemView.imgIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_order_arriving))
        }
        else if (orderArrayList[position]!!.deliveryStatus== 4 && orderArrayList[position]!!.deliveredDate != null){
            viewHolder.itemView.tvOrderedOn.show()
            viewHolder.itemView.tvDeliveredDate.show()
            viewHolder.itemView.tvArrivingDate.hide()
            viewHolder.itemView.tvOrderedOn.text="Ordered on ".plus(orderArrayList[position]!!.orderedDate)
            viewHolder.itemView.tvDeliveredDate.text="Delivered on ".plus(orderArrayList[position]!!.deliveredDate)
            viewHolder.itemView.imgIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_order_delivered))
        }
        else if (orderArrayList[position]!!.deliveryStatus==5){
            viewHolder.itemView.tvOrderedOn.hide()
            viewHolder.itemView.tvDeliveredDate.hide()
            viewHolder.itemView.tvArrivingDate.show()
            viewHolder.itemView.tvArrivingDate.text= "Your order has been cancelled"
            viewHolder.itemView.imgIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_cancel_order))

        }

    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        override fun onClick(view: View?) {
            val position = adapterPosition
            itemClickListener.onItemClickListener(position)
        }
        init {
            itemView.setOnClickListener(this)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int):MyViewHolder {
            return MyViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.row_order,
                    viewGroup,
                    false
                )
            )
    }

    override fun getItemCount(): Int {
        return orderArrayList.size
    }
}
