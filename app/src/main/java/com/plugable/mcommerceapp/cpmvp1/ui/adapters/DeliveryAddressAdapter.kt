package com.plugable.mcommerceapp.cpmvp1.ui.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.callbacks.EventListener
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.AddressListResponse
import com.plugable.mcommerceapp.cpmvp1.utils.extension.hide
import com.plugable.mcommerceapp.cpmvp1.utils.extension.show
import com.plugable.mcommerceapp.cpmvp1.utils.util.capitalizeAll
import kotlinx.android.synthetic.main.layout_delivery_address.view.*
import org.jetbrains.anko.textColor
import java.util.*

/**
 * [DeliveryAddressAdapter] is an adapter used to load list of addresses in recycler view
 *
 * @property addressList
 * @property context
 * @property itemClickListener
 * @property addressListActionListener
 */
class DeliveryAddressAdapter(
    private var addressList: ArrayList<AddressListResponse.Data>,
    var context: Context,
    var itemClickListener: EventListener,
    private val addressListActionListener: AddressListActionListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {

        if (getItemViewType(position) == Type.ITEM.ordinal) {
            viewHolder.itemView.btnAddDeliveryAddress.hide()
            viewHolder.itemView.item.show()
            val address = addressList[position]
            with((viewHolder as DeliveryAddressViewHolder).itemView) {
                textViewDeliveryAddressPersonName.text = address.name.capitalizeAll()
                textViewDeliveryAddressCompleteAddress.text =
                    String.format(
                        "%s, %s, %s, %s, %s, %s- %s",
                        address.address,
                        address.landmark,
                        address.locality,
                        address.city,
                        address.state,
                        address.country,
                        address.pinCode
                    ).capitalizeAll()
                textViewDeliveryAddressContactNumber.text =
                    String.format("Contact No. %s", address.mobileNumber)
                buttonDeliveryAddressEdit.textColor =
                    Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR)
                buttonDeliveryAddressEdit.strokeColor =
                    ColorStateList.valueOf(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))

                viewHolder.itemView.item.background =
                    if (address.isSelected) context.getDrawable(R.drawable.button_background_rounded_corner_layout)
                    else context.getDrawable(
                        R.drawable.rounded_background_unselected
                    )

                imageViewDeliveryAddressDelete.setOnClickListener {
                    addressListActionListener.onDeleteAddressClicked(address)
                }

                buttonDeliveryAddressEdit.setOnClickListener {
                    addressListActionListener.onEditAddressClicked(address)
                }

                viewHolder.itemView.setOnClickListener {
                    selectAddress(address)
                    addressListActionListener.onAddressSelected(address)
                }

            }
        } else {
            viewHolder.itemView.btnAddDeliveryAddress.show()
            viewHolder.itemView.item.hide()
            viewHolder.itemView.btnAddDeliveryAddress.setOnClickListener {
                viewHolder.itemView.btnAddDeliveryAddress.isEnabled = false
                addressListActionListener.onAddAddressClicked()
                viewHolder.itemView.btnAddDeliveryAddress.postDelayed({
                    viewHolder.itemView.btnAddDeliveryAddress.isEnabled = true
                }, 300)
            }
        }
    }

    private fun selectAddress(address: AddressListResponse.Data) {
        addressList.forEach {
            it.isSelected = address.id == it.id
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (addressList.size == 0 || addressList.size == position) Type.FOOTER.ordinal else Type.ITEM.ordinal
    }

    enum class Type {
        ITEM,
        FOOTER
    }

    inner class DeliveryAddressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        override fun onClick(v: View?) {
            val position = adapterPosition
            itemClickListener.onItemClickListener(position)
        }

        init {
            itemView.setOnClickListener(this)
        }

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): RecyclerView.ViewHolder {
        return DeliveryAddressViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.layout_delivery_address,
                viewGroup,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return addressList.size + 1
    }

    fun setlectFirstitem() {
        selectAddress(addressList[0])
        addressListActionListener.onAddressSelected(addressList[0])
        notifyDataSetChanged()
    }
}

interface AddressListActionListener {
    fun onAddAddressClicked()
    fun onEditAddressClicked(address: AddressListResponse.Data)
    fun onDeleteAddressClicked(address: AddressListResponse.Data)
    fun onAddressSelected(address: AddressListResponse.Data)
}