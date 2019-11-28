package com.plugable.mcommerceapp.cpmvp1.mcommerce.models
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