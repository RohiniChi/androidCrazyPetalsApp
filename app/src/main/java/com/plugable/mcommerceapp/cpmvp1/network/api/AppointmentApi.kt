package com.plugable.mcommerceapp.cpmvp1.network


import com.plugable.mcommerceapp.cpmvp1.network.error.Error
import com.plugable.mcommerceapp.cpmvp1.network.service.NetworkService
import com.plugable.mcommerceapp.cpmvp1.network.model.AppointmentListResponse
import retrofit2.http.GET
import retrofit2.http.Query
import rx.Observable
import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Interface defining API methods used for server authentication.
 *
 */
interface AppointmentApi {


    @GET("/api/appointments")
    fun getAppointmentApi(
        @Query("applicationUserId") applicationUserId: String,
        @Query("skip") skip: Int,
        @Query("take") take: Int
    ): Observable<AppointmentListResponse>


    /**
     * This method is used for getting event of validation of Auth Code for JWT Token
     */
    interface AppointmentCallback : BaseCallback {
        fun onAppointmentListSuccess(response: AppointmentListResponse){}
    }


    companion object {

        private val appointmentApi: AppointmentApi =
            NetworkService.get(AppointmentApi::class.java)


        fun getAppointment(
            aapplicationUserId: String,
            skip: Int,
            take: Int, appointmentCallback: AppointmentCallback
        ): Subscription {


            return appointmentApi.getAppointmentApi(aapplicationUserId, skip, take)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Subscriber<AppointmentListResponse>() {
                    override fun onNext(response: AppointmentListResponse) {

                        if (response != null) {
                            appointmentCallback.onAppointmentListSuccess(response)
                        } else {
                            appointmentCallback.failed(Error(Throwable()))
                        }

                    }

                    override fun onCompleted() {
                    }

                    override fun onError(e: Throwable?) {
                        appointmentCallback.failed(Error(Throwable()))
                    }
                })
        }


    }
}