package com.plugable.mcommerceapp.crazypetals.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.callbacks.EventListener
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.OrderItems
import kotlinx.android.synthetic.main.layout_order_summary_order_details.view.*
import java.util.*

/**
 * [OrdersAdapter] is an adapter used to load list of orders in recycler view
 *
 * @property orderedItems
 * @property context
 * @property itemClickListener
 */
class OrdersAdapter(
    private var orderedItems: ArrayList<OrderItems.Data.Items>,
    var context: Context,
    var itemClickListener: EventListener
) : androidx.recyclerview.widget.RecyclerView.Adapter<OrdersAdapter.MyViewHolder>() {
    override fun onBindViewHolder(viewHolder: MyViewHolder, position: Int) {

        val orderedItem = orderedItems[position]


        with(viewHolder.itemView) {
            tvProductName.text = String.format("%s", orderedItem.itemName)
            tvProductQuantity.text = String.format("%s", orderedItem.itemQuantity)
            tvProductPrice.text = String.format("₹%s", orderedItem.itemPrice)
        }
    }

    inner class MyViewHolder(itemView: View) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView),
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
                R.layout.layout_order_summary_order_details,
                viewGroup,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return orderedItems.size
    }
}