package com.plugable.mcommerceapp.cpmvp1.network


import com.plugable.mcommerceapp.cpmvp1.network.error.Error
import com.plugable.mcommerceapp.cpmvp1.network.model.*
import com.plugable.mcommerceapp.cpmvp1.network.service.NetworkService
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
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


    @POST("/api/appointment")
    fun bookAppointmentApi(
        @Body bookAppointment: BookAppointmentRequest
    ): Observable<BookAppointmentResponse>


    @GET("/api/appointment/types")
    fun getAppointmentTypeApi(): Observable<GetAppointmentTypeResponse>


    @GET("/api/appointment")
    fun getAppointmentDetailApi(
        @Query("appointmentId") appointmentId: Int
    ): Observable<AppointmentDetailResponse>


    /**
     * This method is used for getting event of validation of Auth Code for JWT Token
     */
    interface AppointmentCallback : BaseCallback {
        fun onAppointmentListSuccess(response: AppointmentListResponse) {}
        fun onBookAppointmentSuccess(response: BookAppointmentResponse) {}
        fun onGetAppointmentTypeSucces(response: GetAppointmentTypeResponse) {}
        fun onGetAppointmentDetailSucces(response: AppointmentDetailResponse) {}
    }


    companion object {

        private val appointmentApi: AppointmentApi =
            NetworkService.get(AppointmentApi::class.java)


        fun getAppointment(
            applicationUserId: String,
            skip: Int,
            take: Int, appointmentCallback: AppointmentCallback
        ): Subscription {


            return appointmentApi.getAppointmentApi(applicationUserId, skip, take)
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


        fun bookAppointment(
            bookAppointmentRequest: BookAppointmentRequest,
            appointmentCallback: AppointmentCallback
        ): Subscription {

            return appointmentApi.bookAppointmentApi(bookAppointmentRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Subscriber<BookAppointmentResponse>() {
                    override fun onNext(response: BookAppointmentResponse?) {
                        if (response != null) {
                            appointmentCallback.onBookAppointmentSuccess(response)
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

        fun getAppointmentType(appointmentCallback: AppointmentCallback): Subscription {
            return appointmentApi.getAppointmentTypeApi().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Subscriber<GetAppointmentTypeResponse>() {
                    override fun onNext(response: GetAppointmentTypeResponse?) {
                        if (response != null) {
                            appointmentCallback.onGetAppointmentTypeSucces(response)
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

        fun getAppointmentDetail(
            appointmentId: Int,
            appointmentCallback: AppointmentCallback
        ): Subscription {
            return appointmentApi.getAppointmentDetailApi(appointmentId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Subscriber<AppointmentDetailResponse>() {
                    override fun onNext(response: AppointmentDetailResponse?) {
                        if (response != null) {
                            appointmentCallback.onGetAppointmentDetailSucces(response)
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