package com.plugable.mcommerceapp.cpmvp1.mcommerce.models

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
            val title: String? = null
        )
    }

}
