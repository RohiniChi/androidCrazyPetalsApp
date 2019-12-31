package com.plugable.mcommerceapp.crazypetals.network.view


import com.plugable.mcommerceapp.crazypetals.network.model.AppointmentDetailResponse
import com.plugable.mcommerceapp.crazypetals.network.model.AppointmentListResponse
import com.plugable.mcommerceapp.crazypetals.network.model.BookAppointmentResponse
import com.plugable.mcommerceapp.crazypetals.network.model.GetAppointmentTypeResponse

interface AppointmentView : Base {
    fun onAppointmentListSuccess(response: AppointmentListResponse){}
    fun onBookAppointmentSuccess(response: BookAppointmentResponse){}
    fun onGetAppointmentTypeSuccess(response: GetAppointmentTypeResponse){}
    fun onGetAppointmentDetailSuccess(response: AppointmentDetailResponse){}
}