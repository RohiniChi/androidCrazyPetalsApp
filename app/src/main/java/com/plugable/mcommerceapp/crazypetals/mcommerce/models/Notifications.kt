package com.plugable.mcommerceapp.crazypetals.mcommerce.models

/*
data class Notifications(
    val data: Data? = null,
    val message: String? = null,
    val statusCode: String? = null
) {
    data class Data(
        val notificationList: List<NotificationListItem?>
    ) {
        data class NotificationListItem(
            val timeStamp: String? = null,
            val imageUrl: String? = null,
            val id: Int? = null,
            val notificationType: String? = null,
            val message: String? = null,
            val title: String? = null,
            val modifiedTitle: String? = null,
           val categoryId:String?=null,
            val category:String?=null
        )
    }

}
*/
data class Notifications(
    val `data`: Data,
    val message: String,
    val statusCode: String
) {

    data class Data(
        val notificationList: List<NotificationListItem>
    ) {
        data class NotificationListItem(
            val category: String? = null,
            val categoryId: String? = null,
            val id: Int? = null,
            val imageUrl: String? = null,
            val message: String? = null,
            val modifiedTitle: String? = null,
            val notificationType: String? = null,
            val timeStamp: String? = null,
            val title: String? = null
        )
    }
}