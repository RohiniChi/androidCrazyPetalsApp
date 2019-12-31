package com.plugable.mcommerceapp.crazypetals.mcommerce.models

data class GetTotalPrice(
    val `data`: Data,
    val message: String,
    val statusCode: String
) {
    data class Data(
        val shippingCharges: Int,
        val mrp: Int,
        val productDiscounts: Int,
        val subTotal: Int
    )
}