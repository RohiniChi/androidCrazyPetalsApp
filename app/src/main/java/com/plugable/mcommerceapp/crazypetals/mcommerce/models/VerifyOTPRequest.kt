package com.plugable.mcommerceapp.crazypetals.mcommerce.models

data class VerifyOTPRequest(
    val phoneNumber: String,
    val otp: String
)