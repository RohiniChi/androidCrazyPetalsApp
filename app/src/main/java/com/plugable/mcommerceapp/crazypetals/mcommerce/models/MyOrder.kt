package com.plugable.mcommerceapp.crazypetals.mcommerce.models

data class MyOrder(
	val data: List<DataItem?>? = null,
	val count: Int? = null,
	val message: String? = null,
	val statusCode: String? = null

)
{
	data class DataItem(
		val orderNumber: String? = null,
		val orderedDate: String? = null,
		val id: Int? = null,
		val deliveryDay: String? = null,
		val deliveredDate: String? = null,
		val deliveryStatus:Int
	)
}
