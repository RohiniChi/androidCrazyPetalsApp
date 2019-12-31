package com.plugable.mcommerceapp.crazypetals.mcommerce.models

data class UpdatePaymentRequest(
    val orderid: String,
    val paymentstatusid: String
)