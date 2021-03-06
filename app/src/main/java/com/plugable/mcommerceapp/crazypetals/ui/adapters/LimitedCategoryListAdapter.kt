package com.plugable.mcommerceapp.crazypetals.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.callbacks.EventListener
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.Categories
import kotlinx.android.synthetic.main.layout_product_grid_view_item.view.*

/**
 * [LimitedCategoryListAdapter] is an adapter used to load grid of categories in limited count to 5
 *
 * @property categoryList
 * @property context
 * @property itemClickListener
 */
class LimitedCategoryListAdapter(
    private var categoryList: ArrayList<Categories.Data.Category>,
    var context: Context,
    var itemClickListener: EventListener
) : RecyclerView.Adapter<LimitedCategoryListAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.layout_product_grid_view_item,
                viewGroup,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val categoryItems = categoryList[position]
        holder.itemView.txtProductCategoryNames.text = categoryItems.name

        Glide.with(context)
            .load(categoryItems.image)
            .placeholder(R.drawable.ic_placeholder_category)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .apply(RequestOptions().fitCenter())
            .error(R.drawable.ic_placeholder_category)
            .into(holder.itemView.imgCategory)

    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        
        var mLastClickTime = System.currentTimeMillis()
        val click_time_interval = 200

        override fun onClick(view: View?) {
           /* val now = System.currentTimeMillis()
            if (now - mLastClickTime < click_time_interval) {
                return
            }
            mLastClickTime = now*/
            val position = adapterPosition
            itemClickListener.onItemClickListener(position)
        }

        init {
            itemView.setOnClickListener(this)
        }

    }
}