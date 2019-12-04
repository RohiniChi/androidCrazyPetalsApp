package com.plugable.mcommerceapp.cpmvp1.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.callbacks.EventListener
import com.plugable.mcommerceapp.cpmvp1.callbacks.OnButtonClickListener
import com.plugable.mcommerceapp.cpmvp1.callbacks.OnFavoriteListener
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.GetCartResponse
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.Products
import com.plugable.mcommerceapp.cpmvp1.utils.extension.hide
import com.plugable.mcommerceapp.cpmvp1.utils.extension.show
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.cpmvp1.utils.validation.strikeThroughText
import kotlinx.android.synthetic.main.layout_product_item.view.*
import java.util.*

/**
 * [ProductListAdapter] is an adapter used to load list of products in recycler view
 *
 * @property productList
 * @property context
 * @property itemClickListener
 * @property favoriteItemClickListener
 */
class ProductListAdapter : RecyclerView.Adapter<ProductListAdapter.MyViewHolder> {


    private var cartData: List<GetCartResponse.Data>?
    private var productList: ArrayList<Products.Data.ProductDetails>
    private var context: Context
    private var itemClickListener: EventListener
    private val onButtonClickListener: OnButtonClickListener
    private val favoriteItemClickListener: OnFavoriteListener

    constructor(
        productList: ArrayList<Products.Data.ProductDetails>,
        context: Context,
        itemClickListener: EventListener,
        onButtonClickListener: OnButtonClickListener,
        favoriteItemClickListener: OnFavoriteListener
    ) : super() {
        this.productList = productList
        this.context = context
        this.itemClickListener = itemClickListener
        this.onButtonClickListener = onButtonClickListener
        this.favoriteItemClickListener = favoriteItemClickListener
        this.cartData = SharedPreferences.getInstance(context).getAddtoCartData()?.data
    }

    override fun onBindViewHolder(viewHolder: MyViewHolder, position: Int) {

        val productItems = productList[position]
        viewHolder.itemView.textViewProductTitle.text = productItems.name
        viewHolder.itemView.textViewDiscountedPrice.text =
            String.format(
                "${ApplicationThemeUtils.CURRENCY_SYMBOL}%d",
                productItems.discountedPrice
            )
        when {
            productItems.originalPrice.equals(null) -> viewHolder.itemView.textViewActualPrice.hide()
            productItems.originalPrice == 0 -> viewHolder.itemView.textViewActualPrice.hide()
            else -> {
                viewHolder.itemView.textViewActualPrice.show()
                viewHolder.itemView.textViewActualPrice.text =
                    String.format(
                        "${ApplicationThemeUtils.CURRENCY_SYMBOL}%d",
                        productItems.originalPrice
                    )
                viewHolder.itemView.textViewActualPrice.strikeThroughText()
            }
        }

        viewHolder.itemView.textViewQuantity.text =
            String.format(productItems.quantity.plus(productItems.unit))
        viewHolder.itemView.imageViewFavorite.isSelected = productItems.isFavorite

        Glide.with(context)
            .load(productItems.image)
            .placeholder(R.drawable.ic_placeholder_category)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .apply(RequestOptions().fitCenter())
            .error(R.drawable.ic_placeholder_category)
            .into(viewHolder.itemView.imageViewProduct)

        viewHolder.itemView.imageViewFavorite.setOnClickListener {
            productItems.isFavorite = !productItems.isFavorite
            if (productItems.isFavorite) {
                it.isSelected = true
                favoriteItemClickListener.onFavoriteClicked(position, true)
            } else {
                it.isSelected = false
                favoriteItemClickListener.onFavoriteClicked(position, false)
            }
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
                R.layout.layout_product_item,
                viewGroup,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return productList.size
    }


}
