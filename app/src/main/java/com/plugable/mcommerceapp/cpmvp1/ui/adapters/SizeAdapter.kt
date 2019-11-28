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
import org.jetbrains.anko.backgroundDrawable

class SizeAdapter(
    private val sizeList: List<ProductDetail.Data.Size>,
    private val context: Context
) :
    RecyclerView.Adapter<SizeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.layout_size_options, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.tag = sizeList[position]
        val drawable = GradientDrawable()
        drawable.setStroke(3, Color.BLACK)
        drawable.shape = GradientDrawable.OVAL
        drawable.setColor(Color.WHITE)
        holder.textSize.backgroundDrawable = drawable
        holder.textSize.text = sizeList[position].name


        if (sizeList[position].id==ProductDetailActivity.sizeSelectedId) {
            holder.sizeBorder.show()
        } else {
            holder.sizeBorder.hide()
        }



        holder.textSize.setOnClickListener {

            ProductDetailActivity.sizeSelectedId=sizeList[position].id
            notifyDataSetChanged()

        }
    }

    override fun getItemCount(): Int {
        return sizeList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textSize = itemView.findViewById(R.id.textSize) as TextView
        var sizeBorder = itemView.findViewById(R.id.sizeSelectionBorder) as ConstraintLayout
    }

}