package com.plugable.mcommerceapp.crazypetals.mcommerce.models

/*
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
*/
data class MyOrder(
    val count: Int? = null,
    val `data`: List<DataItem>? = null,
    val message: String? = null,
    val statusCode: String? = null
) {

    data class DataItem(
        val city: String? = null,
        val createdDate: String? = null,
        val deliveredDate: String? = null,
        val deliveryDay: String? = null,
        val deliveryStatus: Int? = null,
        val id: Int? = null,
//        val isPaymentFailed: Boolean? = null,
        val paymentStatusId:String?=null,
        val orderNumber: String? = null,
        val orderedDate: String? = null
    )
}