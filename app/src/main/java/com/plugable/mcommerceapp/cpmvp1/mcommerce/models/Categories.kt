package com.plugable.mcommerceapp.cpmvp1.mcommerce.models


/**
 * [Categories] is a model class for loading Categories in grid
 *
 * @property data
 * @property message
 * @property statusCode
 */


/*
data class Categories(
    val `data`: Data,
    val message: String,
    val statusCode: String
) {
    data class Data(
        val categoryList: List<Category>
    ) {
        data class Category(
            val id: Int,
            val image: String,
            val name: String
        )
    }
}*/

data class Categories(
    val `data`: Data,
    val message: String,
    val statusCode: String
) {
    data class Data(
        val categoryList: List<Category>
    ) {
        data class Category(
            val id: Int,
            val image: String,
            val name: String
        )
    }
}