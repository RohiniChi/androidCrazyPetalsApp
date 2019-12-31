package com.plugable.mcommerceapp.crazypetals.mcommerce.models

/*
data class RegisterRequest(
    val appId: String,
    val emailId: String,
    val image: String,
    val password: String,
    val phoneNumber: String,
    val name: String
)*/
data class RegisterRequest(
    val EmailId: String,
    val Password: String,
    val PhoneNumber: String,
    val name: String,
    val image:String
)