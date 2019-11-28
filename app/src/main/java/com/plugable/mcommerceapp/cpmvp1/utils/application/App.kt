package com.plugable.mcommerceapp.cpmvp1.utils.application

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.plugable.mcommerceapp.cpmvp1.BuildConfig
import io.fabric.sdk.android.Fabric


/**
 * [App] is an application class
 *
 */
class App : Application() {

    companion object {

        var HostUrl = ""
    }

    override fun onCreate() {
        super.onCreate()
        initializeCrashlytics()
    }

    /**
     * Initialize [Crashlytics] to log the crashes in the application.
     * The library is also used to report exceptions that are non-fatal.
     *
     */
    private fun initializeCrashlytics() = Fabric.with(
        Fabric.Builder(this)
            .kits(Crashlytics())
            .debuggable(BuildConfig.DEBUG)
            .build()
    )!!

}