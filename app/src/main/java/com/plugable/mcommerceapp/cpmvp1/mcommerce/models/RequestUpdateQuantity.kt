package com.plugable.mcommerceapp.cpmvp1.mcommerce.models

data class RequestUpdateQuantity(
    val AppId: String,
    val ApplicationUserId: Int,
    val ProductId: Int,
    val Quantity: String
)