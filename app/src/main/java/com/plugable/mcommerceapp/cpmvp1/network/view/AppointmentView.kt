package com.plugable.mcommerceapp.cpmvp1.network.view


import com.plugable.mcommerceapp.cpmvp1.network.model.AppointmentListResponse

interface AppointmentView : Base {
    fun onAppointmentListSuccess(response: AppointmentListResponse)

}