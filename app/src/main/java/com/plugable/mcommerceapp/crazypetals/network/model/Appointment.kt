package com.plugable.mcommerceapp.crazypetals.network.model

import android.os.Parcel
import android.os.Parcelable

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
    val appointmentTime: String,
    val appointmentType: List<String>,
    val contactNumber: String,
    val status: String,
    val description: String
):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(appointmentDate)
        parcel.writeInt(appointmentId)
        parcel.writeString(appointmentNumber)
        parcel.writeString(appointmentTime)
        parcel.writeStringList(appointmentType)
        parcel.writeString(contactNumber)
        parcel.writeString(status)
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AppointmentData> {
        override fun createFromParcel(parcel: Parcel): AppointmentData {
            return AppointmentData(parcel)
        }

        override fun newArray(size: Int): Array<AppointmentData?> {
            return arrayOfNulls(size)
        }
    }
}

data class BookAppointmentRequest(
    val ApplicationUserId: String,
    val AppointmentDate: String,
    val ContactNumber: String,
    val Description: String,
    val FromTime: String,
    val TypeArray: HashSet<Int>
)

data class BookAppointmentResponse(
    val `data`: AppointmentData,
    val message: String,
    val statusCode: String
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
