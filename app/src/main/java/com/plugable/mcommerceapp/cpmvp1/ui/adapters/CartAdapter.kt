package com.plugable.mcommerceapp.cpmvp1.ui.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.callbacks.EventListener
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.GetCartResponse
import com.plugable.mcommerceapp.cpmvp1.utils.extension.hide
import com.plugable.mcommerceapp.cpmvp1.utils.extension.show
import com.plugable.mcommerceapp.cpmvp1.utils.validation.strikeThroughText
import com.travijuu.numberpicker.library.Enums.ActionEnum
import kotlinx.android.synthetic.main.layout_cart_product_item.view.*
import org.jetbrains.anko.toast
import java.util.*

/**
 * [CartAdapter] is an adapter used to load list of products in recycler view
 *
 * @property productList
 * @property context
 * @property itemClickListener
 * @property cartItemActionListener
 */
class CartAdapter(
    private var productList: ArrayList<GetCartResponse.Data>,
    var context: Context,
    var itemClickListener: EventListener,
    private val cartItemActionListener: CartItemActionListener
) : androidx.recyclerview.widget.RecyclerView.Adapter<CartAdapter.MyViewHolder>() {
    override fun onBindViewHolder(viewHolder: MyViewHolder, position: Int) {

        val productItems = productList[position]
        with(viewHolder.itemView) {
            textViewProductTitle.text = productItems.productName
            textViewDiscountedPrice.text =
                String.format(
                    "${ApplicationThemeUtils.CURRENCY_SYMBOL}%d",
                    productItems.discountedPrice
                )
            if (productItems.originalPrice == 0 || productItems.originalPrice == null) {
                viewHolder.itemView.textViewActualPrice.visibility = View.INVISIBLE
            } else {
                viewHolder.itemView.textViewActualPrice.text =
                    String.format(
                        "${ApplicationThemeUtils.CURRENCY_SYMBOL}%d",
                        productItems.originalPrice
                    )
                viewHolder.itemView.textViewActualPrice.strikeThroughText()
            }

            imageViewDeleteItem.setOnClickListener {
                cartItemActionListener.onItemRemoved(productItems)
            }


            if (productItems.colorCode.isNullOrEmpty()) {
                tvColorLabel.hide()
                textViewColor.hide()
            } else {
                tvColorLabel.show()
                textViewColor.show()
                textViewColor.setBackgroundResource(R.drawable.circular_shape_view)
                val drawable = textViewColor.background.current as GradientDrawable
                drawable.setColor(Color.parseColor(productItems.colorCode))
            }

            if (productItems.colorCode.isNullOrEmpty()) {
                tvSize.hide()
            } else {
                tvSize.show()
                tvSize.text = "Size: ".plus(productItems.size)
            }
            tvSize.text = "Size: ".plus(productItems.size)
            Glide.with(context)
                .load(productItems.productImageURL)
                .placeholder(R.drawable.ic_placeholder_category)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fallback(R.drawable.ic_placeholder_category).centerInside()
                .apply(RequestOptions().centerInside())
                .error(R.drawable.ic_placeholder_category)
                .into(imageViewProduct)

//            viewHolder.viewColor.setBackgroundResource(R.drawable.circular_shape_view)
//            val drawableColor = viewHolder.viewColor.background.current as GradientDrawable
//            drawableColor.setColor(Color.parseColor(productItems.colorCode))

            val drawableSize = GradientDrawable()
            drawableSize.setStroke(3, Color.BLACK)
            drawableSize.shape = GradientDrawable.OVAL
            drawableSize.setColor(Color.WHITE)
//            viewHolder.sizeLayout.backgroundDrawable = drawableSize
//            viewHolder.viewSize.text = productItems.size

            val increment =
                numberPickerCartItemCount.findViewById<AppCompatButton>(R.id.increment)
            val decrement =
                numberPickerCartItemCount.findViewById<AppCompatButton>(R.id.decrement)
            val display = numberPickerCartItemCount.findViewById<EditText>(R.id.display)
//            numberPickerCartItemCount.max=productItems.availableQuantity
            numberPickerCartItemCount.value = productItems.quantity

            if (productItems.availableQuantity > 10) {
                numberPickerCartItemCount.max = 10
            } else if (productItems.isAvailable && productItems.availableQuantity == 0) {
                numberPickerCartItemCount.max = 10
            } else {
                numberPickerCartItemCount.max = productItems.availableQuantity
            }
            numberPickerCartItemCount.setValueChangedListener { value, actionEnum ->
                if (actionEnum == ActionEnum.INCREMENT) {
                    cartItemActionListener.onIncremented(productItems)
                } else {
                    cartItemActionListener.onDecremented(productItems)
                }
            }

//            numberPickerCartItemCount.setOnClickListener {

            numberPickerCartItemCount.setLimitExceededListener { limit, exceededValue ->
                if (exceededValue > limit) {
                    context.toast("Sorry, can't add more than this items")
                } else {
                    context.toast("Please remove your item using delete")
                }
            }
//            }

            if (productItems.isAvailable) {
                viewHolder.itemView.tvOutOfStock.hide()
                increment.isEnabled = true
                increment.setBackgroundColor(
                    Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR)
                )
                decrement.setBackgroundColor(
                    Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR)
                )
                display.setTextColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))

                decrement.isEnabled = true
                numberPickerCartItemCount.background = ContextCompat.getDrawable(
                    viewHolder.itemView.context,
                    R.drawable.button_background_rounded_corner_layout
                )
            } else {
                viewHolder.itemView.tvOutOfStock.show()
                increment.setBackgroundColor(
                    ContextCompat.getColor(
                        viewHolder.itemView.context,
                        R.color.number_picker_disabled_color
                    )
                )
                decrement.setBackgroundColor(
                    ContextCompat.getColor(
                        viewHolder.itemView.context,
                        R.color.number_picker_disabled_color
                    )
                )
                display.setTextColor(
                    ContextCompat.getColor(
                        viewHolder.itemView.context,
                        R.color.number_picker_disabled_color
                    )
                )
                numberPickerCartItemCount.background = ContextCompat.getDrawable(
                    viewHolder.itemView.context,
                    R.drawable.button_background_rounded_corner_layout_disabled
                )
                increment.isEnabled = false
                decrement.isEnabled = false
            }
        }
    }

    inner class MyViewHolder(itemView: View) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        /*val viewColor = itemView.findViewById(R.id.valueColor) as TextView
        val viewSize = itemView.findViewById(R.id.valueSize) as TextView*/


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
                R.layout.layout_cart_product_item,
                viewGroup,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return productList.size
    }
}

interface CartItemActionListener {
    fun onIncremented(getCartResponseData: GetCartResponse.Data)
    fun onDecremented(getCartResponseData: GetCartResponse.Data)
    fun onItemRemoved(getCartResponseData: GetCartResponse.Data)
}