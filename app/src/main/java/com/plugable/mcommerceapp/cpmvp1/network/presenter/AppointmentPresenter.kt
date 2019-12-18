package com.plugable.mcommerceapp.cpmvp1.network.presenter

import com.plugable.mcommerceapp.cpmvp1.network.AppointmentApi
import com.plugable.mcommerceapp.cpmvp1.network.error.Error
import com.plugable.mcommerceapp.cpmvp1.network.model.*
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


    fun bookAppointment(bookAppointmentRequest: BookAppointmentRequest) {
        authenticationView.showProgress()
        val subscription = appointmentApi.bookAppointment(bookAppointmentRequest,
            object : AppointmentApi.AppointmentCallback {
                override fun onBookAppointmentSuccess(response: BookAppointmentResponse) {
                    authenticationView.hideProgress()
                    authenticationView.onBookAppointmentSuccess(response)
                }

                override fun failed(error: Error) {
                    authenticationView.hideProgress()
                    authenticationView.failed(error)
                }
            })
        subscriptions.add(subscription)
    }

    fun getAppointmentType() {
        authenticationView.showProgress()
        val subscription =
            appointmentApi.getAppointmentType(object : AppointmentApi.AppointmentCallback {
                override fun onGetAppointmentTypeSucces(response: GetAppointmentTypeResponse) {
                    authenticationView.hideProgress()
                    authenticationView.onGetAppointmentTypeSuccess(response)
                }

                override fun failed(error: Error) {
                    authenticationView.hideProgress()
                    authenticationView.failed(error)
                }
            })
        subscriptions.add(subscription)
    }

    fun getAppointmentDetail(  appointmentId: Int){
        authenticationView.showProgress()
        val subscription=appointmentApi.getAppointmentDetail(appointmentId,
            object :AppointmentApi.AppointmentCallback {
                override fun onGetAppointmentDetailSucces(response: AppointmentDetailResponse) {
                    authenticationView.hideProgress()
                    authenticationView.onGetAppointmentDetailSuccess(response)
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