package com.plugable.mcommerceapp.crazypetals.mcommerce.models


data class GetMyProfile(
    val `data`: Data,
    val message: String,
    val statusCode: String
) {
    data class Data(
        val emailId: String,
        val mobileNumber: String,
        val name: String,
        val profilePicture: String?,
        val city:String
    )
}