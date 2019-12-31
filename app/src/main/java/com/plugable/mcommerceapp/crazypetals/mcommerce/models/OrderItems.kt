package com.plugable.mcommerceapp.crazypetals.mcommerce.models

/**
 * [OrderItems] is a model class for Orders
 *
 * @property data
 * @property message
 * @property statusCode
 */
data class OrderItems(
    val data: Data,
    val message: String,
    val statusCode: String
) {
    data class Data(
        val categoryList: List<Items>
    ) {
        data class Items(
            val itemName: String,
            val id: Int,
            val itemPrice: String,
            val itemQuantity: Int
        )
    }
}