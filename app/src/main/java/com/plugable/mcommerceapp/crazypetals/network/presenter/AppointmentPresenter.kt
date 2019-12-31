package com.plugable.mcommerceapp.crazypetals.network.presenter

import com.plugable.mcommerceapp.crazypetals.network.AppointmentApi
import com.plugable.mcommerceapp.crazypetals.network.error.Error
import com.plugable.mcommerceapp.crazypetals.network.model.*
import com.plugable.mcommerceapp.crazypetals.network.view.AppointmentView
import rx.subscriptions.CompositeSubscription

class AppointmentPresenter(private val appointmentView: AppointmentView) {

    private val subscriptions: CompositeSubscription = CompositeSubscription()

    private val appointmentApi: AppointmentApi.Companion = AppointmentApi

    fun getAppointment(
        applicationUserId: String,
        skip: Int,
        take: Int
    ) {
        appointmentView.showProgress()
        val subscription = appointmentApi.getAppointment(
            applicationUserId,
            skip,
            take,
            object : AppointmentApi.AppointmentCallback {

                override fun onAppointmentListSuccess(response: AppointmentListResponse) {
                    appointmentView.hideProgress()
                    appointmentView.onAppointmentListSuccess(response)
                }

                override fun failed(error: Error) {
                    appointmentView.hideProgress()
                    appointmentView.failed(error)

                }
            })

        subscriptions.add(subscription)
    }


    fun bookAppointment(bookAppointmentRequest: BookAppointmentRequest) {
        appointmentView.showProgress()
        val subscription = appointmentApi.bookAppointment(bookAppointmentRequest,
            object : AppointmentApi.AppointmentCallback {
                override fun onBookAppointmentSuccess(response: BookAppointmentResponse) {
                    appointmentView.hideProgress()
                    appointmentView.onBookAppointmentSuccess(response)
                }

                override fun failed(error: Error) {
                    appointmentView.hideProgress()
                    appointmentView.failed(error)
                }
            })
        subscriptions.add(subscription)
    }

    fun getAppointmentType() {
        appointmentView.showProgress()
        val subscription =
            appointmentApi.getAppointmentType(object : AppointmentApi.AppointmentCallback {
                override fun onGetAppointmentTypeSucces(response: GetAppointmentTypeResponse) {
                    appointmentView.hideProgress()
                    appointmentView.onGetAppointmentTypeSuccess(response)
                }

                override fun failed(error: Error) {
                    appointmentView.hideProgress()
                    appointmentView.failed(error)
                }
            })
        subscriptions.add(subscription)
    }

    fun getAppointmentDetail(  appointmentId: Int){
        appointmentView.showProgress()
        val subscription=appointmentApi.getAppointmentDetail(appointmentId,
            object :AppointmentApi.AppointmentCallback {
                override fun onGetAppointmentDetailSucces(response: AppointmentDetailResponse) {
                    appointmentView.hideProgress()
                    appointmentView.onGetAppointmentDetailSuccess(response)
                }

                override fun failed(error: Error) {
                    appointmentView.hideProgress()
                    appointmentView.failed(error)
                }
            })
        subscriptions.add(subscription)
    }
    fun onStop() {
        subscriptions.unsubscribe()
    }


}