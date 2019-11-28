package com.plugable.mcommerceapp.cpmvp1.mcommerce.models

data class GetTotalPrice(
    val `data`: Data,
    val message: String,
    val statusCode: String
) {
    data class Data(
        val deliveryCharges: Int,
        val mrp: Int,
        val productDiscounts: Int,
        val subTotal: Int
    )
}