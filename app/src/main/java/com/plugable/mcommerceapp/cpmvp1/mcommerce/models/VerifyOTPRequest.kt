package com.plugable.mcommerceapp.cpmvp1.mcommerce.models

data class VerifyOTPRequest(
    val phoneNumber: String,
    val otp: String
)