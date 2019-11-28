package com.plugable.mcommerceapp.cpmvp1.mcommerce.models

data class DeliveryChartResponse(
    val `data`: List<Data>,
    val message: String?,
    val statusCode: String
) {

    data class Data(
        val area: String,
        val day: String,
        val pinCode: String,
        val time: String,
        val id: Int
    )
}