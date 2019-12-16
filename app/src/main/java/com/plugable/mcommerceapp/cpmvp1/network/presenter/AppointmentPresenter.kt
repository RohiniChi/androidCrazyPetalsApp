package com.plugable.mcommerceapp.cpmvp1.network.presenter

import com.plugable.mcommerceapp.cpmvp1.network.AppointmentApi
import com.plugable.mcommerceapp.cpmvp1.network.error.Error
import com.plugable.mcommerceapp.cpmvp1.network.model.AppointmentListResponse
import com.plugable.mcommerceapp.cpmvp1.network.view.AppointmentView
import rx.subscriptions.CompositeSubscription

class AppointmentPresenter(private val authenticationView: AppointmentView) {

    private val subscriptions: CompositeSubscription = CompositeSubscription()

    private val appointmentApi: AppointmentApi.Companion = AppointmentApi

    fun getAppointment(
        applicationUserId: String,
        skip: Int,
        take: Int
    ) {
        authenticationView.showProgress()
        val subscription = appointmentApi.getAppointment(
            applicationUserId,
            skip,
            take,
            object : AppointmentApi.AppointmentCallback {

                override fun onAppointmentListSuccess(response: AppointmentListResponse) {
                    authenticationView.hideProgress()
                    authenticationView.onAppointmentListSuccess(response)
                }

                override fun failed(error: Error) {
                    authenticationView.hideProgress()
                    authenticationView.failed(error)

                }
            })

        subscriptions.add(subscription)
    }

    fun onStop() {
        subscriptions.unsubscribe()
    }
}