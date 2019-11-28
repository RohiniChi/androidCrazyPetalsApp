package com.plugable.mcommerceapp.cpmvp1.mcommerce.models

/**
 * [ProductBooking] is a model for sending user data to Server
 *
 * @property data
 * @property message
 * @property statusCode
 */

data class ProductBooking(
    val `data`: Data,
    val message: String,
    val statusCode: String
) {
    data class Data(
        val appId: String,
        val area: String,
//        val createdDate: Any?,
        val description: String,
        val email: String,
        val mobileNumber: String,
        val name: String,
        val orderNumber: String,
        val pinCode: String
    )
}