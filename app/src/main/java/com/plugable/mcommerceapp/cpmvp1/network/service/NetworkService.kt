package com.plugable.mcommerceapp.cpmvp1.network.service


import com.plugable.mcommerceapp.cpmvp1.network.presenter.AppointmentPresenter
import com.plugable.mcommerceapp.cpmvp1.network.view.AppointmentView
import com.plugable.mcommerceapp.cpmvp1.BuildConfig
import com.plugable.mcommerceapp.cpmvp1.utils.application.App
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit


class NetworkService {

    companion object {
        enum class ApiType {
            TOKEN,
            AUTH,
            API
        }

        const val CONNECT_TIMEOUT = 45L
        const val READ_TIMEOUT = 45L
        const val WRITE_TIMEOUT = 45L
        const val JWT_TAG = "##JWT##"
        val baseUrl = App.HostUrl


        fun <API_INTERFACE> get(clazz: Class<API_INTERFACE>): API_INTERFACE {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level =
                if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            return Retrofit.Builder()
//                .baseUrl("https://maps.googleapis.com")
                .baseUrl(baseUrl)
                .client(
                    OkHttpClient.Builder()
                        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                        .addInterceptor(httpLoggingInterceptor).build()
                )
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build().create(clazz)
        }
    }
}