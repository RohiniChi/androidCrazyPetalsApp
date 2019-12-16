package com.plugable.mcommerceapp.cpmvp1.network.model

data class AppointmentListResponse(
    val count: Int,
    val `data`: List<AppointmentData>,
    val message: String,
    val statusCode: String
)

data class AppointmentData(
    val appointmentDate: String,
    val appointmentId: Int,
    val appointmentNumber: String,
    val fromTime: String,
    val modifiedDate: String,
    val status: String
)