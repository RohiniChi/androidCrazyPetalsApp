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
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.callbacks.EventListener
import com.plugable.mcommerceapp.crazypetals.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.OrderDetailResponse
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.Products
import com.plugable.mcommerceapp.crazypetals.ui.activities.ProductDetailActivity
import com.plugable.mcommerceapp.crazypetals.utils.constants.IntentFlags
import kotlinx.android.synthetic.main.row_order_product.view.*


/**
 * [OrderProductAdapter] is an adapter used to load list of order in recycler view
 *
 * @property context
 * @property orderArrayList
 * @property itemClickListener
 */
class OrderProductAdapter(
    private var context: Context,
    private var productArrayList: ArrayList<OrderDetailResponse.Data.ProductListItem>,
    var itemClickListener: EventListener
) : RecyclerView.Adapter<OrderProductAdapter.MyViewHolder>() {

    override fun onBindViewHolder(viewHolder: MyViewHolder, position: Int) {

        viewHolder.itemView.tvProductName.text = productArrayList[position].productName

        Glide.with(context)
            .load(productArrayList[position].productImageURL)
            .placeholder(R.drawable.ic_placeholder_category)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .fallback(R.drawable.ic_placeholder_category).centerInside()
            .apply(RequestOptions().centerInside())
            .error(R.drawable.ic_placeholder_category)
            .into(viewHolder.itemView.imgIcon)

        viewHolder.itemView.btnBuy.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))

        viewHolder.itemView.btnBuy.setOnClickListener {

            val product=Products.Data.ProductDetails(productArrayList[position].productName!!,
                productArrayList[position].productId!!
            )

            product.image=productArrayList[position].productImageURL!!


            val intent = Intent(context, ProductDetailActivity::class.java)
            intent.putExtra(IntentFlags.PRODUCT_MODEL, product)
            intent.putExtra(IntentFlags.CATEGORY_ID, productArrayList[position].categoryId)
            context.startActivity(intent)
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

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.row_order_product,
                viewGroup,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return productArrayList.size
    }
}
