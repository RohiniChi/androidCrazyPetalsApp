package com.plugable.mcommerceapp.cpmvp1.mcommerce.models

data class GetCartResponse(
    val count: Int,
    val `data`: List<Data>,
    val estimatedDeliveryDate: String,
    val message: String,
    val statusCode: String
) {
    data class Data(
        val applicationUserId: Int,
        val colorCode: String,
        val discountedPrice: Int,
        val id: Int,
        val originalPrice: Int?,
        val productId: Int,
        val productImageURL: String,
        val productName: String,
        var quantity: Int,
        val shortDescription: String,
        val size: String,
        var colorId: String,
        var sizeId: String,
        val availableQuantity: Int,
        val isAvailable: Boolean
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Data

            if (id != other.id) return false

            return true
        }

        override fun hashCode(): Int {
            return id
        }
    }
}