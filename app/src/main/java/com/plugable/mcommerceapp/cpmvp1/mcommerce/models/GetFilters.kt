package com.plugable.mcommerceapp.cpmvp1.mcommerce.models

data class GetFilters(
    val `data`: Data,
    val message: String,
    val statusCode: String
) {
    data class Data(
        val filterList: List<Filter>
    ) {
        data class Filter(
            val id: Int,
            val name: String,
            var isSelected:Boolean
        )
    }
}