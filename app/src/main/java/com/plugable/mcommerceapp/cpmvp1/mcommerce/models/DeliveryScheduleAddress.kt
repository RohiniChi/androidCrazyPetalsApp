package com.plugable.mcommerceapp.cpmvp1.mcommerce.models

import android.os.Parcel
import android.os.Parcelable

data class DeliveryScheduleAddress(
    val data: Data,
    val message: String,
    val statusCode: String
)

data class Data(
    val deliveryLocationList: List<DeliveryLocation>,
    val totalCount: Int
)

data class DeliveryLocation(
    var area: String,
    val day: String,
    val id: Int,
    val pinCode: String,
    val time: String
)


data class AddressRequest(
    val Address: String,
    val ApplicationUserId: String,
    val Locality: String,
    val MobileNumber: String,
    val PinCode: String,
    var ID: String? = null,
    var name: String? = null,
    var Landmark: String? = null,
    var city: String? = null,
    var PinCodeId: Int? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(Address)
        parcel.writeString(ApplicationUserId)
        parcel.writeString(Locality)
        parcel.writeString(MobileNumber)
        parcel.writeString(PinCode)
        parcel.writeString(ID)
        parcel.writeString(name)
        parcel.writeString(Landmark)
        parcel.writeString(city)
        parcel.writeInt(PinCodeId!!)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AddressRequest> {
        override fun createFromParcel(parcel: Parcel): AddressRequest {
            return AddressRequest(parcel)
        }

        override fun newArray(size: Int): Array<AddressRequest?> {
            return arrayOfNulls(size)
        }
    }
}

data class DeleteAddressRequest(val ID: String)

data class AddressAddResponse(
    val message: String,
    val statusCode: String
)

data class DeliveryDayResponse(
    val deliveryDay: String,
    val message: String,
    val statusCode: String
)

data class AddressListResponse(
    val count: Int,
    val `data`: List<Data>,
    val message: String,
    val statusCode: String
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.createTypedArrayList(Data)!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    data class Data(
        val address: String,
        val appId: String?,
        val applicationUserId: Int,
        val id: Int,
        val locality: String,
        val landmark: String,
        val mobileNumber: String,
        val name: String,
        val city: String,
        val pinCode: String,
        val pinCodeId: Int,
        var isSelected: Boolean = false
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readInt()

        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(address)
            parcel.writeInt(applicationUserId)
            parcel.writeInt(id)
            parcel.writeString(locality)
            parcel.writeString(landmark)
            parcel.writeString(mobileNumber)
            parcel.writeString(name)
            parcel.writeString(city)
            parcel.writeString(pinCode)
            parcel.writeInt(if (isSelected) 1 else 0)
        }

        override fun describeContents(): Int {
            return 0
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Data

            if (id != other.id) return false

            return true
        }

        override fun hashCode(): Int {
            return id
        }

        companion object CREATOR : Parcelable.Creator<Data> {
            override fun createFromParcel(parcel: Parcel): Data {
                return Data(parcel)
            }

            override fun newArray(size: Int): Array<Data?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(count)
        parcel.writeTypedList(data)
        parcel.writeString(message)
        parcel.writeString(statusCode)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AddressListResponse> {
        override fun createFromParcel(parcel: Parcel): AddressListResponse {
            return AddressListResponse(parcel)
        }

        override fun newArray(size: Int): Array<AddressListResponse?> {
            return arrayOfNulls(size)
        }
    }
}

data class DeliveryScheduleResponse(
    val `data`: List<Data>,
    val message: String,
    val statusCode: String
) {

    data class Data(
        val area: String,
        val time: String,
        val day: String,
        val pinCode: String
    )
}