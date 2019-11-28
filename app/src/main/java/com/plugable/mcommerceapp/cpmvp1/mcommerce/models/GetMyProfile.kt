package com.plugable.mcommerceapp.cpmvp1.mcommerce.models


data class GetMyProfile(
    val `data`: Data,
    val message: String,
    val statusCode: String
) {
    data class Data(
        val emailId: String,
        val mobileNumber: String,
        val name: String,
        val profilePicture: String?
    )
}