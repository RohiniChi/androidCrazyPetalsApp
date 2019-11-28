package com.plugable.mcommerceapp.cpmvp1.mcommerce.models

import android.os.Parcel
import android.os.Parcelable

data class OrderDetailResponse(
    val data: Data? = null,
    val message: String? = null,
    val productCount: Int? = null,
    val statusCode: String? = null
) {
    data class Data(
        val orderDetails: OrderDetails? = null,
        val productList: List<ProductListItem?>
    ) {
        data class OrderDetails(
            val deliveredDate: String? = null,
            val orderNumber: String? = null,
            val address: String? = null,
            val orderedDate: String? = null,
            val deliveryDay: String? = null,
            val orderTotal: Double? = null,
            val deliveryStatus:Int
        ): Parcelable {
            constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readValue(Double::class.java.classLoader) as? Double,
                parcel.readInt()
                ) {
            }

            override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeString(deliveredDate)
                parcel.writeString(orderNumber)
                parcel.writeString(address)
                parcel.writeString(orderedDate)
                parcel.writeString(deliveryDay)
                parcel.writeValue(orderTotal)
            }

            override fun describeContents(): Int {
                return 0
            }

            companion object CREATOR : Parcelable.Creator<OrderDetails> {
                override fun createFromParcel(parcel: Parcel): OrderDetails {
                    return OrderDetails(parcel)
                }

                override fun newArray(size: Int): Array<OrderDetails?> {
                    return arrayOfNulls(size)
                }
            }
        }

        data class ProductListItem(
            val productImageURL: String? = null,
            val quantity: Int? = null,
            val productId: Int? = null,
            val size: String? = null,
            val originalPrice: Double? = null,
            val discountedPrice: Double? = null,
            val colorCode: String? = null,
            val id: Int? = null,
            val shortDescription: String? = null,
            val productName: String? = null,
            val categoryId: Int? = null
        ): Parcelable {
            constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readValue(Int::class.java.classLoader) as? Int,
                parcel.readValue(Int::class.java.classLoader) as? Int,
                parcel.readString(),
                parcel.readValue(Double::class.java.classLoader) as? Double,
                parcel.readValue(Double::class.java.classLoader) as? Double,
                parcel.readString(),
                parcel.readValue(Int::class.java.classLoader) as? Int,
                parcel.readString(),
                parcel.readString(),
                parcel.readValue(Int::class.java.classLoader) as? Int
            ) {
            }

            override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeString(productImageURL)
                parcel.writeValue(quantity)
                parcel.writeValue(productId)
                parcel.writeString(size)
                parcel.writeValue(originalPrice)
                parcel.writeValue(discountedPrice)
                parcel.writeString(colorCode)
                parcel.writeValue(id)
                parcel.writeString(shortDescription)
                parcel.writeString(productName)
                parcel.writeValue(categoryId)
            }

            override fun describeContents(): Int {
                return 0
            }

            companion object CREATOR : Parcelable.Creator<ProductListItem> {
                override fun createFromParcel(parcel: Parcel): ProductListItem {
                    return ProductListItem(parcel)
                }

                override fun newArray(size: Int): Array<ProductListItem?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
}
