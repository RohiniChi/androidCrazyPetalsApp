package com.plugable.mcommerceapp.crazypetals.mcommerce.models


data class FilterData(
    val CategoryId: String,
    val Filters: List<Int>,
    val skip: String,
    val take: String,
    val isExclusive:String
)