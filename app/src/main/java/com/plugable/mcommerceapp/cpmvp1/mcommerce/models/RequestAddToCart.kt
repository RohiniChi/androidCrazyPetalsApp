package com.plugable.mcommerceapp.cpmvp1.mcommerce.models

/*
data class RequestAddToCart(
    val ApplicationUserId: Int,
    val ColorsId: Int,
    val ProductId: Int,
    val SizeId: Int
)*/

data class RequestAddToCart(
    val ApplicationUserId: Int,
    val ProductId: Int,
    val ColorsId: Int,
    val SizeId: Int
)

data class RemoveFromCartRequest(
    val applicationUserId: String,
    val productId: String
)