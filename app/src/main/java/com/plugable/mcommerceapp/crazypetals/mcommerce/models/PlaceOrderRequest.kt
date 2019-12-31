package com.plugable.mcommerceapp.crazypetals.mcommerce.models

import android.os.Parcel
import android.os.Parcelable

data class PlaceOrderRequest(
    val AddressId: String,
    val ApplicationUserId: String,
    val Comments: String,
    val DeliveryCharges: String,
    val MRP: String,
    val OrderDetails: List<OrderDetail>,
    val ProductsDiscount: String,
    val SubTotal: String
) {

    data class OrderDetail(
        val ColorsId: String?,
        val ProductDetailId: String,
        val Quantity: String,
        val SizeId: String?,
        val TotalPrice: String
    )
}

data class PlaceOrderResponse(
    val deliveryDay: String,
    val message: String,
    val orderId: Int,
    val orderNumber: String,
    val statusCode: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(deliveryDay)
        parcel.writeString(message)
        parcel.writeInt(orderId)
        parcel.writeString(orderNumber)
        parcel.writeString(statusCode)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PlaceOrderResponse> {
        override fun createFromParcel(parcel: Parcel): PlaceOrderResponse {
            return PlaceOrderResponse(parcel)
        }

        override fun newArray(size: Int): Array<PlaceOrderResponse?> {
            return arrayOfNulls(size)
        }
    }
}