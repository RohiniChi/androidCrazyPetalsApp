package com.plugable.mcommerceapp.cpmvp1.mcommerce.models


data class FilterData(
    val CategoryId: String,
    val Filters: List<Int>,
    val skip: String,
    val take: String
)