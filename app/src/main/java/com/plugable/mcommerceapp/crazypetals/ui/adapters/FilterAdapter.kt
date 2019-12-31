package com.plugable.mcommerceapp.crazypetals.ui.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.callbacks.OnItemCheckListener
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.GetFilters

class FilterAdapter(
    private val filterList: ArrayList<GetFilters.Data.Filter>,
    private val context: Context,
    private val onItemCheckListener: OnItemCheckListener,
    private var checkedId: HashSet<Int>
) : RecyclerView.Adapter<FilterAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.recycler_filter_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return filterList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.tag = filterList[position]
        holder.checklist.text = filterList[position].name
        holder.checklist.isChecked = filterList[position].isSelected
        holder.checklist.buttonTintList = ColorStateList.valueOf(Color.BLACK)


        holder.checklist.isChecked = checkedId.contains(filterList[position].id)


        /*Toast.makeText(context,"Clicked ${filterList[position]}",Toast.LENGTH_SHORT).show()
        filterList[position].isSelected = !filterList[position].isSelected*/
        holder.checklist.setOnClickListener {
            if (holder.checklist.isChecked) {
                onItemCheckListener.onItemCheck(filterList[position].id, true)
            } else {
                onItemCheckListener.onItemCheck(filterList[position].id, false)

            }
        }


    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var checklist = itemView.findViewById(R.id.checkbox_filter) as CheckBox

    }
}