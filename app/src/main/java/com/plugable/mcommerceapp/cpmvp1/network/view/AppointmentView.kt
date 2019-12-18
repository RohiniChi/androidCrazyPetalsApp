package com.plugable.mcommerceapp.cpmvp1.network.view


import com.plugable.mcommerceapp.cpmvp1.network.model.AppointmentDetailResponse
import com.plugable.mcommerceapp.cpmvp1.network.model.AppointmentListResponse
import com.plugable.mcommerceapp.cpmvp1.network.model.BookAppointmentResponse
import com.plugable.mcommerceapp.cpmvp1.network.model.GetAppointmentTypeResponse

interface AppointmentView : Base {
    fun onAppointmentListSuccess(response: AppointmentListResponse){}
    fun onBookAppointmentSuccess(response: BookAppointmentResponse){}
    fun onGetAppointmentTypeSuccess(response: GetAppointmentTypeResponse){}
    fun onGetAppointmentDetailSuccess(response: AppointmentDetailResponse){}
}