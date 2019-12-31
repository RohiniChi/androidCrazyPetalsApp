package com.plugable.mcommerceapp.crazypetals.mcommerce.models

data class RequestUpdateQuantity(
    val AppId: String,
    val ApplicationUserId: Int,
    val ProductId: Int,
    val Quantity: String
)