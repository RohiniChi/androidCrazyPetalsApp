package com.plugable.mcommerceapp.crazypetals.mcommerce.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

data class Search(
    val `data`: Data,
    val message: String,
    val statusCode: String
) {
    data class Data(
        val productList: List<Product>
    ) {
        @Parcelize
        @Entity(tableName = "productWishList")

        data class Product(
//            val categoryName: String,
            @ColumnInfo(name = "discountPercentage")
            val discountPercentage: Int,
            @ColumnInfo(name = "discountedPrice")
            val discountedPrice: Int,
            @ColumnInfo(name = "filters")
            val filters: List<String>,
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
}