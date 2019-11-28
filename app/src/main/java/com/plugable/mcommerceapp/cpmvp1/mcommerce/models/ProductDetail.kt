package com.plugable.mcommerceapp.cpmvp1.mcommerce.models

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
        val colorList: List<Color>,
//        val deliveryTime: String?=null,
        val description: String,
//        val discountPercent: Int,
        val discountedPrice: Int,
//        val height: String?=null,
        val id: Int,
        val images: List<Image>,
//        val includedAccesories: String?=null,
        val isAvailable: Boolean,
     /*   val length: String?=null,
        val materialType: String?=null,*/
        val name: String,
        val originalPrice: Int,
//        val precautions: String?=null,
        val sizeList: List<Size>,
//        val weight: String?=null,
        val brand: String?=null,
        val unitName: String?=null,
        val quantity: String?=null
//        val width: String?=null
    ) {
        data class Color(
            val colorName: String,
            val hashCode: String,
            val id: Int,
            var isChecked: Boolean
        )

        data class Image(
            val image: String
        ) : Parcelable {
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


