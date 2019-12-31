package com.plugable.mcommerceapp.crazypetals.mcommerce.models
/*

data class RegisterWithData(
    val `data`: Data,
    val message: String,
    val statusCode: String
) {
    data class Data(
        val applicationUserId: Int,
        val email: String,
        val name: String
    )
}*/

data class RegisterWithData(
    val `data`: Data,
    val message: String,
    val statusCode: String
) {
    data class Data(
        val applicationUserId: Int,
        val phoneNumber: String,
        val name: String
    )
}