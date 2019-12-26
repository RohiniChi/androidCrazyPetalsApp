package com.plugable.mcommerceapp.cpmvp1.mcommerce.models

data class UpdatePaymentRequest(
    val orderid: String,
    val paymentstatusid: String
)