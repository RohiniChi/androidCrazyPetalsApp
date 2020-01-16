package com.plugable.mcommerceapp.crazypetals.mcommerce.models


data class RegisterRequest(
    val EmailId: String,
    val Password: String,
    val PhoneNumber: String,
    val name: String,
    val city:String,
    val image:String
)