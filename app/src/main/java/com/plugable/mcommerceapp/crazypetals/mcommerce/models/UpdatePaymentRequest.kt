package com.plugable.mcommerceapp.crazypetals.mcommerce.models

data class UpdatePaymentRequest(
    val orderId: String,
    val paymentStatusId: String
)