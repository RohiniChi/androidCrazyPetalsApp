package com.plugable.mcommerceapp.cpmvp1.ui.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.ProductDetail
import com.plugable.mcommerceapp.cpmvp1.ui.activities.ProductDetailActivity
import com.plugable.mcommerceapp.cpmvp1.utils.extension.hide
import com.plugable.mcommerceapp.cpmvp1.utils.extension.show

class ColorAdapterMVP1(
    private val colorList: List<ProductDetail.Data.Color>,
    private val context: Context
) :
    RecyclerView.Adapter<ColorAdapterMVP1.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.layout_color_options, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.tag = colorList[position]
        val colorView = colorList[position]
        holder.viewColor.setBackgroundResource(R.drawable.circular_shape_view)
        val drawable = holder.viewColor.background.current as GradientDrawable
        drawable.setColor(Color.parseColor(colorView.hashCode))

        if (colorList[position].id==ProductDetailActivity.colorSelectedId) {
            holder.colorBorder.show()
        } else {
            holder.colorBorder.hide()
        }



        holder.viewColor.setOnClickListener {

         ProductDetailActivity.colorSelectedId=colorList[position].id
            notifyDataSetChanged()

        }
    }


    override fun getItemCount(): Int {
        return colorList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var viewColor = itemView.findViewById(R.id.textViewColor) as TextView
        var colorBorder = itemView.findViewById(R.id.colorSelectionBorder) as ConstraintLayout

    }

}