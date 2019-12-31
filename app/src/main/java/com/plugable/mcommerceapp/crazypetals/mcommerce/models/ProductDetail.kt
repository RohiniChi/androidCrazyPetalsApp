package com.plugable.mcommerceapp.crazypetals.mcommerce.models

import android.os.Parcel
import android.os.Parcelable


/**
 * [ProductDetail] is a model class for fetching product details using product id
 *
 * @property data
 * @property message
 * @property statusCode
 */


data class ProductDetail(
    val `data`: Data,
    val message: String,
    val statusCode: String
) {

    data class Data(
        var colorList: List<Color>? = null,
        var deliveryTime: String? = null,
        var description: String? = null,
        var discountPercent: Int = 0,
        var discountedPrice: Int = 0,
        var height: String? = null,
        var id: Int = 0,
        var images: List<Image>? = null,
        var includedAccesories: String? = null,
        var isAvailable: Boolean? = false,
        var length: String? = null,
        var materialType: String? = null,
        var name: String? = null,
        var originalPrice: Int = 0,
        var precautions: String? = null,
        var sizeList: List<Size>? = null,
        var weight: String? = null,
        var brand: String?=null,
        var unitName: String?=null,
        var quantity: String?=null,
        var width: String? = null
    ) {
        data class Color(
            val colorName: String,
            val hashCode: String,
            val id: Int,
            var isChecked:Boolean
        )

        data class Image(
            val image: String
        ):Parcelable {
            constructor(parcel: Parcel) : this(parcel.readString()!!)

            override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeString(image)
            }

            override fun describeContents(): Int {
                return 0
            }

            companion object CREATOR : Parcelable.Creator<Image> {
                override fun createFromParcel(parcel: Parcel): Image {
                    return Image(parcel)
                }

                override fun newArray(size: Int): Array<Image?> {
                    return arrayOfNulls(size)
                }
            }
        }

        data class Size(
            val id: Int,
            val name: String
        )

    }
}

