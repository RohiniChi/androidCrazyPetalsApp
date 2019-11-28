package com.plugable.mcommerceapp.cpmvp1.mcommerce.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

/**
 * [Products] is a model class for showing list of products
 *
 * @property data
 * @property message
 * @property statusCode
 */
data class Products(
    val data: Data,
    val message: String,
    val statusCode: String
) {
    data class Data(
        val productList: List<ProductDetails>,
        val totalCount: Int
    ) {
        @Parcelize
        @Entity(tableName = "productWishList")
        data class ProductDetails(
//            val categoryName: Any?,
            @ColumnInfo(name = "discountPercentage")
            val discountPercentage: Int,
            @ColumnInfo(name = "discountedPrice")
            var discountedPrice: Int,
            /*  @ColumnInfo(name = "filters")
              val filters: String,*/
            @PrimaryKey
            var id: Int,
            @ColumnInfo(name = "imageUrl")
            var image: String,
            /*  @ColumnInfo(name = "isExclusive")
              val isExclusive: Boolean,*/
            @ColumnInfo(name = "productName")
            var name: String,
            @ColumnInfo(name = "originalPrice")
            var originalPrice: Int,
            @ColumnInfo(name = "isFavorite")
            var isFavorite: Boolean,
            @ColumnInfo(name = "brand")
            val brand: String?,
            @ColumnInfo(name = "discount")
            val discount: Int,
            @ColumnInfo(name = "quantity")
            val quantity: String?,
            @ColumnInfo(name = "shortDescription")
            val shortDescription: String?,
            @ColumnInfo(name = "status")
            val status: String?,
            @ColumnInfo(name = "stockKeepingUnit")
            val stockKeepingUnit: String?,
            @ColumnInfo(name = "unit")
            val unit: String?,
            @ColumnInfo(name = "isAvailable")
            var isAvailable: Boolean

        ) : Parcelable {
            constructor(name: String, id: Int) : this(
                0,
                0,
                id,
                "",
                name,
                0,
                false,
                null,
                0,
                "0",
                "",
                "",
                null,
                "",
                true
            )
        }
    }
}

