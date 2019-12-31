package com.plugable.mcommerceapp.crazypetals.mcommerce.models

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


/*
data class ApplyFilters(
    val `data`: Data,
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
            val discountedPrice: Int,
//            @ColumnInfo(name = "filters")
//            val filters: String,
            @PrimaryKey
            val id: Int,
            @ColumnInfo(name = "image")
            val image: String,
            @ColumnInfo(name = "isExclusive")
            val isExclusive: Boolean,
            @ColumnInfo(name = "name")
            val name: String,
            @ColumnInfo(name = "originalPrice")
            val originalPrice: Int,
            @ColumnInfo(name = "isFavorite")
            var isFavorite: Boolean
        ):Parcelable
    }
}*/

data class ApplyFilters(
    val `data`: Data,
    val message: String,
    val statusCode: String
) {

    data class Data(
        val productList: List<Product>,
        val totalCount: Int
    ) {
        @Parcelize
        @Entity(tableName = "productWishList")

        data class Product(
//            val categoryName: Any?,
            @ColumnInfo(name = "discountPercentage")
            val discountPercentage: Int,
            @ColumnInfo(name = "discountedPrice")
            val discountedPrice: Int,
            @ColumnInfo(name = "filterId")
            val filterId: Int,
//            val filters: Any?,
            @PrimaryKey
            val id: Int,
            @ColumnInfo(name = "image")
            val image: String,
            @ColumnInfo(name = "isExclusive")
            val isExclusive: Boolean,
            @ColumnInfo(name = "name")
            val name: String,
            @ColumnInfo(name = "originalPrice")
            val originalPrice: Int,
            @ColumnInfo(name = "isFavorite")
            var isFavorite: Boolean
        ) : Parcelable
    }
}
