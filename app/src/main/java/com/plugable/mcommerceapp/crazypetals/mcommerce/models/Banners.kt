package com.plugable.mcommerceapp.crazypetals.mcommerce.models

data class Banners(
    val `data`: Data,
    val message: String,
    val statusCode: String
) {
    data class Data(
        val bannerList: List<Banner>
    ) {
        data class Banner(
            val id: Int,
            val image: String,
            val title: String
        )
    }
}