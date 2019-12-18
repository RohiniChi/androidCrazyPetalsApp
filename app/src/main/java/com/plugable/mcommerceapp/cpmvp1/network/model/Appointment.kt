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

data class BookAppointmentRequest(
    val ApplicationUserId: String,
    val AppointmentDate: String,
    val ContactNumber: String,
    val Description: String,
    val FromTime: String,
    val TypeArray: HashSet<Int>
)

data class BookAppointmentResponse(
    val `data`: BookAppointmentResponseData,
    val message: String,
    val statusCode: String
)

data class BookAppointmentResponseData(
    val appointmentDate: String,
    val appointmentId: Int,
    val appointmentNumber: String,
    val appointmentTime: String,
    val appointmentType: List<String>,
    val contactNumber: String,
    val description: String
)

data class GetAppointmentTypeResponse(
    val `data`: List<AppointmentTypeData>,
    val message: String,
    val statusCode: String
){

    data class AppointmentTypeData(
        val appointmentTypeId: Int,
        var name: String,
        var isSelected:Boolean
    )
}


data class AppointmentDetailResponse(
    val `data`: AppointmentDetailData,
    val message: String,
    val statusCode: String
){
    data class AppointmentDetailData(
        val appointmentDate: String,
        val appointmentNumber: String,
        val appointmentTime: String,
        val appointmentType: ArrayList<String>,
        val contactNumber: String,
        val description: String
    )
}
