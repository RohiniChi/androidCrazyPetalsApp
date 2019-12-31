package com.plugable.mcommerceapp.crazypetals.mcommerce.models


data class UpdateProfileData(
    val ApplicationUserId: String,
    val Email: String,
    val Image: String?=null,
    val Name: String,
    val PhoneNumber: String
)